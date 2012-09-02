package com.v2soft.styxlib.library;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLContext;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.ssl.SslFilter;
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
    private SSLContext mSSL;
    private int mIOBufSize = 8192;
    private ClientBalancer mBalancer;
    private IoAcceptor mSocketAcceptor;

    public StyxServerManager(InetAddress address, int port, SSLContext ssl, 
            IVirtualStyxFile root, String protocol) throws IOException {
        mPort = port;
        mSSL = ssl;
        mSocketAcceptor = new NioSocketAcceptor();
        if ( mSSL != null ) {
            // enable SSL
            final DefaultIoFilterChainBuilder chain = mSocketAcceptor.getFilterChain();
            final SslFilter sslFilter = new SslFilter(mSSL);
            chain.addLast("sslFilter", sslFilter);
            System.out.println("SSL support is added..");
        }

        mSocketAcceptor.getSessionConfig().setReadBufferSize(mIOBufSize);
        mSocketAcceptor.getFilterChain().addLast("codec", 
                new ProtocolCodecFilter(
                        new StyxServerCodecFactory(mIOBufSize)));
        mBalancer = new ClientBalancer(mIOBufSize, root, protocol);
        mSocketAcceptor.setHandler(mBalancer);
        mSocketAcceptor.getSessionConfig().setReadBufferSize(mIOBufSize);
        //        mSocketAcceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, mTimeout);
        mSocketAcceptor.bind(new InetSocketAddress(port));
    }

    public void start() {
    }



    @Override
    public void close() {
        mSocketAcceptor.dispose(true);
    }
    //-------------------------------------------------------------------------------------
    // Getters
    //-------------------------------------------------------------------------------------
    public int getTimeout() {return mTimeout;}
    public long getIOBufSize() {return mIOBufSize;}
    public int getPort() {return mPort;}
    //-------------------------------------------------------------------------------------
    // Setters
    //-------------------------------------------------------------------------------------
    public void setTimeout(int mTimeout) {
        this.mTimeout = mTimeout;
    }
}
