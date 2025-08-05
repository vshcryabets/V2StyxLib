package com.v2soft.styxlib.l6.io;

import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.utils.GetMessagesFactoryUseCase;

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
     * @param fid styx file id.
     * @param iounit maximal block size.
     */
    public StyxFileBufferedInputStream(IMessageTransmitter messenger,
                                       long fid,
                                       int iounit,
                                       int clientId,
                                       GetMessagesFactoryUseCase getMessagesFactoryUseCase) {
        mUnbufferedInput = new StyxUnbufferedInputStream(fid, messenger, iounit, clientId,
                getMessagesFactoryUseCase);
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
