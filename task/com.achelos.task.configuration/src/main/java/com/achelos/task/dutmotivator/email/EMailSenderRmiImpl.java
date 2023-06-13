package com.achelos.task.dutmotivator.email;

import com.achelos.task.rmi.clientexecution.MailClientConnectionExecutor;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Wrapper class for the RMI Implementation of MailClientConnectionExecutor.
 */
public class EMailSenderRmiImpl {

	private MailClientConnectionExecutor mailClientConnectionExecutor;

	public EMailSenderRmiImpl(String rmiHost, int rmiPort) throws Exception {
		
		String serviceURI = "rmi://"
				+ rmiHost + ":"
				+ rmiPort + "/" + MailClientConnectionExecutor.SERVICE_NAME;

		mailClientConnectionExecutor = (MailClientConnectionExecutor) Naming.lookup(serviceURI);

	} 


    
    /**
	 * Request the RMI Implementation of MailClientConnectionExecutor to send an E-Mail (hence initiate an TLS Connection) to specified EMail Address.
	 * @param eMailAddress The E-Mail Address where the E-Mail should be sent to. Probably the test@tlstest.task mail address is the right one to use.
	 * @return the output of the RMI objects 'sendEMailToTaSK' method.
     */
    protected List<String> sendEMailToTaSK(final String eMailAddress) {
		try {
			return mailClientConnectionExecutor.sendEMailToTaSK(eMailAddress);
		} catch (RemoteException e) {
			return List.of("Error sending request to RMI Component: " + e.getMessage());
		}
	}

	public void finalizeRMI() {
		mailClientConnectionExecutor = null;
		System.runFinalization();
		System.gc();
	}

}
