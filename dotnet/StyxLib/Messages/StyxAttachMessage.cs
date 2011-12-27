// StyxAttachMessage.cs - Styx Attach message representation
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
    public class StyxAttachMessage : StyxMessage, IStyxMessage
    {
        #region Variables
        private UInt32 fid;
        private UInt32 afid;
        private byte[] username;
        private byte[] mountpoint;
        private QID qid;
        #endregion

        #region Constructors
        public StyxAttachMessage()
        {
        }

		/// <summary>
		/// With this constructor a Tattach message will be created
		/// </summary>
		/// <param name="tag">
		/// A <see cref="System.UInt16"/> message tag
		/// </param>
		/// <param name="fid">
		/// A <see cref="UInt32"/> root fid
		/// </param>
		/// <param name="afid">
		/// A <see cref="UInt32"/>
		/// </param>
		/// <param name="username">
		/// A <see cref="System.String"/> user name
		/// </param>
		/// <param name="mountpoint">
		/// A <see cref="System.String"/>
		/// </param>
        public StyxAttachMessage(ushort tag, UInt32 fid, UInt32 afid, String username, String mountpoint)
        {
            this.Tag = tag;
            this.fid = fid;
            this.Type = MessageType.Tattach;
            this.afid = afid;
            UserName = username;
            MountPoint = mountpoint;
        }

        /// <summary>
        /// With this constructor a Rattach message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="qid"></param>
        public StyxAttachMessage(ushort tag, StyxMessage.QID qid)
        {
            this.Tag = tag;
            this.Type = MessageType.Rattach;
            this.qid = qid;
        }
		#endregion

        #region Properties
        public UInt32 Fid
        {
            get { return fid; }
            set { fid = value; }
        }

        public UInt32 AFid
        {
            get { return afid; }
            set { afid = value; }
        }

        public String UserName
        {
            get { return Encoding.UTF8.GetString(username); }
            set 
            { 
                username = StyxMessage.MsgEncoding.GetBytes(value);
            }
        }

        public String MountPoint
        {
            get { return Encoding.UTF8.GetString(mountpoint); }
            set
            {
                mountpoint = StyxMessage.MsgEncoding.GetBytes(value);
            }
        }

        public QID Qid
        {
            get { return qid; }
            set
            {
                qid = value;
            }
        }
        #endregion

        #region Encoders
        public new uint GetBinarySize()
        {
            
            if (Type == MessageType.Tattach)
            {
                if ((username == null) || (mountpoint == null))
                {
                    throw new NullReferenceException("Not defined UserName or MountPoint");
                }
                // size = base header size + fid(32 bits) + afid(32 bits) + string lentgh (16 bits) + string value size + string lentgh (16 bits) + string value size
                return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(uint) + sizeof(ushort) + username.Length + sizeof(ushort) + mountpoint.Length);
            }
            if (Type == MessageType.Rattach)
                // size = base header size + aqid(13 bytes)
                return (uint)(base.GetBinarySize() + StyxMessage.QIDSIZE);
            return 0;
        }

        /// <summary>
        /// Tattach message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetTBinary(byte[] res, int pos)
        {
            // fid
            Array.Copy(BitConverter.GetBytes(fid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);
            // afid
            Array.Copy(BitConverter.GetBytes(afid), 0, res, pos, sizeof(uint));
            pos += sizeof(uint);

            // user name length
            Array.Copy(BitConverter.GetBytes((ushort)username.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            // user name
            Array.Copy(username, 0, res, pos, username.Length);
            pos += username.Length;

            // aname length
            Array.Copy(BitConverter.GetBytes((ushort)mountpoint.Length), 0, res, pos, sizeof(ushort));
            pos += sizeof(ushort);
            // aname                
            Array.Copy(mountpoint, 0, res, pos, mountpoint.Length);
            pos += mountpoint.Length;
            return pos;
        }

        /// <summary>
        /// Rattach message encoder
        /// </summary>
        /// <param name="res"></param>
        /// <param name="pos"></param>
        /// <returns></returns>
        public int GetRBinary(byte[] res, int pos)
        {
            // qid
            Array.Copy( StyxMessage.getQuidArray(qid), 0, res, pos, StyxMessage.QIDSIZE);
            pos += (int)StyxMessage.QIDSIZE;

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
            if (Type == MessageType.Tattach)
            {
                pos = GetTBinary(res, pos);
            }
            if (Type == MessageType.Rattach)
            {
                pos = GetRBinary(res, pos);
            }
            return res;
        }

		/// <summary>
		/// Rattach decoder
		/// </summary>
		/// <param name="data">
		/// A <see cref="System.Byte"/> input data buffer
		/// </param>
        public void SetRBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            this.qid = StyxMessage.getQuid(data, pos);
        }

        /// <summary>
        /// Tattach decoder
        /// </summary>
        /// <param name="data">
        /// A <see cref="System.Byte"/> input data buffer
        /// </param>
        public void SetTBinary(byte[] data)
        {
            int pos = (int)base.GetBinarySize();
            
            this.fid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(UInt32);

            this.afid = BitConverter.ToUInt32(data, pos);
            pos += sizeof(UInt32);

            ushort size = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);

            username = new byte[size];
            System.Array.Copy(data,pos,username,0,size);
            pos += size;

            size = BitConverter.ToUInt16(data, pos);
            pos += sizeof(ushort);

            mountpoint = new byte[size];
            System.Array.Copy(data, pos, mountpoint, 0, size);
            pos += size;
        }

		/// <summary>
		/// Message decoder
		/// </summary>
		/// <param name="data">
		/// A <see cref="System.Byte"/> input data buffer
		/// </param>
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if (this.Type == MessageType.Rattach)
            {
                SetRBinary(data);
                return;
            }

            if (this.Type == MessageType.Tattach)
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
