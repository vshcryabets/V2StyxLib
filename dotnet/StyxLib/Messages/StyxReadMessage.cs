// StyxReadMessage.cs - Styx Read message representation
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
    public class StyxReadMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // TRead
        private UInt32 fid;
        private UInt64 offset;
        private UInt32 count;
        //Rread
        private byte[] data;
        #endregion

        #region Constructors
        public StyxReadMessage()
        {
        }

        /// <summary>
        /// With this constructor a Rread message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="fid"></param>
        /// <param name="offset"></param>
        /// <param name="count"></param>
        public StyxReadMessage(ushort tag, byte [] data, UInt32 readed)
        {
            this.Tag = tag;
            this.Type = MessageType.Rread;
            this.data = data;
            this.count = readed;
        }

        /// <summary>
        /// With this constructor a TRead message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="fid"></param>
        /// <param name="offset"></param>
        /// <param name="count"></param>
        public StyxReadMessage(ushort tag, UInt32 fid, UInt64 offset, UInt32 count)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Tread;
            this.offset = offset;
            this.count = count;
        }
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }

        public UInt64 Offset
        {
            get { return offset; }
            set { offset = value; }
        }

        public UInt32 Count
        {
            get { return count; }
            set { count = value; }
        }
        public byte[] Data
        {
            get { return data; }
        }
        #endregion

        #region Encoder & decoder
        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tread )
                // size = base header size + fid(32 bits) + offset(64 bits) + count (32 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(ulong) + sizeof(uint));
            if (Type == MessageType.Rread)
                // size = base header size + count(32 bits) + count*8
                return (uint)(base.GetBinarySize() + sizeof(uint) + count);
            return 0;
        }
        
        /// <summary>
        /// Tread message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // offset
            Array.Copy(BitConverter.GetBytes(offset), 0, res, pos, sizeof(ulong));
            pos += sizeof(ulong);
            // count
            Array.Copy(BitConverter.GetBytes(count), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            return pos;
        }

        /// <summary>
        /// Rread message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetRBinary(byte[] res, int pos)
        {
            // count
            Array.Copy(BitConverter.GetBytes(count), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // data
            Array.Copy(data, 0, res, pos, count);
            pos += (int)count;
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
            if (Type == MessageType.Tread)
            {
                pos = GetTBinary(res, pos);
            }
            if (Type == MessageType.Rread)
            {
                pos = GetRBinary(res, pos);
            }
            return res;
        }

        /// <summary>
        /// Tread decoder
        /// </summary>
        /// <param name="data"></param>
        public void SetTBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            // fid
            fid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
            // offset
            offset = BitConverter.ToUInt64(data, pos);
            pos += sizeof(ulong);
            // count
            count = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
        }

        /// <summary>
        /// Rread decoder
        /// </summary>
        /// <param name="data"></param>
        public void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            count = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
            this.data = new byte[count];
            Array.Copy(data, pos, this.data, 0, count);
            pos += (int)count;
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <param name="data"></param>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rread)
            {
                SetRBinary(data);
                return;
            }
            if (this.Type == MessageType.Tread)
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
