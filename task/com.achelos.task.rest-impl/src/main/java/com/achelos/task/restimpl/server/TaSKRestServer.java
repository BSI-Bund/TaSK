package com.achelos.task.restimpl.server;

import com.achelos.task.logging.LoggingConnector;
import com.achelos.task.restimpl.api.ExecuteApi;
import com.achelos.task.restimpl.responsefilter.CORSContainerResponseFilter;
import com.sun.net.httpserver.HttpServer;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.net.URI;

public class TaSKRestServer {

	private static final String LOGGING_PREFIX = "TaSK REST Server: ";

	private final LoggingConnector logger;
	
	private final HttpServer server;
	
	private final TesttoolRunner testtoolRunner;
	
	private final static String API_IMPL_PACKAGE = ExecuteApi.class.getPackageName();
	
	private TaSKRestServer(final URI uri, final SSLContext sslContext, final LoggingConnector logger, final File globalConfigFile) {
		this.logger = logger;
		final ResourceConfig configuration = new ResourceConfig().packages(API_IMPL_PACKAGE).register(MultiPartFeature.class);
		configuration.register(new CORSContainerResponseFilter());
		server = JdkHttpServerFactory.createHttpServer(uri, configuration, sslContext, false);

		// Initialize TesttoolRequestResource
		TesttoolRequestResource.initReportDir(globalConfigFile);
		// Initialize TesttoolRunner instance.
		testtoolRunner = new TesttoolRunner(globalConfigFile);
		
		final TaSKRestServer thisService = this;
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.debug(LOGGING_PREFIX +"About to STOP REST-Server at \"" + server.getAddress() + "\"");
				thisService.stop();
			}
		});
	}

	public TaSKRestServer(final int port, final SSLContext sslContext, final LoggingConnector logger, final File globalConfigFile) {
		this(buildAndCheckUri(port, sslContext), sslContext, logger, globalConfigFile);
	}

	private static URI buildAndCheckUri(final int port, final SSLContext sslContext) {
		final int MAX_PORT_NUMBER = 65535; // 2^16 - 1

		// Check port.
		if (port < 0 || port > MAX_PORT_NUMBER) {
			throw new RuntimeException("TaSK Framework was executed in server mode, but no server port could not be verified.");
		}

		// Build URI.
		String scheme;
		if (sslContext == null) {
			scheme = "http";
		} else {
			scheme = "https";
		}

		return URI.create(scheme + "://" + "0.0.0.0" + ":" + port + "/");
	}

	
	public void start() {
		logger.debug(LOGGING_PREFIX + "STARTING REST-Server at \"" + server.getAddress() + "\"");
		this.server.start();
		new Thread(testtoolRunner).start();
		logger.info(LOGGING_PREFIX + "STARTED REST-Server at \"" + server.getAddress() + "\"");
	}
	
	public void stop() {
		logger.debug(LOGGING_PREFIX + "STOPPING REST-Server at \"" + server.getAddress() + "\"");
		try {
			server.stop(0);
		} catch (final Exception e) {
			// Ignore.
		}
		try {
			testtoolRunner.cancel();
		} catch (final Exception e) {
			// Ignore.
		}
		logger.info(LOGGING_PREFIX + "STOPPED REST-Server at \"" + server.getAddress() + "\"");
	}

}