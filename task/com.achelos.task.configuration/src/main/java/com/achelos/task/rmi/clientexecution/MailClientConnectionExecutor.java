package com.achelos.task.rmi.clientexecution;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * If the test interface TR-03108-1-EMSP-CLIENT-CETI-NO-DANE or TR-03108-1-EMSP-CLIENT-CETI-DANE is selected,
 * an RMI service needs to be implemented, which satisfies this interface.
 *
 * The main goal of classes implementing this interface should be:
 * - Sending an E-Mail to an address on which the TaSK Framework is listening via the DUT E-Mail Trsp.
 * - Returning logs to the TaSK Framework.
 */
public interface MailClientConnectionExecutor extends Remote {
    /**
     * A hardcoded String representing the RMI service name.
     * The TaSK Framework will try to use the following String in regard to this RMI interface.
     */
    public static final String SERVICE_NAME = "TaSKEMailClientExecutor";
    /**
     * This method should:
     * - Connect to the hostname and the port provided by the TaSK Framework, using session resumption if the boolean flag is set.
     * - Returning logs to the TaSK Framework.
     * @param receivingEMailAddress EMail Address where a mail should be sent to via the DUT E-Mail server..
     * @return potential logs
     * @throws RemoteException in case of RMI errors.
     */
    List<String> sendEMailToTaSK(final String receivingEMailAddress) throws RemoteException;
}
