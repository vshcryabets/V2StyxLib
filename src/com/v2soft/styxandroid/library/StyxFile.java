package com.v2soft.styxandroid.library;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import com.v2soft.styxandroid.library.core.Messenger;
import com.v2soft.styxandroid.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxandroid.library.exceptions.StyxException;
import com.v2soft.styxandroid.library.io.StyxInputStream;
import com.v2soft.styxandroid.library.messages.StyxROpenMessage;
import com.v2soft.styxandroid.library.messages.StyxRStatMessage;
import com.v2soft.styxandroid.library.messages.StyxTCreateMessage;
import com.v2soft.styxandroid.library.messages.StyxTOpenMessage;
import com.v2soft.styxandroid.library.messages.StyxTRemoveMessage;
import com.v2soft.styxandroid.library.messages.StyxTStatMessage;
import com.v2soft.styxandroid.library.messages.StyxTWStatMessage;
import com.v2soft.styxandroid.library.messages.StyxTWalkMessage;
import com.v2soft.styxandroid.library.messages.base.StyxMessage;
import com.v2soft.styxandroid.library.messages.base.enums.FileMode;
import com.v2soft.styxandroid.library.messages.base.enums.ModeType;
import com.v2soft.styxandroid.library.messages.base.structs.StyxStat;
import com.v2soft.styxandroid.library.types.ULong;

public class StyxFile {
	public static final String SEPARATOR = "/";
	
	private StyxClientManager mManager;
	private long mFID = StyxMessage.NOFID;
	private StyxStat mStat;
	private String mPath;
	private StyxFile mParent;
	private long mTimeout = StyxClientManager.DEFAULT_TIMEOUT;
	
	public StyxFile(StyxClientManager manager)
	{
		mManager = manager;
		mPath = null;
		//sendWalkMessage(null, null);
	}
	
	public StyxFile(StyxClientManager manager, String path)
	{
		mManager = manager;
		mPath = path;
		//sendWalkMessage(path, null);
	}
	
	public StyxFile(StyxFile parent, String path)
	{
		mManager = parent.getManager();
		mPath = combinePath(parent, path);
		mParent = parent;
		//sendWalkMessage(path, parent);
	}
	
	public StyxFile(StyxClientManager manager, long fid)
	{
		mManager = manager;
		mPath = null;
		mFID = fid;
	}
	
	private StyxFile(StyxFile parent, StyxStat stat)
	{
		mManager = parent.getManager();
		mStat = stat;
		mPath = combinePath(parent, stat.getName());
		mParent = parent;
		//sendWalkMessage(stat.getName(), parent);
	}
	
	private String combinePath(StyxFile parent, String path)
	{
		if (parent.mPath == null)
			return SEPARATOR + path;
		return parent.mPath + SEPARATOR + path;
	}
	
	public StyxClientManager getManager()
	{
		return mManager;
	}
	
	public StyxFile getParent()
	{
		if (mParent == null)
			mParent = createParent();
		return mParent;
	}
	
	private StyxFile createParent() 
	{
		StringBuilder builder = new StringBuilder(getPath());
		while (builder.toString().startsWith(SEPARATOR))
			builder.delete(0, 1);
		while (builder.toString().endsWith(SEPARATOR))
			builder.delete(builder.length() - 1, builder.length());
		
		int index = builder.toString().lastIndexOf(SEPARATOR);
		if (index < 0)
			return null;
		
		builder.delete(index, builder.length());
		return new StyxFile(getManager(), builder.toString());
	}
	
	public String getPath()
	{
		if (mPath == null)
			return "/";
		return mPath;
	}
	
	public long getFID() throws InterruptedException, StyxException, TimeoutException
	{
		if (mFID == StyxMessage.NOFID)
			sendWalkMessage(mPath);
		return mFID;
	}
	
	private int open(ModeType mode) 
	        throws StyxException, InterruptedException, TimeoutException {
		StyxClientManager manager = this.getManager();
		StyxTOpenMessage tOpen = new StyxTOpenMessage(manager.getActiveTags().getTag(), 
				getFID(), mode);
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tOpen);
		StyxMessage rMessage = tOpen.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
		
		StyxROpenMessage rOpen = (StyxROpenMessage) rMessage;
		return (int)rOpen.getIOUnit();
	}
	
	private void close() throws InterruptedException, StyxException, TimeoutException
	{
		StyxClientManager manager = this.getManager();
		manager.clunk(getFID());
		doClunk(false);
	}
	
	private void doClunk(boolean release)
	{
		if (mFID == StyxMessage.NOFID)
			return;
		
		StyxClientManager manager = getManager();
		if (release)
			manager.getActiveFids().releaseFid(mFID);
		mFID = StyxMessage.NOFID;
		mStat = null;
	}
	
	private StyxStat[] listStat() throws StyxException, InterruptedException, TimeoutException
	{
		if (!isDirectory())
			return new StyxStat[0];
		int iounit = open(ModeType.OREAD);
		
		ArrayList<StyxStat> stats = new ArrayList<StyxStat>();
		try
		{
			StyxFileInputStream is = new StyxFileInputStream(this, iounit);
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
	
	public String[] list() throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat[] stats = listStat();
		int count = stats.length;
		System.out.println("Count="+count);
		String [] result = new String[count];
		for ( int i = 0; i < count; i++ ) {
		    result[i] = stats[i].getName();
		}
		return result;
	}
	
	public String[] list(StyxFilenameFilter filter) throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat[] stats = listStat();
		
		ArrayList<String> strings = new ArrayList<String>();
		for (StyxStat stat : stats)
			if (filter.accept(this, stat.getName()))
				strings.add(stat.getName());
		
		return strings.toArray(new String[0]);
	}
	
	public StyxFile[] listFiles() throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat[] stats = listStat();
		
		ArrayList<StyxFile> files = new ArrayList<StyxFile>();
		for (StyxStat stat : stats)
			files.add(new StyxFile(this, stat));
		
		return files.toArray(new StyxFile[0]);
	}
	
	public StyxFile[] listFiles(StyxFilenameFilter filter)
		throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat[] stats = listStat();
		
		ArrayList<StyxFile> files = new ArrayList<StyxFile>();
		for (StyxStat stat : stats)
			if (filter.accept(this, stat.getName()))
				files.add(new StyxFile(this, stat.getName()));
		
		return files.toArray(new StyxFile[0]);
	}
	
	public StyxFile[] listFiles(StyxFileFilter filter)
		throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat[] stats = listStat();
		
		ArrayList<StyxFile> files = new ArrayList<StyxFile>();
		for (StyxStat stat : stats)
		{
			StyxFile file = new StyxFile(this, stat);
			if (filter.accept(file))
				files.add(file);
		}
		
		return files.toArray(new StyxFile[0]);
	}
	
	public InputStream openForRead() throws InterruptedException, StyxException, TimeoutException
	{
		int iounit = open(ModeType.OREAD);
		return new StyxFileInputStream(this, iounit);
	}
	
	public OutputStream openForWrite() throws InterruptedException, StyxException, TimeoutException
	{
		int iounit = open(ModeType.OWRITE);
		return new StyxFileOutputStream(this, iounit);
	}
	
	public OutputStream create(long permissions) 
	        throws InterruptedException, StyxException, TimeoutException {
		StyxClientManager manager = getManager();
		StyxFile parent = getParent();
		if (parent == null)
			parent = manager.getRoot();
		StyxTCreateMessage tCreate = new StyxTCreateMessage(manager.getActiveTags().getTag(),
				parent.getFID(), getName(), permissions, ModeType.OWRITE);
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tCreate);
		StyxMessage rMessage = tCreate.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
		
		return openForWrite();
	}
	
	public static boolean exists(StyxClientManager manager, String fileName) 
		throws InterruptedException, StyxException, TimeoutException
	{
		StyxFile file = new StyxFile(manager, fileName);
		return file.exists();
	}
	
	public boolean exists() throws InterruptedException, StyxException, TimeoutException
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
	
	public void delete() throws InterruptedException, StyxException, TimeoutException {
		delete(false);
	}
	
	public void delete(boolean recurse) throws InterruptedException, StyxException, TimeoutException
	{
		if (recurse && this.isDirectory())
		{
			StyxFile[] files = listFiles();
			for (StyxFile file : files)
				file.delete(recurse);
		}
		
		StyxClientManager manager = this.getManager();
		StyxTRemoveMessage tRemove = new StyxTRemoveMessage(manager.getActiveTags().getTag(), getFID());
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tRemove);
		StyxMessage rMessage = tRemove.waitForAnswer(mTimeout);
		doClunk(true);
		StyxErrorMessageException.doException(rMessage);
	}
	
	public static void delete(StyxClientManager manager, String fileName)
		throws InterruptedException, StyxException, TimeoutException {
		StyxFile file = new StyxFile(manager, fileName);
		file.delete();
	}
	
	public static void delete(StyxClientManager manager, String fileName, boolean recurse)
		throws InterruptedException, StyxException, TimeoutException {
		StyxFile file = new StyxFile(manager, fileName);
		file.delete(recurse);
	}
	
	public void renameTo(String name) throws InterruptedException, StyxException, TimeoutException
	{
		StyxStat stat = getStat();
		stat.setName(name);
		
		StyxClientManager manager = this.getManager();
		StyxTWStatMessage tWStat = new StyxTWStatMessage(manager.getActiveTags().getTag(),
				getFID(), stat);
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tWStat);
		StyxMessage rMessage = tWStat.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
	}
	
	public void mkdir(long permissions) throws InterruptedException, StyxException, TimeoutException
	{
		StyxFile parent = getParent();
		permissions = FileMode.getPermissionsByMode(permissions)
			| FileMode.Directory.getMode();
		
		StyxClientManager manager = getManager();
		if (parent == null)
			parent = manager.getRoot();
		StyxTCreateMessage tCreate = new StyxTCreateMessage(manager.getActiveTags().getTag(),
				parent.getFID(), getName(), permissions, ModeType.OREAD);
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tCreate);
		StyxMessage rMessage = tCreate.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
	}
	
	public void mkdirs(long permissions) throws InterruptedException, StyxException, TimeoutException
	{
		StyxFile parent = getParent();
		if (parent != null && !parent.exists())
			parent.mkdirs(permissions);
		mkdir(permissions);
	}
	
	public static void mkdirs(StyxClientManager manager, String path, long permissions)
		throws InterruptedException, StyxException, TimeoutException
	{
		StyxFile file = new StyxFile(manager, path);
		file.mkdirs(permissions);
	}
	
	public boolean checkFileMode(FileMode mode) throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat stat = getStat();
		return mode.check(stat.getMode());
	}
	
	public boolean isDirectory() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.Directory);
	}
	
	public boolean isAppendOnly() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.AppendOnly);
	}
	
	public boolean isExclusiveUse() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ExclusiveUse);
	}
	
	public boolean isMountedChannel() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.MountedChannel);
	}
	
	public boolean isAuthenticationFile() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.AuthenticationFile);
	}
	
	public boolean isTemporaryFile() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.TemporaryFile);
	}
	
	public boolean isReadOwner() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ReadOwnerPermission);
	}
	
	public boolean isWriteOwner() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.WriteOwnerPermission);
	}
	
	public boolean isExecuteOwner() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ExecuteOwnerPermission);
	}
	
	public boolean isReadGroup() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ReadGroupPermission);
	}
	
	public boolean isWriteGroup() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.WriteGroupPermission);
	}
	
	public boolean isExecuteGroup() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ExecuteGroupPermission);
	}
	
	public boolean isReadOthers() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ReadOthersPermission);
	}
	
	public boolean isWriteOthers() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.WriteOthersPermission);
	}
	
	public boolean isExecuteOthers() throws StyxException, InterruptedException, TimeoutException
	{
		return checkFileMode(FileMode.ExecuteOthersPermission);
	}
	
	public Date getAccessTime() throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat stat = getStat();
		return stat.getAccessTime();
	}
	
	public Date getModificationDate() throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat stat = getStat();
		return stat.getModificationTime();
	}
	
	public ULong getLength() throws StyxException, InterruptedException, TimeoutException
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
	
	public String getUserName() throws StyxException, InterruptedException, TimeoutException 
	{
		StyxStat stat = getStat();
		return stat.getUserName();
	}
	
	public String getModificationUser() throws StyxException, InterruptedException, TimeoutException
	{
		StyxStat stat = getStat();
		return stat.getModificationUser();
	}
	
	private void sendWalkMessage(String path) 
		throws StyxException, InterruptedException, TimeoutException
	{
		StyxClientManager manager = getManager();
		//manager.waitForAttached();
		
		long fid = manager.getActiveFids().getFid();
		StyxTWalkMessage tWalk = new StyxTWalkMessage(getManager()
				.getActiveTags().getTag(), manager.getFID(),
				fid);
		tWalk.setPath(path);
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tWalk);
		StyxMessage rWalk = tWalk.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rWalk);
		
		mFID = fid;
	}
		
	private StyxStat getStat() throws StyxException, InterruptedException, TimeoutException
	{
		if (mStat == null)
			mStat = retrieveStat();
		return mStat;
	}
	
	/**
	 * Return stat info of this file
	 * @return
	 * @throws StyxException
	 * @throws InterruptedException
	 * @throws TimeoutException
	 */
	private StyxStat retrieveStat() 
	        throws StyxException, InterruptedException, TimeoutException {
		StyxClientManager manager = getManager();
		StyxTStatMessage tStat = new StyxTStatMessage(manager.
				getActiveTags().getTag(), getFID());
		
		Messenger messenger = manager.getMessenger();
		messenger.send(tStat);
		StyxMessage rMessage = tStat.waitForAnswer(mTimeout);
		StyxErrorMessageException.doException(rMessage);
		
		StyxRStatMessage rStat = (StyxRStatMessage) rMessage;
		return rStat.getStat();
	}

    public long getTimeout() {
        return mTimeout;
    }

    public void setTimeout(long mTimeout) {
        this.mTimeout = mTimeout;
    }
	
}
