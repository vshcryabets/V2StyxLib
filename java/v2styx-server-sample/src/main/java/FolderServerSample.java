import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.v2soft.folderserver.ServerConfig;
import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.FileInputStream;
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
    private static final String CMD_HELP = "help";

    public static void main(String[] args) throws InterruptedException, IOException {
        var configFilePath = "";
        System.out.println("V2StyxLib-JVM console server");
        var mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        var pos = 0;
        while (pos < args.length) {
            if (args[pos].equals("--config")) {
                configFilePath = args[pos + 1];
                pos++;
            }
            pos++;
        }
        System.out.println("Load configuration from file " + configFilePath);
        // load configuration file
        var projectJson = new FileInputStream(configFilePath);
        ServerConfig configuration = mapper.readValue(projectJson, ServerConfig.class);
        projectJson.close();


        Terminal terminal = TerminalBuilder.builder()
                .system(true).build();
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        var driver = new TCPServerChannelDriver(
                InetAddress.getByName(configuration.interfaces().get(0)),
                configuration.port(),
                false);
        var root = new DiskStyxDirectory(new File(configuration.exportPath()), driver.getSerializer());
        var mServer = new StyxServerManager(root, List.of(driver));
        mServer.start();
        System.out.println("Test server listening on " + configuration.interfaces().get(0) + ":"
                + configuration.port() + " share folder " + configuration.exportPath());

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
