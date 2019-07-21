package com.v2soft.styxlib.io;

import java.nio.ByteBuffer;

/**
 * Created by V.Shcryabets on 4/4/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IStyxBuffer {
    /**
     * Read byte array from buffer.
     * @param out output buffer.
     * @param offset offset in out data array.
     * @param length
     * @return number of bytes read.
     */
    int read(byte[] out, int offset, int length);
    /**
     * Get byte array from buffer, this operation will not move read position pointer.
     * @param out
     * @param offset offset in out data array.
     * @param length
     */
    int get(byte[] out, int offset, int length);

    /**
     * Move read position pointer in buffer.
     * @param bytes
     */
    void moveReadPointerBy(int bytes);

    /**
     * Write data to buffer. Usually this method used for testing purposes.
     * @param in input data.
     * @param offset offset in input data array
     * @param length length of input data
     * @return
     */
    int write(byte[] in, int offset, int length);

    /**
     * Return buffer.
     * @return
     */
    ByteBuffer getBuffer();

    /**
     * Reset position &amp; limit
     */
    void clear();
}
