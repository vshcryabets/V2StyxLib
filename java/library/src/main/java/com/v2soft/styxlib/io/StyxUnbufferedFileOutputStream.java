package com.v2soft.styxlib.io;

import com.v2soft.styxlib.IClient;
import com.v2soft.styxlib.StyxFile;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.messages.base.StyxMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by vshcryabets on 12/17/14.
 */
public class StyxUnbufferedFileOutputStream extends StyxUnbufferedOutputStream {
    protected StyxFile mFile;
    protected IClient mConnection;

    public StyxUnbufferedFileOutputStream(IClient connection, String filename)
            throws InterruptedException, StyxException, TimeoutException, IOException {
        super(StyxMessage.NOFID, connection.getMessenger(), connection.getRecepient());
        mFile = new StyxFile(connection, filename);
        mFID = mFile.getCloneFID();
        mConnection = connection;
    }

    @Override
    public void close() throws IOException {
        super.close();
        mFile.close();
    }

    public IClient getIClient() {
        return mConnection;
    }
}
