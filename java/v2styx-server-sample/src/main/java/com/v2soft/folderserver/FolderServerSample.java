package com.v2soft.folderserver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.v2soft.styxlib.l6.vfs.DiskStyxDirectory;
import com.v2soft.styxlib.server.StyxServerManager;
import com.v2soft.styxlib.server.tcp.TCPClientDetails;
import com.v2soft.styxlib.server.tcp.TCPServerChannelDriver;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

/**
 * Java server that share current directory.
 *
 * @author V.Shcriyabets (vshcryabets@gmail.com)
 */
public class FolderServerSample {
    private static final String CMD_HELP = "help";
    private static final String CMD_QUIT = "quit";
    private static final String CMD_CLIENTS = "lsclients";
    private static final String CMD_IP = "ip";

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
        if (configFilePath.isEmpty()) {
            System.err.println("Please specify configuration file i.e --config fileName");
            System.exit(255);
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
            } else if (cmd.equalsIgnoreCase(CMD_CLIENTS)) {
                listClients(terminal, mServer);
                continue;
            } else if (cmd.equalsIgnoreCase(CMD_IP)) {
                showInterfaces(terminal, mServer);
                continue;
            } else if (cmd.equalsIgnoreCase(CMD_QUIT)) {
                terminal.writer().println("Shutdown server");
                mServer.close();
                break;
            }
            terminal.writer().println("Unknown command " + cmd);
        }
        mServer.joinThreads();
    }

    private static void listClients(Terminal terminal, StyxServerManager server) {
        for (var driver : server.getDrivers()) {
            terminal.writer().println("ID\tName\tAddress");
            for (var client : driver.getClients()) {
                terminal.writer().print(client.getId());
                terminal.writer().print("\t");
                terminal.writer().print(client.getCredentials().getUserName());
                terminal.writer().print("\t");
                if (client instanceof TCPClientDetails) {
                    terminal.writer().print(client.toString());
                }
                terminal.writer().println(" .");
            }
        }
    }

    private static void showInterfaces(Terminal terminal, StyxServerManager server) {
        try {
            // Get all network interfaces
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            // Iterate through each network interface
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Print interface name and details
                System.out.println("Name: " + networkInterface.getName());
                System.out.println("Display Name: " + networkInterface.getDisplayName());
                System.out.println("Is Up: " + networkInterface.isUp());
                System.out.println("Supports Multicast: " + networkInterface.supportsMulticast());
                System.out.println("Is Virtual: " + networkInterface.isVirtual());
//                System.out.println("Hardware Address: " + (networkInterface.getHardwareAddress() != null
//                        ? bytesToHex(networkInterface.getHardwareAddress())
//                        : "N/A"));
                System.out.println("MTU: " + networkInterface.getMTU());
                System.out.println("--------------------------------");
            }
        } catch (SocketException e) {
            System.err.println("Error enumerating network interfaces: " + e.getMessage());
        }
    }

    private static void showCommandsHelp(Terminal terminal) {
        terminal.writer().println("Supported commands:");
        terminal.writer().println("\thelp - show help information.");
        terminal.writer().println("\tquit - quit from server.");
        terminal.writer().print("\t");
        terminal.writer().print(CMD_CLIENTS);
        terminal.writer().println(" - list clients.");
    }
}
