package com.achelos.task.rmi;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import com.secunet.ipsmall.rmi.IBrowserSimulator;
import com.secunet.ipsmall.rmi.RmiHttpResponse;

public class BrowserSimulatorRmiImpl  {

	private IBrowserSimulator browserSimulator;
    private final ExecutorService executors;
	
	public BrowserSimulatorRmiImpl(String browserSimHost) throws Exception {
		this(browserSimHost, Registry.REGISTRY_PORT);
	}
		
	public BrowserSimulatorRmiImpl(String browserSimHost, int browserSimPort) throws Exception {
		
		String serviceURI = "rmi://"
				+ browserSimHost + ":"
				+ browserSimPort + "/" + IBrowserSimulator.RMI_SERVICE_NAME;
		
		System.out.println("Browser Simulator Service URI: " + serviceURI);

        browserSimulator = (IBrowserSimulator) Naming.lookup(serviceURI);
        
        executors = Executors.newCachedThreadPool();
        
	} 
    
    /**
     * get the RMI proxy to send commands to
     * 
     * @return
     */
    private IBrowserSimulator getBrowserSimulator() {
        return browserSimulator;
    }
    
	
	public String startApp(){
		try {
			String retVal = getBrowserSimulator().startApp();
			return retVal;
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}
	
	public String stopApp() {
		try {
			List<String> log = getBrowserSimulator().stopApp();
			log.forEach(s -> {
				System.out.println(s);
			});
			return "";
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
		
	}
	
    public void sendHttpRequest(String url, X509Certificate[] trustedCerts, boolean followRedirects) {
//        sendAsyncHttpRequest(url, trustedCerts, followRedirects);
    	sendSyncHttpRequest(url, trustedCerts, followRedirects);
    }
    
    /**
     * Initial http request method for testing. However, sync request might block testbed
     * 
     * @param url
     */
    @SuppressWarnings("unused")
    private void sendSyncHttpRequest(String url, X509Certificate[] trustedCerts, boolean followRedirects) {
        try {
            RmiHttpResponse response = getBrowserSimulator().sendHttpRequest(url, trustedCerts, followRedirects);
            onHttpResponse(response);
        } catch (Exception e) {
            try {
                onHttpException(e);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }
        }
    }
    
    /**
     * Send async http request for not blocking testbed and/or UI.
     * 
     * @param url
     */
    @SuppressWarnings("unused")
    private void sendAsyncHttpRequest(final String url, final X509Certificate[] trustedCerts, final boolean followRedirects) {
        /*
        Runnable runnable = new Runnable() {
        	@Override
        	public void run() {
        		try {
        			getBrowserSimulator().sendHttpRequest(url);
        		} catch (RemoteException e) {
        			e.printStackTrace();
        		}
        	}
        };
        executors.execute(runnable);
        */
        
        Callable<Void> callable = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    RmiHttpResponse response = getBrowserSimulator().sendHttpRequest(url, trustedCerts, followRedirects);
                    checkResponseHeaders(url, response);
                    onHttpResponse(response);
                } catch (Exception e) { 
//                	e.printStackTrace();
                    try {
                        onHttpException(e);
                    } catch (RemoteException e1) {
//                    	Logger.BrowserSim.logState("Error handlying async HttpRequest Exception: " + e1.getMessage(), LogLevel.Error);
                    	System.err.println("Error handling async HttpRequest Exception: " + e1.getMessage());
                    }
                }
                return null;
            }
        };
        try {
            @SuppressWarnings("unused")
            Future<Void> future = executors.submit(callable);
        	if (future.isDone()) {
        		System.out.println("Http request is finished.");
        	}
        } catch (RejectedExecutionException ignore) {
//            Logger.BrowserSim.logState("Rejected http request: " + url, LogLevel.Error);
            System.err.println("Rejected http request: " + url);
		}
    }
    
    /**
     * What to do if an http response comes back from browsersimulator. Logging only until now.
     * available.
     * 
     * @param response
     * @throws RemoteException
     */
    private void onHttpResponse(RmiHttpResponse response) throws RemoteException {
    	// log the response to the test protocol
        response.log();
        
//        if (isRedirect(response)) {
//            testData.sendMessageToCallbacks(TestStep.REDIRECT_BROWSER, response, SourceComponent.BROWSER_SIMULATOR, this);
//        } else {
//            testData.sendMessageToCallbacks(TestStep.BROWSER_CONTENT, response, SourceComponent.BROWSER_SIMULATOR, this);
//        }
        
    }

    /**
     * Check response headers and log a conformity warning if necessary
     * 
     * @param url
     * @param response
     */
    private void checkResponseHeaders(final String url, final RmiHttpResponse response) {
//        String headerServerValue = response.headers.get("Server");
        // [...]
    }
    
//    private boolean isRedirect(RmiHttpResponse response) {
        // redirects have 3xx status code
//        return response.statusCode / 100 == 3;
//    }
    
    /**
     * What to do if an exception comes back from browsersimulator. Logging only until now.
     * available.
     * 
     * @param response
     * @throws RemoteException
     */
    private void onHttpException(Exception e) throws RemoteException {
        System.err.println("HttpException: " + e.getMessage());
//        testData.sendMessageToCallbacks(TestError.BrowserSimulator, e.getMessage(), SourceComponent.BROWSER_SIMULATOR, this);
    }

    public void stop() {
        browserSimulator = null;
        executors.shutdown();
    }
}
