package com.achelos.task.xmlparser.datastructures.applicationmapping;

import com.achelos.task.xmlparser.datastructures.mics.MICS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ICSSection {

    ApplicationUnderTest("Application under Test", "3.1"),
    Profiles("Profiles", "3.2"),
    SupportedCryptography("Supported Cryptography", "3.3"),
    InformationOnTR03145Certification("Information on TR-03145 Certification", "3.4"),
    TLSWithPSKCipherSuites("TLS with PSK Cipher Suites","3.5"),
    ConnectionTimeout("Connection Timeout", "3.6"),
    ZeroRTTData("0-RTT Data", "3.7"),
    TLSCertificates("TLS Certificates", "3.8");

    private String icsSectionName;
    private String sectionNumber;
    ICSSection(String icsSectionName, String sectionNumber) {
        this.icsSectionName = icsSectionName;
        this.sectionNumber = sectionNumber;
    }

    /**
     * Retrieve the Name of the ICS Section.
     * @return the Name of the ICS Section.
     */
    public String getIcsSectionName() {
        return this.icsSectionName;
    }

    /**
     * Retrieve the Number of the ICS Section.
     * @return the Number of the ICS Section.
     */
    public String getSectionNumber() {
        return this.sectionNumber;
    }

    /**
     * Retrieve the ICSSection value corresponding to the section number, or null if it does not match any ICS Section.
     * @param sectionNumber section number of the ICS Section to retrieve.
     * @return ICSSection value corresponding to the section number, or null if it does not match any ICS Section.
     */
    public static ICSSection getICSSectionFromSectionNumber(String sectionNumber) {
        for (var icsSection : ICSSection.values()) {
            if (sectionNumber.strip().equals(icsSection.getSectionNumber())) {
                return icsSection;
            }
        }
        return null;
    }

    public boolean isPresentInMics(MICS mics) {
        switch(this) {
            case ApplicationUnderTest:
                return mics.getApplicationType() != null && !mics.getApplicationType().isEmpty();
            case Profiles:
                return mics.getProfiles() != null && !mics.getProfiles().isEmpty();
            case SupportedCryptography:
                return mics.getSupportedTlsVersions() != null && !mics.getSupportedTlsVersions().isEmpty();
            case InformationOnTR03145Certification:
                return mics.isTR03145CertificationPresent();
            case TLSWithPSKCipherSuites:
                return mics.isPskAvailable();
            case ConnectionTimeout:
                return mics.getSessionLifetime() != null;
            case ZeroRTTData:
                // 0-RTT Data is only a boolean flag indicating, whether it is allowed.
                // If no flag is set, it is not allowed. Eitherway, basically the Section is always present in the MICS.
                // Either as a boolean flag (being true or false) or by missing, (being false, i.e. not supported).
                return true;
            case TLSCertificates:
                return mics.getCertificateChain() != null && !mics.getCertificateChain().isEmpty();
            default:
                // Unknown.
                return false;
        }
    }

    public static List<ICSSection> getValuesExceptZeroRTT() {
        var icsSections = new ArrayList<>(Arrays.asList(ICSSection.values()));
        icsSections.remove(ZeroRTTData);
        return icsSections;
    }
}
