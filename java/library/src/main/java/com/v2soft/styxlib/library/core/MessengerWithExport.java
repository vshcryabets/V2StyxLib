package com.v2soft.styxlib.library.core;

import com.v2soft.styxlib.server.IChannelDriver;
import com.v2soft.styxlib.library.types.ConnectionDetails;
import com.v2soft.styxlib.vfs.IVirtualStyxFile;

import java.io.IOException;

/**
 * @author V.Shcryabets<vshcryabets@gmail.com>
 */
public class MessengerWithExport extends Messenger {
    protected IVirtualStyxFile mRoot;

    public MessengerWithExport(IChannelDriver driver, StyxMessengerListener listener)
            throws IOException {
        super(driver, listener);
    }

    public void export(IVirtualStyxFile root, ConnectionDetails details) throws IOException {
        if ( mRoot != null && root != mRoot ) {
            mRoot.release();
            mRoot = null;
        }
        if (root != null) {
            mRoot = root;
            TMessagesProcessor processor = (TMessagesProcessor) mDriver.getTMessageHandler();
            if ( processor == null ) {
                processor = new TMessagesProcessor(details, root);
            } else {
                processor.setRoot(root);
            }
            mDriver.setTMessageHandler(processor);
        }
    }

    @Override
    public void close() throws IOException {
        if ( mRoot != null ) {
            mRoot.release();
        }
        super.close();
    }
}
