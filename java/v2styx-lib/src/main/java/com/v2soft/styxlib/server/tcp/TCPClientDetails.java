package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.l5.io.Buffer;
import com.v2soft.styxlib.l5.io.BufferLoader;
import com.v2soft.styxlib.l5.io.InChannel;
import com.v2soft.styxlib.l5.io.impl.BufferImpl;
import com.v2soft.styxlib.l5.serialization.IBufferReader;
import com.v2soft.styxlib.l5.serialization.IBufferWritter;
import com.v2soft.styxlib.l5.serialization.impl.BufferReaderImpl;
import com.v2soft.styxlib.l5.serialization.impl.BufferWritterImpl;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.utils.MetricsAndStats;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientDetails extends ClientDetails {
    private SocketChannel mTcpChannel;
    private final ByteBuffer mOutputBuffer;
    private final IBufferWritter mOutputWriter;
    protected Buffer mBuffer;
    protected BufferLoader mBufferLoader;
    protected BufferReaderImpl mReader;
    protected InChannel mChannel;

    public TCPClientDetails(SocketChannel channel, IChannelDriver driver, int iounit, int id) {
        super(driver, id);
        if ( channel == null ) throw new NullPointerException("Client channel can't be null");
        mTcpChannel = channel;
        mChannel = dst -> mTcpChannel.read(dst);

        mOutputBuffer = ByteBuffer.allocate(iounit);
        MetricsAndStats.byteBufferAllocation++;
        BufferImpl impl = new BufferImpl(iounit * 2);
        mBuffer = impl;
        mBufferLoader = impl;
        mReader = new BufferReaderImpl(mBuffer);
        mOutputWriter = new BufferWritterImpl(mOutputBuffer);
    }

    public IBufferWritter getOutputWriter() {
        return mOutputWriter;
    }

    public InChannel getChannel() {
        return mChannel;
    }

    public BufferLoader getBufferLoader() {
        return mBufferLoader;
    }

    public IBufferReader getInputReader() {
        return mReader;
    }

    @Override
    public String toString() {
        try {
            return String.format("%s:%d", mTcpChannel.getRemoteAddress().toString(), mId);
        } catch (IOException e) {
            return super.toString();
        }
    }

    public void disconnect() throws IOException {
        mTcpChannel.close();
        mTcpChannel = null;
    }

    public SocketChannel getTcpChannel() {
        return mTcpChannel;
    }

    public Buffer getBuffer() {
        return mBuffer;
    }

    public void sendOutputBuffer() throws IOException {
        mOutputBuffer.flip();
        mTcpChannel.write(mOutputBuffer);
    }
}
