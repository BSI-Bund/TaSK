package com.achelos.task.dutcommandgenerators;

import com.achelos.task.configuration.TestRunPlanConfiguration;
import com.achelos.task.utilities.logging.LogBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericClientExecutableDUTCommandGenerator implements DUTCommandGenerator {

    private final String dutExecutable;
    private final String dutCallArgumentsResume;
    private final String dutCallArgumentsConnect;
    private final int tlsTestToolPort;
    private final String tlsTestToolHostName;

    public GenericClientExecutableDUTCommandGenerator(final TestRunPlanConfiguration configuration) {
        dutExecutable = configuration.getDUTExecutable();
        dutCallArgumentsResume = configuration.getDUTCallArgumentsResume();
        dutCallArgumentsConnect = configuration.getDUTCallArgumentsConnect();
        tlsTestToolPort = configuration.getTlsTestToolPort();
        tlsTestToolHostName = "localhost";
    }



    private List<String> callExecutionScript(final boolean isSessionResumption) {
        final ArrayList<String> commands = new ArrayList<>();

        final String command = dutExecutable;
        if (command == null || command.isEmpty()) {
            throw new IllegalArgumentException("The DUT executable command not found. Please check your setup.");
        }
        commands.add(command);

        String arguments = isSessionResumption ? dutCallArgumentsResume
                : dutCallArgumentsConnect;
        if (arguments == null || arguments.isEmpty()) {
            throw new IllegalArgumentException("The DUT call arguments not found. Please check your setup.");
        }

        // Remove double quotation marks
        arguments = arguments.replace("\"", "");
        commands.addAll(Arrays.asList(arguments.split(" ")));

        // Return command to call.
        return commands;
    }



    @Override
    public List<String> connectToServer(final boolean isSessionResumption) {
        return this.callExecutionScript(isSessionResumption);
    }

    @Override
    public String applicationSpecificInspectionSearchString(boolean handshakeSuccessful) {
        return null;
    }

    @Override
    public boolean applicationSpecificInspection(boolean handshakeSuccessful, LogBean logBean) {
        return true;
    }
}
