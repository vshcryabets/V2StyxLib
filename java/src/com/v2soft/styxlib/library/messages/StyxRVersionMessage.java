package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

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
        setMaxPacketSize(input.readUInt32());
		setProtocolVersion(input.readUTF());
	}
    @Override
    public void load(DualStateBuffer input) 
        throws IOException  {
        setMaxPacketSize(input.readUInt32());
        setProtocolVersion(input.readUTF());
    }
    
    // TODO should max packet size be long? or int?
	public long getMaxPacketSize() {return mMaxPacketSize;}
	
	public void setMaxPacketSize(long max_packet_size)
	{
		mMaxPacketSize = max_packet_size;
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
	public void writeToBuffer(StyxBufferOperations output) 
	        throws UnsupportedEncodingException, IOException {
		super.writeToBuffer(output);
		output.writeUInt(getMaxPacketSize());
		output.writeUTF(getProtocolVersion());		
	}	

	@Override
	protected String internalToString() {
		return String.format("MaxPocketSize: %d;\nProtocolVersion: %s", 
				getMaxPacketSize(), getProtocolVersion());
	}

}
