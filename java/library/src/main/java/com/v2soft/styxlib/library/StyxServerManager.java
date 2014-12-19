package com.v2soft.styxlib.library;

import com.v2soft.styxlib.handlers.TMessagesProcessor;
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
    protected TMessagesProcessor mBalancer;
    protected IVirtualStyxFile mRoot;
    protected Thread[] mDriverThreads;

    public StyxServerManager(IVirtualStyxFile root) {
        mRoot = root;
        ConnectionDetails details = new ConnectionDetails(getProtocol(), getIOUnit());
        mBalancer = new TMessagesProcessor(details, root);
        mDrivers = new LinkedList<IChannelDriver>();
    }

    public StyxServerManager(IVirtualStyxFile root, IChannelDriver [] drivers) {
        this(root);
        for (IChannelDriver driver : drivers) {
            addDriver( driver );
        }
    }

    public StyxServerManager addDriver(IChannelDriver driver) {
        if (driver == null) {
            throw new NullPointerException("Driver is null");
        }
        mDrivers.add(driver);
        driver.setTMessageHandler(mBalancer);
        driver.setRMessageHandler(mBalancer);
        return this;
    }

    public Thread[] start() {
        int count = mDrivers.size();
        mDriverThreads = new Thread[count];
        for (int i = 0; i < count; i++) {
            mDriverThreads[i] = mDrivers.get(i).start(getIOUnit());
        }
        return mDriverThreads;
    }

    public int getIOUnit() {
        return DEFAULT_IOUNIT;
    }

    @Override
    public void close() throws IOException {
        mBalancer.close();
        for (IChannelDriver driver : mDrivers) {
            driver.close();
        }
    }

    public void closeAndWait() throws IOException, InterruptedException {
        close();
        for ( Thread thread : mDriverThreads ) {
            thread.join();
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
