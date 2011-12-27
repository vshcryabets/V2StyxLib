using System;
using StyxLib.Server;

namespace ServerTest
{
    class Program
    {
        static void Main(string[] args)
        {
            StyxFileSystem.Prefix = "C:/iso/inferno/";
            StyxServer server_interface = new StyxServer();
            SocketServer server = new SocketServer(564, server_interface);
            Console.ReadLine();
        }
    }
}
