package com.v2soft.styxlib.library;

import com.v2soft.styxlib.server.ClientBalancer;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxServerManager
        implements Closeable {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int DEFAULT_IOUNIT = 8192;
    //---------------------------------------------------------------------------
    // Class fields
    //---------------------------------------------------------------------------
    protected List<IChannelDriver> mDrivers;
    protected int mTimeout = DEFAULT_TIMEOUT;
    protected ClientBalancer mBalancer;
    protected IVirtualStyxFile mRoot;

    public StyxServerManager(IVirtualStyxFile root) {
        mRoot = root;
        ConnectionDetails details = new ConnectionDetails(getProtocol(), getIOUnit());
        mBalancer = new ClientBalancer(details, root);
        mDrivers = new LinkedList<IChannelDriver>();
    }

    public StyxServerManager(IVirtualStyxFile root, IChannelDriver [] drivers) {
        this(root);
        for (IChannelDriver driver : drivers) {
            addDriver( driver );
        }
    }

    public void addDriver(IChannelDriver driver) {
        if (driver == null) {
            throw new NullPointerException("Driver is null");
        }
        mDrivers.add(driver);
        driver.setTMessageHandler(mBalancer);
        driver.setRMessageHandler(mBalancer);
    }

    public Thread[] start() {
        int count = mDrivers.size();
        Thread[] result = new Thread[count];
        for (int i = 0; i < count; i++) {
            result[i] = mDrivers.get(i).start(getIOUnit());
        }
        return result;
    }

    public int getIOUnit() {
        return DEFAULT_IOUNIT;
    }

    @Override
    public void close() throws IOException {
        for (IChannelDriver driver : mDrivers) {
            driver.close();
        }
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    //-------------------------------------------------------------------------------------
    // Getters
    //-------------------------------------------------------------------------------------
    public IVirtualStyxFile getRoot() {
        return mRoot;
    }

    //-------------------------------------------------------------------------------------
    // Setters
    //-------------------------------------------------------------------------------------
    public List<IChannelDriver> getDrivers() {
        return mDrivers;
    }
}
