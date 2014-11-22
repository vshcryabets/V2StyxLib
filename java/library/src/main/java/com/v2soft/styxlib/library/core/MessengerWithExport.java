package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class MessengerWithExport extends Messenger {

    public MessengerWithExport(IChannelDriver driver, StyxMessengerListener listener)
            throws IOException {
        super(driver, listener);
    }

    public void export(IVirtualStyxFile root, ConnectionDetails details) throws IOException {
        TMessagesProcessor processor = (TMessagesProcessor) ( (MessagesFilter) mMessageProcessor ).getTProcessor();
        if (( processor != null ) && ( root != null ) && ( !root.equals(processor.getRoot()) )) {
            processor.close();
            processor = null;
        }
        if (root != null) {
            processor = new TMessagesProcessor(details, root);
        }
        ( (MessagesFilter) mMessageProcessor ).setTProcessor(processor);
    }

    protected IMessageProcessor getMessageProcessor() {
        return new MessagesFilter(null, super.getMessageProcessor());
    }
}
