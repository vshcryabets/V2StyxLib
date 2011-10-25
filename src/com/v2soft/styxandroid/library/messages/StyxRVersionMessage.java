package com.v2soft.styxandroid.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.io.StyxOutputStream;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.MessageType;

public class StyxRVersionMessage extends StyxMessage 
{
	private long mMaxPacketSize;
	private String mProtocolVersion;
	
	public StyxRVersionMessage(long maxPacketSize, String protocolVersion)
	{
		super(MessageType.Rversion);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}
	
	public StyxRVersionMessage(int tag)
	{
		super(MessageType.Rversion, tag);
	}
	
    @Override
    public void load(StyxInputStream input) 
        throws IOException  {
        setMaxPacketSize(input.readUInt());
		setProtocolVersion(input.readUTF());
	}
	
	public long getMaxPacketSize() {return mMaxPacketSize;}
	
	public void setMaxPacketSize(long max_pocket_size)
	{
		mMaxPacketSize = max_pocket_size;
	}
	
	public String getProtocolVersion()
	{
		if (mProtocolVersion == null)
			return "";
		return mProtocolVersion;
	}
	
	public void setProtocolVersion(String protocol_version)
	{
		mProtocolVersion = protocol_version;
	}
	
	@Override
	public int getBinarySize() {
		return super.getBinarySize() + 4
			+ StyxMessage.getUTFSize(getProtocolVersion());
	}
	
	@Override
	protected void internalWriteToStream(StyxOutputStream output)
			throws IOException 
	{
		output.writeUInt(getMaxPacketSize());
		output.writeUTF(getProtocolVersion());		
	}

	@Override
	protected String internalToString() {
		return String.format("MaxPocketSize: %d;\nProtocolVersion: %s", 
				getMaxPacketSize(), getProtocolVersion());
	}

}
