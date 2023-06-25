package com.v2soft.styxlib.l6.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by V.Shcryabets on 3/31/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class DualStreams implements Closeable {
    public InputStream input;
    public OutputStream output;

    public DualStreams(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void close() throws IOException {
        input.close();
        output.close();
    }
}
