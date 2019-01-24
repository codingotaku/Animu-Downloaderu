package com.codingotaku.apps.source;

import java.io.IOException;
import java.net.URL;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.codingotaku.apps.callback.EpisodeListListener;

public class Anime {
	String url;
	Source source;
	Document doc = null;
	private String synopsys = null;
	private String poster = null;
	private String name;

	Anime(Source source, Element element) {
		this.source = source;
		name = element.text();
		url = element.attr("href");
	}

	Document getDoc() throws IOException {
		if (doc == null) doc = Jsoup.parse(new URL(url), 60000);
		return doc;
	}

	public String getSynopsys() throws IOException {
		if (synopsys != null) return synopsys;
		return Server.getSynopsys(this);
	}

	public void listAllEpisodes(EpisodeListListener listener) {
		Server.listAllEpisodes(this, listener);
	}

	public String getPosterUrl() throws IOException {
		if (poster != null) return poster;
		return Server.getPosterUrl(this);
	}

	public String getName() {
		if (name != null) return name;
		try {
			return Server.getName(this);
		} catch (IOException e) {
			return "none";
		}
	}
	@Override
	public String toString() {
		return getName();
	}
}