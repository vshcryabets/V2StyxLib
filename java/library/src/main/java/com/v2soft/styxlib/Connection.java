package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
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
        implements IClient, TMessageTransmitter.Listener {
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
    private long mAuthFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID;
    private SyncObject syncObject;
    protected ClientDetails mRecepient;
    private IChannelDriver mDriver;
    protected ConnectionDetails mDetails;
    protected RMessagesProcessor mAnswerProcessor;
    protected boolean isAutoStartDriver;
    protected boolean shouldCloseAnswerProcessor;

    public static class Builder {
        protected Credentials mCredentials = new Credentials("", "");
        protected IChannelDriver mDriver;
        protected RMessagesProcessor mAnswerProcessor;
        protected TMessageTransmitter mTransmitter = new TMessageTransmitter();
        protected ClientDetails mClientDetails;

        public void setCredentials(Credentials credentials) {
            this.mCredentials = credentials;
        }

        public Builder setDriver(IChannelDriver driver) {
            this.mDriver = driver;
            return this;
        }

        public void setAnswerProcessor(RMessagesProcessor processor) {
            this.mAnswerProcessor = processor;
        }

        public void setTransmitter(TMessageTransmitter transmitter) {
            this.mTransmitter = transmitter;
        }

        public void setClientDetails(ClientDetails clientDetails) {
            this.mClientDetails = clientDetails;
        }

        public Connection build() {
            return new Connection(mCredentials, mDriver, mAnswerProcessor, mTransmitter, mClientDetails);
        }
    }

    public Connection(Credentials credentials,
                      IChannelDriver driver,
                      RMessagesProcessor answerProcessor,
                      TMessageTransmitter transmitter,
                      ClientDetails recepient) {
        if (driver == null) {
            throw new NullPointerException("Channel driver can't be null");
        }
        if (credentials == null) {
            throw new NullPointerException("Credentials can't be null");
        }
        mAuthFID = StyxMessage.NOFID;
        mFID = StyxMessage.NOFID;
        isAutoStartDriver = false;
        shouldCloseAnswerProcessor = false;

        syncObject = new SyncObject(mTimeout);
        mTransmitter = transmitter;
        mRecepient = recepient;
        mAnswerProcessor = answerProcessor;
        mDriver = driver;
        mDetails = new ConnectionDetails(getProtocol(), getIOBufSize());
        mCredentials = credentials;
        isConnected = false;
        mMountPoint = "/";
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
        // TODO move to builder
        if (mAnswerProcessor == null) {
            mAnswerProcessor = new RMessagesProcessor("RH" + mDriver.toString());
            shouldCloseAnswerProcessor = true;
        }
        if (!mDriver.isStarted()) {
            mDriver.setRMessageHandler(mAnswerProcessor);
            mDriver.setTMessageHandler(mAnswerProcessor); // TODO fixme, this temp fix. TMessages shoul be handled in other way.
            mDriver.start(getIOBufSize());
            isAutoStartDriver = true;
        }

        if (mRecepient == null) {
            // get first client from driver
            mRecepient = mDriver.getClients().iterator().next();
        }
        if (mRecepient == null) {
            throw new NullPointerException("Recipient can't be null");
        }
        sendVersionMessage();
        isConnected = mDriver.isConnected();
        return isConnected;
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
                mTransmitter.sendMessage(tClunk, mRecepient);
            } catch (Exception e) {
                throw new IOException(e);
            }
            mFID = StyxMessage.NOFID;
        }

        StyxTVersionMessage tVersion = new StyxTVersionMessage(mDetails.getIOUnit(), getProtocol());
        StyxMessage rMessage = mTransmitter.sendMessageAndWaitAnswer(tVersion, mRecepient, syncObject);
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
            throws IOException, InterruptedException, StyxErrorMessageException, TimeoutException {
        mAuthFID = mRecepient.getPolls().getFIDPoll().getFreeItem();

        StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID, getCredentials().getUserName(), getMountPoint());
        StyxMessage rMessage = mTransmitter.sendMessageAndWaitAnswer(tAuth, mRecepient, syncObject);
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
            throws IOException, InterruptedException, StyxErrorMessageException, TimeoutException {
        mFID = mRecepient.getPolls().getFIDPoll().getFreeItem();
        StyxTAttachMessage tAttach = new StyxTAttachMessage(getRootFID(), getAuthFID(),
                getCredentials().getUserName(),
                getMountPoint());
        StyxMessage rMessage = mTransmitter.sendMessageAndWaitAnswer(tAttach, mRecepient, syncObject);
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
        mTransmitter.close();
        if (isAutoStartDriver) {
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
    public void setTimeout(int timeout) {
        mTimeout = timeout;
        syncObject = new SyncObject(timeout);
    }

    public void setAttached(boolean isAttached) {
        this.isAttached = isAttached;
    }

    public boolean isConnected() {
        return mDriver.isConnected();
    }

    @Override
    public void onChannelDisconnected(TMessageTransmitter caller) {
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

    @Override
    public ClientDetails getRecepient() {
        return mRecepient;
    }
}
