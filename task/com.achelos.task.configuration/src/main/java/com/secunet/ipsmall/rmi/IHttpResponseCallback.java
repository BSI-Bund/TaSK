package com.secunet.ipsmall.rmi;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.apache.http.HttpResponse;

/**
 * Interface to get notified on http response of BrowserSimulator
 * @author kersten.benjamin
 *
 */
public interface IHttpResponseCallback extends Serializable {
	
	/**
	 * Proxies any exceptions on HttpRequests of the BrowserSimulator
	 * back to the testbed to be handled there.
	 * 
	 * @param e
	 * @throws RemoteException
	 */
	public void onHttpException(Exception e) throws RemoteException;
	
	/**
	 * Proxies any HttpResponses received by the BrowserSimulator
	 * back to the testbed to be handled there.
	 * @param response
	 * @throws RemoteException
	 */
	public void onHttpResponse(HttpResponse response) throws RemoteException;
	
}
