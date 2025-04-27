package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.TMessagesProcessor;
import com.v2soft.styxlib.l5.serialization.IDataDeserializer;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.l6.vfs.IVirtualStyxFile;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

/**
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxServerManager
        implements Closeable {
    public static class Configuration {
        IVirtualStyxFile root;
        List<IChannelDriver> drivers;
        ClientsRepo clientsRepo;
        IDataDeserializer deserializer;
        IDataSerializer serializer;
        int iounit;
        public Configuration(IVirtualStyxFile root,
                             List<IChannelDriver> drivers,
                             ClientsRepo clientsRepo,
                             IDataSerializer serializer,
                             IDataDeserializer deserializer,
                             int iounit) {
            this.root = root;
            this.drivers = drivers;
            this.clientsRepo = clientsRepo;
            this.serializer = serializer;
            this.deserializer = deserializer;
            this.iounit = iounit;
        }

        public ClientsRepo getClientsRepo() {
            return clientsRepo;
        }

        public IDataSerializer getSerializer() {
            return serializer;
        }
        public IDataDeserializer getDeserializer() {
            return deserializer;
        }

        public IVirtualStyxFile getRoot() {
            return root;
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
        mBalancer = new TMessagesProcessor(details, configuration.root, configuration.clientsRepo);
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

    //-------------------------------------------------------------------------------------
    // Getters
    //-------------------------------------------------------------------------------------
//    public IVirtualStyxFile getRoot() {
//        return mRoot;
//    }

    //-------------------------------------------------------------------------------------
    // Setters
    //-------------------------------------------------------------------------------------
//    public List<IChannelDriver> getDrivers() {
//        return mDrivers;
//    }

    public void joinThreads() throws InterruptedException {
        for (var thread : mDriverThreads) {
            thread.join();
        }
    }
}
