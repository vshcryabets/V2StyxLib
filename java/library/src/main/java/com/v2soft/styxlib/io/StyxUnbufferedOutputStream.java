package com.v2soft.styxlib.io;

import com.v2soft.styxlib.Connection;
import com.v2soft.styxlib.messages.StyxRWriteMessage;
import com.v2soft.styxlib.messages.StyxTWriteMessage;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.utils.MetricsAndStats;
import com.v2soft.styxlib.utils.SyncObject;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Unbuffered output Styx output stream
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 *
 */
public class StyxUnbufferedOutputStream extends OutputStream {
    private SyncObject mSyncObject = new SyncObject(Connection.DEFAULT_TIMEOUT);
    protected byte[] mSingleByteArray;
    protected long mFID;
    protected IMessageTransmitter mMessenger;
    protected long mFileOffset = 0;
    protected ClientDetails mRecipient;

    public StyxUnbufferedOutputStream(long fid, IMessageTransmitter messenger, ClientDetails recepient) {
        if ( messenger == null ) {
            throw new NullPointerException("messenger is null");
        }
        if ( recepient == null ) {
            throw new NullPointerException("recipient is null");
        }
        mSingleByteArray = new byte[1];
        MetricsAndStats.byteArrayAllocation++;
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
            final StyxMessage rMessage = mMessenger.sendMessageAndWaitAnswer(tWrite, mRecipient, mSyncObject);
            final StyxRWriteMessage rWrite = (StyxRWriteMessage) rMessage;
            mFileOffset += rWrite.getCount();
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
        return mSyncObject.getTimeout();
    }

    public void setTimeout(long timeout) {
        mSyncObject.setTimeout(timeout);
    }

    @Override
    public void close() throws IOException {
        super.close();
        // send Tclunk
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
        try {
            mMessenger.sendMessageAndWaitAnswer(tClunk, mRecipient, mSyncObject);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
