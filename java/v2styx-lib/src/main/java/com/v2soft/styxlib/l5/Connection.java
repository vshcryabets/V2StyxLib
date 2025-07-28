package com.v2soft.styxlib.l5;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.EmptyMessagesProcessor;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.handlers.RMessagesProcessor;
import com.v2soft.styxlib.handlers.TMessageTransmitter;
import com.v2soft.styxlib.l5.enums.Constants;
import com.v2soft.styxlib.l5.enums.MessageType;
import com.v2soft.styxlib.l5.messages.*;
import com.v2soft.styxlib.l5.messages.base.StyxMessage;
import com.v2soft.styxlib.l5.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.l5.messages.v9p2000.StyxRVersionMessage;
import com.v2soft.styxlib.l5.structs.StyxQID;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.library.types.Credentials;
import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Styx client connection
 *
 * @author V.Shcryabets <a>vshcryabets@gmail.com</a>
 */
public class Connection
        implements Closeable, IClient {
    public static class Configuration {
        public Credentials credentials;
        public IChannelDriver<?> driver;
        public StyxSessionDI di;
        public RMessagesProcessor answerProcessor; // handle RMessages from server
        public IMessageProcessor requestProcessor; // handle TMessages from server
        public TMessageTransmitter transmitter; //send messages to server

        public Configuration(Credentials credentials,
                             IChannelDriver<?> driver,
                             StyxSessionDI di) {
            this(credentials,
                    driver,
                    di,
                    null,
                    null);
        }

        public Configuration(
                Credentials credentials,
                IChannelDriver<?> driver,
                StyxSessionDI di,
                RMessagesProcessor answerProcessor,
                TMessageTransmitter transmitter) {
            if (driver == null) {
                throw new NullPointerException("Channel driver can't be null");
            }
            if (credentials == null) {
                throw new NullPointerException("Credentials can't be null");
            }

            this.credentials = credentials;
            this.driver = driver;
            this.di = di;
            this.answerProcessor = answerProcessor;
            this.transmitter = transmitter;
        }
    }
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
    private String mMountPoint;
    private int mTimeout = DEFAULT_TIMEOUT;
    private boolean isAttached;
    private long mAuthFID = Constants.NOFID;
    private StyxQID mAuthQID;
    private StyxQID mQID;
    private long mRootFid = Constants.NOFID;

    protected ConnectionDetails mDetails;
    protected boolean isAutoStartDriver = false;
    protected boolean shouldCloseAnswerProcessor = false;
    protected boolean shouldCloseTransmitter = false;
    protected int mClientId = -1;
    protected Configuration mConfiguration;

    public Connection(Configuration configuration) {
        mConfiguration = configuration;
        mDetails = new ConnectionDetails(getProtocol(), getIOBufSize());
        if ( configuration.answerProcessor == null ) {
            configuration.answerProcessor = new RMessagesProcessor("RH" + configuration.driver.toString(),
                    configuration.di.getClientsRepo());
            shouldCloseAnswerProcessor = true;
        }
        if ( mConfiguration.transmitter == null ) {
            mConfiguration.transmitter = new TMessageTransmitter(mTransmitterListener, configuration.di.getClientsRepo());
            shouldCloseTransmitter = true;
        }
        if (mConfiguration.requestProcessor == null)
            mConfiguration.requestProcessor = new EmptyMessagesProcessor();
    }
    /**
     * Connect to server with specified parameters
     *
     * @return true if connected
     */
    public boolean connect()
            throws IOException, InterruptedException, TimeoutException {
        var driver = mConfiguration.driver;
        if (!driver.isStarted()) {
            driver.start(new IChannelDriver.StartConfiguration(
                    mConfiguration.requestProcessor,
                    mConfiguration.answerProcessor
            ));
            isAutoStartDriver = true;
        }
        if (mClientId < 0) {
            var firstClient = driver.getClients().stream().findFirst();
            if (firstClient.isEmpty()) {
                throw new StyxException("No recipient");
            }
            mClientId = (int) firstClient.get();
        }
        mMountPoint = "/";
        sendVersionMessage();
        return driver.isConnected();
    }

    @Deprecated
    public StyxFile getRoot() throws StyxException {
        if (mRoot == null) {
            mRoot = new StyxFile( "",
                    getRootFID(),
                    mClientId,
                    mConfiguration.transmitter,
                    getTimeout(),
                    mConfiguration.di);
        }
        return mRoot;
    }

    protected int getIOBufSize() {
        return DEFAULT_IO_SIZE;
    }

    public String getMountPoint() {
        return mMountPoint;
    }

    public long getRootFID() {
        return mRootFid;
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
            throws StyxException {
        // release attached FID
        if (mRootFid != Constants.NOFID) {
            final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mRootFid);
            mConfiguration.transmitter.sendMessage(tClunk, mClientId, mTimeout).getResult();
            mRootFid = Constants.NOFID;
        }
        StyxMessage rMessage = mConfiguration.transmitter.sendMessage(
                mConfiguration.di.getMessageFactory().constructTVersion(mDetails.ioUnit(), getProtocol()),
                mClientId,
                mTimeout).getResult();
        StyxRVersionMessage rVersion = (StyxRVersionMessage) rMessage;
        if (rVersion.maxPacketSize < mDetails.ioUnit()) {
            mDetails = new ConnectionDetails(getProtocol(), (int) rVersion.maxPacketSize);
        }
        mConfiguration.di.getClientsRepo().getFidPoll(mClientId).clean();
        sendAuthMessage();
    }

    private void sendAuthMessage()
            throws StyxException {
        var credentials = mConfiguration.credentials;
        if (!credentials.getUserName().isEmpty() && !credentials.getPassword().isEmpty()) {
            mAuthFID = mConfiguration.di.getClientsRepo().getFidPoll(mClientId).getFreeItem();

            StyxMessage tAuth = mConfiguration.di.getMessageFactory().constructTAuth(mAuthFID,
                    mConfiguration.credentials.getUserName(), getMountPoint());
            StyxMessage rMessage = mConfiguration.transmitter.sendMessage(tAuth, mClientId, mTimeout).getResult();
            StyxRAuthMessage rAuth = (StyxRAuthMessage) rMessage;
            mAuthQID = rAuth.getQID();

            // TODO uncomment later
            //        StyxOutputStream output = new StyxOutputStream((new StyxFile(this,
            //                ((StyxTAuthMessage)tMessage).getAuthFID())).openForWrite());
            //        output.writeString(getPassword());
            //        output.flush();
        }

        mRootFid = mConfiguration.di.getClientsRepo().getFidPoll(mClientId).getFreeItem();
        StyxMessage tAttach = mConfiguration.di.getMessageFactory().constructTAttach(mRootFid, getAuthFID(),
                mConfiguration.credentials.getUserName(),
                getMountPoint());
        var rAttach = (StyxRAttachMessage)mConfiguration.transmitter.sendMessage(tAttach, mClientId, mTimeout)
                .getResult();
        mQID = rAttach.getQID();
        setAttached(true);
    }


    @Override
    public void close() throws IOException {
        if (shouldCloseAnswerProcessor) {
            mConfiguration.answerProcessor.close();
        }
        if (shouldCloseTransmitter) {
            mConfiguration.transmitter.close();
        }
        if (isAutoStartDriver) {
            mConfiguration.driver.close();
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
        return mConfiguration.driver.isConnected();
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
    public StyxFile open(String filename) throws StyxException {
        return new StyxFile(filename,
                getRootFID(),
                mClientId,
                mConfiguration.transmitter,
                getTimeout(),
                mConfiguration.di);
    }
}
