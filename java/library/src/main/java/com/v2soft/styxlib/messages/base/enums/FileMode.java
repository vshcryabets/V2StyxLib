package com.v2soft.styxlib.messages.base.enums;

/**
 * File mode bits
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 *
 */
public enum FileMode {
	Directory(0x80000000L),
	AppendOnly(0x40000000L),
	ExclusiveUse(0x20000000L),
	MountedChannel(0x10000000L),
	AuthenticationFile(0x08000000L),
	TemporaryFile(0x04000000L),
	SymLinkFile(0x02000000),
	LinkFile(0x01000000),
	DeviceFile(0x00800000),
	NamedPipeFile(0x00200000),
	SocketFile(0x00100000),

	ReadOwnerPermission(0x00000100L),
	WriteOwnerPermission(0x00000080L),
	ExecuteOwnerPermission(0x00000040L),
	ReadGroupPermission(0x00000020L),
	WriteGroupPermission(0x00000010L),
	ExecuteGroupPermission(0x00000008L),
	ReadOthersPermission(0x00000004L),
	WriteOthersPermission(0x00000002L),
	ExecuteOthersPermission(0x00000001L);

	public static long PERMISSION_BITMASK = 0x000001FFL;

	public static FileMode factory(long mode)
	{
		FileMode modes[] = FileMode.values();
		for (FileMode fmode : modes)
			if (fmode.getMode() == mode)
				return fmode;

		return null;
	}

	public static long getPermissionsByMode(long mode)
	{
		return mode & PERMISSION_BITMASK;
	}

	private long mMode;

	private FileMode(long mode)
	{
		mMode = mode;
	}

	public long getMode()
	{
		return mMode;
	}

	public boolean check(long mode)
	{
		return ((mode & getMode()) != 0L);
	}
}
