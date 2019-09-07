package com.codingotaku.apps.source;

public enum Source {
	Anime1, ANIMERAM;

	private Source() {
		vidRegex = "(http[s]?:\\/\\/[^\\/]*.*.mp4\\\\??[^\\\"\\']*)";
		epRegexAlt = null;
		multiPage = false;
		multiEpList = false;
	}

	static {
		Anime1.setListUrl("http://www.anime1.com/content/list/")
				.setListRegex("div.alph-list-box > h4:has(a[name]) + ul > li > a")
				.setNameRegex("h1.blue-main-title")
				.setDocRegex("div.detail-left > span > span")
				.setPosterRegex("div.detail-cover >a >img")
				.setEpRegex("div.left-left > ul.anime-list > li > a");

		ANIMERAM.setListUrl("https://ww2.animeram.cc/series")
		.setListRegex("div.panel > div.panel-footer > ul.series_alpha > li > a")
		.setNameRegex("div.first > h1")
		.setDocRegex("p.ptext")
		.setPosterRegex("img.media-object")
		.setEpRegex("ul.newmanga > li > div > a:(2)");
	}

	private String docRegex;
	private String epRegex;
	private String epRegexAlt;
	private String listRegex;
	private String listUrl;
	private String posterRegex;
	private String vidRegex;
	private String nameRegex;
	private boolean multiPage;
	private boolean multiEpList;

	String docRegex() {
		return docRegex;
	}

	String epRegex() {
		return epRegex;
	}

	String epRegexAlt() {
		return epRegexAlt;
	}

	String listRegex() {
		return listRegex;
	}

	String listUrl() {
		return listUrl;
	}

	String posterRegex() {
		return posterRegex;
	}

	String vidRegex() {
		return vidRegex;
	}

	String nameRegex() {
		return this.nameRegex;
	}

	boolean isMultiPage() {
		return multiPage;
	}

	boolean isMultiEpList() {
		return multiEpList;
	}

	private Source setDocRegex(String docRegex) {
		this.docRegex = docRegex;
		return this;
	}

	private Source setEpRegex(String epRegex) {
		this.epRegex = epRegex;
		return this;
	}

	private Source setListRegex(String listRegex) {
		this.listRegex = listRegex;
		return this;
	}

	private Source setListUrl(String listUrl) {
		this.listUrl = listUrl;
		return this;
	}

	private Source setPosterRegex(String posterRegex) {
		this.posterRegex = posterRegex;
		return this;
	}

	private Source setNameRegex(String nameRegex) {
		this.nameRegex = nameRegex;
		return this;
	}
}
