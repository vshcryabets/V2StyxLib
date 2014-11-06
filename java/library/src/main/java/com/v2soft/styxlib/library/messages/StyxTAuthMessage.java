package com.v2soft.styxlib.library.messages;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTAuthMessage extends StyxTMessageFID {
	private String mUserName;
	private String mMountPoint;
	
	public StyxTAuthMessage(long fid) {
		super(MessageType.Tauth, MessageType.Rauth, fid);
	}

    @Override
    public void load(IStyxDataReader input) 
        throws IOException  {
        super.load(input);
        setUserName(input.readUTFString());
        setMountPoint(input.readUTFString());
    }
	
	public String getUserName() {
		if (mUserName == null)
			return "";
		return mUserName;
	}
	
	public void setUserName(String user_name) {
		mUserName = user_name;
	}
	
	public String getMountPoint() {
		if (mMountPoint == null)
			return "";
		return mMountPoint;
	}
	
	public void setMountPoint(String mount_point)
	{
		mMountPoint = mount_point;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize()
			+ StyxMessage.getUTFSize(getUserName())
			+ StyxMessage.getUTFSize(getMountPoint());
	}
	
	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUTFString(getUserName());
		output.writeUTFString(getMountPoint());		
	}

	@Override
    public String toString() {
		return String.format("%s\nUserName: %s\nMountPoint: %s",
				super.toString(), getUserName(), getMountPoint());
	}
}
