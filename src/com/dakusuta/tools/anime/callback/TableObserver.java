package com.dakusuta.tools.anime.callback;

import com.dakusuta.tools.anime.download.DownloadInfo;

public interface TableObserver {
	public void added(DownloadInfo download);
	public void updated(DownloadInfo download);
}
