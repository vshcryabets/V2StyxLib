package com.v2soft.styxlib.l5.enums;

public class Checks {
    public static boolean isTMessage(int messageId) {
        return messageId % 2 == 0;
    }
}
