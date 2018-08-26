package com.dakusuta.tools.anime.download;

/**
 * @author Rahul S <br>
 *         this callback is redundant, but it helps track of download progress
 */
public abstract class Callback {
	DownloadInfo info;

	public Callback(DownloadInfo info) {
		this.info = info;
	}

	abstract void add(long progress, Status status);

	public Status getStaus() {
		return info.getStatus();
	}
}
