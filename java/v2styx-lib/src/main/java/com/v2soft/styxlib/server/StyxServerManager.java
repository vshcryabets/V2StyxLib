package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxServerManager
        implements Closeable {
    public static class Configuration {
        public final IVirtualStyxFile root;
        public final List<IChannelDriver<?>> drivers;
        public final StyxSessionDI di;
        public final int iounit;
        public Configuration(IVirtualStyxFile root,
                             List<IChannelDriver<?>> drivers,
                             StyxSessionDI di,
                             int iounit) {
            this.root = root;
            this.drivers = drivers;
            this.di = di;
            this.iounit = iounit;
        }
    }
    //---------------------------------------------------------------------------
    // Constants
    //---------------------------------------------------------------------------
    public static final String PROTOCOL = "9P2000";
    public static final int DEFAULT_TIMEOUT = 5000;
    public static final int DEFAULT_IOUNIT = 8192;
    //---------------------------------------------------------------------------
    // Class fields
    //---------------------------------------------------------------------------
    protected final TMessagesProcessor mBalancer;
    protected Thread[] mDriverThreads;
    protected Configuration mConfiguration;

    public StyxServerManager(Configuration configuration) {
        mConfiguration = configuration;
        var details = new ConnectionDetails(getProtocol(), getIOUnit());
        mBalancer = new TMessagesProcessor(details, configuration.root, configuration.di);
    }

    public void start() throws StyxException {
        int count = mConfiguration.drivers.size();
        mDriverThreads = new Thread[count];
        var configuration = new IChannelDriver.StartConfiguration(
                mBalancer,
                mBalancer
        );
        for (int i = 0; i < count; i++) {
            mDriverThreads[i] = mConfiguration.drivers.get(i).start(configuration);
        }
    }

    public int getIOUnit() {
        return mConfiguration.iounit;
    }

    @Override
    public void close() throws IOException {
        for (var driver : mConfiguration.drivers) {
            driver.close();
        }
        mBalancer.close();
    }

    public void closeAndWait() throws IOException, InterruptedException {
        close();
        joinThreads();
    }

    public String getProtocol() {
        return PROTOCOL;
    }

    public void joinThreads() throws InterruptedException {
        for (var thread : mDriverThreads) {
            thread.join();
        }
    }
}
