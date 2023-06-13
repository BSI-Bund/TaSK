package com.achelos.task.rmi.tctokenprovider;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * If the test interface TR-03130-1-EID-SERVER-ECARD-PSK is selected,
 * an RMI service needs to be implemented, which satisfies this interface.
 *
 * The main goal of classes implementing this interface should be:
 * - Initating the Online-Authentication Request on the eID-Server
 * - Returning the corresponding TCTokenURL to the TaSK Framework.
 */
public interface TCTokenURLProvider extends Remote {
    /**
     * A hardcoded String representing the RMI service name.
     * The TaSK Framework will try to use the following String with regards to this RMI interface.
     */
    public static final String SERVICE_NAME = "TCTokenURLProvider";

    /**
     * This method should:
     *  - initiate the Online-Authentication Request on the eID-Server (e.g. using a useIDRequest message)
     *  - return a TCTokenURL to the TaSK Framework, which an eID-Client could use to retrieve a TCToken Object (as specified in TR-03130).
     * @return a TCTokenURL to the TaSK Framework, which an eID-Client could use to retrieve a TCToken Object (as specified in TR-03130).
     * @throws RemoteException in case of RMI errors.
     */
    String retrieveTCTokenURL() throws RemoteException;
}
