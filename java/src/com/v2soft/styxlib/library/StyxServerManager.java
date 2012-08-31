package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.v2soft.styxlib.library.core.StyxServerCodecFactory;
import com.v2soft.styxlib.library.server.ClientBalancer;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

/**
 * 
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 *
 */
public class StyxServerManager 
implements Closeable {
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 5000;
    //---------------------------------------------------------------------------
    // Class fields
    //---------------------------------------------------------------------------
    private int mPort;
    private int mTimeout = DEFAULT_TIMEOUT;
    private boolean mSSL;
    private int mIOBufSize = 8192;
    private ClientBalancer mBalancer;
    private IoAcceptor mSocketAcceptor;

    public StyxServerManager(InetAddress address, int port, boolean ssl, 
            IVirtualStyxFile root, String protocol) throws IOException {
        mPort = port;
        mSocketAcceptor = new NioSocketAcceptor();
        mSocketAcceptor.getSessionConfig().setReadBufferSize(mIOBufSize);
        mSocketAcceptor.getFilterChain().addLast("codec", 
                new ProtocolCodecFilter(
                        new StyxServerCodecFactory(mIOBufSize)));
        
        mBalancer = new ClientBalancer(mIOBufSize, root, protocol);
        //mAcceptor = new ConnectionAcceptor(channel, mBalancer);
        
        mSocketAcceptor.setHandler(mBalancer);
        mSocketAcceptor.getSessionConfig().setReadBufferSize(mIOBufSize);
//        mSocketAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, mTimeout);
        mSocketAcceptor.bind(new InetSocketAddress(port));
    }

    public void start() {
    }

    public long getIOBufSize()
    {
        return mIOBufSize;
    }

    public int getPort()
    {
        return mPort;
    }

    public boolean isSSL()
    {
        return mSSL;
    }

    @Override
    public void close() {

    }
    //-------------------------------------------------------------------------------------
    // Getters
    //-------------------------------------------------------------------------------------
    public int getTimeout() {return mTimeout;}
    //-------------------------------------------------------------------------------------
    // Setters
    //-------------------------------------------------------------------------------------
    public void setTimeout(int mTimeout) {
        this.mTimeout = mTimeout;
    }
}
