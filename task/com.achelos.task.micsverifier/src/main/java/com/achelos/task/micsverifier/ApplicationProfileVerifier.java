package com.achelos.task.micsverifier;

import java.util.ArrayList;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.xmlparser.datastructures.applicationmapping.AppMapping;
import com.achelos.task.xmlparser.datastructures.applicationmapping.ICSSection;
import com.achelos.task.xmlparser.datastructures.mics.MICS;


class ApplicationProfileVerifier {
	/**
	 * Hidden Constructor.
	 */
	private ApplicationProfileVerifier() {
		// Empty.
	}

	/**
	 * Verifies the ApplicationProfiles from MICS.
	 *
	 * @param mics {@link MICS} for the Device Under Test to verify profiles for.
	 * @return true if successfully verified. false otherwise.
	 */
	public static boolean verifyApplicationProfilesFromMics(final AppMapping applicationMapping, final MICS mics) {
		LoggingConnector logger = LoggingConnector.getInstance();

		if (applicationMapping == null || mics == null) {
			throw new IllegalArgumentException("Application mapping or the MICS file is \"null\"!");
		}

		logger.debug(MICSVerifier.LOGGER_COMPONENT + "Verifying if all mandatory profiles are included in the MICS file supported profile list.");
		var profileList = new ArrayList<>(mics.getProfiles());
		// Check if mandatory profiles are set in the MICS supported profiles list.
		for (var profile : applicationMapping.mandatoryProfiles) {
			if (!profileList.contains(profile)) {
				logger.error(
						MICSVerifier.LOGGER_COMPONENT + "Mandatory profile " + profile + " is missing in the MICS file supported profile list.");
				return false;
			}
			profileList.remove(profile);
		}
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "All mandatory profiles are included in the MICS file supported profile list.");

		logger.debug(
				MICSVerifier.LOGGER_COMPONENT + "Verifying if all profiles in the MICS file supported profile are allowed for the applications of type "
						+ mics.getApplicationType() + ".");
		// Check if any additional profiles of the MICS supported profiles list is also
		// in the RecommendedProfiles of the
		// application type.
		for (var profile : profileList) {
			if (!applicationMapping.recommendedProfiles.contains(profile)) {
				logger.error(
						"Profile " + profile + " is not allowed in Application of Type " + mics.getApplicationType());
				return false;
			}
		}
		logger.debug(MICSVerifier.LOGGER_COMPONENT + "All profiles in the MICS the supported profile are allowed for this type of application.");

		return true;
	}

	/**
	 * Verify, that the MICS contains all required ICS Sections.
	 * Verify, that the MICS contains only allowed ICS Sections.
	 * @param applicationMapping The ApplicationMapping of the Application.
	 * @param mics The MICS.
	 * @return True if the MICS contains all required and only allowed ICS Sections. False otherwise.
	 */
	public static boolean verifyMicsCompositionForApplicationType(final AppMapping applicationMapping, final MICS mics) {
		// Go through each section and check if it is present in MICS.
		LoggingConnector logger = LoggingConnector.getInstance();
		var verificationSuccessful = true;
		for (var icsSection : ICSSection.getValuesExceptZeroRTT()) {
			if (applicationMapping.getMandatoryICSSections().contains(icsSection)) {
				// Mandatory
				if (!icsSection.isPresentInMics(mics)) {
					logger.error(MICSVerifier.LOGGER_COMPONENT + "Mandatory ICS Section " + icsSection.getIcsSectionName() + " missing in MICS.");
					verificationSuccessful = false;
				}
			} else if (!applicationMapping.getOptionalICSSections().contains(icsSection)) {
				// Not allowed
				if (icsSection.isPresentInMics(mics)) {
					logger.error(MICSVerifier.LOGGER_COMPONENT + "Not allowed ICS Section " + icsSection.getIcsSectionName() + " present in MICS.");
					verificationSuccessful = false;
				}
			}
				// Optional. Do nothing.
		}

		return verificationSuccessful;
	}
}
