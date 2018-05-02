package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.StyxByteBufferReadable;
import com.v2soft.styxlib.io.StyxDataReader;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientDetails extends ClientDetails {
    private SocketChannel mChannel;
    private ByteBuffer mOutputBuffer;
    protected StyxByteBufferReadable mBuffer;
    protected StyxDataReader mReader;

    public TCPClientDetails(SocketChannel channel, IChannelDriver driver, int iounit, int id) {
        super(driver, id);
        if ( channel == null ) throw new NullPointerException("Client channel can't be null");
        mChannel = channel;
        mOutputBuffer = ByteBuffer.allocate(iounit);
        mBuffer = new StyxByteBufferReadable(iounit * 2);
        mReader = new StyxDataReader(mBuffer);
    }

    public ByteBuffer getOutputBuffer() {
        return mOutputBuffer;
    }

    public SocketChannel getChannel() {
        return mChannel;
    }

    public StyxByteBufferReadable getInputBuffer() {
        return mBuffer;
    }

    public IStyxDataReader getInputReader() {
        return mReader;
    }

    @Override
    public String toString() {
        try {
            return String.format("%s:%d", mChannel.getRemoteAddress().toString(), mClientId);
        } catch (IOException e) {
            return super.toString();
        }
    }

    public void disconnect() throws IOException {
        // TODO something wrong, close should in same place where we have opened it.
        mChannel.close();
        mChannel = null;
    }
}
