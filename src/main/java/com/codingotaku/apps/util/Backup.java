package com.codingotaku.apps.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Rahul S<br>
 */
public class Backup {
	private static Logger logger = Logger.getLogger(Backup.class.getName());

	private Backup() {

	}

	public static void saveDownloadFolder() {
		try {
			var file = new File(Constants.CONFIG_FILE);
			var parent = file.getParentFile();

			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			if (!file.exists() && !file.createNewFile()) {
				// Create failed
			}
			try (FileWriter fileWriter = new FileWriter(Constants.CONFIG_FILE)) {
				fileWriter.write(String.format("folder %s", Constants.getDownloadFolder()));
			}

		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	public static void loadDownloadFolder() {
		File file = new File(Constants.CONFIG_FILE);
		if (file.exists()) {
			try {
				Properties properties = new Properties();
				FileReader fileReader = new FileReader(Constants.CONFIG_FILE);
				properties.load(fileReader);
				String folder = properties.getProperty("folder",(System.getProperty("user.home") + File.separator + "Downloads"));
				if (new File(folder).exists()) {
					// Don't assign if folder doesn't exist
					Constants.setDownloadFolder(folder);
				}
				fileReader.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
	
	public static void saveThreadCount() {
		try {
			try (FileWriter fileWriter = new FileWriter(Constants.CONFIG_FILE)) {
				fileWriter.write(String.format("threadCount %s", Constants.getThreadCount()));
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
		}
	}
	
	public static void loadThreadCount() {
		File file = new File(Constants.CONFIG_FILE);
		if (file.exists()) {
			try {
				Properties properties = new Properties();
				FileReader fileReader = new FileReader(Constants.CONFIG_FILE);
				properties.load(fileReader);
				int count = Integer.parseInt(properties.getProperty("threadCount", "8"));
				Constants.setThreadCount(count);
				fileReader.close();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
	}
}