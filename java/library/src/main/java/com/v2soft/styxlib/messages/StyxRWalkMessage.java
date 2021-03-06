package com.v2soft.styxlib.messages;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.IStyxDataWriter;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.messages.base.structs.StyxQID;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StyxRWalkMessage extends StyxMessage {
	private List<StyxQID> mQIDList;

	public StyxRWalkMessage(int tag, List<StyxQID> qids) {
		super(MessageType.Rwalk, tag);
		mQIDList = qids;
	}

    @Override
    public void load(IStyxDataReader input)
        throws IOException  {
        int count = input.readUInt16();

        mQIDList = new LinkedList<StyxQID>();
        for (int i=0; i<count; i++)
            mQIDList.add(new StyxQID(input));
    }

	public void setQIDList(StyxQID[] array)
	{
		mQIDList = Arrays.asList(array);
	}

	public StyxRWalkMessage setQIDList(Collection<StyxQID> collection) {
		mQIDList = new LinkedList<StyxQID>(collection);
		return this;
	}

	public boolean add(StyxQID qid) {
		if (mQIDList == null)
			mQIDList = new LinkedList<StyxQID>();
		return mQIDList.add(qid);
	}

	public boolean remove(StyxQID qid)
	{
		if (mQIDList == null)
			mQIDList = new ArrayList<StyxQID>();
		return mQIDList.remove(qid);
	}

	public Iterable<StyxQID> getQIDIterable()
	{
		return mQIDList;
	}

	public int getQIDListLength()
	{
		if (mQIDList == null)
			return 0;
		return mQIDList.size();
	}

	@Override
	public int getBinarySize() {
		int size = super.getBinarySize() + 2
			+ getQIDListLength() * StyxQID.CONTENT_SIZE;

		return size;
	}

	@Override
	public void writeToBuffer(IStyxDataWriter output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUInt16(getQIDListLength());
		if (mQIDList != null)
		{
			for (StyxQID qid : mQIDList)
				qid.writeBinaryTo(output);
		}
	}

	@Override
	public String toString() {
        String result = super.toString()+"\nNumber of walks: "+mQIDList.size()+"\n"; int num = 0;

        for (StyxQID qid : getQIDIterable())
        {
            num++;
            if (!result.equals(""))
                result += "\n";
            result += String.format("QID #%d: %s",
                    num, qid.toString());
        }

        return result;
	}
}
