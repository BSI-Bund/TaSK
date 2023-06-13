package com.achelos.task.rmi.server;
import org.apache.commons.cli.*;

import com.achelos.task.rmi.clientexecution.ClientExecutor;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RMIServer {

    /*
     *  NOTE: Please update the RMI_IP address below with the
     *        "real" IP address of the machine the RMI Server is running on.
     *        In the case of multiple network interface controllers make sure the IP address
     *        belongs to the correct interface.
     */

    /**
     * Used to set the java.rmi.server.hostname property.
     * RMI uses IP addresses to indicate the location of a server (embedded in a remote reference).
     * If the use of a hostname is desired, this property is used to specify the fully-qualified hostname
     * for RMI to use for remote objects exported to the local JVM.
     * The property can also be set to an IP address.
     */
    private final static String RMI_IP = "127.0.0.1";

    public static void main(String[] args) throws Exception {
        try {
            final int port = Registry.REGISTRY_PORT;

            Options options = new Options();

            Option clientCommand = new Option("c", "clientCommand", true, "command to start client");
            clientCommand.setRequired(true);
            options.addOption(clientCommand);

            Option resumptionClientCommand = new Option("r", "resumptionClientCommand", true, "resumption command to start client");
            resumptionClientCommand.setRequired(false);
            options.addOption(resumptionClientCommand);

            Option workingDirectory = new Option("w", "workingDirectory", true, "directory where client will be started");
            workingDirectory.setRequired(true);
            options.addOption(workingDirectory);

            Option clientCert = new Option("x", "clientCert", true, "path to client certificate");
            clientCert.setRequired(false);
            options.addOption(clientCert);

            Option clientKey = new Option("k", "clientKey", true, "path to client certificate key");
            clientKey.setRequired(false);
            options.addOption(clientKey);

            CommandLineParser parser = new DefaultParser();
            HelpFormatter formatter = new HelpFormatter();
            CommandLine cmd = null;//not a good practice, it serves it purpose

            try {
                cmd = parser.parse(options, args);
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("utility-name", options);
                System.exit(1);
            }

            System.out.println("Creating registry...");
            LocateRegistry.createRegistry(port);
            Registry registry = LocateRegistry.getRegistry();
            System.setProperty("java.rmi.server.hostname", RMI_IP);
            System.out.println("Done creating registry.");

            System.out.println("Starting implementation...");
            var obj = new ClientExecutorImpl();
            obj.setClientCommand(cmd.getOptionValue(clientCommand));
            obj.setWorkingDirectory(cmd.getOptionValue(workingDirectory));
            if(cmd.hasOption(resumptionClientCommand)){
                obj.setResumptionCommand(cmd.getOptionValue(resumptionClientCommand));
            }
            if(cmd.hasOption(clientCert)){
                obj.setClientCert(cmd.getOptionValue(clientCert));
            }
            if(cmd.hasOption(clientKey)){
                obj.setClientCerKey(cmd.getOptionValue(clientKey));
            }
            Naming.rebind(ClientExecutor.SERVICE_NAME, obj);
            System.out.println("RMI Server listening to " + RMI_IP + ":" + port + "/" + ClientExecutor.SERVICE_NAME);

            String[] lastList = registry.list();

            while (!Thread.interrupted()) {
                boolean running = false;
                boolean print = !Arrays.equals(lastList, registry.list());
                if (print)
                    System.out.println("Ping running entries...");

                for (String entry : registry.list()) {
                    if (print)
                        System.out.println("\t" + entry);

                    running |= ClientExecutor.SERVICE_NAME.equals(entry);
                }
                if (!running) {
                    System.out.print("Need for rebind...");
                    Naming.rebind(ClientExecutor.SERVICE_NAME, obj);
                    System.out.println("\t DONE");
                }

                if (!print)
                    System.out.print(".");

                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
