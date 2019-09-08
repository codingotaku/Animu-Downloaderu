package com.codingotaku.apps.source;

import java.util.ArrayList;
import java.util.List;

import com.codingotaku.apis.animecrawler.Source;
import com.codingotaku.apis.animecrawler.Source.SourceBuilder;

public class AnimeSources {
	private final List<Source> sources = new ArrayList<>();
	private static AnimeSources api;

	private AnimeSources() {
		SourceBuilder builder = new SourceBuilder();
		Source Anime1 = builder.setListUrl("http://www.anime1.com/content/list/")
				.setListRegex("div.alph-list-box > h4:has(a[name]) + ul > li > a")
				.setNameRegex("h1.blue-main-title")
				.setDocRegex("div.detail-left > span > span")
				.setPosterRegex("div.detail-cover >a >img")
				.setEpRegex("div.left-left > ul.anime-list > li > a").build();

		Source ANIMERAM = builder.setListUrl("https://ww2.animeram.cc/series")
				.setListRegex("div.panel > div.panel-footer > ul.series_alpha > li > a")
				.setNameRegex("div.first > h1")
				.setDocRegex("p.ptext")
				.setPosterRegex("img.media-object")
				.setEpRegex("ul.newmanga > li > div > a:(2)")
				.build();
		sources.add(Anime1);
		sources.add(ANIMERAM);
	}

	public List<Source> values() {
		return sources;
	}

	public static AnimeSources getInstance() {
		if (api == null) {
			api = new AnimeSources();
		}
		return api;
	}
}
