package com.v2soft.styxlib.library.io;

import java.io.BufferedInputStream;

import com.v2soft.styxlib.library.core.Messenger;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxFileBufferedInputStream extends BufferedInputStream {
    /**
     * 
     * @param messenger
     * @param file
     * @param iounit
     * @param autocloseFile close StyxFile object with this stream
     */
    public StyxFileBufferedInputStream(Messenger messenger, 
            long file, 
            int iounit) {
        super(new StyxUnbufferedInputStream(file, messenger, iounit), iounit);
    }
}
