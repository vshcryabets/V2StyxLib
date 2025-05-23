package com.v2soft.styxlib.l6.io;

import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.messages.StyxRReadMessage;
import com.v2soft.styxlib.l5.messages.StyxTReadMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unbuffered input styx output stream
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 *
 */
public class StyxUnbufferedInputStream extends InputStream {
    private long mTimeout = Connection.DEFAULT_TIMEOUT;
    private byte[] mSingleByteArray = new byte[1];
    private long mFID;
    private IMessageTransmitter mMessenger;
    private long mFileOffset = 0;
    private int mIOUnitSize;
    protected int mRecepient;

    public StyxUnbufferedInputStream(long file,
                                     IMessageTransmitter messenger,
                                     int iounit,
                                     int clientId) {
        if ( clientId < 0 ) {
            throw new NullPointerException("clientId is negative");
        }
        if ( messenger == null ) {
            throw new NullPointerException("messenger is null");
        }
        MetricsAndStats.byteArrayAllocationIo++;
        mRecepient = clientId;
        mIOUnitSize = iounit;
        mFID = file;
        mMessenger = messenger;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if ( len > mIOUnitSize ) {
            len = mIOUnitSize;
        }
        int read = 0;
        try {
            // send Tread
            final StyxTReadMessage tRead = new StyxTReadMessage(mFID, mFileOffset, len);
            final var rRead = mMessenger.<StyxRReadMessage>sendMessage(tRead, mRecepient, mTimeout)
                    .getResult();
            read = rRead.dataLength;
            if ( read > 0 ) {
                System.arraycopy(rRead.data, 0, b, 0, read);
                mFileOffset += read;
            } else {
                read = -1;
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
        return read;
    }
    @Override
    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    @Override
    public int read() throws IOException {
        read(mSingleByteArray);
        return mSingleByteArray[0];
    }

    @Override
    public void close() throws IOException {
        // send Tclunk
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
        try {
            mMessenger.sendMessage(tClunk, mRecepient, mTimeout).getResult();
        } catch (Exception e) {
            throw new IOException(e);
        }
        super.close();
    }

    public void seek(long position) {
        mFileOffset = position;
    }

    public int ioUnit() {
        return mIOUnitSize;
    }
}
