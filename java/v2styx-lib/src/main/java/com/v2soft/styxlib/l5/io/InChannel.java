package com.v2soft.styxlib.l5.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface InChannel {
    int read(ByteBuffer dst) throws IOException;
}
