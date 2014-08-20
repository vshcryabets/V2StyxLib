package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.ILogListener;
import com.v2soft.styxlib.library.server.IClientChannelDriver;
import com.v2soft.styxlib.library.server.TMessagesProcessor;
import com.v2soft.styxlib.library.server.vfs.IVirtualStyxFile;

import java.io.IOException;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class MessengerWithExport extends Messenger {
    protected MessagesFilter mFilter;

    public MessengerWithExport(IClientChannelDriver driver, int io_unit, StyxMessengerListener listener,
                               ILogListener logListener)
            throws IOException {
        super(driver, io_unit, listener, logListener);
        mFilter = new MessagesFilter(null, mMessageProcessor);
        mMessageProcessor = mFilter;
    }

    public void export(IVirtualStyxFile root, String protocol) throws IOException {
        TMessagesProcessor processor = (TMessagesProcessor) mFilter.getTProcessor();
        if ( root == null || root != processor.getRoot() ) {
            if ( processor != null ) {
                processor.close();
                processor = null;
            }
        }
        if ( root != null ) {
            processor = new TMessagesProcessor(mIOBufferSize, root, protocol);
        }
        mFilter.setTProcessor(processor);
    }
}
