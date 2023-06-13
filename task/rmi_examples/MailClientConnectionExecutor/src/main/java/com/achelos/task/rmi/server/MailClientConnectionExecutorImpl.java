package com.achelos.task.rmi.server;

import com.achelos.task.rmi.clientexecution.MailClientConnectionExecutor;
import com.sun.mail.smtp.SMTPSSLTransport;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.Message;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class MailClientConnectionExecutorImpl extends UnicastRemoteObject implements MailClientConnectionExecutor {
    protected MailClientConnectionExecutorImpl() throws RemoteException {
        super();
    }

    @Override
    public List<String> sendEMailToTaSK(String receivingEMailAddress) throws RemoteException {
        List<String> logList = new LinkedList<>();
        try {
            // Remove achelos specific data here.
            String fromEmail = "Mail.Test2@achelos.net";
            // Request test data from PAH or MGL
            String fromUsername ="NOTTHEusername";
            String password = "NOTTHEpassword";
            String host = "kopano.achelos.de";

            // Setup mail server properties
            Properties properties = System.getProperties();
            properties.put("mail.transport.protocol", "smtps");
            properties.setProperty("mail.smtp.host", host);
            properties.setProperty("mail.smtp.port", "465");
            properties.setProperty("mail.smtp.auth", "true");
            properties.setProperty("mail.smtp.timeout", "10000");
            properties.setProperty("mail.smtp.ssl.enable", "true");

            // Get the Session object
            Session session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(fromUsername, password);
                        }
                    });

            // For Debug purposes enable this line:
            // session.setDebug(true);

            // Create a default MimeMessage object
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header
            message.setFrom(new InternetAddress(fromEmail));

            // Set To: header field of the header
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(receivingEMailAddress));

            // Set Subject: header field
            message.setSubject("Test email");

            // Now set the actual message
            message.setText("This is a test email.");

            // Send message
            SMTPSSLTransport.send(message, fromUsername, password);
            //Transport.send(message);
            logList.add("Send test mail to: " + receivingEMailAddress);
        } catch (Exception e) {
            logList.add("Error: " + e.getMessage());
        }
        return logList;
    }
}
