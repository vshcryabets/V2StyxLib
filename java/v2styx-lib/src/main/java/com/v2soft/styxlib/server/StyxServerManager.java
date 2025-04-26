package com.v2soft.styxlib.server;

import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;

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
    protected final List<IChannelDriver> mDrivers;
    protected final TMessagesProcessor mBalancer;
    protected final IVirtualStyxFile mRoot;
    protected Thread[] mDriverThreads;
    protected ClientsRepo mClientsRepo;

    public StyxServerManager(IVirtualStyxFile root,
                             List<IChannelDriver> drivers,
                             ClientsRepo clientsRepo) {
        mRoot = root;
        var details = new ConnectionDetails(getProtocol(), getIOUnit());
        mClientsRepo = clientsRepo;
        mBalancer = new TMessagesProcessor(details, root, mClientsRepo);
        mDrivers = drivers;
        for (var driver : mDrivers) {
            driver.setTMessageHandler(mBalancer);
            driver.setRMessageHandler(mBalancer);
        }
    }

    public void start() {
        int count = mDrivers.size();
        mDriverThreads = new Thread[count];
        for (int i = 0; i < count; i++) {
            mDriverThreads[i] = mDrivers.get(i).start(getIOUnit());
        }
    }

    public int getIOUnit() {
        return DEFAULT_IOUNIT;
    }

    @Override
    public void close() throws IOException {
        for (var driver : mDrivers) {
            driver.close();
        }
        mBalancer.close();
    }

    public void closeAndWait() throws IOException, InterruptedException {
        close();
        joinThreads();
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

    public void joinThreads() throws InterruptedException {
        for (var thread : mDriverThreads) {
            thread.join();
        }
    }
}
