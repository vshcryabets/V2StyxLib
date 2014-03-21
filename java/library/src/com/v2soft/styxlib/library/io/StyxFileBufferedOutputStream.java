package com.v2soft.styxlib.library.io;

import java.io.BufferedOutputStream;

import com.v2soft.styxlib.library.core.Messenger;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxFileBufferedOutputStream extends BufferedOutputStream {
    public StyxFileBufferedOutputStream(Messenger messenger, long fid, int iounit) {
        super(new StyxUnbufferedOutputStream(fid, messenger), iounit);
    }
}
