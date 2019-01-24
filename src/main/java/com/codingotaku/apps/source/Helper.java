package com.codingotaku.apps.source;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codingotaku.apps.callback.Crawler;

import javafx.scene.image.Image;

public class Helper {
	private static Helper instance = null;
	private Source source = Source.Anime1;
	private Crawler webCrawler;

	// This is just so that we wont overload the server
	private Map<Source, List<Anime>> animeListBackup = new HashMap<>();

	public void setSource(Source source) {
		this.source = source;
	}

	private Helper(Crawler crawler) {
		webCrawler = crawler;
	}

	public static Helper getInstance(Crawler crawler) {
		if (instance == null)
			instance = new Helper(crawler);
		return instance;
	}

	public void loadAnime() {

		if (animeListBackup.containsKey(source)) {
			List<Anime> list = animeListBackup.get(source);
			if (list != null && !list.isEmpty()) {//list can be null if previous fetch failed!
				webCrawler.loadedAnime(animeListBackup.get(source), new Result());
			}
		}
		Server.listAllAnime(source, (list, result) -> {
			animeListBackup.put(source, list);
			webCrawler.loadedAnime(list, result);
		});
	}

	public void loadEpisodes(Anime anime) {
		anime.listAllEpisodes(webCrawler::loadedEpisodes);
	}

	public void getSynopsys(Anime anime) {
		try {
			webCrawler.loadedSynopsys(anime.getSynopsys());
			webCrawler.poster(new Image(anime.getPosterUrl()));
		} catch (IOException e) {
			webCrawler.loadedSynopsys("No Synopsys available");
		}
	}
}