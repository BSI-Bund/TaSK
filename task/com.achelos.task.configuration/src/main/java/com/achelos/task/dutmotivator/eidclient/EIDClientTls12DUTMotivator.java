package com.achelos.task.dutmotivator.eidclient;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.dutmotivator.DUTMotivator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class EIDClientTls12DUTMotivator implements DUTMotivator {

    private static final String URL_TEMPLATE = "http://127.0.0.1:%d/eID-Client?tcTokenURL=https://%s:%d";
    private static final String APP_SPEC_CHECK_REGEX = "^HTTP message from eID-Client: statusCode:\\s*?(\\d+)\\s*?$";
    private final Integer eIDClientPort;
    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;
    private final BrowserSimulatorRmiImpl browserSim;

    public EIDClientTls12DUTMotivator(final TestRunPlanConfiguration configuration) throws Exception {
        eIDClientPort = configuration.getDutEIDClientPort();
        tlsTestToolHostName = configuration.getTaSKServerAddress();
        tlsTestToolPort = configuration.getTlsTestToolPort();

        var browserSimHostname = configuration.getDutRMIURL();
        var browserSimPort = configuration.getDutRMIPort();

        browserSim = new BrowserSimulatorRmiImpl(browserSimHostname, Integer.parseInt(browserSimPort));
    }

    private List<String> connectEIDClient(final String url) {
        var browserSimLogs = new LinkedList<String>();
        try {
            browserSimLogs.add(browserSim.startApp());
            Thread.sleep(3 * 1000);
            browserSimLogs.addAll(browserSim.sendHttpRequest(url, null, false));
            Thread.sleep(3 * 1000);
            browserSimLogs.addAll(browserSim.stopApp());
        } catch (Exception e) {
            browserSimLogs.add("Unable to connectEIDClient: " + e.getMessage());
        }
        return browserSimLogs;
    }

    @Override
    public List<String> motivateConnectionToTaSK(final boolean isSessionResumption) {
        var motivatorLogs = new LinkedList<String>();

        // Ignore isSessionResumption, as there is no way to trigger anything else on the eID Client.
        String url = String.format(URL_TEMPLATE, eIDClientPort, tlsTestToolHostName, tlsTestToolPort);
        motivatorLogs.add("Sending URL " + url + " to BrowserSimulator.");

        motivatorLogs.addAll(connectEIDClient(url));

        return new ArrayList<>(motivatorLogs);
    }

    @Override
    public boolean checkApplicationSpecificInspections(boolean handshakeSuccessful, List<String> logs) {
        if (handshakeSuccessful) {
            return true;
        } else if (logs == null) {
            return false;
        }
        // Search the given message in the provided log list
        var pattern = Pattern.compile(APP_SPEC_CHECK_REGEX);
        for (final String log : logs) {
            var matcher = pattern.matcher(log);
            if (matcher.find()) {
                // Check that the status is a redirect
                return (Integer.parseInt(matcher.group(1)) == 404);
            }
        }
        return false;
    }

    @Override
    public void finalizeRMI() {
        browserSim.finalizeRMI();
    }
}
