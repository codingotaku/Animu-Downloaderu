package com.dakusuta.tools.anime.util;

import java.util.ArrayList;
import java.util.List;

import com.dakusuta.tools.anime.custom.CustomLabel;

/**
 * @author Rahul S<br>
 *         Utility class to help with various redundant tasks
 */
public class Utils {
	//creates a copy of list 
	public static List<CustomLabel> copyList(List<CustomLabel> source) {
		List<CustomLabel> out = new ArrayList<>();
		source.forEach(episode -> {
			out.add(episode.copy());
		});
		return out;
	}
}
