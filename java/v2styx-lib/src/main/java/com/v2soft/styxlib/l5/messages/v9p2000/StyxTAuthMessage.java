package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class StyxTAuthMessage extends StyxTMessageFID {
	public final String mUserName;
	public final String mMountPoint;

	protected StyxTAuthMessage(long fid, String userName, String mountPoint) {
		super(MessageType.Tauth, MessageType.Rauth, fid);
		mUserName = userName;
		mMountPoint = mountPoint;
	}
}
