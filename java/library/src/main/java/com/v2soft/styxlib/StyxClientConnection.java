package com.v2soft.styxlib;

import com.v2soft.styxlib.library.core.RMessagesProcessor;
import com.v2soft.styxlib.library.core.TMessageTransmitter;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.messages.StyxRAttachMessage;
import com.v2soft.styxlib.library.messages.StyxRAuthMessage;
import com.v2soft.styxlib.library.messages.StyxRVersionMessage;
import com.v2soft.styxlib.library.messages.StyxTAttachMessage;
import com.v2soft.styxlib.library.messages.StyxTAuthMessage;
import com.v2soft.styxlib.library.messages.StyxTVersionMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.library.messages.base.enums.MessageType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.library.utils.FIDPoll;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Styx client conection
 *
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class StyxClientConnection
        implements Closeable, IClient {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 10000;
    private static final int DEFAULT_IO_SIZE = 8192;
    //---------------------------------------------------------------------------
    // Class fields
    //---------------------------------------------------------------------------
    private StyxFile mRoot;
    protected Credentials mCredentials;
    private String mMountPoint;
    private int mTimeout = DEFAULT_TIMEOUT;
    private boolean mNeedAuth;
    private boolean isConnected, isAttached;
    private IMessageTransmitter mMessenger;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;
    private FIDPoll mActiveFids = new FIDPoll();
    protected ClientDetails mRecepient;
    private IChannelDriver mDriver;
    protected ConnectionDetails mDetails;
    protected RMessagesProcessor mAnswerProcessor;
    protected boolean isAutoStartDriver = false;

    public StyxClientConnection() {
        this(new Credentials(null, null));
    }

    public StyxClientConnection(Credentials credentials) {
        mDetails = new ConnectionDetails(getProtocol(), getIOBufSize());
        mCredentials = credentials;
        isConnected = false;
        mDriver = null;
    }

    public StyxClientConnection(Credentials credentials, IChannelDriver driver) {
        if (driver == null) {
            throw new NullPointerException("Driver is null");
        }
        mDriver = driver;
        mDetails = new ConnectionDetails(getProtocol(), getIOBufSize());
        mCredentials = credentials;
        isConnected = false;
    }

    /**
     * Connect to server with specified parameters
     *
     * @param driver      channel driver
     * @param credentials user credentials
     * @return true if connected
     * @throws IOException
     * @throws StyxException
     * @throws TimeoutException
     */
    public boolean connect(IChannelDriver driver, Credentials credentials)
            throws IOException, StyxException, InterruptedException, TimeoutException {
        if (driver == null) {
            throw new NullPointerException("Channel driver can't be null");
        }
        setDriver(driver);
        if (!driver.isStarted()) {
            driver.start(getIOBufSize());
            isAutoStartDriver = true;
        }
        if (credentials == null) {
            throw new NullPointerException("Credentials can't be null");
        }
        mCredentials = credentials;
        mMountPoint = "/";
        mNeedAuth = ( mCredentials != null );

        mAnswerProcessor = new RMessagesProcessor();
        mAnswerProcessor.setListener(mAnswerListener);
        driver.setRMessageHandler(mAnswerProcessor);
        mMessenger = new TMessageTransmitter(mAnswerProcessor, mTransmitterListener);

        mRecepient = driver.getClients().iterator().next();
        sendVersionMessage();
        isConnected = driver.isConnected();

        return driver.isConnected();
    }

    /**
     * Connect to server with specified parameters
     *
     * @param driver channel driver
     * @return true if connected
     * @throws IOException
     * @throws StyxException
     * @throws TimeoutException
     */
    public boolean connect(IChannelDriver driver)
            throws IOException, StyxException, InterruptedException, TimeoutException {
        return connect(driver, mCredentials);
    }

    /**
     * Connect to server with specified parameters
     *
     * @return
     * @throws IOException
     * @throws StyxException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    public boolean connect()
            throws IOException, StyxException, InterruptedException, TimeoutException {
        return connect(mDriver, mCredentials);
    }

    public StyxFile getRoot() throws StyxException, InterruptedException, TimeoutException, IOException {
        if (mRoot == null) {
            mRoot = new StyxFile(this, mFID);
        }
        return mRoot;
    }

    public IMessageTransmitter getMessenger() {
        return mMessenger;
    }

    protected FIDPoll getActiveFids() {
        return mActiveFids;
    }

    protected int getIOBufSize() {
        return DEFAULT_IO_SIZE;
    }

    public Credentials getCredentials() {
        return mCredentials;
    }

    public String getMountPoint() {
        return mMountPoint;
    }

    public boolean isNeedAuth() {
        return mNeedAuth;
    }

    public long getFID() {
        return mFID;
    }

    public StyxQID getQID() {
        return mQID;
    }

    public long getAuthFID() {
        return mAuthFID;
    }

    public StyxQID getAuthQID() {
        return mAuthQID;
    }

    public void releaseFID(long fid)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, fid);
        mMessenger.sendMessage(tClunk, mRecepient);
    }

    @Override
    public long allocateFID() {
        return mActiveFids.getFreeItem();
    }

    @Override
    public ConnectionDetails getConnectionDetails() {
        return mDetails;
    }

    public void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        // release attached FID
        if (mFID != StyxMessage.NOFID) {
            try {
                releaseFID(mFID);
            } catch (Exception e) {
                throw new IOException(e);
            }
            mFID = StyxMessage.NOFID;
        }

        StyxTVersionMessage tVersion = new StyxTVersionMessage(mDetails.getIOUnit(), getProtocol());
        mMessenger.sendMessage(tVersion, mRecepient);

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRVersionMessage rVersion = (StyxRVersionMessage) rMessage;
        if (rVersion.getMaxPacketSize() < mDetails.getIOUnit()) {
            mDetails = new ConnectionDetails(getProtocol(), (int) rVersion.getMaxPacketSize());
        }
        mActiveFids.clean();
        if (isNeedAuth()) {
            sendAuthMessage();
        } else {
            sendAttachMessage();
        }
    }

    private void sendAuthMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        mAuthFID = getActiveFids().getFreeItem();

        StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID);
        tAuth.setUserName(getCredentials().getUserName());
        tAuth.setMountPoint(getMountPoint());
        mMessenger.sendMessage(tAuth, mRecepient);

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
        mFID = getActiveFids().getFreeItem();
        StyxTAttachMessage tAttach = new StyxTAttachMessage(getFID(), getAuthFID(),
                getCredentials().getUserName(),
                getMountPoint());
        mMessenger.sendMessage(tAttach, mRecepient);

        StyxMessage rMessage = tAttach.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRAttachMessage rAttach = (StyxRAttachMessage) rMessage;
        mQID = rAttach.getQID();
        setAttached(true);
    }

    @Override
    public void close() throws IOException {
        if (mAnswerProcessor != null) {
            mAnswerProcessor.close();
            mAnswerProcessor = null;
        }
        if (mMessenger != null) {
            mMessenger.close();
            mMessenger = null;
        }
        if (isAutoStartDriver && mDriver != null) {
            mDriver.close();
            mDriver = null;
        }
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    //-------------------------------------------------------------------------------------
    // Getters
    //-------------------------------------------------------------------------------------
    public int getTimeout() {
        return mTimeout;
    }

    public boolean isAttached() {
        return isAttached;
    }

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

    private TMessageTransmitter.Listener mTransmitterListener = new TMessageTransmitter.Listener() {
        @Override
        public void onSocketDisconnected() {
            isConnected = false;
        }

        @Override
        public void onTrashReceived() {
            //something goes wrong, we should restart protocol
            setAttached(false);
            try {
                sendVersionMessage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private RMessagesProcessor.Listener mAnswerListener = new RMessagesProcessor.Listener() {
        @Override
        public void onFIDReleased(long fid) {
            mActiveFids.release(fid);
        }
    };

    @Override
    public ClientDetails getRecepient() {
        return mRecepient;
    }

    protected void setDriver(IChannelDriver driver) {
        mDriver = driver;
    }
}
