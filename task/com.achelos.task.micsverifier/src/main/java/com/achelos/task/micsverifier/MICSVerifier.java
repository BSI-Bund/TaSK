package com.achelos.task.micsverifier;

import java.io.File;
import java.util.List;

import com.achelos.task.abstracttestsuite.Summary;
import com.achelos.task.configuration.MICSConfiguration;
import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.applicationmapping.AppMapping;
import com.achelos.task.xmlparser.datastructures.mics.MICS;
import com.achelos.task.xmlparser.datastructures.tlsspecification.TlsSpecification;
import com.achelos.task.xmlparser.inputparsing.InputParser;


/**
 * A class which is used to verify a MICS Object with regards to the MICS Checklist and the Certificate Checks mentioned in TR-03116-TS.
 */
public class MICSVerifier {

    private final static String CHECK_CERTS_PROFILE = "CHECK_CERTS";

    private final MICSConfiguration configuration;
    private final LoggingConnector logger;
    public static final String LOGGER_COMPONENT = "MICS Verifier: ";


    /**
     * Hidden Constructor.
     */
    public MICSVerifier(final MICSConfiguration configuration) {
        this.configuration = configuration;
        logger = LoggingConnector.getInstance();
    }

    /**
     * Constructor to initialize an MICS Verifier with the respective specifications and profiles which it shall check.
     *
     * @param applicationSpecifications   A list of Application Specification Files.
     * @param applicationProfilesMappings A list of Application Profile Mappings.
     * @param testCasesDir                A directory, in which the TestCases Specification XMLs are present.
     * @param testProfiles                The Test Profiles File to use.
     * @param globalConfig                The Global Configuration File to use.
     * @param tlsConfigData               The TLS Configuration Data File to use.
     */
    public MICSVerifier(final List<File> applicationSpecifications,
                        final List<File> applicationProfilesMappings, final File testCasesDir, final File testProfiles,
                        final File globalConfig,
                        final File tlsConfigData) {
        logger = LoggingConnector.getInstance();
        logInfo("Initializing the MICS verifier.");
        configuration = MICSConfiguration.initializeConfiguration(applicationSpecifications,
                applicationProfilesMappings, testCasesDir, testProfiles, globalConfig, tlsConfigData);
        logInfo("Initialization of the MICS verifier successful.");
    }

    /**
     * Parse an MICS File into an internal representation of the MICS.
     *
     * @param micsFile The MICS File to parse.
     * @return an internal representation of the MICS.
     * @throws MicsParserException If unable to parse the MICS File.
     */
    public MICS parseMICS(final File micsFile) throws MicsParserException {
        // Check if File not null.
        if (micsFile == null) {
            throw new RuntimeException(LOGGER_COMPONENT + " Unable to parse the MICS file. File is \"null\".");
        }
        logInfo("Trying to parse MICS file: " + micsFile.getAbsolutePath());
        // Parse MICS into internal data structure using XML parser module's InputParser.
        MICS mics;
        try {
            mics = InputParser.parseMICS(micsFile);
            if (mics == null) {
                throw new NullPointerException(LOGGER_COMPONENT + " Parsed MICS file is \"null\".");
            }
            logDebug("Successfully parsed the MICS file: " + micsFile.getAbsolutePath());
        } catch (Exception e) {
            logError("Unable to parse the MICS file: " + micsFile, e);
            throw new MicsParserException(LOGGER_COMPONENT + " Unable to parse the MICS file: " + micsFile.getName(), e);
        }
        logInfo("Successfully parsed the MICS file: " + micsFile);
        return mics;
    }

    /**
     * Parse the MICS File into an internal representation and verify the MICS in regard to the checks specified in TR-03116-TS.
     *
     * @param micsFile         The MICS File to verify.
     * @param certificateFiles A list of certificate Files specified by the MICS.
     * @return True, if the MICS could be successfully verified.
     * @throws MicsParserException If an error occurs while parsing the MICS.
     */
    public boolean verifyMICS(final File micsFile, final File... certificateFiles) throws MicsParserException {
        // Parse the MICS file into internal data structure.
        MICS mics = parseMICS(micsFile);
        return verifyMICS(mics, certificateFiles);
    }

    /**
     * Verify the MICS in regard to the checks specified in TR-03116-TS.
     *
     * @param mics             internal representation of the MICS to verify.
     * @param certificateFiles A list of certificate Files specified by the MICS.
     * @return True, if the MICS could be successfully verified.
     */
    public boolean verifyMICS(final MICS mics, final File... certificateFiles) {
        // Check if not File not null.
        if (mics == null) {
            throw new NullPointerException(LOGGER_COMPONENT + " Provided MICS file is \"null\".");
        }
        logInfo("Verifying the MICS file..");
        logDebug("Checking if \"CHECK_CERTS\" profile is set in the MICS file.");
        var verifyCertificates = mics.getProfiles().contains(CHECK_CERTS_PROFILE);
        if (verifyCertificates) {
            logDebug("Found \"CHECK_CERTS\" profile in the MICS file.");
            logDebug("Verifying the existence of provided certificate files.");
            // Check if provided certificates are not null and exist.
            for (var file : certificateFiles) {
                if (file == null) {
                    logError("Provided certificate file is \"null\".");
                }
                if (!file.exists() || file.isDirectory()) {
                    logError("Provided certificate file is a directory or does not exists: "
                            + file.getAbsolutePath());
                }
            }
            logDebug("Existence of provided certificate files successfully verified.");
        }

		// Find ApplicationSpecification and application mapping for the MICS file
		logDebug("Searching for applicable application specification and application mapping.");
		TlsSpecification applicableSpecification;
		AppMapping applicableAppMapping;
		try {
			applicableSpecification = getApplicableTlsSpecification(mics);
            logDebug("Found applicable application specification.");
			applicableAppMapping = getApplicableAppMapping(mics);
			logDebug("Found applicable application mapping.");
		} catch (Exception e) {
			logError("An error occurred while trying to verify the MICS file.", e);
			throw new RuntimeException(LOGGER_COMPONENT + " Error occurred while trying to verify the MICS file.", e);
		}
		try {
			// DUT Verifier
            verifyDUT(mics);

            // Verify MICS Composition
            if (!verifyMICSSections(mics, applicableAppMapping)) {
                return false;
			}

			// Application profile verifier
            if (!verifyApplicationProfiles(mics, applicableAppMapping)) {
				return false;
			}

            // The MICS Checklist Verifier
            var micsChecklistSummary = executeMICSChecklist(mics, applicableSpecification);

            // X.509 Certificate Verifier
            Summary certificateVerificationSummary = null;
            if (verifyCertificates) {
                certificateVerificationSummary = executeCertificateVerification(mics, applicableSpecification, certificateFiles);
            } else {
                logInfo("CHECK_CERTS profile is not specified. Skipping the certificate verification.");
            }

            // Combine results.
            var verificationResult = verifyCertificates ? certificateVerificationSummary.wasSuccessful() && micsChecklistSummary.wasSuccessful() : micsChecklistSummary.wasSuccessful();
            if (verificationResult) {
                logInfo("Successfully verified the MICS file.");
            } else {
                logInfo("Failure while verifying the MICS file.");
            }
            micsChecklistSummary.printTestSuiteSummary();
            if (verifyCertificates) {
                certificateVerificationSummary.printTestSuiteSummary();
            }
            return verificationResult;
        } catch (Exception e) {
            logError("An error occurred while trying to verify the provided MICS file.", e);
            throw new RuntimeException(LOGGER_COMPONENT + " Error occurred while trying to verify the provided MICS file.", e);
        }
    }

    private Summary executeCertificateVerification(MICS mics, TlsSpecification applicableSpecification, File[] certificateFiles) {
        Summary certificateVerificationSummary;
        final String certVerificationSummaryDescription = "Certificate Verification Checks";
        logInfo("Verifying the TLS certificates of the device.");
        try {
            certificateVerificationSummary
                    = new Summary(X509CertificateVerifier.verifyCertificatesFromMics(mics,
                    applicableSpecification, certificateFiles), certVerificationSummaryDescription);
            if (!certificateVerificationSummary.wasSuccessful()) {
                logError("Verification of TLS Certificates has failed.");
            } else {
                logInfo("Verification of the TLS certificates was successful.");
            }
        } catch (Exception e) {
            logError("An error occurred when trying to run checks on Certificates.", e);
            certificateVerificationSummary = new Summary(null, certVerificationSummaryDescription);
        }
        return certificateVerificationSummary;
    }

    private Summary executeMICSChecklist(MICS mics, TlsSpecification applicableSpecification) {
        logInfo("Running the MICS file checklist tests on the provided MICS file.");
        var micsChecklistSummary
                = new Summary(MICSChecklistVerifier.verifyMicsChecklist(mics, applicableSpecification),
                "ICS Verification Checks");
        if (!micsChecklistSummary.wasSuccessful()) {
            logError("Unable to verify the MICS file checklist.");
        } else {
            logInfo("The MICS file checklist was successfully verified.");
        }
        return micsChecklistSummary;
    }

    private boolean verifyApplicationProfiles(MICS mics, AppMapping applicableAppMapping) {
        logInfo("Verifying the application profiles in the MICS file.");
        if (!ApplicationProfileVerifier.verifyApplicationProfilesFromMics(applicableAppMapping,
                mics)) {
            logError("ApplicationProfileVerifier was unable to verify the application profiles.");
            return false;
        }
        logInfo("Verification of application profiles successful.");
        return true;
    }

    private boolean verifyMICSSections(MICS mics, AppMapping applicableAppMapping) {
        logInfo("Verifying the ICS Sections in the MICS file.");
        if (!ApplicationProfileVerifier.verifyMicsCompositionForApplicationType(applicableAppMapping,
                mics)) {
            logError("ApplicationProfileVerifier was unable to verify the ICS Sections in the MICS file.");
            return false;
        }
        logInfo("Verification of the ICS Sections in the MICS file was successful.");
        return true;
    }

    private void verifyDUT(MICS mics) {
        logInfo("Verifying the Device Under Test.");
        if (!DUTVerifier.verifyDUTFromMics(mics)) {
            logError("DUTVerifier was unable to verify the DUT.");
            throw new RuntimeException("DUTVerifier: Unable to verify the DUT.");
        }
        logInfo("Successfully verified the application specific data for the Device Under Test.");
    }


    /**
     * Return the MICSConfiguration which this MICSVerifier is using internally.
     *
     * @return MICSConfiguration which this MICSVerifier is using internally.
     */
    public MICSConfiguration getConfiguration() {
        return configuration;
    }

    private TlsSpecification getApplicableTlsSpecification(final MICS mics) {
        for (var applicationSpec : configuration.applicationSpecifications) {
            if (applicationSpec.getId().equalsIgnoreCase(mics.getApplicationType())) {
                return applicationSpec;
            }
        }
        logError("No specification for application type provided in the MICS file: " + mics.getApplicationType());
        throw new IllegalArgumentException(
                "No specification for application type provided in the MICS file: " + mics.getApplicationType());
    }

    private AppMapping getApplicableAppMapping(final MICS mics) {
        for (var appMappings : configuration.applicationMappings) {
            if (appMappings.id.equalsIgnoreCase(mics.getApplicationType())) {
                return appMappings;
            }
        }
        logError("No application mapping for application type is provided in the MICS file: " + mics.getApplicationType());
        throw new IllegalArgumentException(
                "No application mapping for application type is provided in the MICS file: " + mics.getApplicationType());
    }

    private void logError(final String msg, final Throwable t) {
        logger.error(LOGGER_COMPONENT + msg, t);
    }

    private void logError(final String msg) {
        logger.error(LOGGER_COMPONENT + msg);
    }

    private void logWarning(final String msg) {
        logger.warning(LOGGER_COMPONENT + msg);
    }

    private void logInfo(final String msg) {
        logger.info(LOGGER_COMPONENT + msg);
    }

    private void logDebug(final String msg) {
        logger.debug(LOGGER_COMPONENT + msg);
    }

}
