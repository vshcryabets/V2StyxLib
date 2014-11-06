package com.v2soft.styxlib.library;

import com.v2soft.styxlib.library.core.Messenger.StyxMessengerListener;
import com.v2soft.styxlib.library.server.ClientBalancer;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxServerManager 
implements Closeable, StyxMessengerListener {
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
    private int mTimeout = DEFAULT_TIMEOUT;
    private ClientBalancer mBalancer;

    public StyxServerManager(IVirtualStyxFile root) {
        mBalancer = new ClientBalancer(getIOUnit(), root, getProtocol());
        mDrivers = new LinkedList<IChannelDriver>();
    }

    public void addDriver(IChannelDriver driver) {
        if ( driver == null ) {
            throw new NullPointerException("Driver is null");
        }
        mDrivers.add(driver);
        driver.setMessageHandler(mBalancer);
    }

    public Thread[] start() {
        int count = mDrivers.size();
        Thread[] result = new Thread[count];
        for ( int i = 0 ; i < count; i++ ) {
            result[i] = mDrivers.get(i).start();
        }
        return result;
    }

    public int getIOUnit()
    {
        return DEFAULT_IOUNIT;
    }

    @Override
    public void close() throws IOException {
        int count = mDrivers.size();
        for ( int i = 0 ; i < count; i++ ) {
            mDrivers.get(i).close();
        }
    }

    public String getProtocol() {
        return PROTOCOL;
    }
    //-------------------------------------------------------------------------------------
    // Getters
    //-------------------------------------------------------------------------------------
    public int getTimeout() {return mTimeout;}
    //-------------------------------------------------------------------------------------
    // Setters
    //-------------------------------------------------------------------------------------
    public void setTimeout(int mTimeout) {
        this.mTimeout = mTimeout;
    }
    //-------------------------------------------------------------------------------------
    // Messenger listener
    //-------------------------------------------------------------------------------------
    @Override
    public void onSocketDisconnected() {
    }

    @Override
    public void onTrashReceived() {
    }

    @Override
    public void onFIDReleased(long fid) {
        // TODO Auto-generated method stub
        
    }

    public List<IChannelDriver> getDrivers() {
        return mDrivers;
    }
}
