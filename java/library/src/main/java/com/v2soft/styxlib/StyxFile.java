package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.io.DualStreams;
import com.v2soft.styxlib.io.StyxDataInputStream;
import com.v2soft.styxlib.io.StyxFileBufferedInputStream;
import com.v2soft.styxlib.io.StyxUnbufferedInputStream;
import com.v2soft.styxlib.io.StyxUnbufferedOutputStream;
import com.v2soft.styxlib.messages.StyxROpenMessage;
import com.v2soft.styxlib.messages.StyxRStatMessage;
import com.v2soft.styxlib.messages.StyxRWalkMessage;
import com.v2soft.styxlib.messages.StyxTCreateMessage;
import com.v2soft.styxlib.messages.StyxTOpenMessage;
import com.v2soft.styxlib.messages.StyxTWStatMessage;
import com.v2soft.styxlib.messages.StyxTWalkMessage;
import com.v2soft.styxlib.messages.base.StyxMessage;
import com.v2soft.styxlib.messages.base.StyxTMessageFID;
import com.v2soft.styxlib.messages.base.enums.FileMode;
import com.v2soft.styxlib.messages.base.enums.MessageType;
import com.v2soft.styxlib.messages.base.enums.ModeType;
import com.v2soft.styxlib.messages.base.structs.StyxStat;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IMessageTransmitter;
import com.v2soft.styxlib.library.types.ULong;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxFile implements Closeable {
    public interface StyxFileFilter {
        boolean accept(StyxFile file);
    }

    public interface StyxFilenameFilter {
        boolean accept(StyxFile parent, String name);
    }

    public static final String SEPARATOR = "/";

    private IClient mClient;
    private long mFID = StyxMessage.NOFID;
    private long mParentFID = StyxMessage.NOFID;
    private StyxStat mStat;
    private String mPath;
    private IMessageTransmitter mMessenger;
    private long mTimeout = Connection.DEFAULT_TIMEOUT;
    protected ClientDetails mRecepient;

    public StyxFile(IClient manager, String path)
            throws StyxException, TimeoutException, IOException, InterruptedException {
        this(manager, path, null);
    }

    public StyxFile(IClient manager, String path, StyxFile parent)
            throws StyxException, TimeoutException, IOException, InterruptedException {
        if ( !manager.isConnected() )
            throw new IOException("Styx connection wasn't established");
        mClient = manager;
        mMessenger = mClient.getMessenger();
        mRecepient = mClient.getRecepient();
        mTimeout = mClient.getTimeout();
        mPath = path;
        if ( parent != null ) {
            mParentFID = parent.getFID();
        } else {
            mParentFID = manager.getRootFID();
        }
    }

    public StyxFile(IClient manager, long fid) throws IOException {
        if ( !manager.isConnected() )
            throw new IOException("Styx connection wasn't established");
        mClient = manager;
        mPath = null;
        mFID = fid;
    }

    private String combinePath(StyxFile parent, String path)
    {
        if (parent.mPath == null)
            return SEPARATOR + path;
        return parent.mPath + SEPARATOR + path;
    }


    public String getPath() {
        if (mPath == null)
            return "/";
        return mPath;
    }

    /**
     * Retrieve FID for this file
     * @return FID allocated for this file
     * @throws StyxException
     * @throws TimeoutException
     * @throws IOException
     * @throws InterruptedException
     */
    public long getFID() throws StyxException, TimeoutException, IOException, InterruptedException {
        if (mFID == StyxMessage.NOFID) {
            mFID = sendWalkMessage(mParentFID, mPath);
        }
        return mFID;
    }

    private int open(int mode, long fid)
            throws StyxException, InterruptedException, TimeoutException, IOException {
        final StyxTOpenMessage tOpen = new StyxTOpenMessage(fid, mode);

        mMessenger.sendMessage(tOpen, mRecepient);
        StyxMessage rMessage = tOpen.waitForAnswer(mTimeout);

        StyxROpenMessage rOpen = (StyxROpenMessage) rMessage;
        return (int)rOpen.getIOUnit();
    }

    @Override
    public void close() throws IOException {
        if (mFID == StyxMessage.NOFID) {
            return;
        }
        try {
            // send Tclunk
            final StyxTMessageFID tClunk = new StyxTMessageFID(MessageType.Tclunk, MessageType.Rclunk, mFID);
            mMessenger.sendMessage(tClunk, mRecepient);
            try {
                tClunk.waitForAnswer(mTimeout);
            } catch (Exception e) {
                throw new IOException(e);
            }

            mFID = StyxMessage.NOFID;
            mStat = null;
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }

    private StyxStat[] listStat() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        if (!isDirectory())
            return new StyxStat[0];
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        InputStream is = null;
        ArrayList<StyxStat> stats = new ArrayList<StyxStat>();
        try
        {
            is = new StyxFileBufferedInputStream(mMessenger, tempFID, iounit, mRecepient);
            StyxDataInputStream sis = new StyxDataInputStream(is);
            while (true) {
                StyxStat stat = new StyxStat(sis);
                stats.add(stat);
            }
        } catch (EOFException e) {
            // That's ok
        } finally {
            // TODO ???
//            mRecepient.getPolls().getFIDPoll().release(mFID);
        }
        close();
        return stats.toArray(new StyxStat[0]);
    }

    public long getCloneFID() throws InterruptedException, StyxException, TimeoutException, IOException {
        return sendWalkMessage(getFID(), "");
    }

    public String[] list() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat[] stats = listStat();
        int count = stats.length;
        String [] result = new String[count];
        for ( int i = 0; i < count; i++ ) {
            result[i] = stats[i].getName();
        }
        return result;
    }

    public String[] list(StyxFilenameFilter filter) throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat[] stats = listStat();

        ArrayList<String> strings = new ArrayList<String>();
        for (StyxStat stat : stats)
            if (filter.accept(this, stat.getName()))
                strings.add(stat.getName());

        return strings.toArray(new String[0]);
    }

    public StyxFile[] listFiles() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat[] stats = listStat();

        ArrayList<StyxFile> files = new ArrayList<StyxFile>();
        for (StyxStat stat : stats)
            files.add(new StyxFile(mClient, stat.getName(), this));

        return files.toArray(new StyxFile[0]);
    }

    public StyxFile[] listFiles(StyxFilenameFilter filter)
            throws StyxException, InterruptedException, TimeoutException, IOException
            {
        StyxStat[] stats = listStat();

        ArrayList<StyxFile> files = new ArrayList<StyxFile>();
        for (StyxStat stat : stats)
            if (filter.accept(this, stat.getName()))
                files.add(new StyxFile(mClient, stat.getName(), this));

        return files.toArray(new StyxFile[0]);
            }

    public StyxFile[] listFiles(StyxFileFilter filter) throws StyxException, InterruptedException, TimeoutException,
            IOException {
        StyxStat[] stats = listStat();

        ArrayList<StyxFile> files = new ArrayList<StyxFile>();
        for (StyxStat stat : stats)
        {
            StyxFile file = new StyxFile(mClient, stat.getName(), this);
            if (filter.accept(file))
                files.add(file);
        }

        return files.toArray(new StyxFile[0]);
            }

    /**
     * Open input stream to this file.
     * @return input stream
     * @throws InterruptedException
     * @throws StyxException
     * @throws TimeoutException
     * @throws IOException
     */
    public StyxFileBufferedInputStream openForRead()
            throws InterruptedException, StyxException, TimeoutException, IOException {
        checkConnection();
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        return new StyxFileBufferedInputStream(mMessenger, tempFID, iounit, mRecepient);
    }

    private void checkConnection() throws IOException {
        if ( !mClient.isConnected() ) {
            throw new IOException("Not connected to server");
        }
    }

    /**
     * Get unbuffered input stream to this file.
     * @return unbuffered input stream
     */
    public InputStream openForReadUnbuffered() throws IOException, InterruptedException, TimeoutException, StyxException {
        checkConnection();
        long tempFID = getCloneFID();
        int iounit = open(ModeType.OREAD, tempFID);
        return new StyxUnbufferedInputStream(tempFID, mMessenger, iounit, mRecepient);
    }


    /**
     * Open both streams - input and output.
     */
    public DualStreams openForReadAndWrite() throws InterruptedException, StyxException, TimeoutException, IOException {
        return new DualStreams(openForRead(), openForWrite());
    }

    public OutputStream openForWrite()
            throws InterruptedException, StyxException, TimeoutException, IOException {
        checkConnection();
        long clonedFID = getCloneFID();
        int iounit = open(ModeType.OWRITE, clonedFID);
        return new BufferedOutputStream(new StyxUnbufferedOutputStream(clonedFID, mMessenger, mRecepient), iounit);
    }

    public OutputStream openForWriteUnbuffered()
            throws InterruptedException, StyxException, TimeoutException, IOException {
        checkConnection();
        long clonedFID = getCloneFID();
        int iounit = open(ModeType.OWRITE, clonedFID);
        return new StyxUnbufferedOutputStream(clonedFID, mMessenger, mRecepient);
    }


    public void create(long permissions)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        checkConnection();
        // reserve FID
        long tempFID = sendWalkMessage(mParentFID, "");
        final StyxTCreateMessage tCreate =
                new StyxTCreateMessage(tempFID, mPath, permissions, ModeType.OREAD);
        mMessenger.sendMessage(tCreate, mRecepient);
        tCreate.waitForAnswer(mTimeout);

        // close temp FID
        mRecepient.getPolls().getFIDPoll().release(tempFID);
    }

    public static boolean exists(IClient manager, String fileName)
            throws InterruptedException, StyxException, TimeoutException, IOException
            {
        StyxFile file = new StyxFile(manager, fileName);
        return file.exists();
            }

    public boolean exists() throws InterruptedException, StyxException, TimeoutException, IOException
    {
        try
        {
            long fid = getFID();
            return (fid != StyxMessage.NOFID);
        } catch (StyxErrorMessageException e)
        {
            return false;
        }
    }

    /**
     * Delete file or empty folder
     * @throws InterruptedException
     * @throws StyxException
     * @throws TimeoutException
     * @throws IOException
     */
    public void delete()
            throws InterruptedException, StyxException, TimeoutException, IOException {
        delete(false);
    }

    /**
     * Delete file or folder
     * @param recurse Recursive delete
     * @throws InterruptedException
     * @throws StyxException
     * @throws TimeoutException
     * @throws IOException
     */
    public void delete(boolean recurse)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        if (recurse && this.isDirectory()) {
            StyxFile[] files = listFiles();
            for (StyxFile file : files)
                file.delete(true);
        }
        long fid = getFID();
        mFID = StyxMessage.NOFID;
        StyxTMessageFID tRemove = new StyxTMessageFID(MessageType.Tremove, MessageType.Rremove, fid);
        mMessenger.sendMessage(tRemove, mRecepient);
        StyxMessage rMessage = tRemove.waitForAnswer(mTimeout);
    }

    public static void delete(IClient manager, String fileName, boolean recurse)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        final StyxFile file = new StyxFile(manager, fileName);
        file.delete(recurse);
    }

    public void renameTo(String name)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        StyxStat stat = getStat();
        stat.setName(name);
        StyxTWStatMessage tWStat = new StyxTWStatMessage(getFID(), stat);
        mMessenger.sendMessage(tWStat, mRecepient);
        StyxMessage rMessage = tWStat.waitForAnswer(mTimeout);
    }

    public void mkdir(long permissions) throws InterruptedException, StyxException, TimeoutException, IOException
    {
        permissions = FileMode.getPermissionsByMode(permissions)
                | FileMode.Directory.getMode();

        StyxTCreateMessage tCreate = new StyxTCreateMessage(mParentFID, getName(), permissions, ModeType.OREAD);
        mMessenger.sendMessage(tCreate, mRecepient);
        StyxMessage rMessage = tCreate.waitForAnswer(mTimeout);
    }

    public boolean checkFileMode(FileMode mode) throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat stat = getStat();
        return mode.check(stat.getMode());
    }

    public boolean isDirectory() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.Directory);
    }

    public boolean isAppendOnly() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.AppendOnly);
    }

    public boolean isExclusiveUse() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ExclusiveUse);
    }

    public boolean isMountedChannel() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.MountedChannel);
    }

    public boolean isAuthenticationFile() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.AuthenticationFile);
    }

    public boolean isTemporaryFile() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.TemporaryFile);
    }

    public boolean isReadOwner() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ReadOwnerPermission);
    }

    public boolean isWriteOwner() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.WriteOwnerPermission);
    }

    public boolean isExecuteOwner() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ExecuteOwnerPermission);
    }

    public boolean isReadGroup() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ReadGroupPermission);
    }

    public boolean isWriteGroup() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.WriteGroupPermission);
    }

    public boolean isExecuteGroup() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ExecuteGroupPermission);
    }

    public boolean isReadOthers() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ReadOthersPermission);
    }

    public boolean isWriteOthers() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.WriteOthersPermission);
    }

    public boolean isExecuteOthers() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        return checkFileMode(FileMode.ExecuteOthersPermission);
    }

    public Date getAccessTime() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat stat = getStat();
        return stat.getAccessTime();
    }

    public Date getModificationDate() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat stat = getStat();
        return stat.getModificationTime();
    }

    public ULong getLength() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat stat = getStat();
        return stat.getLength();
    }

    public String getName() throws StyxException, InterruptedException
    {
        //StyxStat stat = getStat();
        //return stat.getName();
        StringBuilder builder = new StringBuilder(getPath());
        while (builder.toString().startsWith(SEPARATOR))
            builder.delete(0, 1);
        while (builder.toString().endsWith(SEPARATOR))
            builder.delete(builder.length() - 1, builder.length());

        int index = builder.toString().lastIndexOf(SEPARATOR);
        if (index < 0)
            return builder.toString();

        builder.delete(0, index);
        return builder.toString();
    }

    public String getUserName() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat stat = getStat();
        return stat.getUserName();
    }

    public String getModificationUser() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        StyxStat stat = getStat();
        return stat.getModificationUser();
    }

    // TODO this method is wrong, it should process parent fid and file name separately
    private long sendWalkMessage(long parentFID, String path)
            throws StyxException, InterruptedException, TimeoutException, IOException {
        long newFID = mRecepient.getPolls().getFIDPoll().getFreeItem();
        final StyxTWalkMessage tWalk = new StyxTWalkMessage(parentFID,
                newFID, path);
        mMessenger.sendMessage(tWalk, mRecepient);
        final StyxMessage rWalk = tWalk.waitForAnswer(mTimeout);
        StyxErrorMessageException.doException(rWalk, mPath);
        if ( ((StyxRWalkMessage)rWalk).getQIDListLength() != tWalk.getPathLength())
            throw new FileNotFoundException("File not found "+mPath);
        return newFID;
    }

    /**
     * Return stat info of this file
     * @return return stat structure of the current file.
     * @throws StyxException
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws IOException
     */
    private StyxStat getStat() throws StyxException, InterruptedException, TimeoutException, IOException
    {
        if (mStat == null) {
            StyxTMessageFID tStat = new StyxTMessageFID(MessageType.Tstat, MessageType.Rstat, getFID());
            mMessenger.sendMessage(tStat, mRecepient);
            StyxMessage rMessage = tStat.waitForAnswer(mTimeout);
            mStat = ((StyxRStatMessage) rMessage).getStat();
        }
        return mStat;
    }

    public IClient getIClient() {
        return mClient;
    }

    public long getTimeout() {return mTimeout;}

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }
}
