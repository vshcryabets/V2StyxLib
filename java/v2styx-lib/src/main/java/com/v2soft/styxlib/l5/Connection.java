package com.v2soft.styxlib.l5;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessageTransmitter;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.handlers.IMessageTransmitter;

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
    private boolean isAttached;
    private TMessageTransmitter mTransmitter;
    private long mAuthFID = StyxMessage.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mFID = StyxMessage.NOFID;

    protected ClientDetails mRecepient;
    protected IChannelDriver mDriver;
    protected ConnectionDetails mDetails;
    protected RMessagesProcessor mAnswerProcessor;
    protected boolean isAutoStartDriver = false;
    protected boolean shouldCloseAnswerProcessor = false;
    protected boolean shouldCloseTransmitter = false;

    public Connection(Credentials credentials, IChannelDriver driver) {
        this(credentials, driver, null, null, null);
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
        mTransmitter = transmitter;
        mRecepient = recepient;
        mAnswerProcessor = answerProcessor;
        mDriver = driver;
        mDetails = new ConnectionDetails(getProtocol(), getIOBufSize());
        mCredentials = credentials;
        if ( mAnswerProcessor == null ) {
            mAnswerProcessor = new RMessagesProcessor("RH" + mDriver.toString());
            shouldCloseAnswerProcessor = true;
        }
        if ( mTransmitter == null ) {
            mTransmitter = new TMessageTransmitter(mTransmitterListener);
            shouldCloseTransmitter = true;
        }
    }
    /**
     * Connect to server with specified parameters
     *
     * @return true if connected
     */
    public boolean connect()
            throws IOException, InterruptedException, TimeoutException {
        if (!mDriver.isStarted()) {
            mDriver.start(getIOBufSize());
            isAutoStartDriver = true;
        }
        if (mRecepient == null) {
            var firstClient = mDriver.getClients().stream().findFirst();
            if (firstClient.isEmpty()) {
                throw new StyxException("No recipient");
            }
            mRecepient = firstClient.get();
        }
        mDriver.setRMessageHandler(mAnswerProcessor);
        mMountPoint = "/";
        sendVersionMessage();
        mDriver.isConnected();
        return mDriver.isConnected();
    }

    public StyxFile getRoot() throws StyxException {
        if (mRoot == null) {
            mRoot = new StyxFile(this, "");
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

        StyxTVersionMessage tVersion = new StyxTVersionMessage(mDetails.ioUnit(), getProtocol());
        mTransmitter.sendMessage(tVersion, mRecepient);

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        StyxRVersionMessage rVersion = (StyxRVersionMessage) rMessage;
        if (rVersion.maxPacketSize < mDetails.ioUnit()) {
            mDetails = new ConnectionDetails(getProtocol(), (int) rVersion.maxPacketSize);
        }
        mRecepient.getPolls().getFIDPoll().clean();
        sendAuthMessage();
    }

    private void sendAuthMessage()
            throws StyxException {
        if (!mCredentials.getUserName().isEmpty() && !mCredentials.getPassword().isEmpty()) {
            mAuthFID = mRecepient.getPolls().getFIDPoll().getFreeItem();

            StyxTAuthMessage tAuth = new StyxTAuthMessage(mAuthFID, getCredentials().getUserName(), getMountPoint());
            mTransmitter.sendMessage(tAuth, mRecepient);

            StyxMessage rMessage = tAuth.waitForAnswer(mTimeout);
            StyxRAuthMessage rAuth = (StyxRAuthMessage) rMessage;
            mAuthQID = rAuth.getQID();

            // TODO uncomment later
            //        StyxOutputStream output = new StyxOutputStream((new StyxFile(this,
            //                ((StyxTAuthMessage)tMessage).getAuthFID())).openForWrite());
            //        output.writeString(getPassword());
            //        output.flush();
        }

        mFID = mRecepient.getPolls().getFIDPoll().getFreeItem();
        StyxTAttachMessage tAttach = new StyxTAttachMessage(getRootFID(), getAuthFID(),
                getCredentials().getUserName(),
                getMountPoint());
        mTransmitter.sendMessage(tAttach, mRecepient);

        var rAttach = (StyxRAttachMessage)tAttach.waitForAnswer(mTimeout);
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
        if (isAutoStartDriver) {
            mDriver.close();
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
        public void onLostConnection() {
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

    @Override
    public IDataDeserializer getDeserializer() {
        return mDriver.getDeserializer();
    }
}
