using System;
using System.IO;
using System.Collections.Generic;
using StyxLib.Interfaces;
using System.Security.Cryptography;
using StyxLib.Utils;
using System.Security.AccessControl;
using System.Security.Principal;
using StyxLib.Messages;
using StyxLib.Streams;

namespace ServerTest
{
    class StyxFileSystem : StyxFileSystemInterface
    {
        #region Variables
        private string local_path = "";
        private bool isopen = false;
        private bool isdirectory = false;
        private bool exists = false;

        private DirectoryInfo directory;
        #endregion

        #region Properties
        public string RemotePath
        {
            get
            {
                return local_path.Substring(prefix.Length);
            }
        }

        public string Name
        {
            get
            {
                return System.IO.Path.GetFileName(local_path);
            }
        }
        #endregion

        #region Constructors
        public StyxFileSystem(string path)
        {
            local_path = path;
            if (!local_path.StartsWith(prefix))
            {
                this.local_path = prefix + this.local_path;
            }
            if (File.Exists(local_path))
            {
                exists = true;
                isdirectory = false;
            }
            if (Directory.Exists(local_path))
            {
                exists = true;
                isdirectory = true;
            }
        }
        #endregion

        #region StyxFileSystemInterface methods
        public int Walk(out StyxFileSystemInterface newitem, 
            out StyxLib.Messages.StyxMessage.QID[]qids, string [] names)
        {
            int res = 0;
            string newname = local_path;
            List<StyxMessage.QID> qids_list = new List<StyxMessage.QID>();
            int count = (names != null ? names.Length : 0);
            for (int i = 0; i < count; i++)
            {
                newname += "/" + names[0];
                if ((!File.Exists(newname)) & (!Directory.Exists(newname)))
                {
                    //throw new Exception("System couldn't find object with specified name");
                    res = -1; // not found
                    break;
                }
                qids_list.Add(GetFSItemQID(newname));
            }
            if (res == 0)
            {
                // all path components was correctly founded
                newitem = new StyxFileSystem(newname);
            }
            else
            {
                newitem = null;
            }
            qids = qids_list.ToArray();
            return res;
        }

        public int Open(byte mode, out StyxMessage.QID qid)
        {
            if (!exists)
            {
                throw new Exception("Specified file doesn't exists");
            }
            // check mode
            if (isdirectory && (mode != StyxStream.FILEMODE_OREAD))
            {
                throw new Exception("You cannot write to directory");
            }

            if (isdirectory)
            {
                // open directory
                directory = new DirectoryInfo(local_path);
            }

            isopen = true;
            //qid
            qid = this.GetQID();
            return 0;
        }

        public int Create(String name, int perm, byte mode)
        {
            throw new Exception("Not implemented");
        }

        public int Remove()
        {
            throw new Exception("Not implemented");
        }

        public int Stat(out StyxLib.Messages.Structures.StatStructure info)
        {
            info = new StyxLib.Messages.Structures.StatStructure();
            if (isdirectory)
            {
                DirectoryInfo fi = new DirectoryInfo(local_path);
                fillDirStatInfo(info, fi);
            }
            else
            {
                FileInfo fi = new FileInfo(local_path);                
                fillFileStatInfo(info, fi);
            }
            return 0;
        }

        public int Wstat(StyxLib.Messages.Structures.StatStructure info)
        {
            throw new Exception("Not implemented");
        }

        public int Read(byte[] buffer, int buffer_offset, UInt64 file_offset, int count, out int readed)
        {
            if (isdirectory)
            {
                return readDirectoryContent(buffer,buffer_offset,file_offset,count,out readed);
            }
            throw new Exception("Not implemented");
            return -1;
        }

        public int Write(byte[] buffer, int buffer_offset, UInt64 file_offset, int count, out int writed)
        {
            throw new Exception("Not implemented");
        }

        public StyxLib.Messages.StyxMessage.QID GetQID()
        {
            StyxMessage.QID res = GetFSItemQID(local_path);
            if (res.path == 0)
            {
                throw new Exception("Unknown file");
            }
            return res;
        }
        public int Close()
        {
            if (!isopen)
            {
                throw new Exception("This file alredy closed");
            }

            if (isdirectory)
            {
                // close directory
                directory = null;
            }
            isopen = false;
            return 0;
        }
        #endregion

        #region Private methods
        private void fillFileStatInfo(StyxLib.Messages.Structures.StatStructure info, FileInfo fi)
        {
            //info = new StyxLib.Messages.Structures.StatStructure();
            //FileInfo fi = new FileInfo(this.local_path);
            info.mtime = (uint)TimeConverter.ConvertToUTP(fi.LastWriteTime);
            info.atime = (uint)TimeConverter.ConvertToUTP(fi.LastAccessTime);
            info.dev = 0;
            info.type = 0;
            info.qid = this.GetQID();

            System.Security.AccessControl.FileSecurity fss = File.GetAccessControl(this.local_path);
            System.Security.Principal.IdentityReference account = fss.GetOwner(typeof(NTAccount));
            info.uid = account.Value;
            info.muid = info.uid;

            account = fss.GetGroup(typeof(NTAccount));
            info.gid = account.Value;

            info.length = (ulong)fi.Length;
            info.name = Name;
            info.mode = 0x16D;
        }


        private int readDirectoryContent(byte[] buffer, int buffer_offset, UInt64 file_offset, int count, out int readed)
        {
            // 1. fill directory files list
            DirectoryInfo[] directories = directory.GetDirectories();
            FileInfo[] files = directory.GetFiles();
            
            // 2. convert fragment of list into Styx stat structures
            uint size = 0;
            StyxLib.Messages.Structures.StatStructure[] items =
                new StyxLib.Messages.Structures.StatStructure[directories.Length + files.Length];
            for (int i = 0; i < directories.Length; i++)
            {
                StyxLib.Messages.Structures.StatStructure info = new StyxLib.Messages.Structures.StatStructure();
                fillDirStatInfo(info, directories[i]);
                items[i] = info;
                size += info.GetBinarySize();
            }
            for (int i = 0; i < files.Length; i++)
            {
                StyxLib.Messages.Structures.StatStructure info = new StyxLib.Messages.Structures.StatStructure();
                fillFileStatInfo(info, files[i]);
                items[i + directories.Length] = info;
                size += info.GetBinarySize();
            }

            // 3. convert it to a binary
            byte[] all_buffer = new byte[size];
            int pos = 0;
            foreach (StyxLib.Messages.Structures.StatStructure item in items)
            {
                byte[] stat_arr = item.GetBinary();
                // stat structure
                Array.Copy(stat_arr, 0, all_buffer, pos, stat_arr.Length);
                pos += stat_arr.Length;
                
            }

            if ((int)file_offset > all_buffer.Length)
            {
                readed = 0;
                return 0;
            }
            if ((int)file_offset + count <= all_buffer.Length)
            {
                readed = count;
            }
            else
            {
                readed = all_buffer.Length - (int)file_offset;
            }

            Array.Copy(all_buffer, (int)file_offset, buffer, buffer_offset, readed);
            return 0;
        }
        #endregion

        #region Static members
        private static string CleanUserInfo(string account)
        {
            string res = account;
            // check Windows NT authority info
            int pos = account.LastIndexOf("\\");
            if ( pos > -1 )
            {
                res = account.Substring(pos+1);
            }
            return res;
        }

        private static string prefix = "";
        public static string Prefix
        {
            get { return prefix; }
            set { prefix = value; }
        }

        private static StyxMessage.QID GetFSItemQID(String name)
        {
            StyxMessage.QID result = new StyxMessage.QID();
            result.path = 0;
            if (Directory.Exists(name))
            {
                DirectoryInfo info = new DirectoryInfo(name);
                result.version = (uint)StyxLib.Utils.TimeConverter.ConvertToUTP(info.LastWriteTimeUtc);
                result.type = StyxLib.Messages.StyxMessage.QIDType.QTDIR;
                StyxLib.Utils.Crc64 crc64 = new StyxLib.Utils.Crc64();
                byte[] hash = crc64.ComputeHash(StyxLib.Messages.StyxMessage.MsgEncoding.GetBytes(name));
                result.path = BitConverter.ToUInt64(hash, 0);
            }
            if (File.Exists(name))
            {
                FileInfo info = new FileInfo(name);
                result.version = (uint)StyxLib.Utils.TimeConverter.ConvertToUTP(info.LastWriteTimeUtc);
                result.type = StyxLib.Messages.StyxMessage.QIDType.QTFILE;
                StyxLib.Utils.Crc64 crc64 = new StyxLib.Utils.Crc64();
                byte[] hash = crc64.ComputeHash(StyxLib.Messages.StyxMessage.MsgEncoding.GetBytes(name));
                result.path = BitConverter.ToUInt64(hash, 0);
            }
            return result;
        }

        private static void fillDirStatInfo(StyxLib.Messages.Structures.StatStructure info, DirectoryInfo fi)
        {
            info.mtime = (uint)TimeConverter.ConvertToUTP(fi.LastWriteTime);
            info.atime = (uint)TimeConverter.ConvertToUTP(fi.LastAccessTime);
            info.dev = 0;
            info.type = 0x2f;
            info.qid = GetFSItemQID(fi.FullName);

            System.Security.AccessControl.FileSecurity fss = File.GetAccessControl(fi.FullName);
            System.Security.Principal.IdentityReference account = fss.GetOwner(typeof(NTAccount));
            info.uid = CleanUserInfo(account.Value);
            info.muid = info.uid;

            account = fss.GetGroup(typeof(NTAccount));
            info.gid = CleanUserInfo(account.Value);

            info.length = 0;
            info.name = fi.Name;
            if (info.name.Equals(""))
            {
                // must be / if the file is the root directory of the server 
                info.name = "/";
            }
            info.mode = StyxMessage.DMDIR | 0x16D;
        }
        #endregion
    }
}
