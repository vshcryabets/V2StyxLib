package com.v2soft.styxlib.l5.io;

/**
 * Created by V.Shcryabets on 4/4/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface Buffer {
    /**
     * Read byte array from buffer
     * @param out output array
     * @param offset position in output array
     * @param length how many bytes to read
     */
    int read(byte[] out, int offset, int length) throws ArrayIndexOutOfBoundsException, NullPointerException;
    /**
     * Get byte array from buffer, this operation will not move read position pointer
     * @param out output array
     * @param offset position in output array
     * @param length how many bytes to read
     */
    int get(byte[] out, int offset, int length);

    /**
     * Move read position pointer in buffer.
     * @param bytes
     */
    void moveReadPointerBy(int bytes);

    int remainsToRead();
}
