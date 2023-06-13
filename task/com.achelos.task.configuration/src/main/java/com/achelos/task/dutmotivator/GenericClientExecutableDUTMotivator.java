package com.achelos.task.dutmotivator;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.rmi.clientexecution.ClientExecutor;

import java.rmi.Naming;
import java.util.Arrays;
import java.util.List;

public class GenericClientExecutableDUTMotivator implements DUTMotivator {

    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;
    private ClientExecutor remoteClientExecutor;

    public GenericClientExecutableDUTMotivator(final TestRunPlanConfiguration configuration) {
        System.setProperty("java.rmi.server.useCodebaseOnly", "false");
        tlsTestToolPort = configuration.getTlsTestToolPort();
        tlsTestToolHostName = configuration.getTaSKServerAddress();
        try {
            // Look up the remote object
            remoteClientExecutor = (ClientExecutor) Naming.lookup("//" + configuration.getDutRMIURL()+ ":"
                    + configuration.getDutRMIPort() + "/" + ClientExecutor.SERVICE_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Unable to register RMI ClientExecutor.", e);
        }
    }

    @Override
    public List<String> motivateConnectionToTaSK(final boolean isSessionResumption) {
        try {
            return this.remoteClientExecutor.connectToServer(tlsTestToolHostName, tlsTestToolPort, isSessionResumption);
        } catch (Exception e) {
            return Arrays.asList(e.getMessage());
        }
    }

    @Override
    public boolean checkApplicationSpecificInspections(boolean handshakeSuccessful, List<String> logs) {
        return true;
    }

    @Override
    public void finalizeRMI() {
        remoteClientExecutor = null;
        System.runFinalization();
        System.gc();
    }

}
