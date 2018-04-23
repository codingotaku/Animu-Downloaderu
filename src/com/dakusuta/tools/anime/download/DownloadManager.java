/**
 * 
 */
package com.dakusuta.tools.anime.download;

import java.util.ArrayList;

import com.dakusuta.tools.anime.callback.DownloadObserver;

/**
 * @author Rahul Sivananda <br>
 * 
 */
public class DownloadManager implements DownloadObserver {

	private static DownloadManager instance;

	private boolean globalStop = false;

	/*
	 * Return DownloadManager instance, this makes sure that there is only one
	 * download manager per application
	 */
	public static DownloadManager getInstance() {
		if (instance == null) instance = new DownloadManager();

		return instance;
	}

	private ArrayList<DownloadInfo> queue = new ArrayList<>();
	private ArrayList<DownloadInfo> downloads = new ArrayList<>();

	private DownloadObserver observer;

	private DownloadManager() {

	}

	public void addDownloadURL(String pageUrl) {
		DownloadInfo downloadInfo = new DownloadInfo(pageUrl, this);
		queue.add(downloadInfo);
		if (downloads.size() < 3) {
			int i = queue.size() > 3 ? 3 : queue.size() - 1;
			for (; i > -1; i--) {
				startDownload(downloadInfo);
				queue.remove(i);
			}
		}
	}

	public void setController(DownloadObserver observer) {
		this.observer = observer;
	}

	@Override
	public void paused(DownloadInfo info) {
		observer.paused(info);
	}

	@Override
	public void resumed(DownloadInfo info) {
		observer.resumed(info);
	}

	@Override
	public void cancelled(DownloadInfo downloadInfo) {
		observer.cancelled(downloadInfo);
		startNextDownload();
	}

	@Override
	public void downloading(DownloadInfo downloadInfo, double progress) {
		observer.downloading(downloadInfo, progress);
	}

	@Override
	public void error(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		queue.add(downloadInfo);
		observer.error(downloadInfo);
		startNextDownload();
	}

	@Override
	public void pending(DownloadInfo downloadInfo) {
		observer.pending(downloadInfo);
	}

	@Override
	public void finished(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		observer.finished(downloadInfo);
		startNextDownload();
	}

	public void cancelAll() {
		globalStop = true;
		for (int i = downloads.size() - 1; i > -1; i--) {
			DownloadInfo info = downloads.get(i);
			if (info.getStatus() == Status.DOWNLOADING) {
				info.cancel();
				downloads.remove(info);
			}
		}
	}

	public void pauseAll() {
		globalStop = true;
		for (int i = downloads.size() - 1; i > -1; i--) {
			DownloadInfo info = downloads.get(i);
			if (info.getStatus() == Status.DOWNLOADING) {
				info.pause();
				downloads.remove(info);
				queue.add(0, info);
			}
		}
	}

	public void resumeAll() {
		globalStop = false;
		int last = queue.size() > 3 ? 3 : queue.size();
		for (int i = last; i > -1; i--) {
			DownloadInfo info = queue.get(i);
			if (downloads.size() < 3 && queue.contains(info) && info.getStatus() == Status.PAUSED) {
				queue.remove(info);
				downloads.add(info);
				info.resume();
			}
		}

		startNextDownload();
	}

	public void retryAll() {
		globalStop = false;
		for (int i = queue.size() - 1; i > -1; i--) {
			DownloadInfo info = queue.get(i);
			if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
				if (downloads.size() < 3) {
					if (!downloads.contains(info)) downloads.add(info);
					if (queue.contains(info)) queue.remove(info);
					info.retry();
				} else {
					queue.remove(info);
					queue.add(0, info);
				}
			}
		}
		startNextDownload();
	}

	public void restartAll() {
		globalStop = false;
		int index = 0;
		for (int i = queue.size() - 1; i > -1; i--) {
			DownloadInfo info = queue.get(i);
			if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
				if (index < 3) {
					if (queue.contains(info)) queue.remove(info);
					info.cancel();
					info.setStatus(Status.PENDING);
					queue.add(0, info);
				} else {
					if (!downloads.contains(info)) downloads.add(info);
					if (queue.contains(info)) queue.remove(info);
					info.restart();
					index++;
				}
			}
		}
		startNextDownload();
	}

	private void addDownload(DownloadInfo downloadInfo) {
		downloads.add(downloadInfo);
	}

	private void startDownload(DownloadInfo downloadInfo) {
		downloadInfo.startDownload();
		addDownload(downloadInfo);
	}

	private void startNextDownload() {
		if (globalStop) return;
		for (int i = downloads.size() - 1; i >= 0; i--) {
			DownloadInfo download = downloads.get(i);
			if (download.getStatus() == Status.ERROR
					|| download.getStatus() == Status.FINISHED
					|| download.getStatus() == Status.CANCELLED) {
				downloads.remove(i);
			}
		}

		int tmp = downloads.size();
		if (!queue.isEmpty() && tmp < 3) {
			int last = 3 - tmp;
			for (int i = 0; i < last && i < queue.size(); i++) {
				startDownload(queue.get(i));
				queue.remove(i);
			}
		}

		if (downloads.size() > 3) {
			for (int i = downloads.size() - 1; i >= 3; i--) {
				DownloadInfo download = downloads.get(i);
				download.pause();
				queue.add(0, download);
				downloads.remove(download);
			}
		}
	}

	public void pause(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) {
			queue.add(info);
			downloads.remove(info);
			info.pause();
		}
		startNextDownload();
	}

	public void resume(DownloadInfo info) {
		if (downloads.size() < 4) {
			if (queue.contains(info) && info.getStatus() == Status.PAUSED) {
				info.resume();
				queue.remove(info);
			}
		} else {
			if (info.getStatus() == Status.PAUSED) {
				queue.remove(info);
				queue.add(0, info);
			}
		}
	}

	public void cancel(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) {
			info.cancel();
			downloads.remove(info);
		}
	}

	public void retry(DownloadInfo info) {
		if (downloads.size() < 4) {
			if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
				if (!downloads.contains(info)) downloads.add(info);
				if (queue.contains(info)) queue.remove(info);
				info.retry();
			}
		} else {
			if (queue.contains(info)) queue.remove(info);
			queue.add(0, info);
		}
	}

	public void restart(DownloadInfo info) {
		if (downloads.size() < 4) {
			if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
				if (!downloads.contains(info)) downloads.add(info);
				if (queue.contains(info)) queue.remove(info);
				info.restart();
			}
		} else {
			if (queue.contains(info)) queue.remove(info);
			info.cancel();
			info.setStatus(Status.PENDING);
			queue.add(0, info);
		}
	}
}