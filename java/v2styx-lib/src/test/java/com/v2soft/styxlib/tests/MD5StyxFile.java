package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.exceptions.StyxErrorMessageException;
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

    protected HashMap<ClientDetails, MessageDigest> mClientsMap = new HashMap<ClientDetails, MessageDigest>();

    public MD5StyxFile() {
        super(FILE_NAME);
    }

    @Override
    public boolean open(ClientDetails clientDetails, int mode)
            throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            mClientsMap.put(clientDetails, md);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        return super.open(clientDetails, mode);
    }
    @Override
    public void close(ClientDetails clientDetails) {
        mClientsMap.remove(clientDetails);
        super.close(clientDetails);
    }
    @Override
    public int write(ClientDetails clientDetails, byte[] data, long offset)
            throws StyxErrorMessageException {
        if ( mClientsMap.containsKey(clientDetails) ) {
            mClientsMap.get(clientDetails).update(data, 0, data.length);
        }
        return super.write(clientDetails, data, offset);
    }
    @Override
    public long read(ClientDetails clientDetails, byte[] outbuffer, long offset, long count)
            throws StyxErrorMessageException {
        if ( mClientsMap.containsKey(clientDetails) ) {
            byte[] digest = mClientsMap.get(clientDetails).digest();
            if (count < digest.length) {
                return 0;
            } else {
                System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                return digest.length;
            }
        }
        return super.read(clientDetails, outbuffer, offset, count);
    }
}
