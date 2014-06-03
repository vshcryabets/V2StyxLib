package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.concurrent.TimeoutException;

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
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.IMessageTransmitter;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

/**
 * Styx client conection
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
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
    private String mUserName;
    private String mPassword;
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
    private ActiveFids mActiveFids = new ActiveFids();
    protected IVirtualStyxFile mExportedRoot = null;

    public StyxClientConnection() {
        this(null, null);
        isConnected = false;
    }

    public StyxClientConnection(String username, String password) {
        mUserName = username;
        mPassword = password;
    }

    /**
     * Connect to server with specified parameters
     * @param username user name
     * @param password password 
     * @return true if connected
     * @throws IOException
     * @throws StyxException
     * @throws TimeoutException 
     */
    public boolean connect(IClientChannelDriver driver, String username, String password)
            throws IOException, StyxException, InterruptedException, TimeoutException {
        mUserName = username;
        mPassword = password;
        mMountPoint = "/";
        mNeedAuth = (username != null);
        mMessenger = new Messenger(driver, mIOBufSize, this, getLogListener());
        ((Messenger)mMessenger).export(mExportedRoot, getProtocol());
        sendVersionMessage();
        isConnected = driver.isConnected();
        return driver.isConnected();
    }

    public StyxFile getRoot() throws StyxException, InterruptedException, TimeoutException, IOException {
        if (mRoot == null)
            mRoot = new StyxFile(this,mFID);
        return mRoot;
    }

    public IMessageTransmitter getMessenger() {
        return mMessenger;
    }		

    public ActiveFids getActiveFids()
    {
        return mActiveFids;
    }

    public int getIOBufSize()
    {
        return mIOBufSize;
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

    public boolean isNeedAuth()
    {
        return mNeedAuth;
    }

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

    public void releaseFID(long fid)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, fid);
        mMessenger.sendMessage(tClunk);
    }

    @Override
    public long allocateFID() {
        return mActiveFids.getFreeFid();
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
        mMessenger.sendMessage(tVersion);

        StyxMessage rMessage = tVersion.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRVersionMessage rVersion = (StyxRVersionMessage) rMessage;
        if (rVersion.getMaxPacketSize() < mIOBufSize)
            mIOBufSize = (int) rVersion.getMaxPacketSize();
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
        mMessenger.sendMessage(tAuth);

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
        mMessenger.sendMessage(tAttach);

        StyxMessage rMessage = tAttach.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rMessage);
        StyxRAttachMessage rAttach = (StyxRAttachMessage) rMessage;
        mQID = rAttach.getQID();
        setAttached(true);
    }

    @Override
    public void close() throws IOException {
        if ( mMessenger != null ) {
            mMessenger.close();
            mMessenger = null;
        }
    }

    public ILogListener getLogListener() {
        return null;
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    public void export(IVirtualStyxFile root) {
        mExportedRoot = root;
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
        mActiveFids.releaseFid(fid);
    }
}
