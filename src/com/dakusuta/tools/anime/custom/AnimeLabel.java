package com.dakusuta.tools.anime.custom;

import org.jsoup.nodes.Element;

import javafx.scene.control.Label;

public class AnimeLabel extends Label {
	private String url = "";

	public AnimeLabel(Element element) {
		super(element.text());
		url = element.attr("href");
	}

	public boolean hasValue(String value) {
		return getText().toLowerCase().contains(value.toLowerCase());
	}

	public String getUrl() {
		return url;
	}
	public String toString() {
		return getText();
	}
}
