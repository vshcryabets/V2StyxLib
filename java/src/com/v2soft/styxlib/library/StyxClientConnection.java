package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLContext;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.v2soft.styxlib.library.core.StyxCodecFactory;
import com.v2soft.styxlib.library.core.StyxSessionHandler;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
import com.v2soft.styxlib.library.messages.StyxTClunkMessage;
import com.v2soft.styxlib.library.messages.StyxTRemoveMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxClientConnection 
implements Closeable {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 10000;
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
    private SSLContext mSSL;
    private boolean mNeedAuth;
    private boolean isConnected, isAttached;
    private StyxSessionHandler mMessenger;
    private int mIOBufSize = 8192;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;
    private ActiveFids mActiveFids = new ActiveFids();
    private IoConnector mConnector;
    private IoSession mSession;

    public StyxClientConnection() {
        this(null, 0, null, null, null);
    }

    public StyxClientConnection(InetAddress address, int port, SSLContext ssl) {
        this(address, port, ssl, null, null);
    }

    public StyxClientConnection(InetAddress address, int port, 
            SSLContext ssl, 
            String username, String password) {
        mAddress = address;
        mPort = port;
        mSSL = ssl;
        mUserName = username;
        mPassword = password;
        setConnected(false);
    }

    /**
     * Connect to server with specified parameters
     * @return true if connected
     * @throws IOException
     * @throws StyxException
     * @throws TimeoutException 
     */
    public boolean connect()
            throws IOException, StyxException, InterruptedException, TimeoutException {
        mMountPoint = "/";
        mNeedAuth = (mUserName != null);
        mConnector = new NioSocketConnector();
        mConnector.getSessionConfig().setReadBufferSize(mIOBufSize);
        mConnector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new StyxCodecFactory()));
        mMessenger = new StyxSessionHandler();
        mConnector.setHandler(mMessenger);
        ConnectFuture future = mConnector.connect(new InetSocketAddress(mAddress, mPort));
        future.awaitUninterruptibly();
        if (!future.isConnected()) {
            return false;
        }
        mSession = future.getSession();
        mSession.getConfig().setUseReadOperation(true);

        
//        final SocketAddress sa = new InetSocketAddress(mAddress, mPort);
//        final SocketChannel channel = SocketChannel.open(sa);
//        channel.configureBlocking(true);
//        Socket socket = channel.socket();
//        socket.setSoTimeout(mTimeout);
//
//        if ( mSSL != null ) {
//            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
//            SSLSocket sslSocket = (SSLSocket) factory.createSocket(socket, 
//                    mAddress.getHostAddress(), mPort, true);
//            printSocketInfo(sslSocket);
//
//            // Connect to the server
//            sslSocket.startHandshake();
//
//            // We need a timeout here for the case where the server is not using SSL,
//            // in which case both sides simply sit waiting for something from the
//            // other side.
//            sslSocket.setSoTimeout(500);
//            // Get the SSL session. This forces a handshake and is used
//            // to indicate that we've performed the handshake for this
//            // SSLSocket.
//            SSLSession session = sslSocket.getSession();
//            //        this.sslSessionMap.put(socket, session);
//
//            // Retrieve the server's certificate chain
//            java.security.cert.Certificate[] serverCerts =
//                    session.getPeerCertificates();
//
//            // Since we don't have isValid() in 1.4 this is the closest we can get to
//            // a validity check.
//            if (session.getId() != null && session.getId().length != 0) {
//                if ( Config.LOG_NETWORK ) {
//                    System.out.println("SSL session details: " + session);
//                }
//            } else {
//                if (sslSocket.getUseClientMode()) {
//                    throw new SSLException("SSL session handshake failed (is the server SSL enabled?)");
//                }
//                // For server's we'll leave it to JSSE to raise an exception
//                // when the client first tries to communicate with us.
//            }
//            // Set the read timeout to the smallest legal value so
//            // when data is available we can read it in blocking mode.
//            // This must be done only after the handshake has completed.
//            sslSocket.setSoTimeout(1);
//
//            SocketChannel channel2 = sslSocket.getChannel();
//            mMessenger = new Messenger(channel2, mIOBufSize, this);
//        } else {
//            mMessenger = new Messenger(socket.getChannel(), mIOBufSize, this);
//        }

        sendVersionMessage();
        setConnected(true);
        return true;
    }
 
    public StyxFile getRoot() throws StyxException, InterruptedException, 
    TimeoutException, IOException {
        if (mRoot == null)
            mRoot = new StyxFile(this);
        return mRoot;
    }

    public StyxSessionHandler getMessenger() {
        return mMessenger;
    }		

    public ActiveFids getActiveFids()
    {
        return mActiveFids;
    }

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

    /**
     * 
     * @return true if connection should use SSL
     */
    public boolean isSSL() {
        return mSSL != null;
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
     * @throws IOException 
     */
    public void clunk(long fid) 
            throws InterruptedException, StyxException, TimeoutException, IOException {
        final StyxTClunkMessage tClunk = new StyxTClunkMessage(fid);
        mMessenger.send(tClunk);
        StyxMessage rMessage = tClunk.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        mActiveFids.releaseFid(fid);
    }

    /**
     * Send TRemove message for specefied FID, note that FID will be released even if remove failed.
     * @param fid FID of the file that should be removed
     * @throws TimeoutException 
     * @throws InterruptedException 
     * @throws StyxErrorMessageException 
     * @throws IOException 
     */
    public void remove(long fid) throws InterruptedException, TimeoutException, StyxErrorMessageException, IOException {
        StyxTRemoveMessage tRemove = new StyxTRemoveMessage(fid);

        mMessenger.send(tRemove);
        StyxMessage rMessage = tRemove.waitForAnswer(mTimeout);
        getActiveFids().releaseFid(fid);
        StyxErrorMessageException.doException(rMessage);
    }    
    /**
     * Restart session with server
     * @throws InterruptedException
     * @throws StyxException
     * @throws IOException
     * @throws TimeoutException
     */
    public void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        StyxTVersionMessage tVersion = new StyxTVersionMessage(mIOBufSize,PROTOCOL);
        mSession.write(tVersion).awaitUninterruptibly();

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRVersionMessage rVersion = (StyxRVersionMessage)rMessage;
        if (rVersion.getMaxPacketSize() < mIOBufSize)
            mIOBufSize = (int)rVersion.getMaxPacketSize();
        mActiveFids.clean();
        if (isNeedAuth())
            sendAuthMessage();
        else
            sendAttachMessage();
    }

    private void sendAuthMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        mAuthFID = getActiveFids().getFreeFid();

        StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID);
        tAuth.setUserName(getUserName());
        tAuth.setMountPoint(getMountPoint());
        mMessenger.send(tAuth);

        StyxMessage rMessage = tAuth.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRAuthMessage rAuth = (StyxRAuthMessage) rMessage;
        mAuthQID = rAuth.getQID();

        // TODO uncomment later
        //        StyxOutputStream output = new StyxOutputStream((new StyxFile(this, 
        //                ((StyxTAuthMessage)tMessage).getAuthFID())).openForWrite());
        //        output.writeString(getPassword());
        //        output.flush();

        sendAttachMessage();
    }

    private void sendAttachMessage()
            throws InterruptedException, StyxException, TimeoutException, IOException {
        mFID = getActiveFids().getFreeFid();
        StyxTAttachMessage tAttach = new StyxTAttachMessage(getFID(), getAuthFID(),
                getUserName(),
                getMountPoint());
        mMessenger.send(tAttach);

        StyxMessage rMessage = tAttach.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRAttachMessage rAttach = (StyxRAttachMessage) rMessage;
        mQID = rAttach.getQID();
        setAttached(true);
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
    public boolean isAttached() {return isAttached;}
    //-------------------------------------------------------------------------------------
    // Setters
    //-------------------------------------------------------------------------------------
    public void setTimeout(int mTimeout) {
        this.mTimeout = mTimeout;
    }
    public void setAttached(boolean isAttached) {
        this.isAttached = isAttached;
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
}
