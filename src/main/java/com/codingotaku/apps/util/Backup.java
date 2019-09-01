package com.codingotaku.apps.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Rahul S<br>
 */
public class Backup {
	public static void saveDownloadFolder() {
		try {
			var file = new File(Constants.CONFIG_FILE);
			var parent = file.getParentFile();

			if (!parent.exists() && !parent.mkdirs()) {
				throw new IllegalStateException("Couldn't create dir: " + parent);
			}
			if(!file.exists()) {
				file.createNewFile();
			}
			var fileWriter = new FileWriter(Constants.CONFIG_FILE);
			fileWriter.write(String.format("folder %s", Constants.downloadFolder));
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadDownloadFolder() {
		File file = new File(Constants.CONFIG_FILE);
		if (file.exists()) {
			try {
				Properties properties = new Properties();
				FileReader fileReader = new FileReader(Constants.CONFIG_FILE);
				properties.load(fileReader);
				String folder = properties.getProperty("folder");
				if (new File(folder).exists()) {
					// Don't assign if folder doesn't exist
					Constants.downloadFolder = folder;
				}
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}