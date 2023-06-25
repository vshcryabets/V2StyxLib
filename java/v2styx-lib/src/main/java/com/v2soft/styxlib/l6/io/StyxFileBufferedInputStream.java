package com.v2soft.styxlib.l6.io;

import com.v2soft.styxlib.l6.io.StyxUnbufferedInputStream;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IMessageTransmitter;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxFileBufferedInputStream extends InputStream {
    private StyxUnbufferedInputStream mUnbufferedInput;
    private CBufferedInputStream mBufferedInput;
    /**
     * @param messenger
     * @param file styx file id.
     * @param iounit maximal block size.
     */
    public StyxFileBufferedInputStream(IMessageTransmitter messenger,
            long file,
            int iounit,
            ClientDetails recepient) {
        mUnbufferedInput = new StyxUnbufferedInputStream(file, messenger, iounit, recepient);
        mBufferedInput = new CBufferedInputStream(mUnbufferedInput, iounit);
    }

    @Override
    public int read() throws IOException {
        return mBufferedInput.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return mBufferedInput.read(b, off, len);
    }
    @Override
    public int read(byte[] b) throws IOException {
        return mBufferedInput.read(b);
    }

    @Override
    public void close() throws IOException {
        mBufferedInput.close();
    }

    public void seek(long position) throws IOException {
        mBufferedInput.clearBuffer();
        mUnbufferedInput.seek(position);
    }

    private class CBufferedInputStream extends BufferedInputStream {

        public CBufferedInputStream(InputStream in, int size) {
            super(in, size);
        }

        public void clearBuffer() {
            count = 0;
            pos = 0;
        }

    }
}
