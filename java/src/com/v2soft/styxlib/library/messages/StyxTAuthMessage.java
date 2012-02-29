package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxTAuthMessage extends StyxTMessage 
{
	private long mAuthFID;
	private String mUserName;
	private String mMountPoint;
	
	public StyxTAuthMessage()
	{
		this(NOFID);
	}
	
	public StyxTAuthMessage(long fid)
	{
		super(MessageType.Tauth);
		mAuthFID = fid;
	}
	
	public StyxTAuthMessage(int tag)
	{
		this(tag, NOFID);
	}
	
	public StyxTAuthMessage(int tag, long fid)
	{
		super(MessageType.Tauth, tag);
		mAuthFID = fid;
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
		setAuthFID(input.readUInt32());
		setUserName(input.readUTF());
		setMountPoint(input.readUTF());
	}
    @Override
    public void load(DualStateBuffer input) 
        throws IOException  {
        setAuthFID(input.readUInt32());
        setUserName(input.readUTF());
        setMountPoint(input.readUTF());
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
		return super.getBinarySize() + 4
			+ StyxMessage.getUTFSize(getUserName())
			+ StyxMessage.getUTFSize(getMountPoint());
	}
	
	@Override
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUInt(getAuthFID());
		output.writeUTF(getUserName());
		output.writeUTF(getMountPoint());		
	}

	@Override
	protected String internalToString() {
		return String.format("AuthFID: %d\nUserName: %s\nMountPoint: %s",
				getAuthFID(), getUserName(), getMountPoint());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rauth;
	}
	
}
