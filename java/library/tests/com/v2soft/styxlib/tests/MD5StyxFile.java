package com.v2soft.styxlib.tests;

import com.v2soft.styxlib.library.exceptions.StyxErrorMessageException;
import com.v2soft.styxlib.library.server.ClientState;
import com.v2soft.styxlib.library.server.vfs.MemoryStyxFile;
import com.v2soft.styxlib.library.types.ULong;

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

    protected HashMap<ClientState, MessageDigest> mClientsMap = new HashMap<ClientState, MessageDigest>();

    public MD5StyxFile() {
        super(FILE_NAME);
    }

    @Override
    public boolean open(ClientState client, int mode)
            throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            mClientsMap.put(client, md);
        } catch (NoSuchAlgorithmException e) {
            return false;
        }
        return super.open(client, mode);
    }
    @Override
    public void close(ClientState client) {
        mClientsMap.remove(client);
        super.close(client);
    }
    @Override
    public int write(ClientState client, byte[] data, ULong offset)
            throws StyxErrorMessageException {
        if ( mClientsMap.containsKey(client) ) {
            mClientsMap.get(client).update(data, 0, data.length);
        }
        return super.write(client, data, offset);
    }
    @Override
    public long read(ClientState client, byte[] outbuffer, ULong offset, long count)
            throws StyxErrorMessageException {
        if ( mClientsMap.containsKey(client) ) {
            byte[] digest = mClientsMap.get(client).digest();
            if (count < digest.length) {
                return 0;
            } else {
                System.arraycopy(digest, 0, outbuffer, 0, digest.length);
                return digest.length;
            }
        }
        return super.read(client, outbuffer, offset, count);
    }
}
