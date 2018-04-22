package com.dakusuta.tools.anime.util;

import java.io.FileWriter;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.dakusuta.tools.anime.download.DownloadInfo;
import com.dakusuta.tools.anime.download.Segment;

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
			JSONObject jsonObject = new JSONObject();
			ArrayList<Segment> segments = info.getSegments();

			jsonObject.put("fileName", info.getFileName()) // Add the download information to the jsonObject
					.put("status", info.getStatus().toString())
					.put("size", info.getSize())
					.put("downloaded", info.getDownloaded())
					.put("progress", info.getProgress())
					.put("url", info.getUrl());

			// Create a new JSONArray object
			JSONArray jsonArray = new JSONArray();

			// Add download segment details to the jsonArray
			segments.forEach(segment -> {
				JSONObject downloadSegment = new JSONObject();
				downloadSegment.put("start", segment.start).put("end", segment.end);
				jsonArray.put(downloadSegment);
			});

			// Add the jsoArray to jsonObject
			jsonObject.put("parts", jsonArray);

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
