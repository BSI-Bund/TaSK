package com.achelos.task.utilities.logging;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.achelos.task.utilities.DateTimeUtils;


/**
 * This class is used to create and hold Log messages.
 */
public class LogBean {

	/**
	 * Log Severity enumeration.
	 */
	public enum LogSeverity {
		/** High severity. */
		HIGH,
		/** MEDIUM severity. */
		MEDIUM,
		/** LOW severity. */
		LOW;
	}

	private final Date timestamp;
	private final LogSeverity severity;
	private final String origin;
	private final String message;

	/**
	 * Default constructor.
	 *
	 * @param timeStamp Log timestamp.
	 * @param severity Log severity.
	 * @param origin Log origin.
	 * @param message Log message.
	 */
	public LogBean(final Date timeStamp, final String severity, final String origin, final String message) {
		timestamp = (Date) timeStamp.clone();
		this.severity = LogSeverity.valueOf(severity);
		this.origin = origin;
		this.message = message;
	}

	/**
	 * Default constructor.
	 *
	 * @param message Log message.
	 */
	public LogBean(final String message) {
		timestamp = null;
		severity = null;
		origin = null;
		this.message = message;
	}


	/**
	 * @return the timestamp
	 */
	public final Date getTimestamp() {
		if (timestamp != null) {
			Date clone = (Date) timestamp.clone();
			return clone;
		}
		return null;
	}


	/**
	 * @return Formatted Date into a date/time string.
	 */
	public final String getTimestampString() {
		return getDateFormat().format(timestamp);
	}


	/**
	 * @return the severity
	 */
	public final LogSeverity getSeverity() {
		return severity;
	}


	/**
	 * @return the origin
	 */
	public final String getOrigin() {
		return origin;
	}


	/**
	 * @return the message
	 */
	public final String getMessage() {
		return message;
	}


	@Override
	public final String toString() {
		if (timestamp == null) {
			return message;
		}
		return getTimestampString() + " " + origin + " " + message;
	}

	private static DateFormat dateFormat = null;

	/**
	 * @return the formatted date "yyyy-MM-dd HH:mm:ss.SSS"
	 */
	private static DateFormat getDateFormat() {
		if (null == dateFormat) {
			dateFormat = new SimpleDateFormat(DateTimeUtils.ISO_8601_DATE_TIME_PATTERN_MILLISECONDS);
		}
		return dateFormat;
	}

	/**
	 * Converts the given logList into {@link LogBean} objects list. <br>
	 * This method Trims and then Splits each string element of the list using "\t" (tab) delimiter, Any list item
	 * having less than 4 parts after spilt, will be ignored and will not be added to the returning list. As it is
	 * required by {@link LogBean#LogBean(Date, String, String, String)}.
	 *
	 * @param logList The log list to convert.
	 * @return the list containing TlsLogBean elements or null if an empty list is passed.
	 */
	public static ArrayList<LogBean> convertToLogBeanList(final List<String> logList) {
		final ArrayList<LogBean> result = new ArrayList<>();
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				DateTimeUtils.ISO_8601_DATE_TIME_PATTERN_MILLISECONDS);
		if (logList.isEmpty()) {
			return result;
		}
		for (final String item : logList) {
			final String[] parts = item.trim().split("\t");
			if (parts.length < 4) { // CRLExecutor Server output.
				result.add(new LogBean(parts[0]));
				continue;
			}
			Date date;
			try {
				date = simpleDateFormat.parse(parts[0]);
			} catch (final ParseException e) {
				continue;
			}
			result.add(new LogBean(date, parts[1], parts[2], parts[3]));
		}
		return result;
	}
}
