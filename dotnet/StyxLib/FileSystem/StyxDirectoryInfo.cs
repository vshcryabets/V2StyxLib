// StyxDirectoryInfo.cs - Exposes methods for creating, deleteing and enumerating through directories and subdirectories.
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

using System;
using System.IO;
using System.Collections;
using System.Text.RegularExpressions;
using StyxLib.Messages;
using StyxLib.Streams;

namespace StyxLib.FileSystem
{
    public class StyxDirectoryInfo : FileSystemInfo
    {
        #region Variables
        private uint fid = StyxMessage.NOFID; // directory FID
        private StyxStream stream = null;
        private String name;
        private bool exists = false;
        private String path;
        private StyxClientManager manager;
        private bool disposed = false; // Track whether Dispose has been called.
        #endregion

        #region Constructor & Dispose
        public StyxDirectoryInfo(String path, StyxClientManager manager)
        {
            this.manager = manager;
            Path = path;            
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            // Check to see if Dispose has already been called.
            if(!this.disposed)
            {
                // If disposing equals true, dispose all managed 
                // and unmanaged resources.
                if(disposing)
                {
                    if (stream != null) stream.Dispose();
                    // Dispose managed resources.
                }
                // if we have opened fid
                if ( fid != StyxMessage.NOFID )
                    // lets free it
                    this.manager.SendClunk(fid);
            }
            disposed = true;         
        }

        ~StyxDirectoryInfo()      
        {
            Dispose(false);
        }
        #endregion

        #region Properties
        public override string Name { get { return name; } }
        public override bool Exists { get { return exists; } }
        public String Path
        {
            get { return path; }
            set
            {
                path = value;
                while (path.StartsWith("/")) path = path.Remove(0, 1);
                while (path.EndsWith("/")) path = path.Remove(path.Length - 1);
                WalkToDirectory();
            }
        }

        public uint Fid {get { return fid; }}
        #endregion

        #region Private methods
        private void WalkToDirectory()
        {
            int count = 0;
            String[] path_components = path.Split('/');
            count = path_components.Length;
            if (path.Equals(""))
                count = 0;
            if ( count > 0)
                name = path_components[path_components.Length - 1];
            else
                name = "";
            fid = manager.getFid();
            try
            {
                StyxWalkMessage msg = manager.SendWalk(manager.Root, fid, path);
                // ok, we can walk, lets check does we enter to all subdirs
                exists = (msg.Qids.Length == count);
                if (!exists) // not all directories has been open, we should free fid
                {
                        manager.SendClunk(fid);
                        fid = StyxMessage.NOFID;
                }
                else
                {
                    // directory(or regular file?) exists, check it's Qid Type
                    if ( (count != 0 ) &&  (msg.Qids[msg.Qids.Length - 1].type != StyxMessage.QIDType.QTDIR) )
                        throw new Exception("This is not a directory \"" + path + "\" not found");
                }
            }
            catch (StyxErrorMessageException err)
            {
                exists = false;
                fid = StyxMessage.NOFID;
            }
        }
        #endregion

        #region Public methods
        /// <summary>
        /// Creates a directory with all parents.
        /// </summary>
        public void Create(bool recursive)
        {
            uint parent_fid = StyxMessage.NOFID;
            StyxDirectoryInfo parent = null;

            if (exists) throw new Exception("This directory already created");

            // get parent directory
            String parent_name = path.Substring(0, path.LastIndexOf('/'));
            // get parent fid
            if (parent_name.Equals(""))
                parent_fid = manager.Root;
            else
            {
                parent = new StyxDirectoryInfo(parent_name, manager);
                if (!parent.Exists)
                {
                    if (!recursive) 
                        throw new Exception("Can't create directory - parent doesn't exists");
                    parent.Create(true);
                }
                parent_fid = parent.Fid;
            }
            if (parent_fid == StyxMessage.NOFID) 
                throw new Exception("Can't get parent fid");

            // reserve fid?
            uint temp_fid = manager.getFid();
            StyxWalkMessage walk_msg = manager.SendWalk(parent_fid, temp_fid, "");
            // now call create
            StyxCreateMessage create_msg = manager.SendCreate(temp_fid, name, StyxMessage.DMDIR | 0x1FF, 0);
            // now we can free parent
            if ( parent != null )
                parent.Dispose();
            // and temp fid
            manager.SendClunk(temp_fid);
            WalkToDirectory();
        }

        /// <summary>
        /// Creates a directory.
        /// </summary>
        public void Create(){Create(false);}

        /// <summary>
        /// Deletes this StyxDirectoryInfo if it is empty.
        /// </summary>
        public override void Delete(){this.Delete(false);}

        /// <summary>
        /// Deletes this instance of a StyxDirectoryInfo, specifying whether to delete subdirectories and files.
        /// </summary>
        /// <param name="recursive">Delete subdirectories and files</param>
        public void Delete(bool recursive)
        {
            if (recursive)
            {
                // delete all childs
                // delete files
                Messages.Structures.StatStructure[] files = this.GetFiles();
                if ( files != null )
                    foreach (Messages.Structures.StatStructure file in files)
                        this.DeleteFile(file.name);
                // delete subdirs
                Messages.Structures.StatStructure[] dirs = this.GetDirectories();
                if (dirs != null)
                    foreach (Messages.Structures.StatStructure dir in dirs)
                    {
                        StyxDirectoryInfo dir_info = new StyxDirectoryInfo(this.path + "/" + dir.name, manager);
                        dir_info.Delete(recursive);
                    }
            }
            // free stream, if it open
            if (stream != null)
            {
                stream.Dispose();
                stream = null;
            }
            StyxRemoveMessage msg = manager.SendRemove(fid);
            // Remove message also works as clunk, so we don't need fid
            fid = StyxMessage.NOFID;
            exists = false;
        }

        /// <summary>
        /// Returns a file list from the current directory matching the given searchPattern 
        /// and using a value to determine whether to search subdirectories.
        /// </summary>
        /// <param name="searchPattern">pattern</param>
        /// <param name="searchOption">search options</param>
        /// <returns>Array of items</returns>
        public Messages.Structures.StatStructure[] GetFiles(string searchPattern, SearchOption searchOption)
        {
            Messages.Structures.StatStructure[] items = GetAllItems(searchPattern, searchOption);
            if (items == null) return null;
            ArrayList list = new ArrayList();
            foreach (Messages.Structures.StatStructure item in items)
                if (item.qid.type == StyxMessage.QIDType.QTFILE) list.Add(item);
            if (list.Count < 1) return null;
            Messages.Structures.StatStructure[] res = new Messages.Structures.StatStructure[list.Count];
            for (int i = 0; i < list.Count; i++) res[i] = (Messages.Structures.StatStructure)list[i];
            return res;
        }

        /**
         * Returns a file list from the current directory matching the given searchPattern.
         */
        public Messages.Structures.StatStructure[] GetFiles(string searchPattern)
        {
            return this.GetFiles(searchPattern, SearchOption.TopDirectoryOnly);
        }

        /**
         * Returns a file list from the current directory
         */
        public Messages.Structures.StatStructure[] GetFiles()
        {
            return this.GetFiles("*", SearchOption.TopDirectoryOnly);
        }

        /// <summary>
        /// Returns a file & directories list from the current directory
        /// </summary>
        /// <param name="searchPattern"></param>
        /// <param name="searchOption"></param>
        /// <returns></returns>
        private Messages.Structures.StatStructure[] GetAllItems(string searchPattern, SearchOption searchOption)
        {
            if (searchOption == SearchOption.AllDirectories) return null; // not implemented
            Regex mask = new Regex(searchPattern.Replace(".", "[.]").Replace("*", ".*").Replace("?", "."));
            ArrayList list = new ArrayList();
            if (stream == null)
            {
                stream = new StyxStream(fid, "", FileMode.Open, FileAccess.Read, manager);
            }
            stream.Seek(0, SeekOrigin.Begin);
            BufferedStream bstream = new BufferedStream(stream);
            Messages.Structures.StatStructure stat;
            do
            {
                stat = Messages.Structures.StatStructure.getStat(bstream);
                if ((stat != null)
                    && (mask.Match(stat.name).Length == stat.name.Length))
                    list.Add(stat);
            } while (stat != null);
            if (list.Count < 1) return null;
            Messages.Structures.StatStructure[] res = new Messages.Structures.StatStructure[list.Count];
            for (int i = 0; i < list.Count; i++) res[i] = (Messages.Structures.StatStructure)list[i];
            return res;
        }

        /// <summary>
        /// Returns an array of directories in the current  DirectoryInfo matching the 
        /// given search criteria and using a value to determine whether to search subdirectories.
        /// </summary>
        /// <param name="searchPattern"></param>
        /// <param name="searchOption"></param>
        /// <returns></returns>
        public Messages.Structures.StatStructure[] GetDirectories(string searchPattern, SearchOption searchOption)
        {
            Messages.Structures.StatStructure[] items = GetAllItems(searchPattern, searchOption);
            if (items == null) return null;
            ArrayList list = new ArrayList();
            foreach (Messages.Structures.StatStructure item in items)
                if (item.qid.type == StyxMessage.QIDType.QTDIR) list.Add(item);
            if (list.Count < 1) return null;
            Messages.Structures.StatStructure[] res = new Messages.Structures.StatStructure[list.Count];
            for (int i = 0; i < list.Count; i++) res[i] = (Messages.Structures.StatStructure)list[i];
            return res;
        }

        /**
         * Returns an array of directories in the current  DirectoryInfo matching the given search criteria.
         */
        public Messages.Structures.StatStructure[] GetDirectories(string searchPattern)
        {
            return this.GetDirectories(searchPattern, SearchOption.TopDirectoryOnly);
        }

        /**
         * Returns the subdirectories of the current directory.
         */
        public Messages.Structures.StatStructure[] GetDirectories()
        {
            return this.GetDirectories("*", SearchOption.TopDirectoryOnly);
        }

        /// <summary>
        /// Deletes a specified file from this directory
        /// </summary>
        /// <param name="name">file name</param>
        public void DeleteFile(String name)
        {
            // we should get file's fid
            uint file_fid = manager.getFid();
            StyxWalkMessage walk_msg = manager.SendWalk(fid, file_fid, name);
            // now send TRemove
            StyxRemoveMessage remove_msg = manager.SendRemove(file_fid);
        }
        #endregion

        #region Styx routines
        #endregion
    }
}
