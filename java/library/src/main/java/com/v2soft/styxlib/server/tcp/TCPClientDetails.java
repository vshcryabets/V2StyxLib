package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.StyxByteBufferReadable;
import com.v2soft.styxlib.io.StyxDataReader;
import com.v2soft.styxlib.io.StyxDataWriter;
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
    private SocketChannel mChannel;
    private ByteBuffer mOutputBuffer;
    private StyxDataWriter mOutputWriter;
    private StyxByteBufferReadable mInputBuffer;
    private StyxDataReader mInputReader;

    public TCPClientDetails(SocketChannel channel, IChannelDriver driver, int iounit, int id) {
        super(driver, id);
        if ( channel == null ) {
            throw new NullPointerException("Client channel can't be null");
        }
        mChannel = channel;
        mOutputBuffer = ByteBuffer.allocate(iounit);
        mOutputWriter = new StyxDataWriter(mOutputBuffer);
        MetricsAndStats.byteBufferAllocation++;
        mInputBuffer = new StyxByteBufferReadable(iounit * 2);
        mInputReader = new StyxDataReader(mInputBuffer);
    }

    public StyxDataWriter getOutputWriter() {
        return mOutputWriter;
    }

    public SocketChannel getChannel() {
        return mChannel;
    }

    public StyxByteBufferReadable getInputBuffer() {
        return mInputBuffer;
    }

    public IStyxDataReader getInputReader() {
        return mInputReader;
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

    public ByteBuffer getOutputBuffer() {
        return mOutputBuffer;
    }
}
