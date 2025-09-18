package com.v2soft.styxlib.l4;

public interface StyxBuffer {
    /**
     * Returns the number of bytes currently in the buffer.
     * @return the size of the buffer
     */
    int size();
    /**
     * Returns true if the buffer is empty.
     * @return true if buffer is empty, false otherwise
     */
    boolean isEmpty();
    /**
     * Clears the buffer.
     */
    void clear();
    /**
     * Returns the contents of the buffer as a byte array.
     * @return the buffer contents
     */
    byte[] getBytes();
    /**
     * Appends the given bytes to the buffer.
     * @param data the bytes to add
     */
    void putBytes(byte[] data);
}
