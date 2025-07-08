package com.v2soft.styxlib.l5.dev;

import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.StyxTWStatMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class Operations {
    public static String toString(byte[] bytes) {
        if (bytes == null)
            return "null";
        if (bytes.length==0)
            return "empty array";
        final StringBuilder result = new StringBuilder();
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

    public static String toString(StyxMessage message) {
        StringBuilder result = new StringBuilder();
        result.append("Message Type: ");
        result.append(message.type);
        result.append(" Tag: ");
        result.append(message.getTag());

        if (message instanceof StyxTMessageFID) {
            StyxTMessageFID fidMessage = (StyxTMessageFID) message;
            result.append(" FID: ");
            result.append(fidMessage.getFID());
        }

        switch (message.type) {
            case MessageType.Twstat: {
                result.append(" Stat: ");
                result.append(((StyxTWStatMessage) message).stat.toString());
            }
        }
        return result.toString();
    }
}
