import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;


public class BufferTest {
	private static final int BUFFER_SIZE = 1024*1024;
	private static final int MAX_BLOCK_SIZE = 8192;
	private static final int[] COUNTS = new int[]{1024,2048,4096,8192};
	private static final String OUTPUT = "output";
	private static final String INPUT = "input";
	
	private int mCountPos = 0;
	private ByteBuffer mInputBuffer, mOutputBuffer, mTransBuffer;
	private byte [] mTrans2;
	private FileInputStream mInput;
	private FileOutputStream mOutput;
	
	public BufferTest() throws IOException {
		mInputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		mOutputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
		mTransBuffer = ByteBuffer.allocate(MAX_BLOCK_SIZE);
		mTrans2 = new byte[MAX_BLOCK_SIZE];
		fillInputBuffer();
		mInput = new FileInputStream(INPUT);
		mOutput = new FileOutputStream(OUTPUT);
		copyBuffer1(mInput.getChannel(), mOutput.getChannel());
	}

	private void copyBuffer1(FileChannel input, FileChannel output) throws IOException {
		mTransBuffer.clear();
		int readed = 0;
		
		while ( (readed = read(input, mTransBuffer)) > 0 ) {
			mOutputBuffer.put(mTransBuffer);
			mTransBuffer.clear();
		}
	}

	private int read(FileChannel input, ByteBuffer buffer) throws IOException {
		int count = COUNTS[mCountPos++];
//		input.read(buffer);
		mInputBuffer.get(mTrans2, 0, count);
		buffer.put(mTrans2, 0, count);
		return count;
	}

	private void fillInputBuffer() throws IOException {
		byte [] temp = new byte[128];
		for ( int i = 0 ; i < temp.length; i++ ) {
			temp[i] = (byte) i;
		}
		mOutput = new FileOutputStream(INPUT);
		for ( int i = 0; i < BUFFER_SIZE/128; i++) {
			mOutput.write(temp);
		}
		mOutput.close();
	}
}
