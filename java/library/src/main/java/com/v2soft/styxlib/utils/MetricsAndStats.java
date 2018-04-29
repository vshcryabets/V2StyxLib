package com.v2soft.styxlib.utils;

public class MetricsAndStats {
    public static long byteBufferAllocation = 0;
    public static long byteArrayAllocation = 0;

    public static void reset() {
        byteBufferAllocation = 0;
        byteArrayAllocation = 0;
    }
}
