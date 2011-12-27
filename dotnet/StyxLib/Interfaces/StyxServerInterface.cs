using System;
using StyxLib.Messages;

namespace StyxLib.Interfaces
{
	public interface StyxServerInterface
	{
        int Auth(UInt32 afid, string uname, string aname, out StyxFileSystemInterface fsitem);
        int Attach(UInt32 afid, string uname, string aname, out StyxFileSystemInterface fsitem);
	}
}
