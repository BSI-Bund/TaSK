package com.achelos.task.commandlineexecution.applications.tlstesttool.messagetextresources;

import java.security.InvalidParameterException;


/**
 * Public enumeration holds all known TLS Test Tool messages used for response verification for displaying to the user.
 */
public enum TestToolResource {
	/** after ClientHello. */
	After_ClientHello("after ClientHello"),
	/** afterHandshake. */
	After_Handshake("afterHandshake"),
	/** after ServerHello. */
	After_ServerHello("after ServerHello"),
	/** before ServerHello. */
	Before_ServerHello("before ServerHello"),
	/** ClientHello message received.. */
	ClientHello("ClientHello message received."),
	/**
	 * For TLS1.3 mapped to the same keyword as the TLS1.2 ClientHello.client_version ("ClientHello.client_version").
	 */
	ClientHello_version("ClientHello.client_version"),
	/** ClientHello.extensions. */
	ClientHello_extensions("ClientHello.extensions"),
	/** ClientHello.cipher_suites. */
	ClientHello_cipher_suites("ClientHello.cipher_suites"),
	/** ClientHello.compression_methods. */
	ClientHello_compression_methods("ClientHello.compression_methods"),
	/** ClientHello.session_id. */
	ClientHello_session_id("ClientHello.session_id"),
	/** ClientHello.random. */
	ClientHello_random("ClientHello.random"),
	/** ClientHello.random. */
	Closed_TLS_Session("Closing the TLS session."),
	/** Certificate.certificate_list.size. */
	Certificate_list_size("Certificate.certificate_list.size"),
	/** Valid CertificateRequest message received.. */
	CertificateRequest_valid("Valid CertificateRequest message received."),
	/** Certificate message transmitted.. */
	Certificate_transmitted("Certificate message transmitted."),
	/** Certificate message received.. */
	Certificate_received_valid("Valid Certificate message received."),
	/** Valid CertificateVerify message received.. */
	CertificateVerify_valid("Valid CertificateVerify message received."),
	/** Certificate Status message received by server when using OCSP stapling*/
	Certificate_status_received("Certificate status message received."),
	/** Certificate Status message transmitted by server when using OCSP stapling*/
	Certificate_status_transmitted("Certificate status message transmitted."),
	/** 00. */
	Compression_null("00"),
	/** 01. */
	Compression_DEFLATE("01"),
	/** Valid EncryptedExtension message received. */
	EncryptedExtension_valid("Valid EncryptedExtensions message received."),
	/** Valid EncryptedExtension message received. */
	EncryptedExtension_extensions("EncryptedExtensions.extensions"),
	/** Valid Finished message received.. */
	Finished_valid("Valid Finished message received."),
	/** ServerHelloDone message transmitted.. */
	ServerHelloDone_transmitted("ServerHelloDone message transmitted."),
	/** Valid ServerHelloDone message received.. */
	ServerHelloDone_valid("Valid ServerHelloDone message received."),
	/** Valid ServerHello message received.. */
	ServerHello_valid("Valid ServerHello message received."),
	/** ServerHello.server_version. */
	ServerHello_server_version("ServerHello.server_version"),
	/** ServerHello.extensions. */
	ServerHello_extensions("ServerHello.extensions"),
	/** ServerHello.session_id. */
	ServerHello_session_id("ServerHello.session_id"),
	/** ServerHello.cipher_suite. */
	ServerHello_cipher_suite("ServerHello.cipher_suite"),
	/** ServerHello.compression_method. */
	ServerHello_compression_method("ServerHello.compression_method"),
	/** *) established.. */
	TCP_IP_Conn_to_established("TCP/IP connection to (.*) established."),
	/** Waiting for TCP/IP connection on port. */
	Waiting_TCP_IP_conn_port("Waiting for TCP/IP connection on port"),
	/** TCP/IP connection from. */
	TCP_IP_conn_from("TCP/IP connection from"),
	/** TCP/IP connection is closed.. */
	TCP_IP_Conn_closed("TCP/IP connection is closed."),
	/** TCP/IP connection is closed by DUT before session lifetime is expired. */
	TCP_IP_Conn_closed_before_lifetime_expired("The DUT closed the connection before the session lifetime expired"),
	/** Alert message received.. */
	Alert_message_received("Alert message received."),
	/** Alert.level. */
	Alert_level("Alert.level"),
	/** Alert.description. */
	Alert_description("Alert.description"),
	/** TLS handshake failed. */
	Handshake_failed("TLS handshake failed"),
	/** NewSessionTicket.ticket. */
	NewSessionTicket_ticket("NewSessionTicket.ticket"),
	/** Handshake successful. */
	Handshake_successful("Handshake successful"),
	/** Server handled all connections. */
	Server_handled_all_connections("Server handled all connections"),
	/** Initial handshake finished. Wait for resumption handshake. */
	Initial_handshake_finished_Wait_for_resumption_handshake(
			"Initial handshake finished. Wait for resumption handshake."), 
	/** ServerKeyExchange.params.dh_p. */
	ServerKeyExchange_params_dh_p("ServerKeyExchange.params.dh_p"),
	Heartbeat_message_received("Heartbeat message received"),
	Heartbeat_message_transmitted("Heartbeat message transmitted"),
	OCSP_response_received("OCSP Response Message received"),
	OCSP_response_successful("OCSP Result: successful"),
	No_Error_After_Hs_Finished("No error occured after handshake finished.");


	private String internalToolOutputMessage;

	/**
	 * Set the internal output message of a given enumeration.
	 *
	 * @param internalToolOutputMessage the message
	 */
	TestToolResource(final String internalToolOutputMessage) {
		this.internalToolOutputMessage = internalToolOutputMessage;
	}


	/**
	 * @return The internal message of a given enumeration.
	 */
	public String getInternalToolOutputMessage() {
		return internalToolOutputMessage;
	}


	/**
	 * Method searches an enumeration based on a given message and returns the found object.
	 *
	 * @param message the message to search.
	 * @return The {@link TestToolResource} object.
	 * @exception InvalidParameterException If no TestToolResource object found for the given text.
	 */
	public TestToolResource searchEnumeration(final String message) {
		for (final TestToolResource testToolMessage : TestToolResource.values()) {
			if (testToolMessage.getInternalToolOutputMessage().equalsIgnoreCase(message)) {
				return testToolMessage;
			}
		}
		throw new InvalidParameterException("No TestToolResource object found for the given text: " + message);
	}
}
