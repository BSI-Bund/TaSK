package com.achelos.task.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class contains formatting methods for timestamps.
 *
 */
public class DateTimeUtils {

	/**
	 * Hidden Constructor
	 */
	private DateTimeUtils() {
		//Empty.
	}

	/**
	 * yyyy-MM-dd'T'HH:mm:ss
	 */
	public static final String ISO_8601_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	/**
	 * yyyy-MM-dd'T'HH:mm:ss.SSS
	 */
	public static final String ISO_8601_DATE_TIME_PATTERN_MILLISECONDS = "yyyy-MM-dd'T'HH:mm:ss.SSS";


	/**
	 * Pattern for creating files and directories. yyyy-MM-dd'T'HHmmss
	 */
	public static final String ISO_8601_DATE_TIME_PATTERN_FOR_DIRECTORIES = "yyyy-MM-dd'T'HHmmss";

	/**
	 * Return the formatted current date and time e.g. yyyy-MM-dd'T'HHmmss.
	 * Returned date and time shall be formatted like '2022-06-14T102656'
	 * @return formatted current date and time e.g. yyyy-MM-dd'T'HHmmss
	 */
	public static String getTimeStampForFileAndDirectoryNames() {
		return new SimpleDateFormat(ISO_8601_DATE_TIME_PATTERN_FOR_DIRECTORIES)
				.format(new Date());

	}

	/**
	 * Return the formatted current date and time e.g. yyyy-MM-dd'T'HH:mm:ss.
	 * Returned date and time shall be formatted like '2022-06-14T11:03:36'
	 * @return formatted current date and time e.g yyyy-MM-dd'T'HH:mm:ss
	 */
	public static String getISOFormattedTimeStamp() {
		return new SimpleDateFormat(ISO_8601_DATE_TIME_PATTERN).format(new Date());
	}

}
