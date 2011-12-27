// StyxStream.cs - this class exposes stream interface around Styx files and directories
//
// Author:  Vladimir Shcryabets <vshcryabets@2vsoft.com>
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of version 2 of the Lesser GNU General 
// Public License as published by the Free Software Foundation.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this program; if not, write to the
// Free Software Foundation, Inc., 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.

﻿using System;
using System.IO;
using StyxLib.Messages;
using StyxLib.FileSystem;

namespace StyxLib.Streams
{
    public class StyxStream : Stream
    {
        #region Constants
        public const byte FILEMODE_OREAD = 0;
        public const byte FILEMODE_OWRITE = 1;
        public const byte FILEMODE_ORDWR = 2;
        public const byte FILEMODE_OEXEC = 3;
        public const byte FILEMODE_OTRUNC = 0x10;
        public const byte FILEMODE_ORCLOSE = 0x40;
        #endregion

        #region Variables
        private uint fid = StyxMessage.NOFID;

        private bool canRead = true;
        private bool canSeek = false;
        private bool canWrite = false;
        private bool isAsync = false;
        private long length = 0;
        private long position = 0;

        private String path;
        private String name;

        private StyxClientManager manager;
        private byte mode = 0;
        private uint iounit = 2048;
        private uint permissions = 0x180;

        // flags
        private bool shouldClunkFid = false; // should dispose clunk fid
        private bool disposed = false; // is object disposed
        private bool exists = false; // is file exists
        private bool create = false; // should we create file?
        #endregion

        #region Constructor & Destructor
        public StyxStream(uint fid, FileMode mode, FileAccess access, StyxClientManager manager)
        {
            this.manager = manager;
            exists = true;
            setMode(mode, access);
            connect(fid);
        }

        public StyxStream(String path, FileMode mode, FileAccess access, StyxClientManager manager)
        {
            while (path.StartsWith("/")) path = path.Remove(0, 1);
            while (path.EndsWith("/")) path = path.Remove(path.Length - 1);
            this.manager = manager;
            walkToFile(path);
            setMode(mode, access );
            if (create)
                createFile(path);
            connect(fid);
        }

        /// <summary>
        /// initialize stream by parent fid and child's name
        /// </summary>
        /// <param name="parent_fid">parent fid</param>
        /// <param name="name">child's name</param>
        /// <param name="mode"></param>
        /// <param name="access"></param>
        /// <param name="manager"></param>
        public StyxStream( uint parent_fid, String name, FileMode mode, FileAccess access, StyxClientManager manager)
        {
            this.manager = manager;
            walkToChild(parent_fid, name);
            setMode(mode, access);
            if (create)
                createFile(parent_fid, name);
            connect(fid);
        }

        public new void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected new virtual void Dispose(bool disposing)
        {
            // Check to see if Dispose has already been called.
            if(!this.disposed)
            {
                // If disposing equals true, dispose all managed 
                // and unmanaged resources.
                if(disposing)
                {
                    // Dispose managed resources.
                }
                // if we have opened fid
                if ( ( fid != StyxMessage.NOFID ) && shouldClunkFid )
                    // lets free it
                    this.manager.SendClunk(fid);
            }
            disposed = true;         
        }

        ~StyxStream()      
        {
            Dispose(false);
        }
        #endregion

        #region Properties
        public override bool CanRead { get { return canRead; } }
        public override bool CanSeek { get { return canSeek; } }
        public override bool CanWrite { get {return canWrite;} }
        public virtual bool IsAsync { get { return isAsync; } }
        public override long Length { get { return length; } }
        public override long Position { get { return position; } set { position = value; } }

        public String Path{ get { return path; }}
        #endregion

        #region Methods
        private void walkToChild(uint parent_fid, String name)
        {
            this.path = name;
            uint stream_fid = manager.getFid();
            
            StyxWalkMessage walk_msg = manager.SendWalk(parent_fid, stream_fid, name);
            if ( !name.Equals("") && (walk_msg.Qids.Length != 1) )
            {
                // look's like file doesn't exists
                exists = false;
                fid = StyxMessage.NOFID;
                return;
            }
            exists = true;
            fid = stream_fid;
            shouldClunkFid = true;
        }

        private void walkToFile(String path)
        {
            this.path = path;
            uint stream_fid = manager.getFid();

            int count = 0;
            String[] path_components = path.Split('/');
            count = path_components.Length;
            if (path.Equals(""))
                count = 0;
            StyxWalkMessage walk_msg = manager.SendWalk(manager.Root, stream_fid, path);
            if (walk_msg.Qids.Length != count)
            {
                // look's like file doesn't exists
                exists = false;
                fid = StyxMessage.NOFID;
                return;
            }
            shouldClunkFid = true;
            fid = stream_fid;
            exists = true;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="mode"></param>
        /// <param name="access"></param>
        private void setMode(FileMode mode, FileAccess access)
        {
            switch ( access )
            {
                case FileAccess.Read:
                    this.mode = FILEMODE_OREAD;
                    break;
                case FileAccess.Write:
                    this.mode = FILEMODE_OWRITE;
                    break;
                case FileAccess.ReadWrite:
                    this.mode = FILEMODE_ORDWR;
                    break;
            }
            if (exists)
            {
                switch (mode)
                {
                    case FileMode.Append:
                        throw new Exception("Append mode not implemented");
                        break;
                    case FileMode.Create:
                        //If the file already exists, it will be overwritten.
                        this.mode |= FILEMODE_OTRUNC;
                        break;
                    case FileMode.CreateNew:
                        // If the file already exists, an  IOException is thrown.
                        throw new IOException("File with same name already exists.");
                        break;
                    case FileMode.Open:
                    case FileMode.OpenOrCreate:
                        break;
                    case FileMode.Truncate:
                        this.mode |= FILEMODE_OTRUNC;
                        break;
                }
            }
            else
            {
                switch (mode)
                {
                    case FileMode.Open:
                    case FileMode.Truncate:
                    case FileMode.Append:
                        throw new Exception("File or directory not found.");
                        break;
                    case FileMode.Create:
                    case FileMode.CreateNew:
                    case FileMode.OpenOrCreate:
                        create = true;
                        break;
                }
            }
        }

        /// <summary>
        /// Creates a file in directory specified by parent_fid
        /// </summary>
        /// <param name="parent_fid">parent directory fid</param>
        /// <param name="name">new file name</param>
        private void createFile(uint parent_fid, String name)
        {
            // reserve fid?
            uint temp_fid = manager.getFid();
            StyxWalkMessage walk_msg = manager.SendWalk(parent_fid, temp_fid, "");
            // now call create
            StyxCreateMessage create_msg = manager.SendCreate(temp_fid, name, permissions, 0);
            // and temp fid
            manager.SendClunk(temp_fid);
            walkToChild(parent_fid, name);
        }

        /// <summary>
        /// This function creates a file
        /// </summary>
        /// <param name="path">new file path</param>
        private void createFile(String path)
        {
            StyxDirectoryInfo parent_dir = null;
            // get parent directory FID
            uint parent_fid = StyxMessage.NOFID;
            int last_slash = path.LastIndexOf('/');
            if (last_slash < 0)
                last_slash = 0;
            String name = path.Substring(last_slash+1);
            // get parent directory
            String parent_name = path.Substring(0, last_slash);
            // get parent fid
            if (parent_name.Equals(""))
                parent_fid = manager.Root;
            else
            {
                parent_dir = new StyxDirectoryInfo(parent_name, manager);
                parent_fid = parent_dir.Fid;
            }
            // create a new file
            createFile(parent_fid, name);
            // free a parent dir
            if (parent_dir != null)
                parent_dir.Dispose();

        }

        /// <summary>
        /// get file stat info and open it
        /// </summary>
        /// <param name="fid">file fid</param>
        private void connect(uint fid)
        {
            this.fid = fid;
            // lets inquire attributes info
            StyxStatMessage stat = manager.SendStat(fid);
            length = (long)stat.Stat.length;
            // and now we should open it
            StyxOpenMessage open = manager.SendOpen(fid, mode);
            iounit = open.IoUnit;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="offset"></param>
        /// <param name="origin"></param>
        /// <returns></returns>
        public override long Seek(long offset , SeekOrigin origin)
        {
            switch (origin)
            {
                case SeekOrigin.Begin:
                    position = offset;
                    break;
                case SeekOrigin.Current:
                    position += offset;
                    break;
                case SeekOrigin.End:
                    position = length - offset;
                    break;
            }
            if (position < 0) position = 0;
            if ( ( length != 0 ) && (position >= length) ) position = length - 1;
            return position;
        }

        public override void Flush()
        {
            throw new Exception("Not implemented");
        }

        public override void SetLength(long length)
        {
            throw new Exception("Not implemented");
        }

        public override int Read(byte[] buf, int offset, int count )
        {
            if (count < 0) return 0;
            int readed = 0;
            while (readed < count)
            {
                StyxReadMessage msg = manager.SendRead(fid, (ulong)position, 
                    (uint)((count-readed) > iounit ? iounit : (count -(uint)readed)) );
                if (msg.Count < 1) break;

                Array.Copy(msg.Data, 0, buf, offset + readed, msg.Count);
                position += msg.Count;
                readed += (int)msg.Count;
            }
            return readed;
        }

        public override void Write(byte[] data, int offset, int count)
        {
            int pos = offset, bs, writed = 0;
            do
            {
                bs = ( iounit < ( count - writed ) ? (int)iounit : (count-writed) );
                StyxWriteMessage msg = manager.SendWrite(fid,(ulong)position, data, pos, bs );
                pos += bs;
                writed += bs;
            }
            while (writed < count);            
        }

        private String checkPath(String path)
        {
            return path;
        }
        #endregion
    }
}
