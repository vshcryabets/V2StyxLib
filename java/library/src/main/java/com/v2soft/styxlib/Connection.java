package com.v2soft.styxlib;

import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.server.TMessageTransmitter;
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
import com.v2soft.styxlib.types.ConnectionDetails;
import com.v2soft.styxlib.types.Credentials;
import com.v2soft.styxlib.utils.SyncObject;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Styx client connection
 *
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class Connection
        implements IClient {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    // TODO duplicates, check all project
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
    private boolean isConnected;
    private boolean isAttached;
    private TMessageTransmitter mTransmitter;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;
    private SyncObject syncObject = new SyncObject();
    protected ClientDetails mRecepient;
    private IChannelDriver mDriver;
    protected ConnectionDetails mDetails;
    protected RMessagesProcessor mAnswerProcessor;
    protected boolean isAutoStartDriver = false;
    protected boolean shouldCloseAnswerProcessor = false;
    protected boolean shouldCloseTransmitter = false;

    // TODO remove or simplify
    public Connection() {
        this(new Credentials(null, null));
    }

    // TODO remove or simplify
    public Connection(Credentials credentials) {
        this(credentials, null);
    }

    // TODO remove or simplify
    public Connection(Credentials credentials, IChannelDriver driver) {
        this(credentials, driver, null, null, null);
    }

    // TODO remove or simplify
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
    // TODO remove or simplify
    public boolean connect(IChannelDriver driver, Credentials credentials)
            throws IOException, StyxException, InterruptedException, TimeoutException {
        if (mAnswerProcessor == null) {
            mAnswerProcessor = new RMessagesProcessor("RH" + driver.toString());
            shouldCloseAnswerProcessor = true;
        }
        if (mTransmitter == null) {
            mTransmitter = new TMessageTransmitter(mTransmitterListener);
            shouldCloseTransmitter = true;
        }

        if (driver == null) {
            throw new NullPointerException("Channel driver can't be null");
        }
        mDriver = driver;
        if (!mDriver.isStarted()) {
            mDriver.setRMessageHandler(mAnswerProcessor);
            mDriver.setTMessageHandler(mAnswerProcessor); // TODO fixme, this temp fix. TMessages shoul be handled in other way.
            mDriver.start(getIOBufSize());
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
    // TODO remove or simplify
    public boolean connect(IChannelDriver driver, Credentials credentials, RMessagesProcessor answerProcessor,
                           TMessageTransmitter transmitter, ClientDetails recepient)
            throws IOException, StyxException, InterruptedException, TimeoutException {

        if (recepient == null) {
            throw new NullPointerException("Recipient can't be null");
        }
        mRecepient = recepient;

        if (transmitter == null) {
            throw new NullPointerException("Transmitter can't be null");
        }
        mTransmitter = transmitter;


        if (answerProcessor == null) {
            throw new NullPointerException("answerProcessor can't be null");
        }
        mAnswerProcessor = answerProcessor;

        if (driver == null) {
            throw new NullPointerException("Channel driver can't be null");
        }
        mDriver = driver;

        if (credentials == null) {
            throw new NullPointerException("Credentials can't be null");
        }
        mCredentials = credentials;
        mMountPoint = "/";
        sendVersionMessage();
        isConnected = mDriver.isConnected();

        return isConnected;
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
    // TODO remove or simplify
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
    // TODO remove or simplify
    public boolean connect()
            throws IOException, StyxException, InterruptedException, TimeoutException {
        return connect(mDriver, mCredentials);
    }

    public StyxFile getRoot() throws IOException {
        if (mRoot == null) {
            mRoot = new StyxFile(this, mFID);
        }
        return mRoot;
    }

    @Override
    public IMessageTransmitter getTransmitter() {
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

    protected ConnectionDetails getConnectionDetails() {
        return mDetails;
    }

    protected void sendVersionMessage()
            throws InterruptedException, StyxException, IOException, TimeoutException {
        // release attached FID
        if (mFID != StyxMessage.NOFID) {
            try {
                final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
//                tClunk.setSyncObject(syncObject);
                mTransmitter.sendMessage(tClunk, mRecepient);
//                tClunk.waitForAnswer(mTimeout);
            } catch (Exception e) {
                throw new IOException(e);
            }
            mFID = StyxMessage.NOFID;
        }

        StyxTVersionMessage tVersion = new StyxTVersionMessage(mDetails.getIOUnit(), getProtocol());
        tVersion.setSyncObject(syncObject);
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

        StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID, getCredentials().getUserName(), getMountPoint());
        tAuth.setSyncObject(syncObject);
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
        tAttach.setSyncObject(syncObject);
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
        public void onSocketDisconnected(TMessageTransmitter caller) {
            isConnected = false;
        }

        @Override
        public void onTrashReceived(TMessageTransmitter caller) {
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
}
