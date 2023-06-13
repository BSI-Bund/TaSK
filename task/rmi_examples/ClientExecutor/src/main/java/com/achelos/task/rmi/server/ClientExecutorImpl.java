package com.achelos.task.rmi.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.achelos.task.rmi.clientexecution.ClientExecutor;

public class ClientExecutorImpl extends UnicastRemoteObject implements ClientExecutor {
    private static String TMP_SESSION_DATA = "/tmp/task_session.pem";
    private String clientCommand = "";
    private String workingDirectory = "";
    private String resumptionCommand = "";
    private String clientCert = "";
    private String clientCerKey = "";
    protected ClientExecutorImpl() throws RemoteException {
        super();
        try {
        	File tmpFile = File.createTempFile("task_session", "pem");
        	TMP_SESSION_DATA = tmpFile.getAbsolutePath();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }



    @Override
    public List<String> connectToServer(String hostname, int port, boolean isSessionResumption) {
        List<String> logs = new ArrayList<>();
        try {
            logs.add("Executing OpenSSL Client.");
            System.out.println("Executing OpenSSL Client.");
            List<String> command = chooseCommand(hostname, port, isSessionResumption);

            logs.add(command.toString());

            var processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(new File(workingDirectory));
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            // Write Q into input to close OpenSSL connection.
            Thread.sleep(2000);

            process.outputWriter().append("Q");
            try {
                process.outputWriter().flush();
            } catch (Exception ignored){
                //do nothing
            }

            int timeout = 580;
            while(process.isAlive() && --timeout > 0) {
            	Thread.sleep(1 * 100);
            }
            if (process.isAlive()) {
            	process.destroy();
            }

            String line;
            while((line = reader.readLine()) != null) {
                logs.add(line);
                // For local debug purposes: System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logs.add("Error executing OpenSSL Client: " + e.getMessage());
        }
        return logs;
    }

    private List<String> chooseCommand(final String hostname, final int port, final boolean isSessionResumption) {
        String command = isSessionResumption ? resumptionCommand : clientCommand;
        if(!clientCert.isEmpty())
            command = command.replace("CLIENT_CERT", clientCert).replace("CLIENT_KEY", clientCerKey);
        command = command.replace("#!/bin/bash", "");
        var commandList = Arrays.stream(command.split(" ")).toList();
        System.out.println(commandList);
        return commandList;
    }

    public void setClientCommand(String clientCommand) {
        this.clientCommand = clientCommand;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setResumptionCommand(String resumptionCommand) {
        this.resumptionCommand = resumptionCommand;
    }

    public void setClientCert(String clientCert) {
        this.clientCert = clientCert;
    }

    public void setClientCerKey(String clientCerKey) {
        this.clientCerKey = clientCerKey;
    }
}
