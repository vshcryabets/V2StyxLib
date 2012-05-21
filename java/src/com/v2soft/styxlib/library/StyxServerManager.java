package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.ServerSocketChannel;
import java.security.InvalidParameterException;
import java.util.LinkedList;

import com.v2soft.styxlib.library.core.Messenger.StyxMessengerListener;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.server.ClientBalancer;
import com.v2soft.styxlib.library.server.ConnectionAcceptor;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxDirectory;

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
    //---------------------------------------------------------------------------
    // Class fields
    //---------------------------------------------------------------------------
    private int mPort;
    private int mTimeout = DEFAULT_TIMEOUT;
    private boolean mSSL;
    private boolean mNeedAuth;
    private int mIOBufSize = 8192;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;
    private ActiveFids mActiveFids = new ActiveFids();
//    private ServerSocket mSocket;
    private ConnectionAcceptor mAcceptor;
    private ClientBalancer mBalancer;
    private Thread mAcceptorThread;

    public StyxServerManager(InetAddress address, int port, boolean ssl, IVirtualStyxDirectory root) throws IOException {
        mPort = port;
        ServerSocketChannel channel = null;
        if ( ssl ) {
            throw new RuntimeException("Not implemented");
        } else {
            channel = ServerSocketChannel.open();
        }

        // Bind the server socket to the local host and port
        InetSocketAddress isa = new InetSocketAddress(address, port);
        ServerSocket socket = channel.socket();
        socket.bind(isa);
        socket.setReuseAddress(true);
        socket.setSoTimeout(mTimeout);
        
        mBalancer = new ClientBalancer(mIOBufSize, root);
        mAcceptor = new ConnectionAcceptor(channel, mBalancer);
    }

    public void start() {
        mAcceptorThread = new Thread(mAcceptor, "Acceptor");
        mAcceptorThread.start();
    }

    public ActiveFids getActiveFids()
    {
        return mActiveFids;
    }

    public long getIOBufSize()
    {
        return mIOBufSize;
    }

    public int getPort()
    {
        return mPort;
    }

    public boolean isSSL()
    {
        return mSSL;
    }

    public boolean isNeedAuth()
    {
        return mNeedAuth;
    }

    /**
     * 
     * @return FID of root folder
     */
    public long getFID()
    {
        return mFID;
    }

    public StyxQID getQID()
    {
        return mQID;
    }

    public long getAuthFID()
    {
        return mAuthFID;
    }

    public StyxQID getAuthQID()
    {
        return mAuthQID;
    }


    @Override
    public void close() {
        if ( mAcceptorThread != null ) {
            mAcceptor.close();
            mAcceptorThread = null;
        }
    }

    public class ActiveFids
    {
        private LinkedList<Long> mAvailableFids = new LinkedList<Long>();
        private long mLastFid = 0L;

        /**
         * @return Return free FID
         */
        protected long getFreeFid() {
            synchronized (mAvailableFids) {
                if (!mAvailableFids.isEmpty())
                    return mAvailableFids.poll();
                mLastFid++;
                if(mLastFid > Consts.MAXUNINT)
                    mLastFid = 0;
                return mLastFid;
            }
        }

        protected boolean releaseFid(long fid) {
            synchronized (mAvailableFids) {
                if (fid == StyxMessage.NOFID)
                    return false;
                if ( mAvailableFids.contains(fid)) {
                    throw new InvalidParameterException(
                            String.format("Something goes wrong, this FID(%d) already has been released", fid));
                }
                return mAvailableFids.add(fid);
            }
        }
        protected void clean() {
            mAvailableFids.clear();
            mLastFid = 0;
        }
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
}
