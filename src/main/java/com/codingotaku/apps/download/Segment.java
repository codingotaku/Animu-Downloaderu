package com.codingotaku.apps.download;

/**
 * @author Rahul S<br>
 *         This class contains segment details for each downloads
 */
public class Segment {
	private long start; // Start byte location
	private long end; // End byte location
	private long downloaded; // bytes downloaded
	private String file; // File name of downloaded segment

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getDownloaded() {
		return downloaded;
	}

	public void setDownloaded(long downloaded) {
		this.downloaded = downloaded;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	Segment(long start, long end, String file) {
		this.start = start;
		this.end = end;
		this.downloaded = 0;
		this.file = file;
	}

	Segment(long start, long end, long downloaded, String file) {
		this.start = start;
		this.end = end;
		this.downloaded = downloaded;
		this.file = file;
	}

	// Returns whether the download is finished or not
	boolean isFinished() {
		return start > end;
	}

	@Override
	public String toString() {
		return String.format("Start : %d end : %d downloaded : %d file : %s", start, end, downloaded, file);
	}
}
