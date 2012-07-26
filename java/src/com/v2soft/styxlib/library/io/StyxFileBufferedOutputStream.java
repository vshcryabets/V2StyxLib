package com.v2soft.styxlib.library.io;

import java.io.BufferedOutputStream;

import org.apache.mina.core.session.IoSession;

import com.v2soft.styxlib.library.StyxFile;

/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxFileBufferedOutputStream extends BufferedOutputStream {
    public StyxFileBufferedOutputStream(IoSession messenger, StyxFile file, int iounit) {
        super(new StyxUnbufferedOutputStream(file, messenger), iounit);
    }
}
