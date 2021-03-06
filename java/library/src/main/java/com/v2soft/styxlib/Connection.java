package com.v2soft.styxlib;

import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessageTransmitter;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.messages.StyxRAttachMessage;
import com.v2soft.styxlib.messages.StyxRAuthMessage;
import com.v2soft.styxlib.messages.StyxRVersionMessage;
import com.v2soft.styxlib.messages.StyxTAttachMessage;
import com.v2soft.styxlib.messages.StyxTAuthMessage;
import com.v2soft.styxlib.messages.StyxTVersionMessage;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.messages.base.structs.StyxQID;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.library.types.Credentials;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Styx client conection
 *
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class Connection
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
    private boolean isConnected, isAttached;
    private TMessageTransmitter mTransmitter;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;

    protected ClientDetails mRecepient;
    private IChannelDriver mDriver;
    protected ConnectionDetails mDetails;
    protected RMessagesProcessor mAnswerProcessor;
    protected boolean isAutoStartDriver = false;
    protected boolean shouldCloseAnswerProcessor = false;
    protected boolean shouldCloseTransmitter = false;

    public Connection() {
        this(new Credentials(null, null));
    }

    public Connection(Credentials credentials) {
        this(credentials, null);
    }

    public Connection(Credentials credentials, IChannelDriver driver) {
        this(credentials, driver, null, null, null);
    }

    public Connection(Credentials credentials,
                      IChannelDriver driver,
                      RMessagesProcessor answerProcessor,
                      TMessageTransmitter transmitter,
                      ClientDetails recepient) {
        mTransmitter = transmitter;
        mRecepient = recepient;
        mAnswerProcessor = answerProcessor;
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
        if ( mAnswerProcessor == null ) {
            mAnswerProcessor = new RMessagesProcessor("RH" + driver.toString());
            shouldCloseAnswerProcessor = true;
        }
        if ( mTransmitter == null ) {
            mTransmitter = new TMessageTransmitter(mTransmitterListener);
            shouldCloseTransmitter = true;
        }

        if (!driver.isStarted()) {
            driver.start(getIOBufSize());
            isAutoStartDriver = true;
        }

        if (mRecepient == null) {
            // get first client from driver
            mRecepient = driver.getClients().iterator().next();
        }

        return this.connect(driver, credentials, mAnswerProcessor, mTransmitter, mRecepient);
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
    public boolean connect(IChannelDriver driver, Credentials credentials, RMessagesProcessor answerProcessor,
                           TMessageTransmitter transmitter, ClientDetails recepient)
            throws IOException, StyxException, InterruptedException, TimeoutException {

        if (recepient == null) {
            throw new NullPointerException("recepient can't be null");
        }
        mRecepient = recepient;

        if (transmitter == null) {
            throw new NullPointerException("transmitter can't be null");
        }
        mTransmitter = transmitter;

        if (driver == null) {
            throw new NullPointerException("Channel driver can't be null");
        }
        setDriver(driver);

        if ( answerProcessor == null ) {
            throw new NullPointerException("answerProcessor can't be null");
        }
        mAnswerProcessor = answerProcessor;
        driver.setRMessageHandler(mAnswerProcessor);

        if (credentials == null) {
            throw new NullPointerException("Credentials can't be null");
        }
        mCredentials = credentials;
        mMountPoint = "/";
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
     * @return true if connection was success
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
        return mTransmitter;
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

    public long getRootFID() {
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

    @Override
    public ConnectionDetails getConnectionDetails() {
        return mDetails;
    }

    public void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        // release attached FID
        if (mFID != StyxMessage.NOFID) {
            try {
                final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
                mTransmitter.sendMessage(tClunk, mRecepient);
            } catch (Exception e) {
                throw new IOException(e);
            }
            mFID = StyxMessage.NOFID;
        }

        StyxTVersionMessage tVersion = new StyxTVersionMessage(mDetails.getIOUnit(), getProtocol());
        mTransmitter.sendMessage(tVersion, mRecepient);

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        StyxRVersionMessage rVersion = (StyxRVersionMessage) rMessage;
        if (rVersion.getMaxPacketSize() < mDetails.getIOUnit()) {
            mDetails = new ConnectionDetails(getProtocol(), (int) rVersion.getMaxPacketSize());
        }
        mRecepient.getPolls().getFIDPoll().clean();
        if ((mCredentials.getUserName() != null) && (mCredentials.getPassword() != null)) {
            sendAuthMessage();
        } else {
            sendAttachMessage();
        }
    }

    private void sendAuthMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        mAuthFID = mRecepient.getPolls().getFIDPoll().getFreeItem();

        StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID);
        tAuth.setUserName(getCredentials().getUserName());
        tAuth.setMountPoint(getMountPoint());
        mTransmitter.sendMessage(tAuth, mRecepient);

        StyxMessage rMessage = tAuth.waitForAnswer(mTimeout);
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
        mFID = mRecepient.getPolls().getFIDPoll().getFreeItem();
        StyxTAttachMessage tAttach = new StyxTAttachMessage(getRootFID(), getAuthFID(),
                getCredentials().getUserName(),
                getMountPoint());
        mTransmitter.sendMessage(tAttach, mRecepient);

        StyxMessage rMessage = tAttach.waitForAnswer(mTimeout);
        StyxRAttachMessage rAttach = (StyxRAttachMessage) rMessage;
        mQID = rAttach.getQID();
        setAttached(true);
    }

    @Override
    public void close() throws IOException {
        if (shouldCloseAnswerProcessor && mAnswerProcessor != null) {
            mAnswerProcessor.close();
            mAnswerProcessor = null;
        }
        if (mTransmitter != null) {
            mTransmitter.close();
            mTransmitter = null;
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
        return mDriver.isConnected();
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

    @Override
    public ClientDetails getRecepient() {
        return mRecepient;
    }

    protected void setDriver(IChannelDriver driver) {
        mDriver = driver;
    }
}
