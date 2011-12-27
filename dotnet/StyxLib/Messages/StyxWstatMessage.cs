// StyxWstatMessage.cs - Styx Wstat message representation
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
using StyxLib.Messages.Structures;

namespace StyxLib.Messages
{
    public class StyxWstatMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Tstat
        private UInt32 fid;
        private StatStructure stat;
        #endregion

        #region Constructors
        public StyxWstatMessage()
        {
        }

        /// <summary>
        /// With this constructor a TWstat message will be created
        /// </summary>
        /// <param name="tag">message tag</param>
        /// <param name="fid">File id</param>
        /// <param name="stat">Stat info</param>
        public StyxWstatMessage(ushort tag, UInt32 fid, StatStructure stat)
        {
            this.Tag = tag;
            this.fid = fid;
            this.stat = stat;
            this.Type = MessageType.Twstat;
        }
        #endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }

        public StatStructure Stat
        {
            get { return stat; }
        }
        #endregion

        public new uint GetBinarySize()
        {
            if ( stat == null )
            {
                throw new NullReferenceException("Stat is null");
            }
            if ( Type == MessageType.Twstat )
                // size = base header size + fid(32 bits) + Stat
                return (uint)(base.GetBinarySize() + sizeof(uint) + stat.GetBinarySize());
            if (Type == MessageType.Rwstat)
                // size = base header size + count(32 bits) + count*8
                return (uint)base.GetBinarySize();
            return 0;
        }

        /// <summary>
        /// Twstat message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            Array.Copy(BitConverter.GetBytes(stat.GetBinarySize()), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            Array.Copy(stat.GetBinary(), 0, res, pos, stat.GetBinarySize());
            pos += (int)stat.GetBinarySize();

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
            if (Type == MessageType.Twstat)
                pos = GetTBinary(res, pos);
            if (Type == MessageType.Rwstat)
            {
                throw new Exception("Rwstat not implemented yet");
            }
            return res;
        }

        /// <summary>
        /// Rread decoder
        /// </summary>
        /// <param name="data"></param>
        public void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <param name="data"></param>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rwstat)
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

    }
}
