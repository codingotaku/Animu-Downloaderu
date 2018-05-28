package com.dakusuta.tools.anime.custom;

import org.jsoup.nodes.Element;

import javafx.scene.control.Label;

public class CustomLabel extends Label {
	private String url = "";
	private String anime = "";
	private CustomLabel(String anime,String url, String text) {
		super(text.replaceAll("[\\\\/:*?\"<>|]", "_"));
		anime = anime.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
		this.anime=anime;
		this.url = url;
	}

	public CustomLabel(Element element) {
		super(element.text());
		url = element.attr("href");
	}
	public CustomLabel(String anime,Element element) {
		super(element.text());
		this.anime=anime;
		url = element.attr("href");
	}

	public boolean hasValue(String value) {
		return getText().toLowerCase().contains(value.toLowerCase());
	}

	public String getUrl() {
		return url;
	}

	public String getAnime() {
		return anime;
	}

	public CustomLabel copy() {
		return new CustomLabel(anime, url, getText());
	}

	public String toString() {
		return getText();
	}
}
