package com.v2soft.styxlib.library;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class that manage drivers.
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

    public StyxServerManager(IVirtualStyxFile root, IChannelDriver[] drivers) {
        mRoot = root;
        // TODO inheritance not works in constructor
        ConnectionDetails details = new ConnectionDetails(getProtocol(), getIOUnit());
        mBalancer = new TMessagesProcessor(details, root);
        mDrivers = new LinkedList<>();
        if (drivers != null) {
            for (IChannelDriver driver : drivers) {
                addDriver(driver);
            }
        }
    }

    public StyxServerManager addDriver(IChannelDriver driver) {
        if (mDriverThreads != null) {
            // we already called start
            throw new IllegalStateException("Start() already called");
        }
        if (driver == null) {
            throw new NullPointerException("Driver is null");
        }
        mDrivers.add(driver);
        driver.setTMessageHandler(mBalancer);
        driver.setRMessageHandler(mBalancer);
        return this;
    }

    public Thread[] start() {
        final int count = mDrivers.size();
        final int ioUnit = getIOUnit();
        mDriverThreads = new Thread[count];
        for (int i = 0; i < count; i++) {
            mDriverThreads[i] = mDrivers.get(i).start(ioUnit);
        }
        return mDriverThreads;
    }

    /**
     * Get supported IO unit size.
     *
     * @return supported IO unit size.
     */
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
        for (Thread thread : mDriverThreads) {
            thread.join();
        }
    }

    /**
     * Get supported protocol name.
     *
     * @return supported protocol name.
     */
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
