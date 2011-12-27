// StyxAuthMessage.cs - Styx Auth message representation
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
    public class StyxAuthMessage : StyxMessage, IStyxMessage
    {
        #region Variables
        // TAuth
        private UInt32 afid;
        private String username;
        private String mountpoint;
        // RAuth
        private StyxMessage.QID aqid;
        #endregion

        #region Constructors
        public StyxAuthMessage()
        {
        }

        /// <summary>
        /// With this constructor a Rauth message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="afid"></param>
        /// <param name="username"></param>
        /// <param name="mountpoint"></param>
        public StyxAuthMessage(ushort tag, StyxMessage.QID aqid)
        {
            this.Tag = tag;
            this.Type = MessageType.Rauth;
            this.aqid = aqid;
        }

        /// <summary>
        /// With this constructor a Tauth message will be created
        /// </summary>
        /// <param name="tag"></param>
        /// <param name="afid"></param>
        /// <param name="username"></param>
        /// <param name="mountpoint"></param>
        public StyxAuthMessage(ushort tag, UInt32 afid, String username, String mountpoint )
        {
            this.Tag = tag;
            this.Type = MessageType.Tauth;
            this.afid = afid;
            this.username = username;
            this.mountpoint = mountpoint;
        }
        #endregion

        #region Properties
        public UInt32 AFid
        {
            get { return afid; }
            set { afid = value; }
        }

        public String UserName
        {
            get { return username; }
            set 
            { 
                username = value;
                Size = GetBinarySize();
            }
        }

        public String MountPoint
        {
            get { return mountpoint; }
            set
            {
                mountpoint = value;
                Size = GetBinarySize();
            }
        }

        public StyxMessage.QID AQid
        {
            get { return aqid; }
            set
            {
                aqid = value;
            }
        }
        #endregion

        public new uint GetBinarySize()
        {
            if ( Type == MessageType.Tauth )
                // size = base header size + afid(32 bits) + string lentgh (16 bits) + string value size + string lentgh (16 bits) + string value size
                return (uint)(base.GetBinarySize() + sizeof(uint) + sizeof(ushort) + username.Length + sizeof(ushort) + mountpoint.Length);
            if (Type == MessageType.Rauth)
                // size = base header size + aqid(13 bytes)
                return (uint)(base.GetBinarySize() + StyxMessage.QIDSIZE);
            return 0;
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
            if (Type == MessageType.Tauth)
            {
                Array.Copy(base_binary, 0, res, 0, base_binary.Length);
                pos = base_binary.Length;
                Array.Copy(BitConverter.GetBytes(afid), 0, res, pos, sizeof(uint));
                pos += sizeof(uint);

                Array.Copy(BitConverter.GetBytes((ushort)username.Length), 0, res, pos, sizeof(ushort));
                pos += sizeof(ushort);
                
                byte[] text = StyxMessage.MsgEncoding.GetBytes(username);
                Array.Copy(text, 0, res, pos, text.Length);
                pos += text.Length;

                Array.Copy(BitConverter.GetBytes((ushort)mountpoint.Length), 0, res, pos, sizeof(ushort));
                pos += sizeof(ushort);
                text = StyxMessage.MsgEncoding.GetBytes(mountpoint);
                Array.Copy(text, 0, res, pos, text.Length);
                pos += text.Length;
            }
            if (Type == MessageType.Rauth)
            {
                throw new Exception("Rauth not implemented yet");
            }
            return res;
        }

        /**
         * message decoder
         */
        public new void SetBinary(byte[] data)
        {
            base.SetBinary(data);
            if ((this.Type != MessageType.Tauth) && (this.Type != MessageType.Rauth))
            {
                if (this.Type == MessageType.Rerror)
                {
                    StyxErrorMessage error = new StyxErrorMessage();
                    error.SetBinary(data);
                    throw new StyxErrorMessageException(error);
                }
                else
                    throw new Exception("Incorrect message type");
            }
            throw new Exception("SetBinary not implemented yet");
        }

    }
}
