package com.v2soft.styxlib.library;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.core.Messenger;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.StyxRReadMessage;
import com.v2soft.styxlib.library.messages.StyxTReadMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.types.ULong;

public class StyxFileInputStream extends InputStream
{
	private ULong mFilePosition;
	private long mTimeout = StyxClientManager.DEFAULT_TIMEOUT;
	private StyxFile mFile;
	private int mIOUnit;
	private byte [] mBuffer;
	private int mBufPos, mBufEnd;
	public boolean mEOF = false;
	private StyxClientManager mManager;
	
	public StyxFileInputStream(StyxClientManager manager, StyxFile file, int iounit)
	{
		mManager = manager;
		mTimeout = manager.getTimeout();
		mFile = file;
		mIOUnit = iounit;
		mBuffer = new byte[mIOUnit*2];
		mBufEnd = 0;
		mBufPos = 0;
		mFilePosition = ULong.ZERO;
	}
	
	@Override
	public int available() throws IOException {
	    if ( mBufEnd < mBufPos ) throw new IOException("Something wrong (mBufEnd < mBufPos "+mBufEnd+":"+mBufPos+")");
	    return mBufEnd-mBufPos;
	}
	
	@Override
	public boolean markSupported() {
	    return false;
	}
	
	@Override
	public long skip(long arg0) throws IOException {
	    if ( arg0 > available() ) {
	        arg0 -= available();
	        mFilePosition = mFilePosition.add(arg0);
	        mBufEnd = 0;
	        mBufPos = 0;
	    } else {
	        mBufPos += arg0;
	    }
	    return arg0;
	}
	
	@Override
	public int read() throws IOException {
	    if ( mEOF ) return -1;
	    try {
    	    if ( available() < 1 ) {
    	        System.out.println("Load next part");
    	        // upload next part
    	        loadNextPart();
    	    }
    	    if ( available() < 1 )
    	        throw new EOFException();
    	    byte res = mBuffer[mBufPos];
    	    mBufPos++;
    	    return res;
	    } catch (Exception e) {
	        throw new IOException(e.toString());
        }
	}

    @Override
	public int read(byte[] outBuffer, int offset, int toRead) throws IOException {
//        if ( mEOF ) throw new EOFException();
        if ( mEOF ) return -1;
        int readed = 0;
        try {
            int available = available();
            while ( toRead > available ) {
                System.arraycopy(mBuffer, mBufPos, outBuffer, offset, available);
                offset+=available;
                toRead-=available;
                readed += available;
                mBufPos = 0;
                mBufEnd = 0;
                loadNextPart();
                available = available();
                if ( available == 0 ) {
                    return readed;
                }
            }
            System.arraycopy(mBuffer, mBufPos, outBuffer, offset, toRead);
            mBufPos+=toRead;
            readed+=toRead;
            return readed;
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
	}
	
    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }

    private void loadNextPart() 
            throws InterruptedException, StyxException, TimeoutException, IOException {
    	// TODO may we should move this code to StyxFile?
        // shift buffer
        int length = available();
        if ( length > 0 ) {
            System.arraycopy(mBuffer, mBufPos, mBuffer, 0, length);
        }
        mBufPos = 0;
        mBufEnd = length;
        
        // send Tread
        StyxTReadMessage tRead = new StyxTReadMessage(mManager
                .getActiveTags().getTag(), mFile.getFID(), mFilePosition, mIOUnit);
        
        Messenger messenger = mManager.getMessenger();
        messenger.send(tRead);
        StyxMessage rMessage = tRead.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        
        StyxRReadMessage rRead = (StyxRReadMessage) rMessage;
        int readed = rRead.getDataLength();
        if ( readed > 0 ) {
            InputStream inp = rRead.getDataStream();
            byte [] tempbuf = new byte[readed];
            inp.read(tempbuf, 0, readed);
            System.arraycopy(tempbuf, 0, mBuffer, mBufEnd, readed);
            mBufEnd+=readed;
            mFilePosition = mFilePosition.add(readed);
        } else {
            mEOF = true;
        }
    }
    
    @Override
    public void close() throws IOException {
    	mFile.close();
    	super.close();
    }
}
