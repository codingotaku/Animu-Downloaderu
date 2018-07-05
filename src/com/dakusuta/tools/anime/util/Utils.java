package com.dakusuta.tools.anime.util;

import java.util.ArrayList;
import java.util.List;

import com.dakusuta.tools.anime.custom.EpisodeLabel;

/**
 * @author Rahul S<br>
 *         Utility class to help with various redundant tasks
 */
public class Utils {
	// creates a copy of list
	public static List<EpisodeLabel> copyList(List<EpisodeLabel> source) {
		List<EpisodeLabel> out = new ArrayList<>();
		source.forEach(episode -> out.add(episode.copy()));
		return out;
	}
}
