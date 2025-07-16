package com.v2soft.styxlib.server;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.handlers.IMessageProcessor;
import com.v2soft.styxlib.handlers.IMessageTransmitter;
import com.v2soft.styxlib.utils.StyxSessionDI;

import java.io.Closeable;
import java.util.Collection;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public interface IChannelDriver<IC extends IChannelDriver.InitConfiguration> extends IMessageTransmitter {
    class InitConfiguration {
        public final int iounit;
        public final StyxSessionDI di;


        public InitConfiguration(StyxSessionDI di,
                                 int iounit) {
            this.iounit = iounit;
            this.di = di;
        }
    }
    class StartConfiguration {
        IMessageProcessor tProcessor;
        IMessageProcessor rProcessor;

        public StartConfiguration(IMessageProcessor tProcessor,
                                  IMessageProcessor rProcessor) {
            if (tProcessor == null) {
                throw new IllegalArgumentException("tProcessor cannot be null");
            }
            if (rProcessor == null) {
                throw new IllegalArgumentException("rProcessor cannot be null");
            }
            this.tProcessor = tProcessor;
            this.rProcessor = rProcessor;
        }

        public IMessageProcessor getTProcessor() {
            return tProcessor;
        }
        public IMessageProcessor getRProcessor() {
            return rProcessor;
        }
    }
    void prepare(IC configuration);
    Thread start(StartConfiguration configuration) throws StyxException;

    /**
     * Get all active clients.
     * @return all active clients.
     */
    Collection<Integer> getClients();
    boolean isConnected();
    boolean isStarted();
}
