package com.dakusuta.tools.anime.downloader;

import java.util.ArrayList;
import java.util.List;

public class Utils {
	public static List<CustomLabel> copyList(List<CustomLabel> source) {
		List<CustomLabel> out = new ArrayList<>();
		source.forEach(episode -> {
			out.add(episode.copy());
		});
		return out;
	}
}
