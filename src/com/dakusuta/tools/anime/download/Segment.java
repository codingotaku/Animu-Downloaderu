package com.dakusuta.tools.anime.download;

/**
 * @author Rahul S<br>
 *         This class contains segment details for each downloads
 */
public class Segment {
	public int start; // Start byte location
	public int end;// End byte location

	Segment(int start, int end) {
		this.start = start;
		this.end = end;
	}

	// Returns whether the download is finished or not
	boolean isFinished() {
		return start < end;
	}

}
