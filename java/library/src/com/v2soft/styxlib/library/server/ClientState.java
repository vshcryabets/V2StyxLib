package com.v2soft.styxlib.library.server;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Client state handler (this class exists one per client)
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class ClientState 
implements Closeable {
    protected String mUserName;
    protected String mProtocol;
    protected int mIOUnit;
    protected IVirtualStyxFile mServerRoot;
    protected IVirtualStyxFile mClientRoot;
    protected HashMap<Long, IVirtualStyxFile> mAssignedFiles;
    protected IChannelDriver mDriver;

    public ClientState(IChannelDriver driver) throws FileNotFoundException {
        if ( driver == null ) throw new NullPointerException("Driver is null");
        mAssignedFiles = new HashMap<Long, IVirtualStyxFile>();
        mUserName = "nobody";
        mDriver = driver;
    }

    protected IVirtualStyxFile getAssignedFile(long fid) throws StyxErrorMessageException {
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

    @Override
    public void close() throws IOException {
    }

    public void setIOUnit(int IOUnit) {
        this.mIOUnit = IOUnit;
    }

    public void setRoot(IVirtualStyxFile root) {
        this.mServerRoot = root;
    }

    public void setProtocol(String protocol) {
        this.mProtocol = protocol;
    }

    public void setUserName(String userName) {
        this.mUserName = userName;
    }

    public IVirtualStyxFile getServerRoot() {
        return mServerRoot;
    }

    public void setClientRoot(IVirtualStyxFile clientRoot) {
        this.mClientRoot = clientRoot;
    }

    public IVirtualStyxFile getClientRoot() {
        return mClientRoot;
    }

    public IChannelDriver getDriver() {
        return mDriver;
    }

}
