package com.dakusuta.tools.anime.source;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import com.dakusuta.tools.anime.custom.CustomLabel;

import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.Window;

public abstract class ISource {
	protected Document selectedDoc = null;
	protected String regex = "(http://.*(.mp4\\?)[^\"]*)";
	protected List<CustomLabel> animeList = new ArrayList<>();
	protected List<CustomLabel> episodes = new ArrayList<>();

	public abstract List<CustomLabel> loadAnime(Window window);

	public abstract void getSynopsys(MouseEvent ev);

	public abstract List<CustomLabel> loadEpisodes();

	abstract Image getPoster(String imgUrl);

	public String crawlerRegEx() {
		return regex;
	}
}
