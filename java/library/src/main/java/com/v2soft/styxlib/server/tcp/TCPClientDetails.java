package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

import java.io.FileNotFoundException;
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
}