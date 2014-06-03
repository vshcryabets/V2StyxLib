package com.v2soft.styxlib.library.io;

import java.io.IOException;
import java.io.InputStream;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.StyxRReadMessage;
import com.v2soft.styxlib.library.messages.StyxTReadMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.server.IMessageTransmitter;
import com.v2soft.styxlib.library.types.ULong;

/**
 * Unbuffered input styx output stream
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxUnbufferedInputStream extends InputStream {
    private long mTimeout = StyxClientConnection.DEFAULT_TIMEOUT;
    private byte[] mSingleByteArray = new byte[1];
    private long mFile;
    private IMessageTransmitter mMessenger;
    private ULong mFileOffset = ULong.ZERO;
    private int mIOUnitSize;

    public StyxUnbufferedInputStream(long file, IMessageTransmitter messenger, int iounit) {
        if ( messenger == null ) {
            throw new NullPointerException("messenger is null");
        }
        mIOUnitSize = iounit;
        mFile = file;
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
            final StyxTReadMessage tRead = new StyxTReadMessage(mFile, mFileOffset, len);
            mMessenger.sendMessage(tRead);
            final StyxMessage rMessage = tRead.waitForAnswer(mTimeout);
            StyxErrorMessageException.doException(rMessage);

            final StyxRReadMessage rRead = (StyxRReadMessage) rMessage;
            read = rRead.getDataLength();
            if ( read > 0 ) {
                System.arraycopy(rRead.getDataBuffer(), 0, b, 0, read);
                mFileOffset = mFileOffset.add(read);
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
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFile);
        mMessenger.sendMessage(tClunk);
        StyxMessage rMessage;
        try {
            rMessage = tClunk.waitForAnswer(mTimeout);
            StyxErrorMessageException.doException(rMessage);
        } catch (Exception e) {
            throw new IOException(e);
        }
        super.close();
    }

    public void seek(long position) {
        mFileOffset.setValue(position);
    }
}
