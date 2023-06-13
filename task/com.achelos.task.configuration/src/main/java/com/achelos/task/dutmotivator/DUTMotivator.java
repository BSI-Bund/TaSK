package com.achelos.task.dutmotivator;

import java.util.List;

public interface DUTMotivator {
    /**
     * Get the DUT to start the connection to the TaSK Testframework.
     * @param isSessionResumption Whether session resumption should be used.
     * @return The Log of the DUT as a list of strings.
     */
    List<String> motivateConnectionToTaSK(final boolean isSessionResumption);

    /**
     * Check the ApplicationSpecificInspectionInstructions for the DUT.
     * @param handshakeSuccessful Information whether the Handshake was successful.
     * @param logs The Logs of the DUT.
     * @return Boolean information whether the ApplicationSpecificInspectionInstructions could be successfully verified.
     */
    boolean checkApplicationSpecificInspections(final boolean handshakeSuccessful, List<String> logs);

    /**
     * Without explicitly calling this function, we had the problem that the RMI TCP thread was not closed and was sometimes running forever
     * This function should correctly closed the RMI TCP thread
     */
    void finalizeRMI();
}
