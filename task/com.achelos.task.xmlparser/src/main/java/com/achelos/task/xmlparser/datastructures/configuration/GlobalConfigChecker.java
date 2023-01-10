package com.achelos.task.xmlparser.datastructures.configuration;

import java.util.HashMap;


/**
 * Helper class for accessing and checking Global Configuration instances.
 */
public class GlobalConfigChecker {
	/**
	 * Hide Constructor. Static helper class.
	 */
	private GlobalConfigChecker() {
		// Hide Constructor. Static helper class.
	}

	/**
	 * Checks a global configuration and fills in default values.
	 *
	 * @param globalConfig The global configuration to check.
	 * @throws IllegalArgumentException If the global configuration is not valid.
	 */
	public static void checkGlobalConfig(final HashMap<String, GlobalConfigParameter> globalConfig)
			throws IllegalArgumentException {

		// Check that all mandatory global configurations are present.
		for (var globalConfigName : GlobalConfigParameterNames.values()) {
			if (globalConfigName.isMandatory()) {
				boolean isValid = true;
				if (!isGlobalConfigParameterSet(globalConfig, globalConfigName)) {
					isValid = false;
				} else {
					var mandatoryParameter = getGlobalConfigParameter(globalConfig, globalConfigName);
					if (mandatoryParameter.getType() == GlobalConfigParameter.ConfigParameterType.STRING) {
						var stringValue = mandatoryParameter.getValueAsString();
						if (stringValue == null || stringValue.isEmpty()) {
							isValid = false;
						}
					}
				}
				if (!isValid) {
					throw new IllegalArgumentException("Unspecified required global configuration: "
							+ globalConfigName.getParameterName());
				}
			}
		}

		// Check that all specified global configurations have the correct value type.
		for (var configEntry : globalConfig.entrySet()) {
			var globalConfigParamName = GlobalConfigParameterNames.getConfigFromId(configEntry.getKey());
			if (globalConfigParamName == null) {
				continue;
			}
			if (configEntry.getValue().getType() != globalConfigParamName.getType()) {
				throw new IllegalArgumentException("Illegal type of global configuration: "
						+ globalConfigParamName.getParameterName() + ". Is: " + configEntry.getValue().getType().name()
						+ " Should be: " + globalConfigParamName.getType().name());
			}
		}

		// Fill in default values.
		for (var globalConfigName : GlobalConfigParameterNames.values()) {
			if ((globalConfigName.getDefaultValue() == null) || globalConfig.containsKey(globalConfigName.getParameterName())) {
				continue;
			}
			globalConfig.put(globalConfigName.getParameterName(), globalConfigName.getDefaultValue());
		}
	}

	/**
	 * Checks a global configuration and fills in default values if it is valid.
	 *
	 * @param globalConfig The global configuration to check.+
	 * @return True if the global configuration is valid, false otherwise.
	 */
	public static boolean isGlobalConfigValid(final HashMap<String, GlobalConfigParameter> globalConfig) {
		try {
			checkGlobalConfig(globalConfig);
		} catch (Exception ignored) {
			return false;
		}
		return true;
	}

	/**
	 * Checks whether a global configuration contains a specific global configuration parameter name.
	 *
	 * @param globalConfig The global configuration to check.
	 * @param paramName The parameter to check.
	 * @return True if it is contained, false otherwise.
	 */
	public static boolean isGlobalConfigParameterSet(final HashMap<String, GlobalConfigParameter> globalConfig,
			final GlobalConfigParameterNames paramName) {
		if (globalConfig == null) {
			throw new NullPointerException("The global configuration file is \"null\"!");
		}
		return globalConfig.containsKey(paramName.getParameterName());
	}

	/**
	 * Retrieve the value of a Parameter from the global configuration file.
	 *
	 * @param globalConfig The global configuration
	 * @param paramName The parameter to retrieve.
	 * @return The Value of the parameter.
	 */
	public static GlobalConfigParameter getGlobalConfigParameter(
			final HashMap<String, GlobalConfigParameter> globalConfig, final GlobalConfigParameterNames paramName) {
		if (globalConfig == null) {
			throw new NullPointerException("The global configuration file is \"null\"!");
		}

		var value = globalConfig.get(paramName.getParameterName());
		if (value == null) {
			throw new IllegalArgumentException(
					"Parameter " + paramName.getParameterName() + " is not "
							+ "contained in the global configuration file.");
		}
		return value;

	}
}
