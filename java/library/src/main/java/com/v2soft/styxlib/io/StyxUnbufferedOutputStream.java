package com.v2soft.styxlib.io;

import com.v2soft.styxlib.Connection;
import com.v2soft.styxlib.messages.StyxRWriteMessage;
import com.v2soft.styxlib.messages.StyxTWriteMessage;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.library.types.ULong;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Unbuffered output Styx output stream
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 *
 */
public class StyxUnbufferedOutputStream extends OutputStream {
    protected long mTimeout = Connection.DEFAULT_TIMEOUT;
    protected byte[] mSingleByteArray = new byte[1];
    protected long mFID;
    protected IMessageTransmitter mMessenger;
    protected ULong mFileOffset = ULong.ZERO;
    protected ClientDetails mRecipient;

    public StyxUnbufferedOutputStream(long fid, IMessageTransmitter messenger, ClientDetails recepient) {
        if ( messenger == null ) {
            throw new NullPointerException("messenger is null");
        }
        if ( recepient == null ) {
            throw new NullPointerException("recipient is null");
        }
        mRecipient = recepient;
        mFID = fid;
        mMessenger = messenger;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] data, int dataOffset, int dataLength) throws IOException {
        try {
            final StyxTWriteMessage tWrite =
                    new StyxTWriteMessage(mFID, mFileOffset, data, dataOffset, dataLength);
            mMessenger.sendMessage(tWrite, mRecipient);
            final StyxMessage rMessage = tWrite.waitForAnswer(mTimeout);
            final StyxRWriteMessage rWrite = (StyxRWriteMessage) rMessage;
            mFileOffset = mFileOffset.add(rWrite.getCount());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    @Override
    public void write(int b) throws IOException {
        mSingleByteArray[0] = (byte) b;
        write(mSingleByteArray);
    }
    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }

    @Override
    public void close() throws IOException {
        super.close();
        // send Tclunk
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
        mMessenger.sendMessage(tClunk, mRecipient);
        try {
            tClunk.waitForAnswer(mTimeout);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
