package com.v2soft.styxlib.utils;

public class MetricsAndStats {
    public static int byteBufferAllocation = 0;
    public static int byteArrayAllocation = 0;
    public static int newStyxMessage = 0;
    public static int byteArrayAllocationRRead = 0;
    public static int byteArrayAllocationTWrite = 0;
    public static int byteArrayAllocationUlong = 0;
    public static int byteArrayAllocationIo = 0;

    public static void reset() {
        byteBufferAllocation = 0;
        byteArrayAllocation = 0;
        newStyxMessage = 0;
        byteArrayAllocationRRead = 0;
        byteArrayAllocationTWrite = 0;
        byteArrayAllocationUlong = 0;
        byteArrayAllocationIo = 0;
    }
}
