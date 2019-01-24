package com.codingotaku.apps.callback;

import java.util.List;

import com.codingotaku.apps.source.Anime;
import com.codingotaku.apps.source.Result;

public interface AnimeFetchListener {
	void loaded(List<Anime> generateAnimeList, Result result);
}
