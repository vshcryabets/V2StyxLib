package com.v2soft.styxlib.library.io;

import java.io.BufferedOutputStream;

import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.core.StyxSessionHandler;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxFileBufferedOutputStream extends BufferedOutputStream {
    public StyxFileBufferedOutputStream(StyxSessionHandler messenger, StyxFile file, int iounit) {
        super(new StyxUnbufferedOutputStream(file, messenger), iounit);
    }
}
