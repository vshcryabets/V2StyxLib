package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.io.IStyxDataReader;
import com.v2soft.styxlib.io.StyxByteBufferReadable;
import com.v2soft.styxlib.io.StyxDataReader;
import com.v2soft.styxlib.io.StyxDataWriter;
import com.v2soft.styxlib.types.Credentials;
import com.v2soft.styxlib.utils.MetricsAndStats;
import com.v2soft.styxlib.utils.Polls;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;

/**
 * Client data (one instance for one client connection)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public abstract class ClientDetails<T> {
    protected HashMap<Long, IVirtualStyxFile> mAssignedFiles;
    protected IChannelDriver mDriver;
    protected int mClientId;
    protected Polls mPolls; // TODO probably we can move polls here, and remove Polls class
    protected Credentials mCredentials;
    protected T mChannel;
    protected ByteBuffer mOutputBuffer;
    protected StyxDataWriter mOutputWriter;
    protected StyxByteBufferReadable mInputBuffer;
    protected StyxDataReader mInputReader;

    /**
     * Create new instance of {@link ClientDetails}
     * @param channel generic link channel object.
     * @param driver driver object.
     * @param iounit IO unit max size.
     * @param id client id.
     */
    public ClientDetails(T channel, IChannelDriver driver, int iounit, int id) {
        if ( driver == null ) {
            throw new NullPointerException("Driver is null");
        }
        if ( channel == null ) {
            throw new NullPointerException("Channel can't be null");
        }
        mChannel = channel;
        mAssignedFiles = new HashMap<>();
        mDriver = driver;
        mClientId = id;
        mOutputBuffer = ByteBuffer.allocate(iounit);
        mOutputWriter = new StyxDataWriter(mOutputBuffer);
        MetricsAndStats.byteBufferAllocation++;
        mInputBuffer = new StyxByteBufferReadable(iounit * 2);
        mInputReader = new StyxDataReader(mInputBuffer);
    }

    public void setCredentials(Credentials credential) {
        mCredentials = credential;
    }

    public Credentials getCredentials() {
        return mCredentials;
    }

    /**
     * Get polls assigned to this client.
     * @return polls assigned to this client.
     */
    public Polls getPolls() {
	// TODO move initialization to constructor
        if ( mPolls == null ) {
            synchronized (this) {
                if (mPolls == null) {
                    mPolls = new Polls();
                }
            }
        }
        return mPolls;
    }

    public IVirtualStyxFile getAssignedFile(long fid) throws StyxErrorMessageException {
        if ( !mAssignedFiles.containsKey(fid) ) {
            throw StyxErrorMessageException.newInstance(
                    String.format("Unknown FID (%d)", fid));
        }
        return mAssignedFiles.get(fid);
    }

    /**
     * Remove specified file from map.
     * @param fid File ID.
     */
    public void unregisterClosedFile(long fid) {
        mAssignedFiles.remove(fid);
    }

    public void registerOpenedFile(long fid, IVirtualStyxFile file) {
        mAssignedFiles.put(fid, file);
    }

    public IChannelDriver getDriver() {
        return mDriver;
    }

    public int getId() {
        return mClientId;
    }

    @Override
    public int hashCode() {
        return mDriver.hashCode()* mClientId;
    }

    @Override
    public String toString() {
        if ( mDriver != null ) {
            return String.format("%d:%s", mClientId, mDriver.toString());
        } else {
            return String.format("%d", mClientId);
        }
    }

    public StyxDataWriter getOutputWriter() {
        return mOutputWriter;
    }

    public T getChannel() {
        return mChannel;
    }

    public StyxByteBufferReadable getInputBuffer() {
        return mInputBuffer;
    }

    public IStyxDataReader getInputReader() {
        return mInputReader;
    }

    public ByteBuffer getOutputBuffer() {
        return mOutputBuffer;
    }

}
