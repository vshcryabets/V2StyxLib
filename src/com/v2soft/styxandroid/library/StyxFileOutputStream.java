package com.v2soft.styxandroid.library;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxandroid.library.core.Messenger;
import com.v2soft.styxandroid.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxandroid.library.exceptions.StyxException;
import com.v2soft.styxandroid.library.messages.StyxRWriteMessage;
import com.v2soft.styxandroid.library.messages.StyxTWriteMessage;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.types.ULong;

public class StyxFileOutputStream extends OutputStream {
    private long mTimeout = StyxClientManager.DEFAULT_TIMEOUT;
	private StyxFile mFile;
	private byte[] buffer;
	private int index;
	private ULong offset = ULong.ZERO;
	private int mIOUnit;
	
	public StyxFileOutputStream(StyxFile file, int iounit)
	{
		mFile = file;
		mIOUnit = iounit;
	}
	
	public StyxFile getFile()
	{
		return mFile;
	}
	
	public int getIOUnit()
	{
		return mIOUnit;
	}
	
	private void writeBuffer() throws IOException, InterruptedException, StyxException, TimeoutException
	{
		StyxFile file = getFile();
		ByteArrayInputStream is = new ByteArrayInputStream(buffer, 0, index);
		
		StyxClientManager manager = file.getManager();
		StyxTWriteMessage tWrite = new StyxTWriteMessage(manager.getActiveTags().getTag(),
				file.getFID(), offset, is);
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tWrite);
		StyxMessage rMessage = tWrite.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
		
		StyxRWriteMessage rWrite = (StyxRWriteMessage) rMessage;
		offset = offset.add(rWrite.getCount());
	}
	
	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		try
		{
			int iounit = getIOUnit();
			for (int i=0; i<len; i++)
			{
				if (buffer != null && index >= buffer.length)
				{
					writeBuffer();
					buffer = null;
				}
				
				if (buffer == null)
				{
					buffer = new byte[iounit];
					index = 0;
				}
				
				buffer[index] = b[i + off];
				index++;
			}
		} catch (IOException e)
		{
			throw e;
		} catch (Exception e)
		{
			throw new IOException(String.format("%s: %s.", e.getClass().getName(), e.getMessage()));
		}
	}

	@Override
	public void write(int b) throws IOException {
		try
		{
			if (buffer != null && index >= buffer.length)
			{
				writeBuffer();
				buffer = null;
			}
		
			if (buffer == null)
			{
				buffer = new byte[getIOUnit()];
				index = 0;
			}
			
			buffer[index] = (byte) b;
			index++;
		} catch (IOException e)
		{
			throw e;
		} catch (Exception e)
		{
			throw new IOException(String.format("%s: %s.", e.getClass().getName(), e.getMessage()));
		}
	}
	
	@Override
	public void flush() throws IOException {
		try
		{
			writeBuffer();
			super.flush();
		} catch (IOException e)
		{
			throw e;
		} catch (Exception e)
		{
			throw new IOException(String.format("%s: %s.", e.getClass().getName(), e.getMessage()));
		}
	}

    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }
}
