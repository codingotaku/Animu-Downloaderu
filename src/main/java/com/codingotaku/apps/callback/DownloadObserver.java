package com.codingotaku.apps.callback;

import com.codingotaku.apps.download.DownloadInfo;

/**
 * @author Rahul S<br>
 *         Callback for all downloads
 * 
 */
public interface DownloadObserver {
	public void paused(DownloadInfo download);

	public void finished(DownloadInfo download);

	public void mergingFiles(DownloadInfo download);
	
	public void error(DownloadInfo download);

	public void resumed(DownloadInfo download);

	public void cancelling(DownloadInfo download);

	public void cancelled(DownloadInfo download);

	public void downloading(DownloadInfo download);

	public void pending(DownloadInfo download);

}
