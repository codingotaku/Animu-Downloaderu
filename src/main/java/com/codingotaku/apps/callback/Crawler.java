package com.codingotaku.apps.callback;

import com.codingotaku.apis.animecrawler.AnimeList;
import com.codingotaku.apis.animecrawler.EpisodeList;
import com.codingotaku.apis.animecrawler.Result;

public interface Crawler {
	public void loading();
	public void loadedAnime(AnimeList animeList, Result result);
	public void loadedEpisodes(EpisodeList episodeList, Result result);
	public void loadedSynopsys(String content, Result result);
	public void loadedPoster(String url, Result result);
}
