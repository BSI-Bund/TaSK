package com.secunet.ipsmall.browser.simulator;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ShellCommandExecutor {


	Process process;
	
	protected ShellCommandExecutor() {
	}
	
	protected boolean isWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("windows");
	}
	
	protected boolean isRunning() {
		return null != process && process.isAlive();
	}
	
	public long execute(String commandline, Path logfile) throws Exception, IOException, InterruptedException, ExecutionException {

		if (null == commandline || commandline.isBlank()) {
			throw new Exception("no command available for execution");
		}
		
		ProcessBuilder builder = new ProcessBuilder();
		
		List<String> cmd = new ArrayList<String>();
		cmd.addAll(Arrays.asList(commandline.split(" ")));
		builder.command(cmd);
		
		builder.directory(new File(System.getProperty("user.home")));

		builder.redirectErrorStream(true);

		if (logfile != null) {
			builder.redirectOutput(Redirect.appendTo(logfile.toFile()));
		}
		process = builder.start();

		long pid = process.pid();
//		System.out.println("PID " + pid);
		return pid;
	}
	
	public void terminate() {
		try {
			if (isRunning()) {
				process.toHandle().destroy();
				int timeout = 10 * 1000;
				int sleeptime = 250;
				while (process.isAlive() && timeout > 0) {					
					Thread.sleep(sleeptime);
					timeout -= sleeptime;
				}
				if (process.isAlive()) {
					System.out.println("App did not close. Terminating forcibly ...");
					process.toHandle().destroyForcibly();				
					Thread.sleep(1 * 1000);
				}
			}
		} catch (Exception e) {
			System.out.println("Error while terminating app process.");
		}
		process = null;
	}

}
