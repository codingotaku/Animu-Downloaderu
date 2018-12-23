package com.codingotaku.apps.custom;

import org.jsoup.nodes.Element;

public class AnimeLabel {
	private final String url;
	private final String text;

	public AnimeLabel(Element element) {
		text =element.text();
		url = element.attr("href");
	}

	public boolean hasValue(String value) {
		return text.toLowerCase().contains(value.toLowerCase());
	}

	public String getUrl() {
		return url;
	}
	
	@Override
	public String toString() {
		return text;
	}

	public String getText() {
		return text;
	}
}
