package com.codingotaku.apps.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public class CacheManager {
	private String CACHE_FOLDER = String.format("%s%s.animu-downloaderu", System.getProperty("user.home"), File.separator);
	private static CacheManager instance;

	public static CacheManager getInstance() {
		if (instance == null) {
			instance = new CacheManager();
		}
		return instance;
	}

	private HashMap<String, Object> cacheHash = new HashMap<String, Object>();

	public Object loadTmpCache(String key) {
		return cacheHash.get(key);
	}

	public void saveTmpCache(String key, Object value) {
		cacheHash.put(key, value);
	}

	private void saveImage(File file, Image image) {
		try {
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "jpg", file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveCacheToDisk(String name) {

		cacheHash.forEach((key, value) -> {
			File file = new File(String.format("%s%s%s%s%s", CACHE_FOLDER, File.separator, name, File.separator, key));
			saveImage(file, (Image) value);
		});

	}

	public void loadCacheFromDisk(String name) {
		cacheHash.clear();
		File folder = new File(String.format("%s%s%s", CACHE_FOLDER, File.separator, name));
		if (!folder.exists())
			folder.mkdirs();
		for (File fileName : folder.listFiles()) {
			try {
				FileInputStream fis = new FileInputStream(fileName);
				cacheHash.put(fileName.getName(), new Image(fis));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
