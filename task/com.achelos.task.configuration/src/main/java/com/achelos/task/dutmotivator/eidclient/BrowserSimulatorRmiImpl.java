package com.achelos.task.dutmotivator.eidclient;

import com.secunet.ipsmall.rmi.IBrowserSimulator;

import java.rmi.Naming;
import java.rmi.registry.Registry;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BrowserSimulatorRmiImpl  {

	private IBrowserSimulator browserSimulator;

	public BrowserSimulatorRmiImpl(String browserSimHost) throws Exception {
		this(browserSimHost, Registry.REGISTRY_PORT);
	}
		
	public BrowserSimulatorRmiImpl(String browserSimHost, int browserSimPort) throws Exception {
		
		String serviceURI = "rmi://"
				+ browserSimHost + ":"
				+ browserSimPort + "/" + IBrowserSimulator.RMI_SERVICE_NAME;

        browserSimulator = (IBrowserSimulator) Naming.lookup(serviceURI);

	} 
    
    /**
     * get the RMI proxy to send commands to
     * 
     * @return browserSimulator
     */
    private IBrowserSimulator getBrowserSimulator() {
        return browserSimulator;
    }

	/**
	 * Calls the startApp method of the BrowserSimulator instance.
	 * @return the output of the BrowserSimulator instance.
	 */
	public String startApp(){
		try {
            return getBrowserSimulator().startApp();
		} catch (Exception e) {
			return "ERROR: " + e.getMessage();
		}
	}

	/**
	 * Calls the stopApp method of the BrowserSimulator instance.
	 * @return the output of the BrowserSimulator instance.
	 */
	public List<String> stopApp() {
		try {
            return getBrowserSimulator().stopApp();
		} catch (Exception e) {
			return List.of("ERROR: " + e.getMessage());
		}
		
	}
	
    public List<String> sendHttpRequest(String url, X509Certificate[] trustedCerts, boolean followRedirects) {
        return sendSyncHttpRequest(url, trustedCerts, followRedirects);
    }
    
    /**
     * Initial http request method for testing. However, sync request might block testbed
     * 
     * @param url
     */
    private List<String> sendSyncHttpRequest(String url, X509Certificate[] trustedCerts, boolean followRedirects) {
        List<String> responseStrings = new LinkedList<>();
		try {
            var response = getBrowserSimulator().sendHttpRequest(url, trustedCerts, followRedirects);
			responseStrings.add("HTTP message from eID-Client: " + "statusCode: " + response.statusCode);
			responseStrings.add("body: " + response.body);
			responseStrings.add("reasonPhrase: " + response.reasonPhrase);
			responseStrings.add("protocolVersion: " + response.protocolVersion);
			responseStrings.add("contentType: " + response.contentType);
			responseStrings.add("contentLength: " + response.contentLength);
			responseStrings.add("locale: " + response.locale.toString());
			responseStrings.add("Headers {");
			Set<Map.Entry<String, String>> keys = response.headers.entrySet();
			for (Map.Entry<String, String> entry : keys) {
				responseStrings.add("   " + entry.getKey() + ": " + entry.getValue());
			}
			responseStrings.add("}");
        } catch (Exception e) {
			responseStrings.add("Error sending request to BrowserSimulator: " + e.getMessage());
		}
		return responseStrings;
	}

	public void finalizeRMI() {
		browserSimulator = null;
		System.runFinalization();
		System.gc();
	}

}
