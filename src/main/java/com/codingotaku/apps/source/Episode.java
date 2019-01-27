package com.codingotaku.apps.source;

import java.io.IOException;
import java.net.URLEncoder;

import org.jsoup.nodes.Element;

public class Episode {
	String episodeUrl;
	String title;
	Source source;
	String anime;

	Episode(Source source, Element element, String anime) {
		this.anime=anime;
		this.source = source;
		this.episodeUrl = element.attr("href");
		this.title = element.text();
	}

	public String getTitle() {
		return title;
	}

	public String getVideoUrl() throws IOException {
		String url=Server.generateVideoUrl(this);
		int pos = url.lastIndexOf('/') + 1;
		//convert URL to proper format
		url=url.substring(0, pos)+ URLEncoder.encode(url.substring(pos),"utf-8");
		System.out.println(url);
		return url;
	}
	public String getAnime() {
		return anime;
	}

	@Override
	public String toString() {
		return title;
	}
}
