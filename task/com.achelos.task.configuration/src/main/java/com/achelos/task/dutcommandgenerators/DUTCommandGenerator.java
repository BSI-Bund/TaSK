package com.achelos.task.dutcommandgenerators;

import com.achelos.task.utilities.logging.LogBean;

import java.util.List;

public interface DUTCommandGenerator {
    List<String> connectToServer(final boolean isSessionResumption);

    String applicationSpecificInspectionSearchString(final boolean handshakeSuccessful);

    boolean applicationSpecificInspection(final boolean handshakeSuccessful, LogBean logBean);
}
