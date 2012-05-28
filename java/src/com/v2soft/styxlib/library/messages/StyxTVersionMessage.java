package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxTVersionMessage extends StyxTMessage {
	private long mMaxPacketSize;
	private String mProtocolVersion;
	
	public StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
		this();
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}
	
	public StyxTVersionMessage() {
		super(MessageType.Tversion);
	}
	
    @Override
    public void load(IStyxDataReader input)
        throws IOException {
        setMaxPacketSize(input.readUInt32());
        setProtocolVersion(input.readUTFString());
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
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
        output.writeUInt32(getMaxPacketSize());
        output.writeUTFString(getProtocolVersion());      
	}

	@Override
	protected String internalToString() {
		return String.format("MaxPocketSize: %d;\nProtocolVersion: %s", 
				getMaxPacketSize(), getProtocolVersion());
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rversion;
	}
}
