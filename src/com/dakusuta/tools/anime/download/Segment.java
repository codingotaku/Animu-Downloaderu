package com.dakusuta.tools.anime.download;

/**
 * @author Rahul S<br>
 *         This class contains segment details for each downloads
 */
public class Segment {
	public int start; // Start byte location
	public int end; // End byte location
	public String fileName; // File name of downloaded segment

	Segment(int start, int end, String file) {
		this.start = start;
		this.end = end;
		this.fileName = file;
	}

	// Returns whether the download is finished or not
	boolean isFinished() {
		return start < end;
	}
}
