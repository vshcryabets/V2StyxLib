package com.v2soft.styxlib.l5.serializtion;

import java.io.UnsupportedEncodingException;

public class UTF {
    public static int getUTFSize(String utf) {
        if (utf == null)
            return 2;
        return 2 + countUTFBytes(utf);
    }

    public static int countUTFBytes(String utf)	{
        String test = new String(utf);
        byte[] data = null;
        try {
            data = test.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if ( data == null ) {
            return 0;
        } else {
            return data.length;
        }
    }
}
