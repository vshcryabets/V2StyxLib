package com.v2soft.styxlib.library.messages.base;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
import com.v2soft.styxlib.library.messages.StyxRClunkMessage;
import com.v2soft.styxlib.library.messages.StyxRCreateMessage;
import com.v2soft.styxlib.library.messages.StyxRErrorMessage;
import com.v2soft.styxlib.library.messages.StyxRFlushMessage;
import com.v2soft.styxlib.library.messages.StyxROpenMessage;
import com.v2soft.styxlib.library.messages.StyxRReadMessage;
import com.v2soft.styxlib.library.messages.StyxRRemoveMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxRWStatMessage;
import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
import com.v2soft.styxlib.library.messages.StyxRWriteMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
import com.v2soft.styxlib.library.messages.StyxTClunkMessage;
import com.v2soft.styxlib.library.messages.StyxTCreateMessage;
import com.v2soft.styxlib.library.messages.StyxTFlushMessage;
import com.v2soft.styxlib.library.messages.StyxTOpenMessage;
import com.v2soft.styxlib.library.messages.StyxTReadMessage;
import com.v2soft.styxlib.library.messages.StyxTRemoveMessage;
import com.v2soft.styxlib.library.messages.StyxTStatMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTWStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
import com.v2soft.styxlib.library.messages.StyxTWriteMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

public abstract class StyxMessage {
	public static final int BASE_BINARY_SIZE = 7;
	
	public static final int NOTAG  =      0xFFFF;
	public static final long NOFID = 0xFFFFFFFFL;
	
	/*
	public static final int DMDIR    = 0x80000000;
    public static final int DMAPPEND = 0x40000000;
    public static final int DMEXCL   = 0x20000000;
    public static final int DMREAD   = 0x00000004;
    public static final int DMWRITE  = 0x00000002;
    public static final int DMEXEC   = 0x00000001;
    */
	
	private int mTag;
	private MessageType mType;
	
	/**
	 * Construct message from input stream
	 * @param is intput stream
	 * @param io_unit packet size
	 * @return constructed Message object
	 * @throws IOException
	 */
	public static StyxMessage factory(StyxInputStream is, int io_unit) throws IOException {
	    // get common packet data
		long packet_size = is.readUInt();
		if ( packet_size > io_unit ) {
		    // Something wrong, packet size is to big
		    return null;
		}
		MessageType type = MessageType.factory(is.readUByte());
		int tag = is.readUShort();
		// load other data
		int toRead = (int) (packet_size - 7);
//		System.out.println("packet size="+packet_size);
		byte[] data = new byte[toRead];
		int readed = 0;
		while ( readed < toRead ) {
		    readed+= is.read(data, readed, toRead-readed);
		}
        // create Message
		StyxInputStream dataStream = new StyxInputStream(new ByteArrayInputStream(data));
		StyxMessage result = null;
		switch (type) {
        case Tversion:
            result = new StyxTVersionMessage();
            break;
        case Rversion:
            result = new StyxRVersionMessage(tag);
            break;
        case Tauth:
            result = new StyxTAuthMessage(tag);
            break;
        case Tflush:
            result = new StyxTFlushMessage(tag);
            break;
        case Tattach:
            result = new StyxTAttachMessage(tag);
            break;
        case Twalk:
            result = new StyxTWalkMessage(tag);
            break;
        case Rauth:
            result = new StyxRAuthMessage(tag);
            break;
        case Rerror:
            result = new StyxRErrorMessage(tag);
            break;
        case Rflush:
            result = new StyxRFlushMessage(tag);
            break;
        case Rattach:
            result = new StyxRAttachMessage(tag);
            break;
        case Rwalk:
            result = new StyxRWalkMessage(tag); 
            break;
        case Topen:
            result = new StyxTOpenMessage(tag);
            break;
        case Ropen:
            result = new StyxROpenMessage(tag);
            break;
        case Tcreate:
            result = new StyxTCreateMessage(tag);
            break;
        case Rcreate:
            result = new StyxRCreateMessage(tag);
            break;
        case Tread:
            result = new StyxTReadMessage(tag);
            break;
        case Rread:
            result = new StyxRReadMessage(tag);
            break;
        case Twrite:
            result = new StyxTWriteMessage(tag);
            break;
        case Rwrite:
            result = new StyxRWriteMessage(tag);
            break;
        case Tclunk:
            result = new StyxTClunkMessage(tag);
            break;
        case Rclunk:
            result = new StyxRClunkMessage(tag);
            break;
        case Tremove:
            result = new StyxTRemoveMessage(tag);
            break;
        case Rremove:
            result = new StyxRRemoveMessage(tag);
            break;
        case Tstat:
            result = new StyxTStatMessage(tag);
            break;
        case Rstat:
            result = new StyxRStatMessage(tag);
            break;
        case Twstat:
            result = new StyxTWStatMessage(tag);
            break;
        case Rwstat:
            result = new StyxRWStatMessage(tag);
            break;
        }
		result.load(dataStream);
		return result;
	}
	
	public static String toString(byte[] bytes)
	{
		if ( (bytes == null) || (bytes.length==0))
			return "-";
		StringBuilder result = new StringBuilder();
		result.append(Integer.toHexString(((int)bytes[0])&0xFF));
		result.append(",");
		int count = bytes.length;
		for (int i=1; i<count; i++)
		{
			result.append(Integer.toHexString(((int)bytes[i])&0xFF));
			result.append(',');
		}
		
		return String.format("(%s)", result);
	}
	
	public static int getUTFSize(String utf)
	{
		if (utf == null)
			return getUTFSize("");
		return 2 + countUTFBytes(utf);
	}
	
	public static int countUTFBytes(String utf)
	{
	    // TODO !!!! Is it working correct??
		int result = 0;
		int length = utf.length();
		for (int i=0; i<length; i++)
		{
			int posChar = utf.charAt(i);
			if (posChar > 0 && posChar <=127)
				result += 1;
			else if (posChar <= 2047)
				result += 2;
			else 
				result += 3;
		}
		
		return result;
	}
	
	protected StyxMessage(MessageType type)
	{
		this(type, NOTAG);
	}
	
	protected StyxMessage(MessageType type, int tag)
	{
		mType = type;
		mTag = tag;
	}
	
	public MessageType getType()
	{
		return mType;
	}
	
	public int getTag()
	{
		return mTag;
	}
	
	public void setTag(short tag)
	{
		mTag = (tag & 0xFFFF);
	}
	
	public int getBinarySize()
	{
		return BASE_BINARY_SIZE;
	}
	
	public final void writeToStream(OutputStream stream) 
		throws IOException {
		StyxOutputStream output = new StyxOutputStream(stream);
		output.writeUInt(getBinarySize());
		output.writeUByte(getType().getByte());
		output.writeUShort(getTag());
		internalWriteToStream(output);
	}
	
    protected abstract void internalWriteToStream(StyxOutputStream output)
            throws IOException;
	protected abstract String internalToString();
	protected abstract void load(StyxInputStream is)  throws IOException;
	
	@Override
	public String toString() {
		String stmp = String.format("Type: %s\nTag: %d", 
				getType().toString(), getTag());
		
		String internal = internalToString();
		if (internal != null)
			stmp = String.format("%s\n%s", stmp, internal);
		
		return String.format("(\n%s\n)", stmp);
	}
	
}