// StyxWalkMessage.cs - Styx Walk message representation
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
using System.Text;

namespace StyxLib.Messages
{
    public class StyxWalkMessage : StyxMessage, IStyxMessage
    {
        #region Variables
        // Twalk members
        private UInt32 fid;
        private UInt32 newfid;
        //private String path;
        private byte[][] path_components;
        // Rwalk members
        private QID [] qids;
        #endregion

        #region Constructors
        public StyxWalkMessage()
        {
        }

        /// <summary>
        /// With this constructor a Twalk message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        /// <param name="fid">parent fid</param>
        /// <param name="newfid">new fid</param>
        /// <param name="path">path</param>
        public StyxWalkMessage(ushort tag, UInt32 fid, UInt32 newfid, String path)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Twalk;
            this.newfid = newfid;
            Path = path;
        }

        /// <summary>
        /// With this constructor a Rwalk message will be created
        /// </summary>
        /// <param name="qids">Message tag</param>
        public StyxWalkMessage(ushort tag, QID[] qids)
        {
            this.Tag = tag;
            this.Type = MessageType.Rwalk;
            this.qids = qids;
        }
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }
        
        public UInt32 NewFid
        {
            get { return newfid; }
            set { newfid = value; }
        }

        public String[] PathComponents
        {
            get
            {
                if (path_components == null)
                {
                    return null;
                }
                String[] components = new String[path_components.Length];
                for (int i = 0; i < path_components.Length; i++)
                {
                    components[i] = Encoding.UTF8.GetString(path_components[i]);
                }
                return components;
            }
        }

        public String Path
        {
            //get { return path; }
            set 
            { 
                String path = value;
                while (path.StartsWith("/")) path = path.Remove(0, 1);
                while (path.EndsWith("/")) path = path.Remove(path.Length - 1);
                String[] components = path.Split('/');
                if ( (components.Length > 0) && ( !path.Equals("") ) )
                {
                    path_components = new byte[components.Length][];
                    for (int i = 0; i < components.Length; i++)
                    {
                        path_components[i] = StyxMessage.MsgEncoding.GetBytes(components[i]);
                    }
                }
                else
                    path_components = null;
                Size = GetBinarySize();
            }
        }

        public QID[] Qids
        {
            get { return qids; }
            set
            {
                qids = value;
                Size = GetBinarySize();
            }
        }
        #endregion

        #region Encoder & decoder
        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Twalk )
            {
                // size = base header size +     fid(32 bits) +    newfid(32 bits) + nwname(16bits)... + string value size + string lentgh (16 bits) + string value size
                uint res = (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(uint) + sizeof(ushort));
                if ( path_components != null )
                    foreach( byte[] folder in path_components )
                    {
                        res += sizeof(ushort); //wname size (16 bits)
                        res += (uint)folder.Length;
                    }
                return res;
            }       
            if (Type == MessageType.Rwalk)
                // size = base header size + nwqid(16) aqid(13 bytes)
                return (uint)(base.GetBinarySize() + sizeof(ushort) + StyxMessage.QIDSIZE*qids.Length );
            return 0;
        }

        /// <summary>
        /// Twalk message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            
            // newfid
            Array.Copy(BitConverter.GetBytes(newfid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            
            if (path_components == null)
            {
                // count of wnames (nwname) = 0
                Array.Copy(BitConverter.GetBytes((ushort)0), 0, res, pos, sizeof(ushort));
                pos += sizeof(ushort);
            }
            else
            {
                // count of wnames (nwname)
                Array.Copy(BitConverter.GetBytes((ushort)path_components.Length), 0, res, pos, sizeof(ushort));
                pos += sizeof(ushort);

                // wnames
                foreach (byte[] folder in path_components)
                {
                    // wname size
                    Array.Copy(BitConverter.GetBytes((ushort)folder.Length), 0, res, pos, sizeof(ushort));
                    pos += sizeof(ushort);
                    // wname value
                    Array.Copy(folder, 0, res, pos, folder.Length);
                    pos += folder.Length;
                }
            }
            return pos;
        }

        /// <summary>
        /// Rwalk message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetRBinary(byte[] res, int pos)
        {
            // number of qids in answer
            Array.Copy(BitConverter.GetBytes(qids.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            // and then qids
            for (int i = 0; i < qids.Length; i++)
            {
                Array.Copy(StyxMessage.getQuidArray(qids[i]), 0, res, pos, StyxMessage.QIDSIZE);
                pos += (int)StyxMessage.QIDSIZE;
            }
            return pos;
        }

        /// <summary>
        /// message encoder
        /// </summary>
        /// <returns></returns>
        public new byte[] GetBinary()
        {
            int pos;
            Size = GetBinarySize();
            byte [] base_binary = base.GetBinary();
            byte [] res = new byte[GetBinarySize()];
            Array.Copy(base_binary, 0, res, 0, base_binary.Length);
            pos = base_binary.Length;
            if (Type == MessageType.Twalk)
            {
                pos = GetTBinary(res, pos);
            }
            if (Type == MessageType.Rwalk)
            {
                pos = GetRBinary(res, pos);
            }
            return res;
        }

        /// <summary>
        /// Twalk decoder
        /// </summary>
        /// <param name="data"></param>
        private void SetTBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            // fid
            fid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
            // newfid
            newfid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
            // wnames count
            ushort wncount = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);

            if (wncount > 0)
            {
                path_components = new byte[wncount][];
                for (int i = 0; i < wncount; i++)
                {
                    // wname length
                    ushort length = BitConverter.ToUInt16(data, pos);
                    pos += sizeof(ushort);
                    path_components[i] = new byte[length];

                    // wname value
                    Array.Copy(data, pos, path_components[i], 0, length);
                    pos += length;
                }
            }
        }

        /// <summary>
        /// Rwalk decoder
        /// </summary>
        /// <param name="data"></param>
        private void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            // number of qids in answer
            ushort nwqid = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);
            // qids
            qids = new QID[nwqid];
            for (int i = 0; i < nwqid; i++)
            {
                qids[i] = StyxMessage.getQuid(data, pos);
                pos += (int)StyxMessage.QIDSIZE;
            }
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <param name="data"></param>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rwalk)
            {
                SetRBinary(data);
                return;
            }
            if (this.Type == MessageType.Twalk)
            {
                SetTBinary(data);
                return;
            }
            if (this.Type == MessageType.Rerror)
            {
                StyxErrorMessage error = new StyxErrorMessage();
                error.SetBinary(data);
                throw new StyxErrorMessageException(error);
                return;
            }
            throw new Exception("Incorrect message type");
        }
        #endregion
    }
}
