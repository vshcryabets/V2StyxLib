package com.v2soft.styxlib.library;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.core.Messenger;
import com.v2soft.styxlib.library.core.Messenger.StyxMessengerListener;
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
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.IChannelDriver;
import com.v2soft.styxlib.library.server.IMessageTransmitter;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.library.utils.FIDPoll;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Styx client conection
 *
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class StyxClientConnection
        implements Closeable, StyxMessengerListener, IClient {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 10000;
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
    private int mIOBufSize = 8192;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;
    private FIDPoll mActiveFids = new FIDPoll();
    protected ClientState mRecepient;
    private IChannelDriver mDriver;

    public StyxClientConnection() {
        this(new Credentials(null, null));
    }

    public StyxClientConnection(Credentials credentials) {
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
        if (credentials == null) {
            throw new NullPointerException("Credentials can't be null");
        }
        mCredentials = credentials;
        mMountPoint = "/";
        mNeedAuth = ( mCredentials != null );
        mMessenger = initMessenger(driver);
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

    protected IMessageTransmitter initMessenger(IChannelDriver driver) throws IOException {
        IMessageTransmitter result = new Messenger(driver, mIOBufSize, this);
        return result;
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

    public int getIOBufSize() {
        return mIOBufSize;
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

    public void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        // release atached FID
        if (mFID != StyxMessage.NOFID) {
            try {
                releaseFID(mFID);
            } catch (Exception e) {
                throw new IOException(e);
            }
            mFID = StyxMessage.NOFID;
        }

        StyxTVersionMessage tVersion = new StyxTVersionMessage(mIOBufSize, getProtocol());
        mMessenger.sendMessage(tVersion, mRecepient);

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRVersionMessage rVersion = (StyxRVersionMessage) rMessage;
        if (rVersion.getMaxPacketSize() < mIOBufSize) {
            mIOBufSize = (int) rVersion.getMaxPacketSize();
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
        if (mMessenger != null) {
            mMessenger.close();
            mMessenger = null;
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

    //-------------------------------------------------------------------------------------
    // Messenger listener
    //-------------------------------------------------------------------------------------
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

    @Override
    public void onFIDReleased(long fid) {
        mActiveFids.release(fid);
    }

    @Override
    public ClientState getRecepient() {
        return mRecepient;
    }

    public void setDriver(IChannelDriver driver) {
        mDriver = driver;
    }
}
