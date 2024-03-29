package com.v2soft.styxlib.l5.enums;

public enum QIDType {
	QTDIR(0x80),
	QTAPPEND(0x40),
	QTEXCL(0x20),
	QTMOUNT(0x10),
	QTAUTH(0x08),
	QTTMP(0x04),
	QTSYMLINK(0x02),
	QTLINK(0x01),
	QTFILE(0x00);

	private final int mByte;

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
