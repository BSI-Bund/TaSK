package com.achelos.task.rmi.server;

import com.achelos.task.rmi.clientexecution.MailClientConnectionExecutor;

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

            System.out.println("Creating registry...");
            LocateRegistry.createRegistry(port);
            Registry registry = LocateRegistry.getRegistry();
            System.setProperty("java.rmi.server.hostname", RMI_IP);
            System.out.println("Done creating registry.");

            System.out.println("Starting implementation...");
            var obj = new MailClientConnectionExecutorImpl();
            Naming.rebind(MailClientConnectionExecutor.SERVICE_NAME, obj);
            System.out.println("RMI Server listening to " + RMI_IP + ":" + port + "/" + MailClientConnectionExecutor.SERVICE_NAME);

            String[] lastList = registry.list();

            while (!Thread.interrupted()) {
                boolean running = false;
                boolean print = !Arrays.equals(lastList, registry.list());
                if (print)
                    System.out.println("Ping running entries...");

                for (String entry : registry.list()) {
                    if (print)
                        System.out.println("\t" + entry);

                    running |= MailClientConnectionExecutor.SERVICE_NAME.equals(entry);
                }
                if (!running) {
                    System.out.print("Need for rebind...");
                    Naming.rebind(MailClientConnectionExecutor.SERVICE_NAME, obj);
                    System.out.println("\t DONE");
                }

                if (!print)
                    System.out.print(".");

                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            }
            // RMI bug: Server might hang if this is not done here.
            obj = null;
            System.runFinalization();
            System.gc();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
