// StyxFlushMessage.cs - Styx Flush message representation
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
    /// Flush message class
    /// </summary>
    public class StyxFlushMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Tflush fields
        private UInt16 oldtag;
        #endregion

        #region Constructors
        public StyxFlushMessage()
        {
        }

        /// <summary>
        /// With this constructor a Tflush message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        /// <param name="fid">File fid</param>
        public StyxFlushMessage(ushort tag, UInt16 oldtag)
        {
            this.Tag = tag;
            this.oldtag = oldtag;
            this.Type = MessageType.Tflush;
        }
        #endregion

        #region Properties
        public UInt16 OldTag
        {
            get { return oldtag; }
            set { oldtag = value; }
        }
        #endregion

        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tflush )
                // size = base header size + fid(32 bits) + oldtag(32 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(uint));
            if (Type == MessageType.Rattach)
                // size = base header size
                return base.GetBinarySize();
            return 0;
        }

        #region Encoders & decoders
        /// <summary>
        /// Tflush message encoder
        /// </summary>
        /// <param name="res">output buffer</param>
        /// <param name="pos">position in output buffer</param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(oldtag), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);

            return pos;
        }

        /// <summary>
        /// Open message encoder
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
            if (Type == MessageType.Tflush)
                pos = GetTBinary(res, pos);
            if (Type == MessageType.Rflush)
            {
                throw new Exception("Rflush not implemented yet");
            }
            return res;
        }

        /// <summary>
        ///  Rflush message decoder
        /// </summary>
        /// <param name="data">Input buffer</param>
        public void SetRBinary(byte[] data)
        {
        }

        /// <summary>
        /// Open message decoder
        /// </summary>
        /// <exception cref="StyxErrorMessageException">Throws StyxErrorMessageException when Rerror received</exception>
        /// <exception cref="Exception">Throws Exception when unsupported message received</exception>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rflush)
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
