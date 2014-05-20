package com.v2soft.styxlib.library.server.tcp;

import com.v2soft.styxlib.library.io.IStyxBuffer;
import com.v2soft.styxlib.library.io.IStyxDataReader;
import com.v2soft.styxlib.library.io.StyxByteBufferReadable;
import com.v2soft.styxlib.library.io.StyxDataReader;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientState extends ClientState {
    private SocketChannel mChannel;
    private ByteBuffer mOutputBuffer;
    protected StyxByteBufferReadable mBuffer;
    protected StyxDataReader mReader;

    public TCPClientState(SocketChannel channel, IChannelDriver driver, int iounit) throws FileNotFoundException {
        super(driver);
        if ( channel == null ) throw new NullPointerException("Client channel is null");
        mChannel = channel;
        setIOUnit(iounit);
    }

    @Override
    public void setIOUnit(int IOUnit) {
        super.setIOUnit(IOUnit);
        mOutputBuffer = ByteBuffer.allocate(IOUnit);
        mBuffer = new StyxByteBufferReadable(IOUnit*2);
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
