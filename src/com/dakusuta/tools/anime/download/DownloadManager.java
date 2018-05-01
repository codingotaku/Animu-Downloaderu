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
	private static final int MAX_DOWNLOAD = 3;

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
	private ArrayList<DownloadInfo> errQueue = new ArrayList<>();

	private DownloadObserver observer;

	private DownloadManager() {

	}

	public void addDownloadURL(String pageUrl) {
		DownloadInfo downloadInfo = new DownloadInfo(pageUrl, this);
		queue.add(downloadInfo);

		if (downloads.size() < MAX_DOWNLOAD) {
			int end = queue.size() > MAX_DOWNLOAD ? MAX_DOWNLOAD : queue.size() - 1;
			for (int i = 0; i < end; i++) {
				DownloadInfo info = queue.get(i);
				addDownload(info);
				startDownload(info);
			}

			for (int i = end - 1; i > -1; i--) {
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
		downloads.remove(downloadInfo);
		errQueue.add(downloadInfo);
		startNextDownload();
	}

	@Override
	public void downloading(DownloadInfo downloadInfo, double progress) {
		observer.downloading(downloadInfo, progress);
	}

	@Override
	public void error(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		errQueue.add(downloadInfo);
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
				downloads.remove(info);
				info.cancel();
				errQueue.add(info);
			}
		}
	}

	public void pauseAll() {
		globalStop = true;
		for (int i = downloads.size() - 1; i > -1; i--) {
			DownloadInfo info = downloads.get(i);
			if (info.getStatus() == Status.DOWNLOADING) {
				downloads.remove(info);
				queue.add(0, info);
				info.pause();
			}
		}
	}

	public void resumeAll() {
		globalStop = false;

		int i = 0;
		while (downloads.size() < MAX_DOWNLOAD) {
			DownloadInfo info = queue.get(i);
			if (info.getStatus() == Status.PAUSED) {
				queue.remove(info);
				downloads.add(info);
				info.resume();
			}
			i++;
		}

		startNextDownload();
	}

	public void retryAll() {
		globalStop = false;
		for (int i = errQueue.size() - 1; i > -1; i--) {
			DownloadInfo info = queue.get(i);
			if (downloads.size() < MAX_DOWNLOAD) {
				downloads.add(info);
				errQueue.remove(info);
				info.retry();
			} else {
				errQueue.remove(info);
				queue.add(0, info);
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
				if (index >= 3) {
					if (queue.contains(info)) queue.remove(info);
					queue.add(0, info);
					info.cancel();
					info.setStatus(Status.PENDING);
				} else {
					if (queue.contains(info)) queue.remove(info);
					if (!downloads.contains(info)) downloads.add(info);
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
	}

	private void startNextDownload() {
		if (globalStop) return;

		int tmp = downloads.size();
		if (tmp >= MAX_DOWNLOAD) return;

		if (!queue.isEmpty()) {
			int last = 3 - tmp;
			if (last > queue.size()) {
				last = queue.size();
			}
			for (int i = 0; i < last; i++) {
				DownloadInfo info = queue.get(i);
				addDownload(info);

				if (info.getSize() > -1) {
					if (info.getStatus() == Status.PENDING) info.restart();
					else if (info.getStatus() == Status.PAUSED) info.resume();
					else info.retry();
				} else startDownload(info);
			}

			for (int i = last - 1; i < -1; i--) {
				queue.remove(i);
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
		if (info.getStatus() == Status.PAUSED) {
			if (downloads.size() < MAX_DOWNLOAD) {
				queue.remove(info);
				info.resume();
			} else {
				queue.remove(info);
				queue.add(0, info);
			}
		}
	}

	public void cancel(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) info.cancel();
	}

	public void retry(DownloadInfo info) {
		if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
			if (downloads.size() < MAX_DOWNLOAD) {
				if (!downloads.contains(info)) downloads.add(info);
				if (queue.contains(info)) queue.remove(info);
				info.retry();
			} else {
				if (queue.contains(info)) queue.remove(info);
				queue.add(0, info);
			}
		}
	}

	public void restart(DownloadInfo info) {
		if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
			if (downloads.size() < MAX_DOWNLOAD) {
				if (!downloads.contains(info)) downloads.add(info);
				if (queue.contains(info)) queue.remove(info);
				info.restart();

			} else {
				if (queue.contains(info)) queue.remove(info);
				info.cancel();
				info.setStatus(Status.PENDING);
				queue.add(0, info);
			}
		}
	}
}