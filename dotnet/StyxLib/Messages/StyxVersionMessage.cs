// StyxVersionMessage.cs - Styx Version message representation
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
    public class StyxVersionMessage : StyxMessage, IStyxMessage
    {
        #region Variables
        // maximal accepted packet size
        private UInt32 max_packet_size;
        // protocol version string
        private String protocol_version;
        #endregion

        #region Constructors
        public StyxVersionMessage()
        {
        }

        public StyxVersionMessage( UInt32 max_size, String protocol, bool request)
        {
            this.Tag = StyxMessage.NOTAG;
            this.Type = ( request ? MessageType.Tversion : MessageType.Rversion);
            max_packet_size = max_size;
            protocol_version = protocol;
        }
        #endregion

        #region Properties
        public UInt32 MaxPacketSize
        {
            get { return max_packet_size; }
            set { max_packet_size = value; }
        }

        public String ProtocolVersion
        {
            get { return protocol_version; }
            set 
            { 
                protocol_version = value;
                Size = GetBinarySize();
            }
        }
        #endregion

        #region Encoder & decoder
        public new uint GetBinarySize()
        {
            // size = base header size + max_packet_size(32 bits) + string lentgh (16 bits) + string value size
            return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(ushort) + StyxMessage.MsgEncoding.GetBytes(protocol_version).Length);
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
            Array.Copy(BitConverter.GetBytes(max_packet_size), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            Array.Copy(BitConverter.GetBytes((ushort)protocol_version.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            byte[] text = StyxMessage.MsgEncoding.GetBytes(protocol_version);
            Array.Copy(text, 0, res, pos, text.Length);
            return res;
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <param name="data"></param>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rerror)
            {
                StyxErrorMessage error = new StyxErrorMessage();
                error.SetBinary(data);
                throw new StyxErrorMessageException(error);
                return;
            }
            if ((this.Type != MessageType.Tversion) && (this.Type != MessageType.Rversion))
                throw new Exception("Incorrect message type");
            int pos = (int)base.GetBinarySize();
            this.max_packet_size = BitConverter.ToUInt32(data, pos);
            pos += sizeof(uint);
            ushort protocol_size = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);

            protocol_version = Encoding.UTF8.GetString(data, pos, protocol_size);
        }
        #endregion
    }
}
