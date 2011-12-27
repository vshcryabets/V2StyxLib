// ClientHandler - this is class for handling clients queiries
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

using System;
using System.Net.Sockets;
//using System.Threading;
using System.Collections.Generic;
using StyxLib.Messages;
using StyxLib.Interfaces;

namespace StyxLib.Server
{
	class ClientHandler
    {
        #region Variables
        private Socket client;
        private byte[] iobuf; // io-buffer
        private uint iobuf_size = 8192; // io buffer size
        private StyxServerInterface server;
        private Dictionary<UInt32, StyxFileSystemInterface> fid_list;
        #endregion

        #region Constructors
        public ClientHandler(Socket client, StyxServerInterface server)
        {
            fid_list = new Dictionary<UInt32, StyxFileSystemInterface>();
            this.client = client;
            this.server = server;

            iobuf = new byte[iobuf_size];
            client.BeginReceive(iobuf, 0, (int)iobuf_size, SocketFlags.None, 
                new AsyncCallback(ServeInput), null);
        }
        #endregion

        #region Threading
        public void ServeInput(IAsyncResult ar)
        {
            if (!client.Connected)
            {
                CloseSession();
            }
            int readed = client.EndReceive(ar);

            // try to decode data
            IStyxMessage result = new StyxErrorMessage("Unknown message");
            StyxMessage message = new StyxMessage();
            message.SetBinary(iobuf);
            try
            {
                switch (message.Type)
                {
                    case StyxMessage.MessageType.Tversion:
                        result = HandleVersion((StyxVersionMessage)message.GetMessage());
                        break;
                    case StyxMessage.MessageType.Tauth:
                        {
                            StyxAuthMessage msg = new StyxAuthMessage();
                            msg.SetBinary(iobuf);
                            result = HandleAuth(msg);
                        }
                        break;
                    case StyxMessage.MessageType.Tattach:
                        result = HandleAttach((StyxAttachMessage)message.GetMessage());
                        break;
                    case StyxMessage.MessageType.Tstat:
                        result = HandleStat((StyxStatMessage)message.GetMessage());
                        break;
                    case StyxMessage.MessageType.Twalk:
                        result = HandleWalk((StyxWalkMessage)message.GetMessage());
                        break;
                    case StyxMessage.MessageType.Tclunk:
                        result = HandleClunk((StyxClunkMessage)message.GetMessage());
                        break;
                    case StyxMessage.MessageType.Topen:
                        result = HandleOpen((StyxOpenMessage)message.GetMessage());
                        break;
                    case StyxMessage.MessageType.Tread:
                        result = HandleRead((StyxReadMessage)message.GetMessage());
                        break;
                }
            }
            catch (Exception err)
            {
                result = new StyxErrorMessage(err.ToString());
            }
            // continue receiving
            client.BeginReceive(iobuf, 0, (int)iobuf_size, SocketFlags.None,
                new AsyncCallback(ServeInput), null);

            if ( result != null )
            {
                result.SetTag( message.Tag );
                PushToQueue(result);
            }
        }

        private void CloseSession()
        {
            client.Close();
        }
        #endregion

        #region IO Queue
        private void PushToQueue(IStyxMessage message)
        {
            try
            {
                // send data
                client.Send(message.GetBinary());
            }
            catch (Exception err)
            {
            }
        }
        #endregion

        #region FID list methods
        private void RegisterFID( UInt32 fid, StyxFileSystemInterface fsitem )
        {
            // check this fid, may be it already busy
            if (fid_list.ContainsKey(fid) )
            {
                throw new Exception("This fid already registered");
            }
            fid_list.Add(fid, fsitem);
        }

        private void ForgetFID(UInt32 fid )
        {
            if ( fid_list.ContainsKey(fid))
            {
                fid_list[fid].Close();
                fid_list.Remove(fid);
            }
        }        
        #endregion

        #region Message handlers
        private IStyxMessage HandleVersion(StyxVersionMessage message)
        {
            if (message.Tag != StyxMessage.NOTAG)
            {
                throw new Exception("Wrong Tag for TVersion");
            }

            // choose minimum buffer size
            iobuf_size = ( message.MaxPacketSize < iobuf_size ? message.MaxPacketSize : iobuf_size );
            iobuf = new byte[iobuf_size];
            return new StyxVersionMessage(iobuf_size, "9P2000", false);
        }

        private IStyxMessage HandleAuth(StyxAuthMessage message)
        {
            StyxFileSystemInterface fitem;
            server.Auth(message.AFid, message.UserName, message.MountPoint, out fitem);
            RegisterFID(message.AFid, fitem);
            return new StyxAuthMessage(message.Tag, fitem.GetQID());
        }

        private IStyxMessage HandleAttach(StyxAttachMessage message)
        {
            StyxFileSystemInterface fsitem;
            server.Attach(message.AFid, message.UserName, message.MountPoint, out fsitem);
            RegisterFID(message.Fid, fsitem);
            return new StyxAttachMessage(message.Tag, fsitem.GetQID() );
        }

        private IStyxMessage HandleStat(StyxStatMessage message)
        {
            if (!fid_list.ContainsKey(message.Fid))
            {
                throw new Exception("Unknown fid");
            }
            StyxFileSystemInterface fsitem = fid_list[message.Fid];
            StyxLib.Messages.Structures.StatStructure info = new StyxLib.Messages.Structures.StatStructure();
            fsitem.Stat( out info);
            return new StyxStatMessage(message.Tag, info);
        }

        private IStyxMessage HandleWalk(StyxWalkMessage message)
        {
            if (!fid_list.ContainsKey(message.Fid))
            {
                throw new Exception("Unknown fid");
            }
            StyxFileSystemInterface fsitem = fid_list[message.Fid];
            
            StyxFileSystemInterface newitem;
            StyxMessage.QID[] qids;
            int res = fsitem.Walk(out newitem, out qids, message.PathComponents);
            if (res == 0)
            {
                // target found
                // we must register fid
                RegisterFID(message.NewFid, newitem);
            }
            return new StyxWalkMessage(message.Tag, qids);
        }

        private IStyxMessage HandleClunk(StyxClunkMessage message)
        {
            if (!fid_list.ContainsKey(message.Fid))
            {
                throw new Exception("Unknown fid "+message.Fid);
            }
            ForgetFID(message.Fid);
            return new StyxClunkMessage(message.Tag);
        }

        private IStyxMessage HandleOpen(StyxOpenMessage message)
        {
            if (!fid_list.ContainsKey(message.Fid))
            {
                throw new Exception("Unknown fid");
            }
            StyxFileSystemInterface fsitem = fid_list[message.Fid];
            StyxMessage.QID qid;
            int res = fsitem.Open(message.Mode, out qid);
            if (res != 0)
            {
                throw new Exception("Open failed("+res+")");
            }
            // max buffer size = iobuf - TWrite extra info size (23 bytes)
            return new StyxOpenMessage(message.Tag, qid, iobuf_size - 23 );
        }

        private IStyxMessage HandleRead(StyxReadMessage message)
        {
            if (!fid_list.ContainsKey(message.Fid))
            {
                throw new Exception("Unknown fid");
            }
            StyxFileSystemInterface fsitem = fid_list[message.Fid];
            StyxMessage.QID qid;
            byte [] buffer = new byte[message.Count];
            int readed;
            int res = fsitem.Read(buffer, 0, message.Offset, (int)message.Count, out readed );
            if (res != 0)
            {
                throw new Exception("Read failed(" + res + ")");
            }
            return new StyxReadMessage(message.Tag, buffer, (uint)readed );
        }
        #endregion

        #region Constructors
        #endregion
    }
}
