package com.v2soft.styxlib.library.messages;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.StyxBufferOperations;

public class StyxRWalkMessage extends StyxMessage {
	private List<StyxQID> mQIDList;

	public StyxRWalkMessage()
	{
		super(MessageType.Rwalk);
	}
	
	public StyxRWalkMessage(int tag)
	{
		super(MessageType.Rwalk, tag);
	}
	
    @Override
    public void load(StyxBufferOperations input) 
        throws IOException  {
        int count = input.readUInt16();
        
        ArrayList<StyxQID> list = new ArrayList<StyxQID>();
        for (int i=0; i<count; i++)
            list.add(new StyxQID(input));
        setQIDList(list);
    }
	
	public void setQIDList(StyxQID[] array)
	{
		mQIDList = Arrays.asList(array);
	}
	
	public void setQIDList(Collection<StyxQID> collection)
	{
		mQIDList = new ArrayList<StyxQID>(collection);
	}
	
	public boolean add(StyxQID qid)
	{
		if (mQIDList == null)
			mQIDList = new ArrayList<StyxQID>();
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
	public void writeToBuffer(StyxBufferOperations output)
	        throws UnsupportedEncodingException, IOException {
	    super.writeToBuffer(output);
		output.writeUShort(getQIDListLength());
		if (mQIDList != null)
		{
			for (StyxQID qid : mQIDList)
				qid.writeBinaryTo(output);
		}
	}

	@Override
	protected String internalToString() {
		String result = "Number of walks: "+mQIDList.size()+"\n"; int num = 0;
		
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
