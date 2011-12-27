// StyxErrorMessage.cs - Styx Error message representation
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
    public class StyxErrorMessage : StyxMessage, IStyxMessage
    {
        #region Variables
        private byte[] error;
        private ushort errno;
        #endregion

        #region Constructors
        public StyxErrorMessage()
        {
        }

        public StyxErrorMessage(string error)
        {
            this.Error = error;
            this.Type = MessageType.Rerror;
        }        
        #endregion

        #region Properties
        public String Error
        {
            get { return Encoding.UTF8.GetString(error); }
            set { error = StyxMessage.MsgEncoding.GetBytes(value); }
        }

        public UInt16 Errno
        {
            get { return errno; }
            set { errno = value; }
        }
        #endregion

        public new uint GetBinarySize()
        {
            // size = base header size + string lentgh (16 bits) + string value size + errno (2 bytes)
            return (uint)(base.GetBinarySize() + sizeof(ushort) + error.Length + sizeof(ushort));
        }

        /**
         * message encoder
         */
        public new byte[] GetBinary()
        {
            int pos;
            Size = GetBinarySize();
            byte [] base_binary = base.GetBinary();
            byte [] res = new byte[GetBinarySize()];
            pos = base_binary.Length;
            Array.Copy(BitConverter.GetBytes((ushort)error.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            Array.Copy(error, 0, res, pos, error.Length);
            pos += error.Length;
            Array.Copy(BitConverter.GetBytes((ushort)errno), 0, res, pos, sizeof(ushort));
            return res;
        }

        /**
         * message decoder
         */
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type != MessageType.Rerror)
                throw new Exception("Incorrect message type");
            int pos = (int)base.GetBinarySize();
            ushort str_size = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);

            error = new byte[str_size];
            Array.Copy(data, pos, error, 0, str_size);
            pos += str_size;

            if (pos < this.Size)
                errno = BitConverter.ToUInt16(data, pos);
            else
                errno = 0;
        }

    }
}
