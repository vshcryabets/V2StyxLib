package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTAttachMessage extends StyxTMessage 
{
	private long mFID, mAuthFID;
	private String mUserName;
	private String mMountPoint;

	public StyxTAttachMessage(long fid, long afid, String username, String mountpoint){
		super(MessageType.Tattach);
		mFID = fid;
		mAuthFID = afid;
		mUserName = username;
		mMountPoint = mountpoint;
	}
	
    @Override
    public void load(StyxDataReader input) 
        throws IOException  {
        setFID(input.readUInt32());
        setAuthFID(input.readUInt32());
        setUserName(input.readUTFString());
        setMountPoint(input.readUTFString());
    }
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	public long getAuthFID()
	{
		return mAuthFID;
	}
	
	public void setAuthFID(long afid)
	{
		mAuthFID = afid;
	}
	
	public String getUserName()
	{
		if (mUserName == null)
			return "";
		return mUserName;
	}
	
	public void setUserName(String user_name)
	{
		mUserName = user_name;
	}
	
	public String getMountPoint()
	{
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
		int res= super.getBinarySize() + 8
			+ StyxMessage.getUTFSize(getUserName())
			+ StyxMessage.getUTFSize(getMountPoint());
		return res;
	}
	
	@Override
	public void writeToBuffer(StyxDataReader output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
        output.writeUInt(getFID());
        output.writeUInt(getAuthFID());
        output.writeUTF(getUserName());
        output.writeUTF(getMountPoint());       
	}

	@Override
	protected String internalToString() {
		return String.format("FID: %d\nAuthFID: %d\nUserName: %s\nMountPoint: %s",
				getFID(), getAuthFID(), getUserName(), getMountPoint());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rattach;
	}
	
}
