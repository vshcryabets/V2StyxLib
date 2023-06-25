package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IStyxDataReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

public class StyxTAuthMessage extends StyxTMessageFID {
	private String mUserName;
	private String mMountPoint;

	public StyxTAuthMessage(long fid, String userName, String mountPoint) {
		super(MessageType.Tauth, MessageType.Rauth, fid);
		mUserName = userName;
		mMountPoint = mountPoint;
	}

    @Override
    public void load(IStyxDataReader input)
        throws IOException  {
        super.load(input);
		mUserName = input.readUTFString();
        mMountPoint = input.readUTFString();
    }

	public String getUserName() {
		if (mUserName == null)
			return "";
		return mUserName;
	}

	public String getMountPoint() {
		if (mMountPoint == null)
			return "";
		return mMountPoint;
	}

	@Override
	public int getBinarySize() {
		return super.getBinarySize()
			+ UTF.getUTFSize(getUserName())
			+ UTF.getUTFSize(getMountPoint());
	}

	@Override
    public String toString() {
		return String.format("%s\nUserName: %s\nMountPoint: %s",
				super.toString(), getUserName(), getMountPoint());
	}
}
