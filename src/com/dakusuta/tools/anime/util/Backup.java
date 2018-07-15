package com.dakusuta.tools.anime.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.dakusuta.tools.anime.download.DownloadInfo;
import com.dakusuta.tools.anime.download.DownloadManager;
import com.dakusuta.tools.anime.download.Status;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * @author Rahul S<br>
 *         This is a class for keeping track of download information while
 *         closing/launching the program
 */
public class Backup {
	private static final Gson gson = new Gson();
	private static final JsonParser parser = new JsonParser();

	public static void saveDownloadFolder() {
		try {
			File file = new File(Constants.CONFIG_FILE);
			if (!file.exists()) {
				file.getParentFile().mkdir();
				file.createNewFile();
			}
			FileWriter fileWriter = new FileWriter(Constants.CONFIG_FILE);
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
					//Don't assign if folder doesn't exist
					Constants.downloadFolder = folder;
				}
				fileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static JsonObject generateObj(DownloadInfo info) {
		JsonObject object = info.getJson();
		String arr = gson.toJson(info.getSegments());
		object.add("segments", parser.parse(arr).getAsJsonArray());
		return object;
	}

	public static void takeBackup() {
		try {
			DownloadManager instance = DownloadManager.getInstance();
			JsonArray jsonArray = new JsonArray();

			ArrayList<DownloadInfo> list = instance.getQueue(Status.PENDING);
			System.out.println("queue" + list.size());
			for (DownloadInfo info : list) {
				jsonArray.add(generateObj(info));
			}
			list = instance.getQueue(Status.ERROR);
			System.out.println("err" + list.size());
			for (DownloadInfo info : list) {
				jsonArray.add(generateObj(info));
			}
			list = instance.getQueue(Status.DOWNLOADING);
			System.out.println("downloading" + list.size());
			for (DownloadInfo info : list) {
				jsonArray.add(generateObj(info));
			}
			list = instance.getQueue(Status.PAUSED);
			System.out.println("paused" + list.size());
			for (DownloadInfo info : list) {
				jsonArray.add(generateObj(info));
			}

			// Create a new FileWriter object
			FileWriter fileWriter = new FileWriter(Constants.BACKUP_FILE);

			// Writing the jsonObject into jsonPath
			fileWriter.write(jsonArray.toString());
			fileWriter.close();
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public static void restore() {
		File file = new File(Constants.BACKUP_FILE);
		if (!file.exists()) return;
		final Type REVIEW_TYPE = new TypeToken<List<DownloadInfo>>() {
		}.getType();
		DownloadManager instance = DownloadManager.getInstance();
		try {
			JsonReader reader = new JsonReader(new FileReader(file));
			List<DownloadInfo> data = gson.fromJson(reader, REVIEW_TYPE);
			data.forEach(instance::addToQueue);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}