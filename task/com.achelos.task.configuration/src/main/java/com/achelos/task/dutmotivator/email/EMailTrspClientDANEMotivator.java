package com.achelos.task.dutmotivator.email;

import com.achelos.task.commons.constants.Constants;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.dutmotivator.DUTMotivator;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.List;

public class EMailTrspClientDANEMotivator implements DUTMotivator {

    private final DNSServerContainer dnsServerContainer;
    private final EMailSenderRmiImpl mailSenderRmi;
    public EMailTrspClientDANEMotivator(final TestRunPlanConfiguration configuration) throws Exception {
        var rmiHostname = configuration.getDutRMIURL();
        var rmiPort = configuration.getDutRMIPort();

        mailSenderRmi = new EMailSenderRmiImpl(rmiHostname, Integer.parseInt(rmiPort));
        dnsServerContainer = new DNSServerContainer(configuration.getExperimentalDNSContainerIP(), configuration.getExperimentalTaSKHostIP());
    }
    @Override
    public List<String> motivateConnectionToTaSK(boolean isSessionResumption) {
        return mailSenderRmi.sendEMailToTaSK(Constants.getTaskEmailAddress());
    }

    @Override
    public boolean checkApplicationSpecificInspections(boolean handshakeSuccessful, List<String> logs) {
        return true;
    }

    @Override
    public void finalizeRMI() {
        mailSenderRmi.finalizeRMI();
    }

    public void updateCertificate(String filePath) throws Exception{
        // Calculation of SHA-256 hash of certificate and set it as TLSA Record. NOTE: EXPERIMENTAL
        var digest = MessageDigest.getInstance("SHA-256");
        try (var inputStream = new FileInputStream(filePath);) {
            byte[] buffer = new byte[8192];
            int read = 0;

            while ((read = inputStream.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            dnsServerContainer.updateTLSARecord(digest.digest());
        }
    }
}
