package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;

import com.v2soft.styxlib.library.core.Messenger;
import com.v2soft.styxlib.library.core.Messenger.ActiveTags;
import com.v2soft.styxlib.library.core.Messenger.StyxMessengerListener;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.exceptions.StyxWrongMessageException;
import com.v2soft.styxlib.library.io.StyxOutputStream;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
import com.v2soft.styxlib.library.messages.StyxTClunkMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;

public class StyxClientManager 
    implements Closeable, StyxMessengerListener {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 5000;
    //---------------------------------------------------------------------------
    // Class fields
    //---------------------------------------------------------------------------
    private StyxFile mRoot;
    private InetAddress mAddress;
    private String mUserName;
    private String mPassword;
    private String mMountPoint;
    private int mPort;
    private int mTimeout = DEFAULT_TIMEOUT;
    private boolean mSSL;
    private boolean mNeedAuth;
    private boolean isConnected;
    private Messenger mMessenger;
    private int mIOBufSize = 8192;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;
    private ActiveFids mActiveFids = new ActiveFids();

    public StyxClientManager() {
        this(null, 0, false, null, null);
    }

    public StyxClientManager(InetAddress address, int port, boolean ssl)
    {
        this(address, port, ssl, null, null);
        setConnected(false);
    }

    public StyxClientManager(InetAddress address, int port, boolean ssl, String username, String password) {
        mAddress = address;
        mPort = port;
        mSSL = ssl;
        mUserName = username;
        mPassword = password;
    }

    public boolean connect() 
            throws IOException, StyxException, InterruptedException, TimeoutException {
        return connect(getAddress(), getPort(), isSSL(), getUserName(), getPassword());
    }

    public boolean connect(InetAddress address, int port, boolean ssl)
            throws IOException, StyxException, InterruptedException, TimeoutException {
        return connect(address, port, ssl, null, null);
    }

    /**
     * Connect to server with specified parameters
     * @param address server name
     * @param port server port
     * @param ssl use SSL
     * @param username user name
     * @param password password 
     * @return true if connected
     * @throws IOException
     * @throws StyxException
     * @throws TimeoutException 
     */
    public boolean connect(InetAddress address, int port, boolean ssl, String username, String password)
            throws IOException, StyxException, InterruptedException, TimeoutException {
        mAddress = address;
        mPort = port;
        mSSL = ssl;
        mUserName = username;
        mPassword = password;
        mMountPoint = "/";
        mNeedAuth = (username != null);

        SocketFactory socketFactory = null;
        if (ssl)
            socketFactory = SSLSocketFactory.getDefault();
        else socketFactory = SocketFactory.getDefault();
        
        Socket socket = socketFactory.createSocket();
        SocketAddress sa= new InetSocketAddress(address, port);
        socket.connect(sa, mTimeout);
        socket.setSoTimeout(mTimeout);
        mMessenger = new Messenger(socket, mIOBufSize, this);

        sendVersionMessage();
        setConnected(socket.isConnected());
        return socket.isConnected();
    }

    public StyxFile getRoot() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        if (mRoot == null)
            mRoot = new StyxFile(this);
        return mRoot;
    }

    public Messenger getMessenger()
    {
        return mMessenger;
    }		

    public ActiveFids getActiveFids()
    {
        return mActiveFids;
    }

//    public ActiveTags getActiveTags()
//    {
//        return getMessenger().getActiveTags();
//    }

    public long getIOBufSize()
    {
        return mIOBufSize;
    }

    public InetAddress getAddress()
    {
        return mAddress;
    }

    public String getUserName()
    {
        return mUserName;
    }

    public String getPassword()
    {
        return mPassword;
    }

    public String getMountPoint()
    {
        return mMountPoint;
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

    /**
     * Send TClunk message (release FID)
     * @param fid
     * @throws InterruptedException
     * @throws StyxException
     * @throws TimeoutException
     */
    public void clunk(long fid) 
            throws InterruptedException, StyxException, TimeoutException {
        final StyxTClunkMessage tClunk = new StyxTClunkMessage(fid);
        mMessenger.send(tClunk);
        StyxMessage rMessage = tClunk.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        mActiveFids.releaseFid(fid);
    }

    private void processVersionMessage(StyxRVersionMessage message)
    {
        if (message.getMaxPacketSize() < mIOBufSize)
            mIOBufSize = (int) message.getMaxPacketSize();
    }

    public void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        StyxTVersionMessage tVersion = new StyxTVersionMessage(mIOBufSize,PROTOCOL);
        mMessenger.send(tVersion);

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        onReceivedVersion(tVersion, rMessage);
    }

    public void sendAuthMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        mAuthFID = getActiveFids().getFreeFid();

        Messenger messenger = getMessenger();
        StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID);
        tAuth.setUserName(getUserName());
        tAuth.setMountPoint(getMountPoint());
        messenger.send(tAuth);

        StyxMessage rMessage = tAuth.waitForAnswer(mTimeout);
        onReceivedAuth(tAuth, rMessage);
    }

    public void sendAttachMessage()
            throws InterruptedException, StyxException, TimeoutException {
        mFID = getActiveFids().getFreeFid();

        Messenger messenger = getMessenger();
        StyxTAttachMessage tAttach = new StyxTAttachMessage(getFID(), getAuthFID());
        tAttach.setUserName(getUserName());
        tAttach.setMountPoint(getMountPoint());
        messenger.send(tAttach);

        StyxMessage rMessage = tAttach.waitForAnswer(mTimeout);
        onReceivedAttach(tAttach, rMessage);
    }

    private void checkMessages(StyxMessage tMessage, StyxMessage rMessage, MessageType needed)
            throws StyxException {
        StyxErrorMessageException.doException(rMessage);
        if (rMessage.getType() != needed)
            throw new StyxWrongMessageException(rMessage, MessageType.Rversion);
    }

    private void onReceivedVersion(StyxMessage tMessage, StyxMessage rMessage)
            throws StyxException, InterruptedException, IOException, TimeoutException {
        checkMessages(tMessage, rMessage, MessageType.Rversion);
        processVersionMessage((StyxRVersionMessage) rMessage);
        if (isNeedAuth())
            sendAuthMessage();
        else
            sendAttachMessage();
    }

    private void onReceivedAuth(StyxMessage tMessage, StyxMessage rMessage)
            throws StyxException, InterruptedException, IOException, TimeoutException {
        checkMessages(tMessage, rMessage, MessageType.Rauth);

        StyxRAuthMessage rAuth = (StyxRAuthMessage) rMessage;
        mAuthQID = rAuth.getQID();

        StyxOutputStream output = new StyxOutputStream((new StyxFile(this, 
                ((StyxTAuthMessage)tMessage).getAuthFID())).openForWrite());
        output.writeString(getPassword());
        output.flush();

        sendAttachMessage();
    }

    private void onReceivedAttach(StyxMessage tMessage, StyxMessage rMessage)
            throws StyxException {
        checkMessages(tMessage, rMessage, MessageType.Rattach);

        StyxRAttachMessage rAttach = (StyxRAttachMessage) rMessage;
        mQID = rAttach.getQID();
    }

    @Override
    public void close() {
        if ( mMessenger != null ) {
            mMessenger.close();
            mMessenger = null;
        }
    }

    public class ActiveFids
    {
        private LinkedList<Long> mAvailableFids = new LinkedList<Long>();
        private long mLastFid = 0L;

        /**
         * 
         * @return Return free FID
         */
        public long getFreeFid() {
            synchronized (mAvailableFids) {
                if (!mAvailableFids.isEmpty())
                    return mAvailableFids.poll();
                mLastFid++;
                if(mLastFid > Consts.MAXUNINT)
                    mLastFid = 0;
                return mLastFid;
            }
        }

        public boolean releaseFid(long fid) {
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

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }
    public void setAddressPort(InetAddress address, int port) {
        mAddress = address;
        mPort = port;
    }
    //-------------------------------------------------------------------------------------
    // Messenger listener
    //-------------------------------------------------------------------------------------
    @Override
    public void onSocketDisconected() {
        setConnected(false);
    }
}
