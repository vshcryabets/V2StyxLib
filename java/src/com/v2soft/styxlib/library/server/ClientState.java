package com.v2soft.styxlib.library.server;

import java.nio.ByteBuffer;

public class ClientState {
    private ByteBuffer mBuffer;
    private int mPosition;
    
    public ClientState(int iounit) {
        mBuffer = ByteBuffer.allocateDirect(iounit);
    }

    public void process(ByteBuffer buffer, int readed) {
        mBuffer.put(buffer);
        // TODO Auto-generated method stub
        
    }

}
