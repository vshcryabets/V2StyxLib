package com.v2soft.styxandroid.library.messages.base.enums;

public enum ModeType {
	OREAD(0x00), OWRITE(0x01), ORDWR(0x02), OEXEC(0x03);
	
	public static ModeType factory(int b)
	{
		ModeType[] modes = ModeType.values();
		for (ModeType mode : modes)
			if (mode.getByte() == b)
				return mode;
		
		return null;
	}
	
	private int mByte;
	
	private ModeType(int b)
	{
		mByte = b;
	}
	
	public int getByte()
	{
		return mByte;
	}
}
