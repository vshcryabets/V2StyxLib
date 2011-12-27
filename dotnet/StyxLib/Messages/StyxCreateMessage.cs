// StyxCreateMessage.cs - Styx Create message representation
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
using System.Text;

namespace StyxLib.Messages
{
    /// <summary>
    /// Create message class
    /// </summary>
    public class StyxCreateMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Tcreate fields
        private UInt32 fid; // file fid
        private String name; // file name
        private uint permissions; // file permissions
        private byte mode; // access mode
        //Rcreate fields
        private QID qid; // file QID
        private UInt32 iounit; //The maximum number of bytes guaranteed to be read from
                         // or written to the file without breaking into multiple Styx messages
        #endregion

        #region Constructors
        public StyxCreateMessage()
        {
        }

        /// <summary>
        /// With this constructor a Tcreate message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        /// <param name="fid">File fid</param>
        /// <param name="mode">Access mode</param>
        public StyxCreateMessage(ushort tag, UInt32 fid, String name, uint perm, byte mode)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Tcreate;
            this.mode = mode;
            this.name = name;
            this.permissions = perm;
        }
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }

        public byte Mode
        {
            get { return mode; }
            set 
            {
                value = mode;
            }
        }

        public QID Qid
        {
            get { return qid; }
            set
            {
                qid = value;
            }
        }

        public UInt32 IoUnit
        {
            get { return iounit; }
            set { iounit = value; }
        }

        public String Name
        {
            get { return name; }
            set { name = value; }
        }


        public UInt32 Permission
        {
            get { return permissions; }
            set { permissions = value; }
        }

        #endregion

        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tcreate )
                // size = base header size + fid(32 bits) + nname(16 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(ushort)+
                    // + name length + perm(32 bits) + mode (8 bits)
                    StyxMessage.MsgEncoding.GetByteCount(name)+sizeof(uint)+sizeof(byte) );
            if (Type == MessageType.Rcreate)
                // size = base header size + qid(13 bytes) + iounit(uint)
                return (uint)(base.GetBinarySize() + StyxMessage.QIDSIZE + sizeof(uint));
            return 0;
        }

        #region Encoders & decoders
        /// <summary>
        /// Tcreate message encoder
        /// </summary>
        /// <param name="res">output buffer</param>
        /// <param name="pos">position in output buffer</param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            
            // name length
            byte[] text = StyxMessage.MsgEncoding.GetBytes(name);
            Array.Copy(BitConverter.GetBytes(text.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
 
            // name
            Array.Copy(text, 0, res, pos, text.Length);
            pos += text.Length;

            // perm
            Array.Copy(BitConverter.GetBytes(permissions), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            // mode
            res[pos] = mode;
            pos ++;

            return pos;
        }

        /// <summary>
        /// Create message encoder
        /// </summary>
        /// <returns>Result buffer</returns>
        public new byte[] GetBinary()
        {
            int pos;
            Size = GetBinarySize();
            byte [] base_binary = base.GetBinary();
            byte [] res = new byte[GetBinarySize()];
            Array.Copy(base_binary, 0, res, 0, base_binary.Length);
            pos = base_binary.Length;
            if (Type == MessageType.Tcreate)
                pos = GetTBinary(res, pos);
            if (Type == MessageType.Rcreate)
            {
                throw new Exception("Rcreate not implemented yet");
            }
            return res;
        }

        /// <summary>
        ///  Rcreate message decoder
        /// </summary>
        /// <param name="data">Input buffer</param>
        public void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            this.qid = StyxMessage.getQuid(data, pos);
            pos += (int)StyxMessage.QIDSIZE;
            iounit = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
        }

        /// <summary>
        /// Create message decoder
        /// </summary>
        /// <exception cref="StyxErrorMessageException">Throws StyxErrorMessageException when Rerror received</exception>
        /// <exception cref="Exception">Throws Exception when unsupported message received</exception>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rcreate)
            {
                SetRBinary(data);
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
