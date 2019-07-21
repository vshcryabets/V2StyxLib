package com.v2soft.styxlib.server.tcp;

import com.v2soft.styxlib.server.ClientDetails;
import com.v2soft.styxlib.server.IChannelDriver;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by V.Shcryabets on 5/20/14.
 *
 * @author V.Shcryabets (vshcryabets@gmail.com)
 */
public class TCPClientDetails extends ClientDetails<SocketChannel> {

    public TCPClientDetails(SocketChannel channel, IChannelDriver driver, int iounit, int id) {
        super(channel, driver, iounit, id);
    }

    @Override
    public String toString() {
        try {
            return String.format("%s:%d", mChannel.getRemoteAddress().toString(), mClientId);
        } catch (IOException e) {
            return super.toString();
        }
    }

    public void disconnect() throws IOException {
        // TODO something wrong, close should in same place where we have opened it.
        mChannel.close();
        mChannel = null;
    }
}
