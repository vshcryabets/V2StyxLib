// StyxClunkMessage.cs - Styx Clunk message representation
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

namespace StyxLib.Messages
{
    public class StyxClunkMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Tclunk
        private UInt32 fid;
        #endregion

        #region Constructors
        public StyxClunkMessage()
        {
        }

        /// <summary>
        /// With this constructor a Tclunk message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        /// <param name="fid">File ID</param>
        public StyxClunkMessage(ushort tag, UInt32 fid)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Tclunk;
        }

        /// <summary>
        /// With this constructor a Rclunk message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        public StyxClunkMessage(ushort tag)
        {
            this.Tag = tag;
            this.Type = MessageType.Rclunk;
        }        
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }
        #endregion

        #region Encoder & decoder
        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tclunk )
                // size = base header size + fid(32 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint));
            if (Type == MessageType.Rclunk)
                // size = base header size
                return base.GetBinarySize();
            return 0;
        }

        /// <summary>
        /// Tclunk message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        private int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
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
            if (Type == MessageType.Tclunk)
                pos = GetTBinary(res, pos);
            if (Type == MessageType.Rclunk)
                return res;
            return res;
        }

        /// <summary>
        /// Tclunk decoder
        /// </summary>
        /// <param name="data"></param>
        private void SetTBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            // fid
            fid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <returns></returns>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Tclunk)
            {
                SetTBinary(data);
                return;
            }
            if (this.Type == MessageType.Rclunk)
                return; // Rclunk have no extra fields

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
