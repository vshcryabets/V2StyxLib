package com.v2soft.styxlib.library.io;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.mina.core.session.IoSession;

import com.v2soft.styxlib.library.StyxClientConnection;
import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.StyxRWriteMessage;
import com.v2soft.styxlib.library.messages.StyxTWriteMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.types.ULong;

/**
 * Unbuffered output styx output stream
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxUnbufferedOutputStream extends OutputStream {
    private long mTimeout = StyxClientConnection.DEFAULT_TIMEOUT;
    private byte[] mSingleByteArray = new byte[1];
    private StyxFile mFile;
    private IoSession mMessenger;
    private ULong mFileOffset = ULong.ZERO;

    StyxUnbufferedOutputStream(StyxFile file, IoSession messnger) {
        if ( file == null ) throw new NullPointerException("File is null");
        if ( messnger == null ) throw new NullPointerException("messnger is null");

        mFile = file;
        mMessenger = messnger;
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
    @Override
    public void write(byte[] data, int dataOffset, int dataLength) throws IOException {
        try {
            StyxTWriteMessage tWrite = new StyxTWriteMessage(mFile.getFID(), mFileOffset, data, dataOffset, dataLength);
            mMessenger.write(tWrite);
            StyxMessage rMessage = tWrite.waitForAnswer(mTimeout);
            StyxErrorMessageException.doException(rMessage);

            StyxRWriteMessage rWrite = (StyxRWriteMessage) rMessage;
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
}
