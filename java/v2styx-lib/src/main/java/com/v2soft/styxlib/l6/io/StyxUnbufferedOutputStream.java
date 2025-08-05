package com.v2soft.styxlib.l6.io;

import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.dev.MetricsAndStats;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxRWriteMessage;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.utils.GetMessagesFactoryUseCase;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Unbuffered output Styx output stream
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 *
 */
public class StyxUnbufferedOutputStream extends OutputStream {
    protected long mTimeout = Connection.DEFAULT_TIMEOUT;
    protected byte[] mSingleByteArray;
    protected long mFID;
    protected IMessageTransmitter mMessenger;
    protected long mFileOffset = 0;
    protected int mRecipient;
    private int mIOUnitSize;
    private GetMessagesFactoryUseCase getMessagesFactoryUseCase;

    public StyxUnbufferedOutputStream(long fid,
                                      IMessageTransmitter messenger,
                                      int recipient,
                                      int ioUnit,
                                      GetMessagesFactoryUseCase getMessagesFactoryUseCase) {
        if ( messenger == null ) {
            throw new NullPointerException("messenger is null");
        }
        if ( recipient < 0 ) {
            throw new NullPointerException("recipient negative");
        }
        this.getMessagesFactoryUseCase = getMessagesFactoryUseCase;
        mSingleByteArray = new byte[1];
        MetricsAndStats.byteArrayAllocation++;
        mRecipient = recipient;
        mFID = fid;
        mIOUnitSize = ioUnit;
        mMessenger = messenger;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] data, int dataOffset, int dataLength) throws IOException {
        try {
            final StyxMessage tWrite = getMessagesFactoryUseCase.get()
                    .constructTWriteMessage(mFID, mFileOffset, data, dataOffset, dataLength);
            final var rWrite = mMessenger.<StyxRWriteMessage>sendMessage(tWrite, mRecipient)
                    .getResult(mTimeout);
            mFileOffset += rWrite.count;
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
        try {
            mMessenger.sendMessage(new StyxTMessageFID(MessageType.Tclunk, mFID),
                    mRecipient).getResult(mTimeout);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public int ioUnit() {
        return mIOUnitSize;
    }
}
