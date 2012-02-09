package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientState 
implements Closeable {
	private ByteBuffer mBuffer;
	private int mIOUnit;
	private long mCount;
	private FileOutputStream mTestOut;

	public ClientState(int iounit) throws FileNotFoundException {
		mIOUnit = iounit;
		mBuffer = ByteBuffer.allocateDirect(iounit*2);
		mCount = 0;
		mTestOut = new FileOutputStream("test.out");
	}

	public void process() throws IOException {
		//		System.out.println(
		//				" Before: Remaining="+mBuffer.remaining()+
		//				" Capacity="+mBuffer.capacity()+
		//				" Limit="+mBuffer.limit()+
		//				" Position="+mBuffer.position()
		//				);
		if ( mBuffer.position() >= mIOUnit ) {
			byte [] out = new byte[mIOUnit];
			mBuffer.flip();
			//			System.out.println(
			//					" After: Remaining="+mBuffer.remaining()+
			//					" Capacity="+mBuffer.capacity()+
			//					" Limit="+mBuffer.limit()+
			//					" Position="+mBuffer.position()
			//					);
			mBuffer.get(out, 0, mIOUnit);
			mTestOut.write(out, 0, mIOUnit);
			mBuffer.flip();
			mCount += mIOUnit;
			System.out.print("\rReaded "+mCount);
		}
	}

	public ByteBuffer getBuffer() {return mBuffer;}

	@Override
	public void close() throws IOException {
		// write the rest data in buffer
		mBuffer.flip();
		int rest = mBuffer.remaining();
		if ( rest > 0 ) {
			byte [] out = new byte[rest];
			mBuffer.get(out, 0, rest);
			mTestOut.write(out, 0, rest);
			mTestOut.close();
			mCount += rest;
			System.out.print("\rReaded "+mCount);

		}
		mBuffer.clear();
		mBuffer = null;
		mCount = 0;
	}
}
