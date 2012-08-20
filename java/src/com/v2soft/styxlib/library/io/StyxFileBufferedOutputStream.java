package com.v2soft.styxlib.library.io;

import java.io.BufferedOutputStream;

import org.apache.mina.core.session.IoSession;


/**
 * 
 * @author V.Shcryabets<vshcryabets@gmail.com>
 *
 */
public class StyxFileBufferedOutputStream extends BufferedOutputStream {
    public StyxFileBufferedOutputStream(IoSession messenger, long file, int iounit) {
        super(new StyxUnbufferedOutputStream(file, messenger), iounit);
    }
}
