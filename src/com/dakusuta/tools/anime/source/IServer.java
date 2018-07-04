package com.dakusuta.tools.anime.source;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import com.dakusuta.tools.anime.custom.CustomLabel;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;

public abstract class IServer {
	protected String path = null;
	protected Document selectedDoc = null;
	protected String regex = "(http://.*(.mp4\\?)[^\"]*)";
	protected List<CustomLabel> animeList = new ArrayList<>();
	protected List<CustomLabel> episodes = new ArrayList<>();

	protected String getPath() {
		return path;
	}

	public abstract List<CustomLabel> loadAnime(Document doc);

	public abstract void getSynopsys(MouseEvent ev);

	public abstract List<CustomLabel> loadEpisodes();

	abstract Image getPoster(String imgUrl);

	public String crawlerRegEx() {
		return regex;
	}
}
