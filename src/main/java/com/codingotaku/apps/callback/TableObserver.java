package com.codingotaku.apps.callback;

import com.codingotaku.apps.download.DownloadInfo;

public interface TableObserver {
	public void added(DownloadInfo download);
	public void updated(DownloadInfo download);
}
