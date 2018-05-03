package com.dakusuta.tools.anime.util;

import java.io.FileWriter;
import java.util.ArrayList;

import com.dakusuta.tools.anime.download.DownloadInfo;
import com.dakusuta.tools.anime.download.Segment;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * @author Rahul S<br>
 *         This is a class for keeping track of download information while
 *         closing/launching the program
 */
public class DownloadBackup {
	DownloadInfo info;
	String jSonPath;

	public DownloadBackup(DownloadInfo info, String jsonPath) {
		this.info = info;
		this.jSonPath = jsonPath;
	}

	public void takeBackup() {
		try {
			// Create a new JSONObject
			JsonObject jsonObject = new JsonObject();
			ArrayList<Segment> segments = info.getSegments();

			jsonObject.addProperty("fileName", info.getFileName()); // Add the download information to the jsonObject
			jsonObject.addProperty("status", info.getStatus().toString());
			jsonObject.addProperty("size", info.getSize());
			jsonObject.addProperty("downloaded", info.getDownloaded());
			jsonObject.addProperty("progress", info.getProgress());
			jsonObject.addProperty("url", info.getUrl());

			// Create a new JSONArray object
			JsonArray jsonArray = new JsonArray();

			// Add download segment details to the jsonArray
			segments.forEach(segment -> {
				JsonObject downloadSegment = new JsonObject();
				downloadSegment.addProperty("start", segment.start);
				downloadSegment.addProperty("end", segment.end);
				downloadSegment.addProperty("downloaded", segment.downloaded);
				jsonArray.add(downloadSegment);
			});

			// Add the jsoArray to jsonObject
			jsonObject.add("parts", jsonArray);

			// Create a new FileWriter object
			FileWriter fileWriter = new FileWriter(jSonPath);

			// Writing the jsonObject into jsonPath
			fileWriter.write(jsonObject.toString());
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void restore() {
	}
}
