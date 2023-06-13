package com.achelos.task.dutmotivator.email;

import com.achelos.task.commons.constants.Constants;
import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.dutmotivator.DUTMotivator;

import java.util.List;

public class EMailTrspClientNoDANEMotivator implements DUTMotivator {
    private final EMailSenderRmiImpl mailSenderRmi;
    public EMailTrspClientNoDANEMotivator(final TestRunPlanConfiguration configuration) throws Exception {
        var rmiHostname = configuration.getDutRMIURL();
        var rmiPort = configuration.getDutRMIPort();

        mailSenderRmi = new EMailSenderRmiImpl(rmiHostname, Integer.parseInt(rmiPort));
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
}
