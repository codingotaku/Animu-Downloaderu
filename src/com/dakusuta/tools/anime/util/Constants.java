package com.dakusuta.tools.anime.util;

/**
 * @author Rahul S<br>
 *         Constants class store constant values
 */
public class Constants {
	public static final String CONFIG_FILE = "settings\\anime.properties";
	public static final String BACKUP_FILE = "settings\\anime.json";
	public static String downloadFolder = (System.getProperty("user.home") + "\\Downloads").replace("\\", "/");
}