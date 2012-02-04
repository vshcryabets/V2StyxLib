package com.v2soft.styxlib.library.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ClientState 
	implements Closeable {
	private ByteBuffer mBuffer;
	private int mPosition;
	private int mIOUnit;
	private long mCount;

	public ClientState(int iounit) {
		mIOUnit = iounit;
		mBuffer = ByteBuffer.allocateDirect(iounit*2);
		mCount = 0;
	}

	public void process() {
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
//			System.out.println(
//					" After: Remaining="+mBuffer.remaining()+
//					" Capacity="+mBuffer.capacity()+
//					" Limit="+mBuffer.limit()+
//					" Position="+mBuffer.position()
//					);
			mBuffer.flip();
			mCount += mIOUnit;
			System.out.print("\rReaded "+mCount);
			
//			System.out.println(
//					" After: Remaining="+mBuffer.remaining()+
//					" Capacity="+mBuffer.capacity()+
//					" Limit="+mBuffer.limit()+
//					" Position="+mBuffer.position()
//					);
//			try {
//				String outString = new String(out, "utf-8");
//				System.out.print(outString);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}

	public ByteBuffer getBuffer() {return mBuffer;}

	@Override
	public void close() throws IOException {
		mBuffer.clear();
		mBuffer = null;
		mCount = 0;
	}
}
