import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Java server that share current directory.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class FolderServerSample {
    private static final int PORT = 6666;
    private static final String SHARE_FOLDER = ".";
    private static final String CMD_HELP = "help";

    public static void main(String[] args) throws StyxException, InterruptedException, IOException {
        var shareFolder = SHARE_FOLDER;
        System.out.println("V2StyxLib-JVM console server");

        var pos = 0;
        var port = PORT;
        while (pos < args.length) {
            if (args[pos].equals("--folder")) {
                shareFolder = args[pos + 1];
                pos++;
            }
            if (args[pos].equals("--port")) {
                port = Integer.parseInt(args[pos + 1]);
                pos++;
            }
            pos++;
        }

        Terminal terminal = TerminalBuilder.builder()
                .system(true).build();
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        var driver = new TCPServerChannelDriver(
                InetAddress.getByName("127.0.0.1"),
                port,
                false);
        var root = new DiskStyxDirectory(new File(shareFolder), driver.getSerializer());
        var mServer = new StyxServerManager(root, List.of(driver));
        mServer.start();
        System.out.println("Test server listen on 127.0.0.1:" + port + " share folder " + shareFolder);

        while (true) {
            var cmd = lineReader.readLine(">");
            if (cmd.isEmpty())
                continue;
            if (cmd.equalsIgnoreCase(CMD_HELP)) {
                showCommandsHelp(terminal);
                continue;
            }
            if (cmd.equalsIgnoreCase("quit")) {
                terminal.writer().println("Shutdown server");
                mServer.close();
                break;
            }
            terminal.writer().println("Unknown command " + cmd);
        }
        mServer.joinThreads();
    }

    private static void showCommandsHelp(Terminal terminal) {
        terminal.writer().println("Supported commands:");
        terminal.writer().println("\thelp - show help information.");
        terminal.writer().println("\tquit - quit from server.");
    }
}
