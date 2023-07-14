package com.v2soft.styxlib.l5.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface OutChannel {
    int write(ByteBuffer dst) throws IOException;
}
