package com.codingotaku.apps.callback;

import java.util.List;

import com.codingotaku.apps.source.Anime;
import com.codingotaku.apps.source.Episode;
import com.codingotaku.apps.source.Result;

import javafx.scene.image.Image;

public interface Crawler {
	public void loading();
	public void loadedAnime(List<Anime> animeList, Result result);
	public void loadedEpisodes(List<Episode> episodeList, Result result);
	public void loadedSynopsys(String content);
	public void poster(Image image);
}
