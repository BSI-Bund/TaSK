package com.achelos.task.xmlparser.datastructures.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import generated.jaxb.configuration.Configuration.Parameters.Parameter;
import jakarta.xml.bind.DatatypeConverter;

/**
 * Class representing a Parameter of a GlobalConfiguration.
 */
public class GlobalConfigParameter {

	/**
	 * Enumeration specifying the potential types of a ConfigParameter.
	 */
	public enum ConfigParameterType {
		/**
		 * Integer type.
		 */
		INT,
		/**
		 * Boolean parameter type.
		 */
		BOOLEAN,
		/**
		 * String parameter type.
		 */
		STRING,
		/**
		 * Hex-string parameter type.
		 */
		HEXSTRING,
		/**
		 * File parameter type.
		 */
		FILE
	}

	private final List<String> values;
	private final ConfigParameterType type;
	private final String id;

	GlobalConfigParameter(final String value, final ConfigParameterType type, final String id) {
		this.values = Collections.singletonList(value);
		this.type = type;
		this.id = id;
	}

	GlobalConfigParameter(final List<String> values, final ConfigParameterType type, final String id) {
		this.values = values;
		this.type = type;
		this.id = id;
	}

	/**
	 * Parse the Jaxb generated class parameter into an {@link GlobalConfigParameter} object.
	 *
	 * @param parameter instance of Jaxb generated class parameter
	 * @return ConfigParameter object parsed from provided parameter.
	 */
	public static GlobalConfigParameter parseConfigParameterFromJaxb(final Parameter parameter) {
		var id = parameter.getId();
		List<String> values = new LinkedList<>();
		ConfigParameterType type;
		if (parameter.getInt() != null && !parameter.getInt().isEmpty()) {
			for (var value : parameter.getInt()) {
				values.add(value.toString());
			}
			type = ConfigParameterType.INT;
		} else if (parameter.getFile() != null && !parameter.getFile().isEmpty()) {
			values = parameter.getFile();
			type = ConfigParameterType.FILE;
		} else if (parameter.getHexstring() != null && !parameter.getHexstring().isEmpty()) {
			for (var hexValue : parameter.getHexstring()) {
				values.add(DatatypeConverter.printHexBinary(hexValue));
			}
			type = ConfigParameterType.HEXSTRING;
		} else if (parameter.getBoolean() != null && !parameter.getBoolean().isEmpty()) {
			for (var boolValue : parameter.getBoolean()) {
				values.add(boolValue.toString());
			}
			type = ConfigParameterType.BOOLEAN;
		} else {
			values = parameter.getString();
			type = ConfigParameterType.STRING;
		}

		return new GlobalConfigParameter(values, type, id);
	}

	/**
	 * Return the Value of the parameter as String Object.
	 *
	 * @return Value as String
	 * @throws IllegalArgumentException if type is not STRING
	 */
	public String getValueAsString() {
		if (type != ConfigParameterType.STRING) {
			throw new IllegalArgumentException("ConfigurationParameter is not of type String.");
		}
		return values.isEmpty() ? "" : values.get(0);
	}

	/**
	 * Return the Values of the Parameter as List of String Objects.
	 *
	 * @return List of Values as Strings
	 * @throws IllegalArgumentException if type is not STRING
	 */
	public List<String> getValueAsStringList() {
		if (type != ConfigParameterType.STRING) {
			throw new IllegalArgumentException("ConfigurationParameter is not of type String.");
		}
		return new ArrayList<>(values);
	}

	/**
	 * Return the Value of the parameter as Integer Object.
	 *
	 * @return Value as Integer
	 * @throws IllegalArgumentException if type is not INT
	 */
	public Integer getValueAsInteger() {
		if (type != ConfigParameterType.INT) {
			throw new IllegalArgumentException("ConfigurationParameter is not of type Integer.");
		}
		var value = values.isEmpty() ? "" : values.get(0);
		return Integer.parseInt(value);
	}

	/**
	 * Return the Value of the parameter as Boolean Object.
	 *
	 * @return Value as Boolean
	 * @throws IllegalArgumentException if type is not BOOLEAN
	 */
	public Boolean getValueAsBoolean() {
		if (type != ConfigParameterType.BOOLEAN) {
			throw new IllegalArgumentException("ConfigurationParameter is not of type Boolean.");
		}
		var value = values.isEmpty() ? "" : values.get(0);
		return Boolean.parseBoolean(value);
	}

	/**
	 * Return the Value of the parameter as HexString.
	 *
	 * @return Value as HexString
	 * @throws IllegalArgumentException if type is not HexString
	 */
	public String getValueAsHexString() {
		if (type != ConfigParameterType.HEXSTRING) {
			throw new IllegalArgumentException("ConfigurationParameter is not of type HexString.");
		}
		var value = values.isEmpty() ? "" : values.get(0);
		return value;
	}

	/**
	 * Return the Value of the parameter as File Object.
	 *
	 * @return Value as File
	 * @throws IllegalArgumentException if type is not FILE
	 */
	public File getValueAsFile() {
		if (type != ConfigParameterType.FILE) {
			throw new IllegalArgumentException("ConfigurationParameter is not of type File.");
		}
		var value = values.isEmpty() ? "" : values.get(0);
		return new File(value);
	}

	@Override
	public String toString() {
		return values.toString();
	}

	/**
	 * Get all stored values for this Global Configuration Parameter object.
	 * @return a list of all stored values for this Global Configuration Parameter object.
	 */
	public List<String> getValues() {
		return new ArrayList<>(values);
	}

	/**
	 * Get the type of this Global Configuration Parameter object.
	 * @return the type of this Global Configuration Parameter object.
	 */
	public ConfigParameterType getType() {
		return type;
	}

	/**
	 * Get the Global Configuration Parameter Name of this object.
	 * @return The Global Configuration Parameter Name of this object.
	 */
	public String getId() {
		return id;
	}
}
