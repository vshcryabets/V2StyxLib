package com.v2soft.styxlib.library.io;

import java.io.BufferedInputStream;

import com.v2soft.styxlib.library.StyxFile;
import com.v2soft.styxlib.library.core.Messenger;

public class StyxFileBufferedInputStream extends BufferedInputStream {
    public StyxFileBufferedInputStream(Messenger messenger, StyxFile file, int iounit) {
        super(new StyxUnbufferedInputStream(file, messenger), iounit);
    }
}
