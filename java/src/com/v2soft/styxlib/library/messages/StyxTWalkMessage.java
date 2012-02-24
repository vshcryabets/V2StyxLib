package com.v2soft.styxlib.library.messages;

import java.io.IOException;

import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.DualStateBuffer;

public class StyxTWalkMessage extends StyxTMessage {
	private long mFID, mNewFID;
	private String mPath;
	private String[] mPathElements;

	public StyxTWalkMessage(){this(NOFID, NOFID);}
    public StyxTWalkMessage(int tag){this(tag, NOFID, NOFID);}

    public StyxTWalkMessage(long fid, long new_fid){
		super(MessageType.Twalk);
		mFID = fid;
		mNewFID = new_fid;
	}
	
	public StyxTWalkMessage(int tag, long fid, long new_fid){
		super(MessageType.Twalk, tag);
		mFID = fid;
		mNewFID = new_fid;
	}
	
	@Override
	public void load(StyxInputStream input) throws IOException	{
	    String path = "";
		setFID(input.readUInt32());
		setNewFID(input.readUInt32());
		int count = input.readUInt16();
		for (int i=0; i<count; i++)	{
			String stmp = input.readUTF();
			if (!path.equals(""))
				path += "/";
			path += stmp;
		}
		setPath(path);
	}
    @Override
    public void load(DualStateBuffer input) throws IOException  {
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
    protected void internalWriteToStream(StyxOutputStream output)
            throws IOException 
    {
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
	
	public void setPath(String path)
	{
		if (path == null)
			return;
		if (path.equals(""))
			return;
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
				getFID(), getNewFID(), mPathElements.length, getPath());
	}

	@Override
	protected MessageType getNeeded() {
		return MessageType.Rwalk;
	}
	
}
