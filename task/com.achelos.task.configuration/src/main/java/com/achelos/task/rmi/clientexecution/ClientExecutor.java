package com.achelos.task.rmi.clientexecution;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * If the test interface TR-03116-4-CLIENT, TR-03116-3-SMGW-HAN-CLIENT or TR-03116-3-SMGW-WAN-CLIENT is selected,
 * an RMI service needs to be implemented, which satisfies this interface.
 *
 * The main goal of classes implementing this interface should be:
 * - Connecting to the hostname and the port provided by the TaSK Framework, using session resumption if the boolean flag is set.
 * - Returning logs to the TaSK Framework.
 */
public interface ClientExecutor extends Remote {
    /**
     * A hardcoded String representing the RMI service name.
     * The TaSK Framework will try to use the following String in regard to this RMI interface.
     */
    public static final String SERVICE_NAME = "TLSClientExecutor";
    /**
     * This method should:
     * - Connect to the hostname and the port provided by the TaSK Framework, using session resumption if the boolean flag is set.
     * - Returning logs to the TaSK Framework.
     * @param hostname Hostname to connect to.
     * @param port port to connect to.
     * @param isSessionResumption boolean flag indicating whether session resumption shall be used.
     * @return potential logs
     * @throws RemoteException in case of RMI errors.
     */
    List<String> connectToServer(final String hostname, final int port, final boolean isSessionResumption) throws RemoteException;
}
