package com.v2soft.styxandroid.library.messages.base.enums;

public enum QIDType {
	QTDIR(0x80), QTAPPEND(0x40), QTEXCL(0x20), QTMOUNT(0x10), QTAUTH(0x08), QTFILE(0x00);
	
	private int mByte;
	
	public static QIDType factory(int b)
	{
		QIDType[] types = QIDType.values();
		for (QIDType type : types)
			if (type.getByte() == b)
				return type;
		
		return null;
	}
	
	private QIDType(int b)
	{
		mByte = b;
	}
	
	public int getByte()
	{
		return mByte;
	}
}
