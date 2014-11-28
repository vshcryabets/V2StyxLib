package com.v2soft.styxlib.server;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Client data (one instance for one client connection)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public abstract class ClientDetails {
    protected HashMap<Long, IVirtualStyxFile> mAssignedFiles;
    protected IChannelDriver mDriver;
    protected int mId;

    public ClientDetails(IChannelDriver driver, int id) {
        if ( driver == null ) throw new NullPointerException("Driver is null");
        mAssignedFiles = new HashMap<Long, IVirtualStyxFile>();
        mDriver = driver;
        mId = id;
    }

    public IVirtualStyxFile getAssignedFile(long fid) throws StyxErrorMessageException {
        if ( !mAssignedFiles.containsKey(fid) ) {
            StyxErrorMessageException.doException(
                    String.format("Unknown FID (%d)", fid));
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
}
