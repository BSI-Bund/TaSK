package com.achelos.task.commons.constants;

/**
 * Helper class used to contain constants used by different modules.
 */
public class Constants {
    /**
     * Hidden Constructor.
     */
    private Constants() {

    }
    private static final String TASK_HOSTNAME = "tlstest.task";
    /**
     * During the tests of E-Mail Trsp. the connection of the DUT is made by trying to send an E-Mail to the following address.
     */
    private static final String TASK_EMAIL_ADDRESS = "test@tlstest.task";
    private static final int EMAIL_TO_EMAIL_SMTP_PORT = 25;
    private static final String PSK_IDENTITY_DEFAULT = "Client_identity";
    public static String getTaskHostname() {
        return TASK_HOSTNAME;
    }
    public static String getTaskEmailAddress() {
        return TASK_EMAIL_ADDRESS;
    }
    public static int getEmailToEmailSmtpPort() {
        return EMAIL_TO_EMAIL_SMTP_PORT;
    }

    public static String getPSKIdentityDefault() {
        return PSK_IDENTITY_DEFAULT;
    }
}
