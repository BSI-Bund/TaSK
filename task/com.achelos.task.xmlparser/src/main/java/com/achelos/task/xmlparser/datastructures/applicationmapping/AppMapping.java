package com.achelos.task.xmlparser.datastructures.applicationmapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.achelos.task.xmlparser.configparsing.StringHelper;

import generated.jaxb.configuration.ApplicationMapping;


/**
 * Internal data structure representing the mapping of Application Specifications to a list of mandatory and recommended test profiles.
 */
public class AppMapping {

	/**
	 * The ID of the Application Mapping.
	 */
	public String id;
	/**
	 * The Title of the Application Mapping.
	 */
	public String title;
	/**
	 * The Version of the Application Mapping.
	 */
	public String version;
	/**
	 * The description of the Application Mapping.
	 */
	public String description;
	/**
	 * A List of references of the Application Mapping.
	 */
	public List<String> references;
	/**
	 * The ID of the related Application Specification.
	 */
	public String baseSpecId;
	/**
	 * A list of Application Specific Inspection Instructions.
	 */
	public List<String> appSpecificInspectionInstructions;
	/**
	 * A list of mandatory Test/Application Profiles.
	 */
	public List<String> mandatoryProfiles;
	/**
	 * A list of recommended Test/Application Profiles.
	 */
	public List<String> recommendedProfiles;
	/**
	 * A list of mandatory ICS Sections.
	 */
	private final List<ICSSection> mandatoryICSSections = new LinkedList<>();;
	/**
	 * A list of optional ICS Sections.
	 */
	private List<ICSSection> optionalICSSections;

	private AppMapping(final String id, final String title, final String version, final String description) {
		this.id = id;
		this.title = title;
		this.version = version;
		this.description = description;
	}

	/**
	 * Parse the JAXB representation of the ApplicationMapping XML file into an internal data representation
	 * @param applicationMapping AXB representation of the ApplicationMapping XML file
	 * @return n internal data representation containing the same information.
	 */
	public static AppMapping parseFromJaxb(final ApplicationMapping applicationMapping) {
		if (applicationMapping == null) {
			return null;
		}
		var appMapping = new AppMapping(applicationMapping.getId(), applicationMapping.getTitle(),
				applicationMapping.getVersion(), applicationMapping.getDescription());

		appMapping.references = applicationMapping.getReference();

		appMapping.appSpecificInspectionInstructions
				= applicationMapping.getApplicationSpecificInspectionInstructions() != null
						? Arrays.asList(applicationMapping.getApplicationSpecificInspectionInstructions().getText())
						: new ArrayList<>();
		appMapping.baseSpecId
				= applicationMapping.getApplicationSpecificInspectionInstructions() != null
						? applicationMapping.getApplicationSpecificInspectionInstructions().getBaseSpecification()
						: "";

		var profiles = applicationMapping.getProfiles();
		appMapping.mandatoryProfiles = profiles.getMandatoryProfiles().getProfile();
		appMapping.recommendedProfiles = profiles.getRecommendedProfiles() != null
				? profiles.getRecommendedProfiles().getProfile() : new ArrayList<>();

		var icsSections = applicationMapping.getICSSections();
		for (var sectionId : icsSections.getMandatoryICSSections().getSection()) {
			var icsSection = ICSSection.getICSSectionFromSectionNumber(sectionId);
			if (icsSection == null) {
				throw new RuntimeException("Unable to match mandatory ICS Section: " + sectionId);
			}
			appMapping.mandatoryICSSections.add(icsSection);
		}
		appMapping.optionalICSSections = new LinkedList<>();
		if (icsSections.getOptionalICSSections() != null) {
			for (var sectionId : icsSections.getOptionalICSSections().getSection()) {
				var icsSection = ICSSection.getICSSectionFromSectionNumber(sectionId);
				if (icsSection == null) {
					throw new RuntimeException("Unable to match optional ICS Section: " + sectionId);
				}
				appMapping.mandatoryICSSections.add(icsSection);
			}
		}

		return appMapping;
	}

	@Override
	public String toString() {
		var appMappingAsString = new StringBuilder("Application mapping: " + id + System.lineSeparator());
		StringHelper.appendAttrToStringBuilder(appMappingAsString, "ID", id);
		StringHelper.appendAttrToStringBuilder(appMappingAsString, "Title", title);
		StringHelper.appendAttrToStringBuilder(appMappingAsString, "Version", version);
		StringHelper.appendAttrToStringBuilder(appMappingAsString, "Description", description);
		StringHelper.appendListToStringBuilder(appMappingAsString, "References", references, 1);
		StringHelper.appendAttrToStringBuilder(appMappingAsString, "Base Specification", baseSpecId);
		StringHelper.appendListToStringBuilder(appMappingAsString, "Application Specific Inspection Instructions",
				appSpecificInspectionInstructions, 1);
		StringHelper.appendListToStringBuilder(appMappingAsString, "Mandatory Profiles", mandatoryProfiles, 1);
		StringHelper.appendListToStringBuilder(appMappingAsString, "Recommended Profiles", recommendedProfiles, 1);
		StringHelper.appendListToStringBuilder(appMappingAsString, "Mandatory ICS Sections", mandatoryICSSections,
				1);
		StringHelper.appendListToStringBuilder(appMappingAsString, "Optional ICS Sections", optionalICSSections,
				1);
		return appMappingAsString.toString();
	}

	/**
	 * Return the Mandatory ICS Sections.
	 * @return the Mandatory ICS Sections.
	 */
	public List<ICSSection> getMandatoryICSSections() {
		return new ArrayList<>(mandatoryICSSections);
	}

	/**
	 * Return the Optional ICS Sections.
	 * @return the Optional ICS Sections.
	 */
	public List<ICSSection> getOptionalICSSections() {
		return new ArrayList<>(optionalICSSections);
	}

}
