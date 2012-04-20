package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxTWalkMessage 
    extends StyxTMessage {
	private long mFID, mNewFID;
	private String mPath;
	private String[] mPathElements;

	public StyxTWalkMessage(long fid, long new_fid, String path){
		super(MessageType.Twalk);
		mFID = fid;
		mNewFID = new_fid;
		setPath(path);
	}
	
    @Override
    public void load(StyxBufferOperations input) throws IOException  {
        String path = "";
        setFID(input.readUInt32());
        setNewFID(input.readUInt32());
        int count = input.readUInt16();
        for (int i=0; i<count; i++) {
            String stmp = input.readUTF();
            if (!path.equals(""))
                path += "/";
            path += stmp;
        }
        setPath(path);
    }
    @Override
    public void writeToBuffer(StyxBufferOperations output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt(getFID());
        output.writeUInt(getNewFID());
        if (mPathElements != null)
        {
            output.writeUShort(mPathElements.length);
            for (String pathElement : mPathElements)
                output.writeUTF(pathElement);
        } else {
            output.writeUShort(0);
        }
    }
	
	public long getFID()
	{
		return mFID;
	}
	
	public void setFID(long fid)
	{
		mFID = fid;
	}
	
	public long getNewFID()
	{
		return mNewFID;
	}
	
	public void setNewFID(long new_fid)
	{
		mNewFID = new_fid;
	}
	
	public String getPath()
	{
		if (mPath == null)
			return "";
		return mPath;
	}
	
	public void setPath(String path) {
		if (path == null)
			return;
		if (path.equals("")) {
		    mPath = path;
		    mPathElements = new String[0];
			return;
		}
		StringBuilder builder = new StringBuilder(path);
		while (builder.toString().startsWith(StyxFile.SEPARATOR))
			builder.delete(0, 1);
		while (builder.toString().endsWith(StyxFile.SEPARATOR))
			builder.delete(builder.length() - 1, builder.length());
		mPath = builder.toString();
		mPathElements = mPath.split(StyxFile.SEPARATOR);
	}
	
	public int getPathLength()
	{
		if (mPathElements == null)
			return 0;
		return mPathElements.length;
	}
	
	public String[] getPathIterable()
	{
		return mPathElements;
	}
	
	@Override
	public int getBinarySize() {
		int size = super.getBinarySize() + 10;
		if (mPathElements != null)
		{
			for (String pathElement : mPathElements)
				size += StyxMessage.getUTFSize(pathElement);
		}
		
		return size;
	}
	
	@Override
	protected String internalToString() {
		return String.format("FID: %d\nNewFID: %d\nNumber of walks:%d\nPath: %s",
				mFID, mNewFID, mPathElements.length, mPath);
	}

	@Override
	protected MessageType getRequiredAnswerType() {
		return MessageType.Rwalk;
	}
	
}
