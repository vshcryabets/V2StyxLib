package com.v2soft.styxlib.l5.io;

import java.nio.ByteBuffer;

/**
 * Created by V.Shcryabets on 4/4/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IStyxBuffer {
    /**
     * Read byte array from buffer
     * @param out
     * @param offset
     * @param length
     */
    public int read(byte[] out, int offset, int length);
    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out
     * @param offset
     * @param length
     */
    public int get(byte[] out, int offset, int length);

    /**
     * Move read position pointer in buffer.
     * @param bytes
     */
    public void moveReadPointerBy(int bytes);

    int write(byte[] testBuffer, int offset, int length);

    public ByteBuffer getBuffer();

    void clear();

    void limit(int limit);
}
