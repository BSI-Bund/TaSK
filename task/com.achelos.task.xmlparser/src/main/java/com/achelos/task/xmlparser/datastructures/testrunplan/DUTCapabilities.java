package com.achelos.task.xmlparser.datastructures.testrunplan;

/**
 * Enum representating different DUT capabilities, such as Resumption via Session Tickets or Session IDs.
 */
public enum DUTCapabilities {
    SESSION_TICKET("SESSION_TICKET"),
    SESSION_ID("SESSION_ID");
    final private String profileName;
    DUTCapabilities(final String profileName) {
        this.profileName = profileName;
    }
    public String getProfileName() {
        return profileName;
    }
}
