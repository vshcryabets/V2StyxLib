package com.v2soft.styxlib.l5.enums;

public enum MessageType {
    Unspecified(0),
    Tversion(100),
    Rversion(101),
    Tauth(102),
    Rauth(103),
    Tattach(104),
    Rattach(105),
    Rerror(107),
    Tflush(108),
    Rflush(109),
    Twalk(110),
    Rwalk(111),
    Topen(112),
    Ropen(113),
    Tcreate(114),
    Rcreate(115),
    Tread(116),
    Rread(117),
    Twrite(118),
    Rwrite(119),
    Tclunk(120),
    Rclunk(121),
    Tremove(122),
    Rremove(123),
    Tstat(124),
    Rstat(125),
    Twstat(126),
    Rwstat(127);

    private final int mByte;

    public static MessageType factory(int b) {
        MessageType[] messages = MessageType.values();
        for (MessageType message : messages)
            if (message.getByte() == b)
                return message;
        System.out.println("Wrong message type=" + b);
        return null;
    }

    MessageType(int b) {
        mByte = (byte) b;
    }

    public int getByte() {
        return mByte;
    }

    public boolean isTMessage() {
        return mByte % 2 == 0;
    }
}
