// StyxRemoveMessage.cs - Styx Remove message representation
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
    public class StyxRemoveMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Tremove fields
        private UInt32 fid; // file fid
        #endregion

        #region Constructors
        public StyxRemoveMessage()
        {
        }

        /// <summary>
        /// With this constructor a Tremove message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        /// <param name="fid">File fid</param>
        public StyxRemoveMessage(ushort tag, UInt32 fid)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Tremove;
        }
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }
        #endregion

        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tremove )
                // size = base header size + fid(32 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint));
            if (Type == MessageType.Rremove)
                // size = base header size
                return (uint)base.GetBinarySize();
            return 0;
        }

        #region Encoders & decoders
        /// <summary>
        /// Tremove message encoder
        /// </summary>
        /// <param name="res">output buffer</param>
        /// <param name="pos">position in output buffer</param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            
            return pos;
        }

        /// <summary>
        /// Remove message encoder
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
            if (Type == MessageType.Tremove)
                pos = GetTBinary(res, pos);
            if (Type == MessageType.Rremove)
            {
                throw new Exception("Rremove not implemented yet");
            }
            return res;
        }

        /// <summary>
        /// Remove message decoder
        /// </summary>
        /// <exception cref="StyxErrorMessageException">Throws StyxErrorMessageException when Rerror received</exception>
        /// <exception cref="Exception">Throws Exception when unsupported message received</exception>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rremove)
                return; // ok, Rremove have no extra fields, so we can just return

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
