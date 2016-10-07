package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.StyxFile;
import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxTWalkMessage
extends StyxTMessageFID {
    private long mNewFID;
    private List<String> mPathElements;

    public StyxTWalkMessage(long fid, long new_fid, String path){
        super(MessageType.Twalk, MessageType.Rwalk, fid);
        mNewFID = new_fid;
        setPath(path);
    }

    @Override
    public void load(IStyxDataReader input) throws IOException  {
        super.load(input);
        mNewFID = input.readUInt32();
        int count = input.readUInt16();
        mPathElements = new LinkedList<>();
        for (int i=0; i<count; i++) {
            mPathElements.add(input.readUTFString());
        }
    }
    @Override
    public void writeToBuffer(IStyxDataWriter output)
            throws IOException {
        super.writeToBuffer(output);
        output.writeUInt32(mNewFID);
        if (mPathElements != null) {
            output.writeUInt16(mPathElements.size());
            for (String pathElement : mPathElements)
                output.writeUTFString(pathElement);
        } else {
            output.writeUInt16(0);
        }
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
        int size = super.getBinarySize() + 4 + 2;
        if (mPathElements != null)
        {
            for (String pathElement : mPathElements)
                size += StyxMessage.getUTFSize(pathElement);
        }

        return size;
    }

    @Override
    public String toString() {
        return String.format("%s\nNewFID: %d\nNumber of walks:%d\nPath: %s",
                super.toString(), mNewFID, mPathElements.size(), getPath());
    }
}
