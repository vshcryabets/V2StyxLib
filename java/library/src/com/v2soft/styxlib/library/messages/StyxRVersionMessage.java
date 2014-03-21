package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public class StyxRVersionMessage extends StyxMessage {
	private long mMaxPacketSize;
	private String mProtocolVersion;
	
	public StyxRVersionMessage(long maxPacketSize, String protocolVersion) {
		super(MessageType.Rversion, StyxMessage.NOTAG);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}

    @Override
    public void load(IStyxDataReader input) 
        throws IOException  {
        setMaxPacketSize(input.readUInt32());
        setProtocolVersion(input.readUTFString());
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
	public void writeToBuffer(IStyxDataWriter output) 
	        throws UnsupportedEncodingException, IOException {
		super.writeToBuffer(output);
		output.writeUInt32(getMaxPacketSize());
		output.writeUTFString(getProtocolVersion());		
	}	
	@Override
	public String toString() {
        return String.format("%s\nMaxPocketSize: %d;\nProtocolVersion: %s",
                super.toString(),
                getMaxPacketSize(), getProtocolVersion());
	}
}