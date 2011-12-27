// StyxMessage.cs - base Styx message class
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
    public class StyxMessage
    {
        #region Enums
        public enum MessageType : byte
        {
            Tversion = 100,
            Rversion,
            Tauth,
            Rauth,
            Tattach,
            Rattach,
            Terror,
            Rerror,
            Tflush,
            Rflush,
            Twalk,
            Rwalk,
            Topen,
            Ropen,
            Tcreate,
            Rcreate,
            Tread,
            Rread,
            Twrite,
            Rwrite,
            Tclunk,
            Rclunk,
            Tremove,
            Rremove,
            Tstat,
            Rstat,
            Twstat,
            Rwstat
        };
        #endregion

        #region Constants
        public const ushort NOTAG = 0xFFFF;
        public const uint NOFID = 0xFFFFFFFF;
        #endregion

        #region Permission constants
        public const uint DMDIR = 0x80000000;
        public const uint DMAPPEND = 0x40000000;
        public const uint DMEXCL = 0x20000000;
        public const uint DMREAD = 0x4;
        public const uint DMWRITE = 0x2;
        public const uint DMEXEC = 0x1;
        #endregion

        #region QID routines
        public const uint QIDSIZE = 13;
        public enum QIDType : byte
        {
            QTDIR = 0x80,
            QTAPPEND = 0x40,
            QTEXCL = 0x20,
            QTMOUNT = 0x10,
            QTAUTH = 0x08,
            QTFILE = 0x00
        }
        public struct QID
        {
            public QIDType type;
            public UInt32 version;
            public UInt64 path;
        }
        public static QID getQuid(byte[] data, int offset)
        {
            QID res = new QID();
            res.type = (QIDType)data[offset];
            offset += sizeof(byte);
            res.version = BitConverter.ToUInt32(data, offset);
            offset += sizeof(uint);
            res.path = (ulong)BitConverter.ToUInt64(data, offset);
            return res;
        }
        public static byte[] getQuidArray(QID qid)
        {
            byte [] res = new byte[QIDSIZE];
            int pos = 0;
            
            res[pos] = (byte)qid.type;
            pos += sizeof(byte);

            Array.Copy( BitConverter.GetBytes(qid.version), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            Array.Copy(BitConverter.GetBytes(qid.path), 0, res, pos, sizeof(UInt64));
            pos += sizeof(UInt64);

            return res;
        }
        #endregion

        public static System.Text.UTF8Encoding MsgEncoding = new System.Text.UTF8Encoding();

        #region Variables
        // base message fields
        private uint size;
        private MessageType type;
        private UInt16 tag;

        private byte[] data = null;
        #endregion

        #region Properties
        // message Size property
        public uint Size
        {
            get { return size; }
            set { size = value; }
        }

        // message type property
        public MessageType Type
        {
            get { return type; }
            set { type = value; }
        }

        // message type property
        public UInt16 Tag
        {
            get { return tag; }
            set { tag = value; }
        }
        #endregion

        public void SetTag(ushort tag)
        {
            Tag = tag;
        }

        public ushort GetTag() { return tag; }

        #region Encoder
        public virtual uint GetBinarySize()
        {
            return 7; //56 bits -- base message header
        }

        /// <summary>
        /// message to binary format encoder
        /// </summary>
        /// <returns></returns>
        public virtual byte[] GetBinary()
        {
            byte [] res = new byte[GetBinarySize()];
            // message size
            Array.Copy(BitConverter.GetBytes(size), 0, res, 0, sizeof(uint));
            // message type
            res[4] = (byte)type;
            // message tag
            Array.Copy(BitConverter.GetBytes(tag), 0, res, 5, sizeof(ushort));
            return res;
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <param name="data"></param>
        public void SetBinary(byte[] data)
        {
            this.data = data;
            // lets decode size, type and tag
            size = BitConverter.ToUInt32(data, 0);
            type = (MessageType)data[4];
            tag = BitConverter.ToUInt16(data, 5);
        }
        #endregion

        #region Get messages
        public IStyxMessage GetMessage()
        {
            IStyxMessage res = null;
            switch (type)
            {
                case MessageType.Twalk:
                case MessageType.Rwalk:
                    res = new StyxWalkMessage();
                    break;
                case MessageType.Tversion:
                case MessageType.Rversion:
                    res = new StyxVersionMessage();
                    break;
                case MessageType.Tclunk:
                case MessageType.Rclunk:
                    res = new StyxClunkMessage();
                    break;
                case MessageType.Tstat:
                case MessageType.Rstat:
                    res = new StyxStatMessage();
                    break;
                case MessageType.Topen:
                case MessageType.Ropen:
                    res = new StyxOpenMessage();
                    break;
                case MessageType.Tread:
                case MessageType.Rread:
                    res = new StyxReadMessage();
                    break;
                case MessageType.Tattach:
                case MessageType.Rattach:
                    res = new StyxAttachMessage();
                    break;

            }
            if (res != null)
            {
                res.SetBinary(data);
            }
            return res;
        }
        #endregion
    }
}
