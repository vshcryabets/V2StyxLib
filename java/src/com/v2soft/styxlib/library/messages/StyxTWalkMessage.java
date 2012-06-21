package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.IStyxDataWriter;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxTWalkMessage 
extends StyxTMessage {
    private long mFID, mNewFID;
    private List<String> mPathElements;

    public StyxTWalkMessage(long fid, long new_fid, String path){
        super(MessageType.Twalk);
        mFID = fid;
        mNewFID = new_fid;
        setPath(path);
    }

    @Override
    public void load(IStyxDataReader input) throws IOException  {
        mFID = input.readUInt32();
        mNewFID = input.readUInt32();
        int count = input.readUInt16();
        mPathElements = new LinkedList<String>();
        for (int i=0; i<count; i++) {
            mPathElements.add(input.readUTFString());
        }
    }
    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws UnsupportedEncodingException, IOException {
        super.writeToBuffer(output);
        output.writeUInt32(mFID);
        output.writeUInt32(mNewFID);
        if (mPathElements != null) {
            output.writeUInt16(mPathElements.size());
            for (String pathElement : mPathElements)
                output.writeUTFString(pathElement);
        } else {
            output.writeUInt16(0);
        }
    }

    public long getFID()
    {
        return mFID;
    }

    public long getNewFID()
    {
        return mNewFID;
    }
    public String getPath()	{
        StringBuilder builder = new StringBuilder();
        for (String string : mPathElements) {
            builder.append('/');
            builder.append(string);
        }
        return builder.toString();
    }

    public void setPath(String path) {
        if (path == null) {
            throw new NullPointerException("Path is null");
        }
        mPathElements = new LinkedList<String>();
        if (path.length() > 0 ) {
            StringBuilder builder = new StringBuilder(path);
            while (builder.toString().startsWith(StyxFile.SEPARATOR))
                builder.delete(0, 1);
            while (builder.toString().endsWith(StyxFile.SEPARATOR))
                builder.delete(builder.length() - 1, builder.length());
            String [] pathElements = builder.toString().split(StyxFile.SEPARATOR);
            mPathElements.addAll(Arrays.asList(pathElements));
        }
    }

    public int getPathLength() {
        if (mPathElements == null)
            return 0;
        return mPathElements.size();
    }

    public List<String> getPathElements() {
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
                mFID, mNewFID, mPathElements.size(), getPath());
    }

    @Override
    protected MessageType getRequiredAnswerType() {
        return MessageType.Rwalk;
    }

}
