package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.enums.QIDType;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * JSimple console client for 9P2000.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxConsoleClient {
    private static Logger log = Logger.getLogger(StyxConsoleClient.class.getSimpleName());

    public static void main(String[] args) {
        try {
            new StyxConsoleClient().mainLoop(args);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private void listFiles(Connection connection,
                           Terminal terminal,
                           StyxFile currentDir) throws IOException, InterruptedException,
            StyxException, TimeoutException {
        // list files
        var files = currentDir.listStat();
        for (var it : files) {
            var quid = it.getQID();
            if (quid.getType() == QIDType.QTDIR) {
                terminal.writer().println("D\t" +
                        it.getGroupName() + "/" +
                        it.getUserName() + "\t" +
                        it.getLength() + "\t\t" +
                        it.getName());
            } else {
                terminal.writer().println("F\t" +
                        it.getGroupName() + "/" +
                        it.getUserName() + "\t" +
                        it.getLength() + "\t\t" +
                        it.getName());
            }
        }
    }

    public void mainLoop(String[] args) throws IOException {
        // server samnples
        // diod -f -n -l 0.0.0.0:12345 -e ~/temp/
        // or docker docker run -p 6666:6666 --rm -it metacoma/inferno-os:latest
        // styxlisten -A 'tcp!*!6666' export /
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
        Terminal terminal = TerminalBuilder.builder()
                .system(true).build();
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        terminal.writer().println("Connection to the " + host + ":" + port);
        try {
            var driver = new TCPClientChannelDriver(InetAddress.getByName(host), port, false);
            var connection = new Connection(new CredentialsImpl("", ""), driver);
            connection.connect();
            terminal.writer().println("Connected");
            StyxFile currentDir = connection.getRoot();
            while (true) {
                var cmd = lineReader.readLine(">");
                if (cmd.isEmpty())
                    continue;
                if (cmd.equalsIgnoreCase("quit")) {
                    terminal.writer().println("Shutdown server");
                    connection.close();
                    break;
                }
                if (cmd.equalsIgnoreCase("ls")) {
                    listFiles(connection, terminal, currentDir);
                    continue;
                }
                terminal.writer().println("Unknown command " + cmd);

            }
        } catch (Exception err) {
            err.printStackTrace();
            System.exit(255);
        }
    }
}
