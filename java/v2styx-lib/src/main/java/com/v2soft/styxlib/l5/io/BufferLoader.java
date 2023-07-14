package com.v2soft.styxlib.l5.io;

import java.io.IOException;

/**
 * Created by V.Shcryabets on 4/4/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface BufferLoader {

    int write(byte[] testBuffer, int offset, int length);
    int readFromChannelToBuffer(InChannel channel) throws IOException;
}
