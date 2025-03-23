package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.l6.vfs.MemoryStyxFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class MD5StyxFile extends MemoryStyxFile {
    public static final String FILE_NAME = "md5file";

    protected HashMap<Integer, MessageDigest> mClientsMap = new HashMap<>();

    public MD5StyxFile() {
        super(FILE_NAME);
    }

    @Override
    public boolean open(int clientId, int mode)
            throws StyxException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            mClientsMap.put(clientId, md);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        return super.open(clientId, mode);
    }
    @Override
    public void close(int clientId) {
        mClientsMap.remove(clientId);
        super.close(clientId);
    }
    @Override
    public int write(int clientId, byte[] data, long offset)
            throws StyxErrorMessageException {
        if ( mClientsMap.containsKey(clientId) ) {
            mClientsMap.get(clientId).update(data, 0, data.length);
        }
        return super.write(clientId, data, offset);
    }
    @Override
    public int read(int clientId, byte[] outbuffer, long offset, int count)
            throws StyxErrorMessageException {
        if ( mClientsMap.containsKey(clientId) ) {
            byte[] digest = mClientsMap.get(clientId).digest();
            if (count < digest.length) {
                return 0;
            } else {
                System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                return digest.length;
            }
        }
        return super.read(clientId, outbuffer, offset, count);
    }
}
