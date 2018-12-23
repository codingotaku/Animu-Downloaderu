package com.codingotaku.apps.custom;

import org.jsoup.nodes.Element;

public class EpisodeLabel {
	private final String text;
	private final String url;
	private final String anime;

	private EpisodeLabel(String anime, String url, String text) {
		this.text = text.replaceAll("[\\\\/:*?\"<>|]", "_");
		anime = anime.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
		this.anime = anime;
		this.url = url;
	}

	public EpisodeLabel(String anime, Element element) {
		this.text = element.text();
		this.anime = anime;
		url = element.attr("href");
	}

	public boolean hasValue(String value) {
		return this.text.toLowerCase().contains(value.toLowerCase());
	}

	public String getUrl() {
		return url;
	}

	public String getAnime() {
		return anime;
	}

	public EpisodeLabel copy() {
		return new EpisodeLabel(anime, url, this.text);
	}

	@Override
	public String toString() {
		return this.text;
	}
}
