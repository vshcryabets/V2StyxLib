// StyxStatMessage.cs - Styx Stat message representation
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
    public class StyxStatMessage : StyxMessage, IStyxMessage
    {
        #region Message fields
        // Tstat
        private UInt32 fid;
        //Rstat
        private StatStructure stat;
        #endregion

        #region Constructors
        public StyxStatMessage()
        {
        }

        /// <summary>
        /// With this constructor a Rstat message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="fid"></param>
        public StyxStatMessage(ushort tag, StatStructure stat)
        {
            this.Tag = tag;
            this.Type = MessageType.Rstat;
            this.stat = stat;
        }

        /// <summary>
        /// With this constructor a TStat message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="fid"></param>
        public StyxStatMessage(ushort tag, UInt32 fid)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Tstat;
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

        #region Encoders
        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tstat )
                // size = base header size + fid(32 bits)
                return (uint)(base.GetBinarySize() + sizeof(uint));
            if ( (Type == MessageType.Rstat) && (stat != null ))
                // size = base header size + sizeof structure(16 bits) + structure
                return (uint)(base.GetBinarySize() + sizeof(ushort) + stat.GetBinarySize() );
            return 0;
        }

        /// <summary>
        /// Tstat message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            return pos;
        }

        /// <summary>
        /// Rstat message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetRBinary(byte[] res, int pos)
        {
            // stat size
            byte[] stat_arr = stat.GetBinary();
            Array.Copy(BitConverter.GetBytes(stat_arr.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);

            // stat structure
            Array.Copy(stat_arr, 0, res, pos, stat_arr.Length);
            pos += stat_arr.Length;

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
            if (Type == MessageType.Tstat)
            {
                pos = GetTBinary(res, pos);
            }
            if (Type == MessageType.Rstat)
            {
                pos = GetRBinary(res, pos);
            }
            return res;
        }

        /// <summary>
        /// Rstat decoder
        /// </summary>
        /// <param name="data"></param>
        public void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            int length = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);
            stat = StatStructure.getStat(data, pos);
        }

        /// <summary>
        /// Tstat decoder
        /// </summary>
        /// <param name="data"></param>
        public void SetTBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            Fid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(UInt32);
        }

        /// <summary>
        /// message decoder
        /// </summary>
        /// <param name="data"></param>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rstat)
            {
                SetRBinary(data);
                return;
            }
            if (this.Type == MessageType.Tstat)
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
