package com.v2soft.styxlib.l5.dev;

import com.v2soft.styxlib.Logger;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.StyxTAuthMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTAttachMessage;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxTVersionMessage;
import com.v2soft.styxlib.l5.messages.StyxTWStatMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;

public class Operations {
    public static String toString(byte[] bytes) {
        if (bytes == null)
            return "null";
        if (bytes.length == 0)
            return "empty array";
        final StringBuilder result = new StringBuilder();
        result.append(Integer.toHexString(((int) bytes[0]) & 0xFF));
        result.append(",");
        int count = bytes.length;
        for (int i = 1; i < count; i++) {
            result.append(Integer.toHexString(((int) bytes[i]) & 0xFF));
            result.append(',');
        }
        return String.format("(%s)", result);
    }

    public static String toString(StyxMessage message) {
        StringBuilder result = new StringBuilder();
        result.append("Message Type: ");
        result.append(message.getType());
        result.append(" Tag: ");
        result.append(message.getTag());

        if (message instanceof StyxTMessageFID) {
            StyxTMessageFID fidMessage = (StyxTMessageFID) message;
            result.append(" FID: ");
            result.append(fidMessage.getFID());
        }

        switch (message.getType()) {
            case MessageType.Twstat: {
                result.append(" Stat: ");
                result.append(((StyxTWStatMessage) message).stat.toString());
            }
            break;
            case MessageType.Tversion:
                result.append("MaxPocketSize:");
                result.append(((StyxTVersionMessage) message).maxPacketSize);
                result.append(" ProtocolVersion:");
                result.append(((StyxTVersionMessage) message).protocolVersion);
                break;
            case MessageType.Tattach:
                result.append("AuthFID: ");
                result.append(((StyxTAttachMessage) message).authFID);
                result.append("UserName: ");
                result.append(((StyxTAttachMessage) message).userName);
                result.append("MountPoint: ");
                result.append(((StyxTAttachMessage) message).mountPoint);
                break;
            case MessageType.Tauth:
                result.append("UserName: ");
                result.append(((StyxTAuthMessage) message).mUserName);
                result.append("MountPoint: ");
                result.append(((StyxTAuthMessage) message).mMountPoint);
                break;
        }
        return result.toString();
    }

    public static void printStacktrace(String tag) {
        var stackTrace = Thread.currentThread().getStackTrace();
        Logger.d(tag, "Stack trace:");
        for (StackTraceElement element : stackTrace) {
            Logger.d(tag, element.toString());
        }
    }
}
