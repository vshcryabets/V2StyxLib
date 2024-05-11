package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.UTF;

import java.io.IOException;

public class StyxTAuthMessage extends StyxTMessageFID {
	private final String mUserName;
	private final String mMountPoint;

	public StyxTAuthMessage(long fid, String userName, String mountPoint) {
		super(MessageType.Tauth, MessageType.Rauth, fid);
		mUserName = userName;
		mMountPoint = mountPoint;
	}

	public String getUserName() {
		return mUserName;
	}

	public String getMountPoint() {
		return mMountPoint;
	}

	@Override
    public String toString() {
		return String.format("%s\nUserName: %s\nMountPoint: %s",
				super.toString(), getUserName(), getMountPoint());
	}
}
