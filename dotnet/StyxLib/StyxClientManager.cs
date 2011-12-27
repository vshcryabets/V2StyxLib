// StyxClientManager.cs - Client interface for Styx (9P) protocol
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
using System.IO;
using System.Net;
using System.Net.Sockets;

using StyxLib.Messages;
using StyxLib.Streams;

namespace StyxLib
{
    public class StyxClientManager
    {
        #region Variables
        private String username;
        private String mountpoint;

        private Socket socket;
        private byte[] iobuf; // io-buffer
        private uint iobuf_size = 8192; // io buffer size

        private ushort tag = 100; // current message tag
        private uint afid = StyxMessage.NOFID;
        private uint fid = 10; // or anything else

        private uint root_fid = StyxMessage.NOFID; // remote file system root FID
        private String server; // remote server name
        private int port; // remote server port
        #endregion

        #region Constructors
        public StyxClientManager(String server, int port, String username, String attachpoint, bool needauth )
        {
            this.server = server;
            this.port = port;
            this.username = username;
            this.mountpoint = attachpoint;
            iobuf = new byte[iobuf_size];
            Connect( needauth );
        }
        #endregion

        #region Properties
        public uint Root
        {
            get { return root_fid; }
        }
        #endregion

        #region Private methods
        private void Connect(bool needauth )
        {
            IPHostEntry iphost;
            IPAddress[] addr;
            EndPoint ep = null;

			iphost = Dns.GetHostEntry(server);
			addr = iphost.AddressList;
            foreach (IPAddress item in addr)
            {
                if (item.AddressFamily == AddressFamily.InterNetwork)
                {
                    ep = new IPEndPoint(item, port);
                    break;
                }
            }
            if (ep == null) throw new Exception("Can't connect (over IPv4)");
			socket = new Socket(ep.AddressFamily,SocketType.Stream,ProtocolType.Tcp);
            socket.Connect(ep);
            SendVersion();
            if ( needauth )
                if (SendAuth(ref afid) != 0)
                    throw new Exception("Authentication error");
            if (SendAttach(afid, fid) != 0)
                throw new Exception("Attach error");
        }

        private ushort getTag()
        {
            tag ++;
            return tag;
        }
        #endregion

        public uint getFid()
        {
            fid++;
            Console.WriteLine("Reserved {0}",fid);
            return fid;
        }

        #region Client message routines
        /// <summary>
        /// Send a Tremove message to server
        /// </summary>
        /// <param name="fid">FID of file, that should be removed</param>
        /// <param name="newfid">file FID</param>
        /// <param name="path">file path</param>
        /// <returns></returns>
        public StyxRemoveMessage SendRemove(uint fid)
        {
            StyxRemoveMessage msg = new StyxRemoveMessage(getTag(), fid);
            SendMessage(msg);
            return msg;
        }


        /// <summary>
        /// Send a Tcreate message to server
        /// </summary>
        /// <param name="fid">Root FID</param>
        /// <param name="newfid">file FID</param>
        /// <param name="path">file path</param>
        /// <returns></returns>
        public StyxCreateMessage SendCreate(uint fid, String name, uint perm, byte mode)
        {
            StyxCreateMessage msg = new StyxCreateMessage(getTag(), fid, name, perm, mode);
            SendMessage(msg);
            return msg;
        }

        /// <summary>
        /// Function sends an attach message
        /// </summary>
        /// <param name="afid"></param>
        /// <param name="fid"></param>
        /// <returns></returns>
        private int SendAttach(uint afid, uint fid)
        {
            // set root directory fid
            root_fid = getFid();
            StyxAttachMessage msg = new StyxAttachMessage(getTag(), root_fid, afid, username, "");
            try
            {
                SendMessage(msg);
            }
            catch (StyxErrorMessageException err)
            {
                root_fid = StyxMessage.NOFID;
                return -1;
            }
            return 0;
        }

        /// <summary>
        /// Send a Twalk message to server
        /// </summary>
        /// <param name="fid">Root FID</param>
        /// <param name="newfid">file FID</param>
        /// <param name="path">file path</param>
        /// <returns></returns>
        public StyxWalkMessage SendWalk(uint fid, uint newfid, String path)
        {
            StyxWalkMessage msg = new StyxWalkMessage(getTag(), fid, newfid, path);
            SendMessage(msg);
            return msg;
        }

        /// <summary>
        /// Send a Tclunk message to server
        /// </summary>
        /// <param name="fid">File fid</param>
        /// <returns>Answer from a server</returns>
        public StyxClunkMessage SendClunk(uint fid)
        {
            Console.WriteLine("Free {0}",fid);
            StyxClunkMessage msg = new StyxClunkMessage(getTag(), fid);
            SendMessage(msg);
            return msg;
        }

        /**
         * Sending authentication information
         */
        private int SendAuth(ref uint afid)
        {
            StyxAuthMessage msg = new StyxAuthMessage(getTag(), afid, username, mountpoint);
            int atag = msg.Tag;
            socket.Send(msg.GetBinary());
            int received = socket.Receive(iobuf, (int)iobuf_size, SocketFlags.None);
            try
            {
                msg.SetBinary(iobuf);
            }
            catch (StyxErrorMessageException err)
            {
                afid = StyxMessage.NOFID;
                return 0;
            }
            if ( msg.Tag != atag )
                throw new Exception("SendAuth: Answer tag not match");
            return -1;
        }

        /// <summary>
        /// initiating connection (we should send Tversion)
        /// </summary>
        public void SendVersion()
        {
            StyxVersionMessage msg = new StyxVersionMessage(iobuf_size, "9P2000", true);
            socket.Send(msg.GetBinary());

            int received = socket.Receive(iobuf, 8192, SocketFlags.None);
            msg.SetBinary(iobuf);
            // choose minimum buffer size
            iobuf_size = ( msg.MaxPacketSize < iobuf_size ? msg.MaxPacketSize : iobuf_size );
            iobuf = new byte[iobuf_size];
            return;
        }

        /**
         * Function sends an open message
         */
        public StyxOpenMessage SendOpen(uint fid, byte mode)
        {
            StyxOpenMessage msg = new StyxOpenMessage(getTag(), fid, mode);
            SendMessage(msg);
            return msg;
        }

        /**
         * Function sends an read message
         */
        public StyxReadMessage SendRead(uint fid, ulong offset, uint count)
        {
            StyxReadMessage msg = new StyxReadMessage(getTag(), fid, offset, count);
            SendMessage(msg);
            return msg;
        }


        /// <summary>
        /// Function creates and sends Twrite message
        /// </summary>
        /// <param name="fid">File id</param>
        /// <param name="offset">offset in file</param>
        /// <param name="data">output data buffer</param>
        /// <returns></returns>
        public StyxWriteMessage SendWrite(uint fid, ulong offset, byte[] data, int start_index, int length )
        {
            StyxWriteMessage msg = new StyxWriteMessage(getTag(), fid, offset, data, start_index, length);
            SendMessage(msg);
            return msg;
        }

        /// <summary>
        /// Function sends TRead message
        /// </summary>
        /// <param name="fid">File identificator</param>
        /// <returns></returns>
        public StyxStatMessage SendStat(uint fid)
        {
            StyxStatMessage msg = new StyxStatMessage(getTag(), fid);
            SendMessage(msg);
            return msg;
        }

        /// <summary>
        /// This function sends a message to client
        /// </summary>
        /// <param name="outcome"></param>
        /// <returns></returns>
        public void SendMessage(IStyxMessage outcome)
        {
            ushort atag = outcome.GetTag();
            socket.Send(outcome.GetBinary());
            int received = socket.Receive(iobuf, (int)iobuf_size, SocketFlags.None);
            if (received == 0) throw new Exception("No answer from server");
            outcome.SetBinary(iobuf);
            if (outcome.GetTag() != atag)
                throw new Exception("SendMessage: Answer tag not match");
        }
        #endregion
    }
}
