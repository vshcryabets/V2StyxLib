// StyxFileSystemInterface.cs - 
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
using StyxLib.Messages;

namespace StyxLib.Interfaces
{
	public interface StyxFileSystemInterface
	{
        int Walk(out StyxFileSystemInterface newitem, out StyxMessage.QID [] qids, string [] names);

        int Open(byte mode, out StyxMessage.QID qid);
        int Create(String name, int perm, byte mode);

        int Remove();

        int Stat(out StyxLib.Messages.Structures.StatStructure info);
        int Wstat(StyxLib.Messages.Structures.StatStructure info);

        int Read(byte[] buffer, int buffer_offset, UInt64 file_offset, int count, out int readed);
        int Write(byte[] buffer, int buffer_offset, UInt64 file_offset, int count, out int writed);

        StyxLib.Messages.StyxMessage.QID GetQID();
        int Close();

	}
}
