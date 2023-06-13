package com.secunet.ipsmall.browser.simulator;

import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.secunet.ipsmall.rmi.IBrowserSimulator;

/**
 * The main class of the browser simulator. To be started on VMWare-Guest before
 * running automated testcases from IPSmallUI.
 * 
 * Starts the RMI Server to listen to RMI commands from the testbed.
 * 
 * @author kersten.benjamin
 * 
 */
public class BrowserSimulator {
    
    public static void main(String args[]) {
    	String commandline = "AusweisApp2";
    	String ip = "localhost";
    	int port = Registry.REGISTRY_PORT;
    	
        try {
            if (args.length < 1) {
                System.err.println("Usage: java -jar browsersimulator.jar [IP address to bind server to] [COMMAND to start the app] ");
                System.exit(0);
            }
            
            try {
            	ip = args[0];
            	commandline = args[1];
            	
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
            	System.out.println("DEBUG: Exception when reading params: " + e.getMessage());
            }
            
            System.out.println("Browser Simulator Rmi Server starting... (IP: " + args[0] + ")");
            
            
            System.out.print("Creating Registry...");
            LocateRegistry.createRegistry(port);
            Registry registry = LocateRegistry.getRegistry();
            System.setProperty("java.rmi.server.hostname", ip);
            System.out.println("\t DONE");
            
            System.out.print("Starting browsersimulator...");
            final BrowserSimulatorRmiServer rmiServer = new BrowserSimulatorRmiServer(commandline);
            Naming.rebind(IBrowserSimulator.RMI_SERVICE_NAME, rmiServer);
            System.out.println("\t DONE");
            System.out.println("Rmi Server listening to " + ip + ":" + port + "/" + IBrowserSimulator.RMI_SERVICE_NAME);
            
            String[] lastList = registry.list();
            
            while (!Thread.interrupted()) {
                boolean running = false;
                boolean print = !Arrays.equals(lastList, registry.list());
                if (print)
                    System.out.println("Ping running entries...");
                
                for (String entry : registry.list()) {
                    if (print)
                        System.out.println("\t" + entry);
                    
                    running |= IBrowserSimulator.RMI_SERVICE_NAME.equals(entry);
                }
                if (!running) {
                    System.out.print("Need for rebind...");
                    Naming.rebind(IBrowserSimulator.RMI_SERVICE_NAME, rmiServer);
                    System.out.println("\t DONE");
                }
                
                if (!print)
                    System.out.print(".");
                
                Thread.sleep(TimeUnit.MINUTES.toMillis(5));
            }
            
        } catch (Exception e) {
            System.out.println();
            if (e instanceof ConnectException) {
                System.err
                        .println("ConnectException: -Has the rmiregistry been started? -Has this machines IP been passed as start-arg?");
            }
            System.out.println("Error: " + e.getMessage());
        }
    }
    
}
