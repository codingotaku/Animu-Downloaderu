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
		
		Source GOGOAnime = builder.setListUrl("https://www.gogoanime1.com/home/anime-list")
				.setListRegex("div.container-left > div.container-item > ul > li > a")
				.setNameRegex("div.anime-title)")
				.setDocRegex("p.anime-details")
				.setPosterRegex("div.animeDetail-image > img")
				.setEpRegex("div.ci-contents > div:eq(1) > ul > li >  a")
				.build();
		
		sources.add(Anime1);
		sources.add(GOGOAnime);
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
