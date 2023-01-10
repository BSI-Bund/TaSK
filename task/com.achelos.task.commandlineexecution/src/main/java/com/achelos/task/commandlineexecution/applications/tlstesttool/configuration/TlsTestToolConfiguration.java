package com.achelos.task.commandlineexecution.applications.tlstesttool.configuration;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.achelos.task.commandlineexecution.genericcommandlineexecution.Executor;
import com.achelos.task.commandlineexecution.genericcommandlineexecution.Logging;
import com.achelos.task.logging.BasicLogger;
import com.achelos.task.logging.LoggingConnector;


/**
 * Representation of configuration options for the TLS Test Tool.
 */
public class TlsTestToolConfiguration extends Logging {
	private final Map<TlsTestToolConfigurationOption, String> options
			= new HashMap<>();

	/**
	 * Constructor. Instantiates Logging connector.
	 */
	public TlsTestToolConfiguration() {
		this(LoggingConnector.getInstance());
	}


	/**
	 * Constructor.
	 *
	 * @param logger Logger for logging the messages.
	 */
	public TlsTestToolConfiguration(final BasicLogger logger) {
		super(logger, Executor.TLSTESTTOOL);
	}


	/**
	 * Write the configuration options to a plain text file that can be read by the TLS Test Tool.
	 *
	 * @param configFile File that will be written
	 */
	public final void writeTo(final Path configFile) {
		final String headerText = "TLS Test Tool configuration file";
		try {
			final StringBuilder sb = new StringBuilder();
			sb.append("# ");
			sb.append(headerText);
			sb.append('\n');
			for (Entry<TlsTestToolConfigurationOption, String> entry : options.entrySet()) {
				logDebug("Writing configuration option " + entry.getKey() + "=" + entry.getValue() + ".");
				sb.append(entry.getKey().toString());
				sb.append('=');
				sb.append(entry.getValue());
				sb.append('\n');
			}

			final FileChannel channel = FileChannel.open(configFile, StandardOpenOption.WRITE,
					StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			final ByteBuffer buffer = ByteBuffer.wrap(sb.toString().getBytes(StandardCharsets.UTF_8));
			while (buffer.hasRemaining()) {
				channel.write(buffer);
			}
			channel.close();
			logDebug("Configuration file created: " + configFile);
		} catch (Exception e) {
			logError("Cannot create configuration file", e);
		}
	}


	/**
	 * Set a configuration option and assign a value.
	 *
	 * @param name The option's name.
	 * @param value The option's value.
	 */
	public final void setOption(final TlsTestToolConfigurationOption name, final String value) {
		options.put(name, value);
	}


	/**
	 * Get the value of a configuration option.
	 *
	 * @param name The option's name.
	 * @return The option's value, or {@code null}, if it is not set.
	 */
	public final String getOption(final TlsTestToolConfigurationOption name) {
		return options.get(name);
	}


	/**
	 * Remove all options.
	 */
	public final void clear() {
		options.clear();
	}

}
