package com.v2soft.styxlib.library.messages.base.enums;

public class ModeType {
    public static final int OREAD = 0;
    public static final int OWRITE = 1;
    public static final int ORDWR = 2;
    public static final int OEXEC = 3;
    public static final int OTRUNC = 0x10;
    
//    OREAD(0x00), OWRITE(0x01), ORDWR(0x02), OEXEC(0x03);
//
//	public static ModeType factory(int b)
//	{
//		ModeType[] modes = ModeType.values();
//		for (ModeType mode : modes)
//			if (mode.getByte() == b)
//				return mode;
//		
//		return null;
//	}
//	
//	private int mByte;
//	
//	private ModeType(int b)
//	{
//		mByte = b;
//	}
//	
//	public int getByte()
//	{
//		return mByte;
//	}
}
