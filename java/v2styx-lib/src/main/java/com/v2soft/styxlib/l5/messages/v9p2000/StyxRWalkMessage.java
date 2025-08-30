package com.v2soft.styxlib.l5.messages.v9p2000;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.structs.QID;

import java.util.List;

public class StyxRWalkMessage extends BaseMessage {
	public final List<QID> qidList;

	protected StyxRWalkMessage(int tag, List<QID> qids) {
		super(MessageType.Rwalk, tag, null, 0, 0, null);
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
