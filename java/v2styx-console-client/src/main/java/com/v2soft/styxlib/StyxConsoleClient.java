package com.v2soft.styxlib;

import com.v2soft.styxlib.exceptions.StyxException;
import com.v2soft.styxlib.l5.Connection;
import com.v2soft.styxlib.l5.enums.QidType;
import com.v2soft.styxlib.l5.serialization.IDataSerializer;
import com.v2soft.styxlib.l5.structs.StyxStat;
import com.v2soft.styxlib.l6.StyxFile;
import com.v2soft.styxlib.library.types.impl.CredentialsImpl;
import com.v2soft.styxlib.server.tcp.TCPClientChannelDriver;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

/**
 * JSimple console client for 9P2000.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class StyxConsoleClient {
    private static final String COMMAND_LS = "ls";
    private static final String COMMAND_EXIT = "exit";
    private static final String COMMAND_PWD = "pwd";
    private static final String COMMAND_CD = "cd ";
    private static final String COMMAND_LCD = "lcd ";
    private static final String COMMAND_LPWD = "lpwd";
    private static final String COMMAND_DOWNLOAD = "download";
    private static final String COMMAND_UPLOAD = "upload";
    private static final String DIR_PARENT = "..";

    private static Logger log = Logger.getLogger(StyxConsoleClient.class.getSimpleName());

    public static void main(String[] args) {
        try {
            new StyxConsoleClient().mainLoop(args);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private void getFile(Connection connection,
                         Terminal terminal,
                         Deque<String> currentPath,
                         String fileName,
                         File localFolder) {
        try {
            var path = new StringBuilder();
            currentPath.forEach(it -> {
                path.append('/');
                path.append(it);
            });
            path.append(fileName);
            terminal.writer().println("Downloading file \"" + path + "\" to " + localFolder.getAbsolutePath());
            var src = connection.open(path.toString());
            if (!src.exists()) {
                terminal.writer().println(String.format("File %s doesn't exists", path.toString()));
                return;
            }
            var stat = src.getStat();
            terminal.writer().printf("File size %d bytes", stat.length());
            var inputStream = src.openForReadUnbuffered();
            var outputStream = new FileOutputStream(new File(localFolder, fileName));
            var bufferSize = inputStream.ioUnit() - IDataSerializer.BASE_BINARY_SIZE - 4;
            var buffer = new byte[bufferSize];
            var read = 0;
            long totalRead = 0;
            long lastMark = totalRead;
            long startTime = System.currentTimeMillis();
            do {
                read = inputStream.read(buffer);
                if (read > 0) {
                    outputStream.write(buffer, 0, read);
                    totalRead += read;
                }
                if (totalRead - lastMark > 256000) {
                    long timeDelta = System.currentTimeMillis() - startTime;
                    terminal.writer().printf("\rRead %d bytes in %d ms", totalRead, timeDelta);
                    terminal.writer().flush();
                    lastMark = totalRead;
                }
            } while (read > 0);
            long timeDelta = System.currentTimeMillis() - startTime;
            terminal.writer().printf("\nDone. Download speed %f b/s. \n", ((float)totalRead)/((float)timeDelta/1000));
            inputStream.close();
            outputStream.close();
            src.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void listFiles(Connection connection,
                           Terminal terminal,
                           Deque<String> currentPath) throws IOException {
        // list files
        StringBuilder path = new StringBuilder();
        currentPath.forEach(it -> {
            path.append('/');
            path.append(it);
        });
        // TODO use connection.open()
        var currentDir = connection.getRoot().walk(path.toString());
        var files = currentDir.listStat();
        var dirs = files.stream().filter(it -> it.QID().type() == QidType.QTDIR)
                .sorted(Comparator.comparing(StyxStat::name))
                .toList();
        var other = files.stream().filter(it -> it.QID().type() != QidType.QTDIR)
                .sorted(Comparator.comparing(StyxStat::name))
                .toList();
        var sorted = new ArrayList<>(dirs);
        sorted.addAll(other);
        for (var it : sorted) {
            var quid = it.QID();
            if (quid.type() == QidType.QTDIR) {
                terminal.writer().print("D ");
            } else {
                terminal.writer().print("F ");
            }
            terminal.writer().println(it.groupName() + "/" +
                    it.userName() + "\t" +
                    it.length() + "\t\t" +
                    it.name());
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
            File currentLocalDirectory = new File("");
            Deque<String> currentDirPath = new LinkedBlockingDeque<>();

            while (true) {
                var cmd = lineReader.readLine(currentDirPath + " >");
                if (cmd.isEmpty())
                    continue;
                try {
                    if (cmd.equalsIgnoreCase("quit") || cmd.equalsIgnoreCase(COMMAND_EXIT)) {
                        terminal.writer().println("Shutdown server");
                        connection.close();
                        break;
                    }
                    if (cmd.equalsIgnoreCase(COMMAND_LS)) {
                        listFiles(connection, terminal, currentDirPath);
                        continue;
                    }
                    if (cmd.equalsIgnoreCase(COMMAND_PWD)) {
                        terminal.writer().println(currentDirPath);
                        continue;
                    }
                    if (cmd.equalsIgnoreCase(COMMAND_LPWD)) {
                        terminal.writer().println(currentLocalDirectory.getAbsolutePath());
                        continue;
                    }
                    if (cmd.startsWith(COMMAND_DOWNLOAD)) {
                        // download specified file to current local directory
                        var fileName = cmd.substring(COMMAND_DOWNLOAD.length()).trim();
                        getFile(connection,
                                terminal,
                                currentDirPath,
                                fileName,
                                currentLocalDirectory);
                        continue;
                    }
                    if (cmd.startsWith(COMMAND_LCD)) {
                        // change local directory
                        var subdir = cmd.substring(COMMAND_LCD.length()).trim();
                        if (subdir.startsWith("/")) {
                            currentLocalDirectory = new File(subdir);
                        } else {
                            currentLocalDirectory = new File(currentLocalDirectory, subdir);
                        }
                        continue;
                    }
                    if (cmd.startsWith(COMMAND_CD)) {
                        // change remote directory
                        var subdir = cmd.substring(COMMAND_CD.length()).trim();
                        if (subdir.equalsIgnoreCase(DIR_PARENT)) {
                            if (currentDirPath.isEmpty()) {
                                terminal.writer().println("Already in root");
                            } else {
                                currentDirPath.removeLast();
                            }
                        } else {
                            currentDirPath.addLast(subdir);
                        }
                        // TODO check that folder exists
                        //currentDir = chdir(connection, terminal, currentDir, subdir);
                        continue;
                    }
                    terminal.writer().println("Unknown command " + cmd);
                } catch (StyxException error) {
                    terminal.writer().println("Got error: " + error.toString());
                }


            }
        } catch (Exception err) {
            err.printStackTrace();
            System.exit(255);
        }
    }
}
