package com.v2soft.styxlib.library.server;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.v2soft.styxlib.library.messages.base.StyxMessage;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

public class ClientBalancer implements IoHandler {
    private String mProtocol;
    private int mIOUnit;
    private Map<IoSession, ClientState> mClientStatesMap;
    private IVirtualStyxFile mRoot;

    public ClientBalancer(int iounit, IVirtualStyxFile root, 
            String protocol) throws IOException {
        mIOUnit = iounit;
        mClientStatesMap = new HashMap<IoSession, ClientState>();
        mRoot = root;
        mProtocol = protocol;
    }

    @Override
    public void exceptionCaught(IoSession arg0, Throwable arg1)
            throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void messageReceived(IoSession arg0, Object arg1) throws Exception {
        if ( mClientStatesMap.containsKey(arg0)) {
            mClientStatesMap.get(arg0).processMessage((StyxMessage) arg1);
        }
    }

    @Override
    public void messageSent(IoSession arg0, Object arg1) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sessionClosed(IoSession arg0) throws Exception {
        // close client
        final ClientState state = mClientStatesMap.get(arg0);
        if ( state != null ) {
            state.close();
            mClientStatesMap.remove(arg0);
        }
    }

    @Override
    public void sessionCreated(IoSession arg0) throws Exception {
        // register new client
        final ClientState client = new ClientState(mIOUnit, mRoot, mProtocol, arg0);
        mClientStatesMap.put(arg0, client);
    }

    @Override
    public void sessionIdle(IoSession arg0, IdleStatus arg1) throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void sessionOpened(IoSession arg0) throws Exception {
        // TODO Auto-generated method stub
        
    }
}
