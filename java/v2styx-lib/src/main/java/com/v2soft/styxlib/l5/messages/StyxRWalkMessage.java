package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.serialization.BufferReader;
import com.v2soft.styxlib.l5.structs.StyxQID;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class StyxRWalkMessage extends StyxMessage {
	private List<StyxQID> mQIDList;

	public StyxRWalkMessage(int tag, List<StyxQID> qids) {
		super(MessageType.Rwalk, tag);
		mQIDList = qids;
	}

    @Override
    public void load(BufferReader input)
        throws IOException  {
        int count = input.readUInt16();

        mQIDList = new LinkedList<StyxQID>();
        for (int i=0; i<count; i++)
            mQIDList.add(new StyxQID(input));
    }

//	public boolean add(StyxQID qid) {
//		if (mQIDList == null)
//			mQIDList = new LinkedList<StyxQID>();
//		return mQIDList.add(qid);
//	}
//
//	public boolean remove(StyxQID qid)
//	{
//		if (mQIDList == null)
//			mQIDList = new ArrayList<StyxQID>();
//		return mQIDList.remove(qid);
//	}

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
