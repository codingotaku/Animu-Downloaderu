package com.codingotaku.apps.util;

import java.io.File;

/**
 * @author Rahul S<br>
 *         Constants class store constant values
 */
public class Constants {
	private Constants() {

	}

	public static final String CONFIG_FILE = "settings" + File.separator + "anime.properties";
	public static final String EXIT_QUESTION = "Are you sure you want to exit?";
	private static String downloadFolder = (System.getProperty("user.home") + File.separator + "Downloads");
	private static int THREAD_COUNT = 8;

	public static String getDownloadFolder() {
		return downloadFolder;
	}

	public static void setDownloadFolder(String folder) {
		Constants.downloadFolder = folder;
		Backup.saveDownloadFolder();
	}

	public static void setThreadCount(Integer value) {
		THREAD_COUNT = value;
		Backup.saveThreadCount();
	}

	public static int getThreadCount() {
		return THREAD_COUNT;
	}
}