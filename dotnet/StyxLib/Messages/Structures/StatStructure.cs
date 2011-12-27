// StatStructure.cs - 9P stat structure representation
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
using System.IO;
using StyxLib.Messages;

namespace StyxLib.Messages.Structures
{
    public class StatStructure
    {
        /// <summary>
        /// for kernel use 
        /// </summary>
        public ushort type;
        /// <summary>
        /// for kernel use 
        /// </summary>
        public uint dev;
        public StyxMessage.QID qid;
        /// <summary>
        /// permissions and flags 
        /// </summary>
        public uint mode;
        /// <summary>
        /// last access time
        /// </summary>
        public uint atime;
        /// <summary>
        /// last modification time
        /// </summary>
        public uint mtime;
        /// <summary>
        /// length of file in bytes 
        /// </summary>
        public ulong length;
        /// <summary>
        /// file name; must be / if the file is the root directory of the server 
        /// </summary>
        public String name = "";
        /// <summary>
        /// Owner name
        /// </summary>
        public String uid = "";
        /// <summary>
        /// group name
        /// </summary>
        public String gid = "";
        /// <summary>
        /// name of the user who last modified the file 
        /// </summary>
        public String muid = "";

        public byte[] GetBinary()
        {
            int pos = 0;
            uint size = GetBinarySize();
            byte[] res = new byte[size];

            // size
            Array.Copy(BitConverter.GetBytes((ushort)size-2), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            // type
            Array.Copy(BitConverter.GetBytes(type), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            // dev
            Array.Copy(BitConverter.GetBytes(dev), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // qid
            res[pos] = (byte)qid.type;
            pos++;
            Array.Copy(BitConverter.GetBytes(qid.version), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            Array.Copy(BitConverter.GetBytes(qid.path), 0, res, pos, sizeof(ulong));
            pos += sizeof(ulong);
            // mode
            Array.Copy(BitConverter.GetBytes(mode), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // atime
            Array.Copy(BitConverter.GetBytes(atime), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // mtime
            Array.Copy(BitConverter.GetBytes(mtime), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // length
            Array.Copy(BitConverter.GetBytes(length), 0, res, pos, sizeof(ulong));
            pos += sizeof(ulong);
            // name
            ushort slen = (ushort)StyxMessage.MsgEncoding.GetByteCount(name);
            Array.Copy(BitConverter.GetBytes(slen), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            Array.Copy(StyxMessage.MsgEncoding.GetBytes(name), 0, res, pos, slen);
            pos += slen;
            // uid
            slen = (ushort)StyxMessage.MsgEncoding.GetByteCount(uid);
            Array.Copy(BitConverter.GetBytes(slen), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            Array.Copy(StyxMessage.MsgEncoding.GetBytes(uid), 0, res, pos, slen);
            pos += slen;
            // guid
            slen = (ushort)StyxMessage.MsgEncoding.GetByteCount(gid);
            Array.Copy(BitConverter.GetBytes(slen), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            Array.Copy(StyxMessage.MsgEncoding.GetBytes(gid), 0, res, pos, slen);
            pos += slen;
            // muid
            slen = (ushort)StyxMessage.MsgEncoding.GetByteCount(muid);
            Array.Copy(BitConverter.GetBytes(slen), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            Array.Copy(StyxMessage.MsgEncoding.GetBytes(muid), 0, res, pos, slen);
            pos += slen;
            return res;
        }

        public uint GetBinarySize()
        {
            //        size (16 bits) + type( 16 bits )
            uint res = (sizeof(ushort) + sizeof(ushort) + sizeof(uint) +
                StyxMessage.QIDSIZE + 3 * sizeof(uint) + sizeof(ulong) +
                sizeof(ushort) + (uint)StyxMessage.MsgEncoding.GetByteCount(name) +
                sizeof(ushort) + (uint)StyxMessage.MsgEncoding.GetByteCount(uid) +
                sizeof(ushort) + (uint)StyxMessage.MsgEncoding.GetByteCount(gid) +
                sizeof(ushort) + (uint)StyxMessage.MsgEncoding.GetByteCount(muid));
            return res;
        }

        public static StatStructure getStat(Stream stream)
        {
            int b1 = stream.ReadByte(), b2 = stream.ReadByte();
            if ((b1 == -1) || (b2 == -1)) return null;

            int length = b1 + (b2 << 8);
            byte[] buffer = new byte[length + 2];
            if (stream.Read(buffer, 2, length) < length) return null;
            return getStat(buffer, 0);
        }

        public static StatStructure getStat(byte[] data, int offset)
        {
            StatStructure res = new StatStructure();
            // size
            //ushort size = BitConverter.ToUInt16(data, offset);
            offset += sizeof(ushort);
            // type
            res.type = BitConverter.ToUInt16(data, offset);
            offset += sizeof(ushort);
            // dev
            res.dev = BitConverter.ToUInt32(data, offset);
            offset += sizeof(uint);
            // qid
            res.qid = StyxMessage.getQuid(data, offset);
            offset += (int)StyxMessage.QIDSIZE;
            // mode
            res.mode = BitConverter.ToUInt32(data, offset);
            offset += sizeof(uint);
            // atime
            res.atime = BitConverter.ToUInt32(data, offset);
            offset += sizeof(uint);
            // mtime
            res.mtime = BitConverter.ToUInt32(data, offset);
            offset += sizeof(uint);
            // length
            res.length = BitConverter.ToUInt64(data, offset);
            offset += sizeof(ulong);
            //name
            int strlen = BitConverter.ToUInt16(data, offset);
            offset += sizeof(ushort);
            res.name = Encoding.UTF8.GetString(data, offset, strlen);
            offset += strlen;
            //uid
            strlen = BitConverter.ToUInt16(data, offset);
            offset += sizeof(ushort);
            res.uid = Encoding.UTF8.GetString(data, offset, strlen);
            offset += strlen;
            //gid
            strlen = BitConverter.ToUInt16(data, offset);
            offset += sizeof(ushort);
            res.gid = Encoding.UTF8.GetString(data, offset, strlen);
            offset += strlen;
            //muid
            strlen = BitConverter.ToUInt16(data, offset);
            offset += sizeof(ushort);
            res.muid = Encoding.UTF8.GetString(data, offset, strlen);
            offset += strlen;

            return res;
        }
    }
}
