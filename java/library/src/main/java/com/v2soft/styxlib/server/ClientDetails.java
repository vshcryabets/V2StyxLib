package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.types.Credentials;
import com.v2soft.styxlib.utils.Polls;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.util.HashMap;

/**
 * Client data (one instance for one client connection)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public abstract class ClientDetails {
    protected HashMap<Long, IVirtualStyxFile> mAssignedFiles;
    protected IChannelDriver mDriver;
    protected int mClientId;
    protected Polls mPolls; // TODO probably we can move polls here, and remove Polls class
    protected Credentials mCredentials;

    public ClientDetails(IChannelDriver driver, int id) {
        if ( driver == null ) throw new NullPointerException("Driver is null");
        mAssignedFiles = new HashMap<>();
        mDriver = driver;
        mClientId = id;
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
}
