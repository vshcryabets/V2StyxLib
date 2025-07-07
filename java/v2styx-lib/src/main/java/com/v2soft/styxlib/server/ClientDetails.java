package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.utils.Polls;

import java.util.HashMap;

/**
 * Client data (one instance for one client connection)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientDetails {
    public final int NOT_ASSIGNED = -1;
    protected final HashMap<Long, IVirtualStyxFile> mAssignedFiles;
    protected final IChannelDriver<?> mDriver; // TODO move to map
    protected int mId = NOT_ASSIGNED;
    protected Polls mPolls;
    protected String mUsername;
    private boolean isAuthenticated = false;

    public ClientDetails(IChannelDriver<?> driver) {
        if ( driver == null ) throw new NullPointerException("Driver is null");
        mAssignedFiles = new HashMap<>();
        mDriver = driver;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUserName() {
        return mUsername;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated() {
        isAuthenticated = true;
    }

    /**
     * Get polls assigned to this client.
     * @return polls assigned to this client.
     */
    public Polls getPolls() {
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
            throw StyxErrorMessageException.newInstance(String.format("Unknown FID (%d)", fid));
        }
        return mAssignedFiles.get(fid);
    }

    public void closeFile(long fid) {
        mAssignedFiles.remove(fid);
    }

    public void registerOpenedFile(long fid, IVirtualStyxFile file) {
        mAssignedFiles.put(fid, file);
    }

    public IChannelDriver getDriver() {
        return mDriver;
    }

    public int getId() {
        return mId;
    }

    @Override
    public int hashCode() {
        return mDriver.hashCode()*mId;
    }

    @Override
    public String toString() {
        if ( mDriver != null ) {
            return String.format("%d:%s", mId, mDriver.toString());
        } else {
            return String.format("%d", mId);
        }
    }

    public void setId(int mId) {
        this.mId = mId;
    }

}
