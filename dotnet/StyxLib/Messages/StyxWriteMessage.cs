// StyxWriteMessage.cs - Styx Write message representation
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
    /// Open message class
    /// </summary>
    public class StyxWriteMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Twrite fields
        private UInt32 fid; // file fid
        private UInt64 offset; // access mode
        private byte[] data;
        // Rwrite
        private UInt32 count;
        #endregion

        #region Constructors
        public StyxWriteMessage()
        {
        }

        /// <summary>
        /// With this constructor a Twrite message will be created
        /// </summary>
        /// <param name="tag">Message tag</param>
        /// <param name="fid">File fid</param>
        /// <param name="offset">File offset</param>
        /// <param name="data">Output buffer</param>
        public StyxWriteMessage(ushort tag, UInt32 fid, UInt64 offset, byte[] data, int start_index, int length)
        {
            this.Tag = tag;
            this.fid = fid;
            this.offset = offset;
            this.data = new byte[length];
            this.Type = MessageType.Twrite;
            System.Array.Copy(data, start_index, this.data, 0, length);
        }
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }

        public UInt32 Count
        {
            get { return count; }
            set { count = value; }
        }

        public UInt64 Offset
        {
            get { return offset; }
            set 
            {
                value = offset;
            }
        }

        public byte[] Data
        {
            get { return data; }
            set
            {
                data = value;
            }
        }
        #endregion

        public new uint GetBinarySize()
        {
            if ( ( Type == MessageType.Twrite ) && (data != null) )
                // size = base header size +   fid(32 bits) +    offset(64 bits) + count(32 bits) + count
                return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(ulong)+sizeof(uint) + data.Length);
            if (Type == MessageType.Rwrite)
                // size = base header size + count(32 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint));
            return 0;
        }

        #region Encoders & decoders
        /// <summary>
        /// Twrite message encoder
        /// </summary>
        /// <param name="res">output buffer</param>
        /// <param name="pos">position in output buffer</param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            if ( data == null ) 
                throw new Exception("Data array not specified");
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            // offset
            Array.Copy(BitConverter.GetBytes(offset), 0, res, pos, sizeof(ulong));
            pos += sizeof(ulong);

            // count
            Array.Copy(BitConverter.GetBytes(data.Length), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            // data
            System.Array.Copy(data, 0, res, pos, data.Length);
            pos += data.Length;

            return pos;
        }

        /// <summary>
        /// Write message encoder
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
            if (Type == MessageType.Twrite)
                pos = GetTBinary(res, pos);
            if (Type == MessageType.Rwrite)
            {
                throw new Exception("Rwrite not implemented yet");
            }
            return res;
        }

        /// <summary>
        ///  Rwrite message decoder
        /// </summary>
        /// <param name="data">Input buffer</param>
        public void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            count = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
        }

        /// <summary>
        /// Write message decoder
        /// </summary>
        /// <exception cref="StyxErrorMessageException">Throws StyxErrorMessageException when Rerror received</exception>
        /// <exception cref="Exception">Throws Exception when unsupported message received</exception>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rwrite)
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
            throw new Exception("Incorrect or unimplemented message type");
        }
        #endregion
    }
}
