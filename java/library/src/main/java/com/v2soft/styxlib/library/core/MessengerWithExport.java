package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class MessengerWithExport extends Messenger {
    protected MessagesFilter mFilter;

    public MessengerWithExport(IChannelDriver driver, StyxMessengerListener listener)
            throws IOException {
        super(driver, listener);
        mFilter = new MessagesFilter(null, mMessageProcessor);
        mMessageProcessor = mFilter;
        mDriver.setMessageHandler(mMessageProcessor);
    }

    public void export(IVirtualStyxFile root, ConnectionDetails details) throws IOException {
        TMessagesProcessor processor = (TMessagesProcessor) mFilter.getTProcessor();
        if ( (processor != null) && (root != null) && (!root.equals(processor.getRoot()))) {
            processor.close();
            processor = null;
        }
        if ( root != null ) {
            processor = new TMessagesProcessor(details, root);
        }
        mFilter.setTProcessor(processor);
    }
}
