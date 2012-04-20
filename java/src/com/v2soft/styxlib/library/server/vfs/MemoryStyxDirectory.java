package com.v2soft.styxlib.library.server.vfs;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.types.ULong;

public class MemoryStyxDirectory 
	implements IVirtualStyxDirectory {
	private LinkedList<IVirtualStyxDirectory> subdirs;
	
	@Override
	public StyxQID getQID() {
		return new StyxQID(QIDType.QTDIR, 0, new ULong(this.hashCode()));
	}

	@Override
	public IVirtualStyxFile getFile(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IVirtualStyxDirectory getDirectory(String path) {
		if ( path.length() == 0 || path.equals("/")) return this;
		return null;
	}

	@Override
	public StyxStat getStat() {
		StyxStat result = new StyxStat((short)0, 
				1, 
				getQID(), 
				getMode(),
				getAccessTime(), 
				getModificationTime(), 
				getLength(), 
				getName(), 
				getOwnerName(), 
				getGroupName(), 
				getModificationUser());
		return result;
	}

	@Override
	public int getMode() {
		return 0x800001FF;
	}

	@Override
	public String getName() {
		return "memory";
	}

	@Override
	public Date getAccessTime() {
		return new Date();
	}

	@Override
	public Date getModificationTime() {
		return new Date();
	}

	@Override
	public ULong getLength() {
		return new ULong(0);
	}

	@Override
	public String getOwnerName() {
		return "nobody";
	}

	@Override
	public String getGroupName() {
		return "nobody";
	}

	@Override
	public String getModificationUser() {
		return "nobody";
	}

    @Override
    public IVirtualStyxFile walk(String path, List<StyxQID> qids) {
        if ( path.length() < 1 ) {
            qids.clear();
            return this;
        }
        return null;
    }
}
