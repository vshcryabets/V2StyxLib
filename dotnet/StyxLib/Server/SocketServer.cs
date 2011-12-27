// SocketServer.cs - 
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
using System.Net;
using System.Net.Sockets;
using System.Threading;
using System.Collections;
using StyxLib.Interfaces;

namespace StyxLib.Server
{
    /// <summary>
    /// 
    /// </summary>
	public class SocketServer 
    {
        #region Variables
        private ArrayList client_handlers;
        private Thread backgorund_listener;
        private Socket listener_v4;
        private Socket listener_v6;
        private StyxServerInterface server;
        private bool process;
        #endregion

        #region Constructors
        public SocketServer(int port, StyxServerInterface server )
        {
            this.server = server;

            client_handlers = new ArrayList();

            IPEndPoint point = new IPEndPoint(IPAddress.Any, port);
            BindSocketV4(point);

            backgorund_listener = new Thread(new ThreadStart(ServerThread));
            process = true;
            backgorund_listener.Start();
        }
        #endregion

        #region Network methods
        private void BindSocketV4(IPEndPoint local_address)
        {
            listener_v4 = new Socket(AddressFamily.InterNetwork,SocketType.Stream,ProtocolType.Tcp);
            listener_v4.Bind(local_address);
            listener_v4.Listen(10);
        }        
        #endregion

        public void ClientThread()
        {
        }

        public void ServerThread()
        {
            while (process)
            {
                Socket client = listener_v4.Accept();
                ClientHandler handler = new ClientHandler(client, server);
                //Thread client_thread = new Thread(new ThreadStart(ClientThread));
                //client_thread.
            }
        }

        #region Callbacks
        public void OnClientConnect(IAsyncResult asyn)
        {
            try
            {
                Socket server = (Socket)asyn.AsyncState;
                Socket client = server.EndAccept(asyn);
                server.BeginAccept(new AsyncCallback(OnClientConnect), null);
                //                m_socWorker = m_socListener.EndAccept(asyn);
  //              WaitForData(m_socWorker);
            }
            catch (ObjectDisposedException)
            {
                System.Diagnostics.Debugger.Log(0, "1", "\n OnClientConnection: Socket has been closed\n");
            }
            catch (SocketException err)
            {
            }
        }
        #endregion
    }
}
