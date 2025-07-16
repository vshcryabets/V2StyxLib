package com.v2soft.styxlib.l5.messages;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.BaseMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;

import java.util.List;

public class StyxRWalkMessage extends BaseMessage {
	public final List<StyxQID> qidList;

	public StyxRWalkMessage(int tag, List<StyxQID> qids) {
		super(MessageType.Rwalk, tag, null);
		qidList = qids;
	}

	@Override
	public String toString() {
        String result = super.toString()+"\nNumber of walks: "+ qidList.size()+"\n"; int num = 0;
        for (var qid : qidList)
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
