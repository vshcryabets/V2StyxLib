import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;

import java.net.InetAddress;
import java.util.logging.Logger;

/**
 * JSimple console client for 9P2000.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxConsoleClient {
    private static Logger log = Logger.getLogger(StyxConsoleClient.class.getSimpleName());

    public static void main(String[] args) {
        System.out.println("V2StyxLib-JVM console client");
        var host = "";
        var port = 0;
        var pos = 0;
        while (pos < args.length) {
            if (args[pos].equals("--host")) {
                host = args[pos + 1];
                pos++;
            }
            if (args[pos].equals("--port")) {
                port = Integer.parseInt(args[pos + 1]);
                pos++;
            }
            pos++;
        }
        if (host.isEmpty() || port == 0) {
            System.err.println("""
                    Please specify arguments:
                    \t --host server_ipadderss_or_hostname
                    \t --port server_port
                    """);
            System.exit(255);
        }
        System.out.println("Connection to the " + host + ":" + port);
        try {
            var driver = new TCPClientChannelDriver(InetAddress.getByName(host), port, false);
            var connection = new Connection(new CredentialsImpl("user", ""), driver);
            connection.connect();
            // list files
            StyxFile rootDir = connection.getRoot();
            var files = rootDir.listStat();
            for (var it : files) {
                System.out.println("File: " + it.getName() + " " + it.getGroupName() + " " + it.getUserName());
            }
            connection.close();
        } catch (Exception err) {
            err.printStackTrace();
            System.exit(255);
        }
    }
}
