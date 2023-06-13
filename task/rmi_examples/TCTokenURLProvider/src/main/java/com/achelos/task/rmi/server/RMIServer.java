package com.achelos.task.rmi.server;

import com.achelos.task.rmi.tctokenprovider.TCTokenURLProvider;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * This class implements an RMI Server.
 * This class registers a TCTokenURLProvider implementation and binds it to the corresponding name.
 * Afterwards, it starts a Server and listens to incoming RMI connections.
 */
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

    /**
     * Entry point for the program.
     * @param args Arguments. Not used.
     * @throws Exception If an uncaught exception occurs.
     */
    public static void main(String[] args) throws Exception {
        try {
            /*
             * Take the default registry port(i.e. 1099) and create a registry there.
             */
            final int port = Registry.REGISTRY_PORT;

            System.out.println("Creating registry...");
            LocateRegistry.createRegistry(port);
            Registry registry = LocateRegistry.getRegistry();
            /*
             * Set the java.rmi.server.hostname property to the previously defined RMI_IP
             */
            System.setProperty("java.rmi.server.hostname", RMI_IP);
            System.out.println("Done creating registry.");

            /*
             * Create an object of type TCTokenURLProvider and bind it to the service name coming from the interface.
             */
            System.out.println("Starting implementation...");
            var obj = new TCTokenURLProviderImpl();
            Naming.rebind(TCTokenURLProvider.SERVICE_NAME, obj);
            System.out.println("RMI Server listening to " + RMI_IP + ":" + port + "/" + TCTokenURLProvider.SERVICE_NAME);

            /*
             * Check for the service to run, rebind if necessary.
             */
            String[] lastList = registry.list();

            while (!Thread.interrupted()) {
                boolean running = false;
                boolean print = !Arrays.equals(lastList, registry.list());
                if (print)
                    System.out.println("Ping running entries...");

                for (String entry : registry.list()) {
                    if (print)
                        System.out.println("\t" + entry);

                    running |= TCTokenURLProvider.SERVICE_NAME.equals(entry);
                }
                if (!running) {
                    System.out.print("Need for rebind...");
                    Naming.rebind(TCTokenURLProvider.SERVICE_NAME, obj);
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
