using System;
using StyxLib.Interfaces;
using StyxLib.Messages;

namespace ServerTest
{
    class StyxServer : StyxServerInterface
    {
        public int Auth(UInt32 afid, string uname, string aname, out StyxFileSystemInterface fsitem)
        {
            throw new Exception("Authentication not required");
        }

        public int Attach(UInt32 afid, string uname, string aname, out StyxFileSystemInterface fsitem)
        {
            while (aname.StartsWith("/")) aname.Remove(0, 1);
            fsitem = new StyxFileSystem(aname);
            return 0;
        }
    }
}
