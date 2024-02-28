package com.istt.inboundtraffic.service;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CustomLoggingListener {

	private static final String LOG_FILE_PREFIX = "sip";
	private static String currentLogFile = null;
	private static FileWriter fileWriter;

	static {
		try {
			// Set up the FileWriter for the log file
			updateFileWriter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void updateFileWriter() throws IOException {
		String dateFormat = new SimpleDateFormat("yyyyMMdd").format(new Date());
		String newLogFile = LOG_FILE_PREFIX + "_" + dateFormat + ".log";

		if (!newLogFile.equals(currentLogFile)) {
			if (fileWriter != null) {
				fileWriter.close();
			}
			fileWriter = new FileWriter(newLogFile, true);

			currentLogFile = newLogFile;
		}
	}

	public static void logMessage(String message) {
		try {
			updateFileWriter();

			String logEntry = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " - " + message + "\n";
			fileWriter.write(logEntry);
			fileWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
