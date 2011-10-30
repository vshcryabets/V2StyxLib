package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTVersionMessage extends StyxTMessage 
{
	private long mMaxPacketSize;
	private String mProtocolVersion;
	
	public StyxTVersionMessage(long maxPacketSize, String protocolVersion)
	{
		super(MessageType.Tversion);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}
	
	public StyxTVersionMessage(int tag)
	{
		super(MessageType.Tversion, tag);
	}
	
	@Override
	public void load(StyxInputStream input) 
		throws IOException	{
		setMaxPacketSize(input.readUInt());
		setProtocolVersion(input.readUTF());
	}
	
	public long getMaxPacketSize()
	{
		return mMaxPacketSize;
	}
	
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

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rversion;
	}
}
