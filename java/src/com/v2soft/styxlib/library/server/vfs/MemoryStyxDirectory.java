package com.v2soft.styxlib.library.server.vfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.v2soft.styxlib.library.core.StyxByteBuffer;
import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.messages.base.enums.ModeType;
import com.v2soft.styxlib.library.messages.base.enums.QIDType;
import com.v2soft.styxlib.library.messages.base.structs.StyxQID;
import com.v2soft.styxlib.library.messages.base.structs.StyxStat;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.types.ULong;

/**
 * In-Memory directory
 * @author vshcryabets@gmail.com
 *
 */
public class MemoryStyxDirectory 
	implements IVirtualStyxDirectory {
    private Map<ClientState, StyxByteBuffer> mBuffersMap;
	private List<IVirtualStyxFile> mFiles;
	private String mName;
	
	public MemoryStyxDirectory(String name) {
	    if ( name == null ) throw new NullPointerException("Name is null");
	    mName = name;
	    mFiles = new LinkedList<IVirtualStyxFile>();
	    mBuffersMap = new HashMap<ClientState, StyxByteBuffer>();
    }
	
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
		return mName;
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
    public IVirtualStyxFile walk(List<String> pathElements, List<StyxQID> qids) {
        if ( pathElements.size() < 1 ) {
            return this;
        } else {
            String filename = pathElements.get(0);
            for (IVirtualStyxFile file : mFiles) {
                if ( file.getName().equals(filename)) {
                    pathElements.remove(0);
                    qids.add(file.getQID());
                    if ( file instanceof IVirtualStyxDirectory ) {
                        return ((IVirtualStyxDirectory)file).walk(pathElements, qids);
                    } else {
                        return file;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean open(ClientState client, ModeType mode) throws IOException {
        boolean result = (mode == ModeType.OREAD);
        if ( result ) {
            // prepare binary structure of the directory
            int size = 0;
            final List<StyxStat> stats = new LinkedList<StyxStat>();
            for (IVirtualStyxFile file : mFiles) {
                final StyxStat stat = file.getStat();
                size += stat.getSize();
                stats.add(stat);
            }
            // allocate buffer
            StyxByteBuffer buffer = new StyxByteBuffer(ByteBuffer.allocateDirect(size));
            for (StyxStat state : stats) {
                state.writeBinaryTo(buffer);
            }
            mBuffersMap.put(client, buffer);
        }
        return result;
    }

    @Override
    public long read(ClientState client, byte[] outbuffer, ULong offset, long count) throws StyxErrorMessageException {
        if ( !mBuffersMap.containsKey(client)) StyxErrorMessageException.doException("This file isn't open");
        final ByteBuffer buffer = mBuffersMap.get(client).getBuffer();
        int boffset = buffer.limit();
        if ( offset.asLong() > boffset ) return 0;
        buffer.position((int) offset.asLong());
        int bleft = buffer.remaining();
        if ( count > bleft ) {
            count = bleft;
        }
        buffer.get(outbuffer, 0, (int) count);
        return count;
    }

    @Override
    public void close(ClientState client) {
        // remove buffer
        mBuffersMap.remove(client);
    }

    /**
     * Add child file
     * @param file
     */
    public void addFile(IVirtualStyxFile file) {
        mFiles.add(file);
    }
}
