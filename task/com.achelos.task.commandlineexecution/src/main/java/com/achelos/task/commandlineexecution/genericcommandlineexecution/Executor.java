/**
 *
 */
package com.achelos.task.commandlineexecution.genericcommandlineexecution;

/**
 * Public enumeration holds all known executors, their file names with extensions for report.
 */
public enum Executor {
	/** "TLS Test Tool". */
	TLSTESTTOOL("TLS Test Tool", "_tls-tool", ".log"),
	/** CRL Responder. */
	CRL("CRL Responder ", "_crl", ".log"),
	/** OCSP Server. */
	OCSPSERVER("OCSP Server", "_ocsp_server", ".log"),
	/** OCSP Request. */
	OCSPREQUEST("OCSP Request", "_cached_ocsp_reponse", ""),
	/** TShark. */
	TSHARK("TShark", "_tshark-capture", ".pcap");

	private String name;
	private String logFileName;
	private String fileExtension;

	/**
	 * The constructor of the cipher suite enumeration.
	 *
	 * @param name The executor name.
	 * @param logFileName The log file name.
	 * @param fileExtension The log file extension.
	 */
	Executor(final String name, final String logFileName, final String fileExtension) {
		this.name = name;
		this.logFileName = logFileName;
		this.fileExtension = fileExtension;
	}

	/**
	 * Returns the name of this executor. e.g <b> TLS Test Tool, CRL Responder, TShark </b> etc. 
	 * 
	 * @return name of this executor.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns the log file name of this executor.
	 * 
	 * @return the log file name of this executor.
	 */
	public final String getLogFileName() {
		return logFileName;
	}

	/**
	 * Returns the log file extension of this executor.
	 * 
	 * @return the log file extension of this executor.
	 */
	public String getFileExtension() {
		return fileExtension;
	}

}
