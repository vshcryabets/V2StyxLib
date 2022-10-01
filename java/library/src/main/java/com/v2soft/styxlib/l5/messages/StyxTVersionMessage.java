package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.io.IStyxDataReader;
import com.v2soft.styxlib.l5.io.IStyxDataWriter;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessage;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.serializtion.UTF;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class StyxTVersionMessage extends StyxTMessage {
	private long mMaxPacketSize;
	private String mProtocolVersion;

	public StyxTVersionMessage(long maxPacketSize, String protocolVersion) {
        super(MessageType.Tversion, MessageType.Rversion);
		mMaxPacketSize = maxPacketSize;
		mProtocolVersion = protocolVersion;
	}

    @Override
    public void load(IStyxDataReader input)
        throws IOException {
        super.load(input);
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
			+ UTF.getUTFSize(getProtocolVersion());
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
