package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;

public class StyxTAttachMessage extends StyxTMessage 
{
	private long mFID, mAuthFID;
	private String mUserName;
	private String mMountPoint;

	public StyxTAttachMessage()
	{
		this(NOFID, NOFID);
	}
	
	public StyxTAttachMessage(long fid, long afid)
	{
		super(MessageType.Tattach);
		mFID = fid;
		mAuthFID = afid;
	}
	
	public StyxTAttachMessage(int tag)
	{
		this(tag, NOFID, NOFID);
	}
	
	public StyxTAttachMessage(int tag, long fid, long afid)
	{
		super(MessageType.Tattach, tag);
		mFID = fid;
		mAuthFID = afid;
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setFID(input.readUInt32());
		setAuthFID(input.readUInt32());
		setUserName(input.readUTF());
		setMountPoint(input.readUTF());
	}
    @Override
    public void load(DualStateBuffer input) 
        throws IOException  {
        setFID(input.readUInt32());
        setAuthFID(input.readUInt32());
        setUserName(input.readUTF());
        setMountPoint(input.readUTF());
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
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
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
	protected MessageType getNeeded() {
		return MessageType.Rattach;
	}
	
}
