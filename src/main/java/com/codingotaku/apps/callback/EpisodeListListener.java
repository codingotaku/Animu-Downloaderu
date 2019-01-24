package com.codingotaku.apps.callback;

import java.util.List;

import com.codingotaku.apps.source.Episode;
import com.codingotaku.apps.source.Result;

public interface EpisodeListListener {

	void loaded(List<Episode>generateEpList, Result result);

}
