package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;

public class StyxTAuthMessage extends StyxTMessage {
	public final String mUserName;
	public final String mMountPoint;

	protected StyxTAuthMessage(long fid, String userName, String mountPoint) {
		super(MessageType.Tauth, null, fid);
		mUserName = userName;
		mMountPoint = mountPoint;
	}
}
