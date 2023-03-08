package com.achelos.task;

import java.security.cert.X509Certificate;

import com.achelos.task.rmi.BrowserSimulatorRmiImpl;

public class BrowserSimulatorRemote {

    private BrowserSimulatorRmiImpl browserSimulatorRmiClient;
    
    public BrowserSimulatorRemote () throws Exception {
        
    }
    

	public static void main(String[] args) {

		if (null == args || args.length < 3) {
			System.out.println("Usage: java -jar browsersimulatorclient.jar <BROWSERSIM_HOST> <BROWSERSIM_PORT> <EID_CLIENT_URL>");
			System.exit(0);
		}
		
		String browsersimHost = "";
		int browsersimPort = 0;
		String clientURL = "";
		
		try {
			browsersimHost = args[0];
			browsersimPort = Integer.parseInt(args[1]);
			clientURL = args[2];
		}
		catch (Exception e) {
			System.out.println("Invalid parameter for browsersimulatorclient");
			System.exit(0);
		}
		
		try {
			BrowserSimulatorRemote browserSim = new BrowserSimulatorRemote();
			browserSim.connect(browsersimHost, browsersimPort);
			
			browserSim.startEidClient();
			Thread.sleep(3 * 1000);
			browserSim.sendRequest(clientURL);
			Thread.sleep(2 * 1000);
			browserSim.stopEidClient();
			
		}
		catch (Exception e) {
			System.out.println("RMI failed:");
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("BrowserSimulator Client finished");
		System.exit(0);
	}
	
	public void connect(String host, int port) throws Exception {

		System.out.println("Starting connection to browser simulator...");
		browserSimulatorRmiClient = new BrowserSimulatorRmiImpl(host, port);
		System.out.println("connection established");
	}

	public void startEidClient() {
//		System.out.println("Start eID-Client");
		browserSimulatorRmiClient.startApp();
//		System.out.println("eID-Client started");
	}
	
	public void stopEidClient() {
//		System.out.println("Stop eID-Client");
		browserSimulatorRmiClient.stopApp();
//		System.out.println("eID-Client exited");
	}
	
	public void sendRequest(String clientURL, String certFile) {
		// ignore certFile
		sendRequest(clientURL);
	}
	
	public void sendRequest(String clientURL) {
		
		X509Certificate[] trustedCerts = null;
    		
//        Logger.BrowserSim.logState("Trying to trigger browsersimulator: " + clientURL);
		System.out.println("Trying to trigger browsersimulator: " + clientURL);
            
		browserSimulatorRmiClient.sendHttpRequest(clientURL, trustedCerts, false);
            
//        Logger.BrowserSim.logState("Triggered: " + clientURL);
		System.out.println("Triggered: " + clientURL);
	}

}
