package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxlib.library.core.Messenger;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.exceptions.StyxException;
import com.v2soft.styxlib.library.io.StyxInputStream;
import com.v2soft.styxlib.library.messages.StyxROpenMessage;
import com.v2soft.styxlib.library.messages.StyxRStatMessage;
import com.v2soft.styxlib.library.messages.StyxRWalkMessage;
import com.v2soft.styxlib.library.messages.StyxTCreateMessage;
import com.v2soft.styxlib.library.messages.StyxTOpenMessage;
import com.v2soft.styxlib.library.messages.StyxTRemoveMessage;
import com.v2soft.styxlib.library.messages.StyxTStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWStatMessage;
import com.v2soft.styxlib.library.messages.StyxTWalkMessage;
import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.messages.base.enums.FileMode;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.types.ULong;

public class StyxFile implements Closeable {
	public static final String SEPARATOR = "/";
	
	private StyxClientManager mManager;
	private long mFID = StyxMessage.NOFID;
	private long mParentFID = StyxMessage.NOFID;
	private StyxStat mStat;
	private String mPath;
	private Messenger mMessenger;
	private long mTimeout = StyxClientManager.DEFAULT_TIMEOUT;
	
	public StyxFile(StyxClientManager manager) throws StyxException, TimeoutException, IOException, InterruptedException {
		this(manager, null);
	}
	
	public StyxFile(StyxClientManager manager, String path) throws StyxException, TimeoutException, IOException, InterruptedException {
		this(manager, path, null);
	}
	
	public StyxFile(StyxClientManager manager, String path, StyxFile parent) 
	        throws StyxException, TimeoutException, IOException, InterruptedException {
	    if ( !manager.isConnected() )
	        throw new IOException("Styx connection wasn't established");
		mManager = manager;
		mMessenger = mManager.getMessenger();
		mTimeout = mManager.getTimeout();
		if ( parent != null ) {
			mPath = combinePath(parent, path);
			mParentFID = parent.getFID();
		} else {
			mPath = path;
			mParentFID = manager.getFID();
		}
	}
	
	public StyxFile(StyxClientManager manager, long fid) throws IOException {
        if ( !manager.isConnected() )
            throw new IOException("Styx connection wasn't established");
		mManager = manager;
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
	
	public long getFID() throws StyxException, TimeoutException, IOException, InterruptedException
	{
		if (mFID == StyxMessage.NOFID)
			sendWalkMessage(mPath);
		return mFID;
	}
	
	private int open(ModeType mode) 
	        throws StyxException, InterruptedException, TimeoutException, IOException {
		StyxTOpenMessage tOpen = new StyxTOpenMessage(getFID(), mode);
		
		mMessenger.send(tOpen);
		StyxMessage rMessage = tOpen.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
		
		StyxROpenMessage rOpen = (StyxROpenMessage) rMessage;
		return (int)rOpen.getIOUnit();
	}
	
	@Override
	public void close() throws IOException
	{
		if (mFID == StyxMessage.NOFID) return;
		try {
			mManager.clunk(mFID);
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
		int iounit = open(ModeType.OREAD);
		
		ArrayList<StyxStat> stats = new ArrayList<StyxStat>();
		try
		{
			StyxFileInputStream is = new StyxFileInputStream(mManager, this, iounit);
			StyxInputStream sis = new StyxInputStream(is);
			while (true) {
				StyxStat stat = new StyxStat(sis);
				stats.add(stat);
			}
		} catch (EOFException e) {
            // That's ok
        } catch (IOException e) {
//            e.printStackTrace();
        } 
		
		close();
		return stats.toArray(new StyxStat[0]);
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
			files.add(new StyxFile(mManager, stat.getName(), this));
		
		return files.toArray(new StyxFile[0]);
	}
	
	public StyxFile[] listFiles(StyxFilenameFilter filter)
		throws StyxException, InterruptedException, TimeoutException, IOException
	{
		StyxStat[] stats = listStat();
		
		ArrayList<StyxFile> files = new ArrayList<StyxFile>();
		for (StyxStat stat : stats)
			if (filter.accept(this, stat.getName()))
				files.add(new StyxFile(mManager, stat.getName(), this));
		
		return files.toArray(new StyxFile[0]);
	}
	
	public StyxFile[] listFiles(StyxFileFilter filter)
		throws StyxException, InterruptedException, TimeoutException, IOException
	{
		StyxStat[] stats = listStat();
		
		ArrayList<StyxFile> files = new ArrayList<StyxFile>();
		for (StyxStat stat : stats)
		{
			StyxFile file = new StyxFile(mManager, stat.getName(), this);
			if (filter.accept(file))
				files.add(file);
		}
		
		return files.toArray(new StyxFile[0]);
	}
	
	public StyxFileInputStream openForRead() 
	        throws InterruptedException, StyxException, TimeoutException, IOException {
        if ( !mManager.isConnected()) {
            throw new IOException("Not connected to server");
        }
		int iounit = open(ModeType.OREAD);
		return new StyxFileInputStream(mManager, this, iounit);
	}
	
	public StyxFileOutputStream openForWrite() 
	        throws InterruptedException, StyxException, TimeoutException, IOException {
	    if ( !mManager.isConnected()) {
	        throw new IOException("Not connected to server");
	    }
		int iounit = open(ModeType.OWRITE);
		return new StyxFileOutputStream(mManager, this, iounit);
	}
	
	public OutputStream create(long permissions) 
	        throws InterruptedException, StyxException, TimeoutException, IOException {
		StyxTCreateMessage tCreate = new StyxTCreateMessage(mParentFID, getName(), permissions, ModeType.OWRITE);
		
		mMessenger.send(tCreate);
		StyxMessage rMessage = tCreate.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
		
		return openForWrite();
	}
	
	public static boolean exists(StyxClientManager manager, String fileName) 
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
	
	public void delete() throws InterruptedException, StyxException, TimeoutException, IOException {
		delete(false);
	}
	
	public void delete(boolean recurse) throws InterruptedException, StyxException, TimeoutException, IOException
	{
		if (recurse && this.isDirectory())
		{
			StyxFile[] files = listFiles();
			for (StyxFile file : files)
				file.delete(recurse);
		}
		
		StyxTRemoveMessage tRemove = new StyxTRemoveMessage(getFID());
		
		mMessenger.send(tRemove);
		StyxMessage rMessage = tRemove.waitForAnswer(mTimeout);
		close();
		StyxErrorMessageException.doException(rMessage);
	}
	
	public static void delete(StyxClientManager manager, String fileName)
		throws InterruptedException, StyxException, TimeoutException, IOException {
		StyxFile file = new StyxFile(manager, fileName);
		file.delete();
	}
	
	public static void delete(StyxClientManager manager, String fileName, boolean recurse)
		throws InterruptedException, StyxException, TimeoutException, IOException {
		StyxFile file = new StyxFile(manager, fileName);
		file.delete(recurse);
	}
	
	public void renameTo(String name) 
	        throws InterruptedException, StyxException, TimeoutException, IOException {
		StyxStat stat = getStat();
		stat.setName(name);
		StyxTWStatMessage tWStat = new StyxTWStatMessage(getFID(), stat);
		mMessenger.send(tWStat);
		StyxMessage rMessage = tWStat.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
	}
	
	public void mkdir(long permissions) throws InterruptedException, StyxException, TimeoutException, IOException
	{
		permissions = FileMode.getPermissionsByMode(permissions)
			| FileMode.Directory.getMode();
		
		StyxTCreateMessage tCreate = new StyxTCreateMessage(mParentFID, getName(), permissions, ModeType.OREAD);
		
		mMessenger.send(tCreate);
		StyxMessage rMessage = tCreate.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
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
	
	private void sendWalkMessage(String path) 
		throws StyxException, InterruptedException, TimeoutException, IOException {
		long newFID = mManager.getActiveFids().getFreeFid();
		StyxTWalkMessage tWalk = new StyxTWalkMessage(mManager.getFID(),
				newFID);
		tWalk.setPath(path);
		mMessenger.send(tWalk);
		StyxMessage rWalk = tWalk.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rWalk, mPath);
		if ( ((StyxRWalkMessage)rWalk).getQIDListLength() != tWalk.getPathLength())
			throw new FileNotFoundException("File not found "+mPath);
		
		mFID = newFID;
	}
		
	/**
	 * Return stat info of this file
	 * @return
	 * @throws StyxException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 * @throws IOException 
	 */
	private StyxStat getStat() throws StyxException, InterruptedException, TimeoutException, IOException
	{
		if (mStat == null) {
			StyxTStatMessage tStat = new StyxTStatMessage(getFID());
			mMessenger.send(tStat);
			StyxMessage rMessage = tStat.waitForAnswer(mTimeout);
			StyxErrorMessageException.doException(rMessage);
			
			mStat = ((StyxRStatMessage) rMessage).getStat();
		}
		return mStat;
	}
	
    public long getTimeout() {return mTimeout;}

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }
	
}
