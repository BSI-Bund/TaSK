package com.secunet.ipsmall.browser.simulator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.secunet.ipsmall.rmi.IBrowserSimulator;
import com.secunet.ipsmall.rmi.RmiHttpResponse;

/**
 * Implementation of the IBrowserSimulator interface to communicate
 * with the testbed via RMI, e.g. to receive commands
 * (e.g. to request a page). 
 * That is, this is the RMI-server running within VMWare guest.
 * 
 * Also allows for sending commands (e.g. notify on results) via backchannel, 
 * which is initiated upon creation (todo).
 * 
 * @author kersten.benjamin
 *
 */
public class BrowserSimulatorRmiServer extends UnicastRemoteObject implements IBrowserSimulator {

	
	private static final long serialVersionUID = 4887240908307691704L;

	private ShellCommandExecutor appExecutor;
	private String applicationExecutionCommand;
	private Path logfile;

	protected BrowserSimulatorRmiServer() throws RemoteException {
		super();
	}
	
	protected BrowserSimulatorRmiServer(String commandline) throws RemoteException {
		super();
		this.applicationExecutionCommand = commandline;
	}


	@Override
	public RmiHttpResponse sendHttpRequest(String url, X509Certificate[] trustedCerts, boolean followRedirects) throws Exception {
//		System.out.println("\nIncoming RMI-command for http request: " + url);
		return  (new HttpHandler()).sendRequest(url, trustedCerts, followRedirects);
	}


	@Override
	public String startApp() throws Exception {
//		System.out.println("\nStarting App: " + applicationExecutionCommand);
        logfile = Files.createTempFile(null, ".log");
        appExecutor = new ShellCommandExecutor();
		appExecutor.execute(applicationExecutionCommand, logfile);
		return "App was started"; //"Beispiel-Rückgabe";
	}


	@Override
	public List<String> stopApp() throws Exception {
//		System.out.println("Stopping App");
		if (null != appExecutor) {
			appExecutor.terminate();
		}
	
		List<String> log = new ArrayList<String>();

		try (Stream<String> stream = Files.lines(logfile, StandardCharsets.UTF_8)) {
			stream.forEach(s -> log.add(s));
		}
		catch (IOException e) {
			String message = "Exception while reading log file";
			System.out.println(message);
			log.add(message);
			log.add(e.getMessage());
		}
		return log;
	}
	
}
