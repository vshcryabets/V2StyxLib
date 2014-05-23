package com.v2soft.styxlib.library;

import com.v2soft.styxlib.library.core.Messenger.StyxMessengerListener;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.ClientBalancer;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.Closeable;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;

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
    protected IChannelDriver mDriver;
    private int mTimeout = DEFAULT_TIMEOUT;
    private ClientBalancer mBalancer;

    public StyxServerManager(IVirtualStyxFile root) {
        mBalancer = new ClientBalancer(getIOUnit(), root, getProtocol());
    }

    public void setDriver(IChannelDriver driver) {
        if ( driver == null ) {
            throw new NullPointerException("Driver is null");
        }
        mDriver = driver;
        mDriver.setMessageHandler(mBalancer);
    }

    public Thread start() {
        return mDriver.start();
    }

    public int getIOUnit()
    {
        return DEFAULT_IOUNIT;
    }

    @Override
    public void close() throws IOException {
        mDriver.close();
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
    public void onSocketDisconected() {
    }

    @Override
    public void onTrashReceived() {
    }

    @Override
    public void onFIDReleased(long fid) {
        // TODO Auto-generated method stub
        
    }
}
