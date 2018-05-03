/**
 * 
 */
package com.dakusuta.tools.anime.download;

import java.util.ArrayList;

import com.dakusuta.tools.anime.callback.DownloadObserver;
import com.dakusuta.tools.anime.callback.TableObserver;

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

	private TableObserver observer;

	private DownloadManager() {

	}

	public void addDownloadURL(String pageUrl) {
		DownloadInfo downloadInfo = new DownloadInfo(pageUrl, this);
		queue.add(downloadInfo);

		if (downloads.size() < MAX_DOWNLOAD) {

			ArrayList<DownloadInfo> toRemove = new ArrayList<>();
			int end = queue.size() > MAX_DOWNLOAD ? MAX_DOWNLOAD : queue.size();
			for (int i = 0; i < end; i++) {
				DownloadInfo info = queue.get(i);
				toRemove.add(info);
				addDownload(info);
				startDownload(info);
			}

			toRemove.forEach(queue::remove);
		}
	}

	public void setController(TableObserver observer) {
		this.observer = observer;
	}

	@Override
	public void paused(DownloadInfo info) {
		observer.updated(info);
	}

	@Override
	public void resumed(DownloadInfo info) {
		observer.updated(info);
	}

	@Override
	public void cancelled(DownloadInfo downloadInfo) {
		observer.updated(downloadInfo);
		downloads.remove(downloadInfo);
		errQueue.add(downloadInfo);
		startNextDownload();
	}

	@Override
	public void downloading(DownloadInfo downloadInfo) {
		observer.updated(downloadInfo);
	}

	@Override
	public void error(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		errQueue.add(downloadInfo);
		observer.updated(downloadInfo);
		startNextDownload();
	}

	@Override
	public void pending(DownloadInfo downloadInfo) {
		observer.added(downloadInfo);
	}

	@Override
	public void finished(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		observer.updated(downloadInfo);
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

		ArrayList<DownloadInfo> toRemove = new ArrayList<>();

		for (int i = downloads.size() - 1; i > -1; i--) {
			DownloadInfo info = downloads.get(i);
			if (info.getStatus() == Status.DOWNLOADING) {
				toRemove.add(info);
				queue.add(0, info);
				info.pause();
			}
		}

		toRemove.forEach(downloads::remove);
	}

	public void resumeAll() {
		globalStop = false;

		int i = 0;
		ArrayList<DownloadInfo> toRemove = new ArrayList<>();
		while (downloads.size() < MAX_DOWNLOAD) {
			DownloadInfo info = queue.get(i);
			toRemove.add(info);
			if (info.getStatus() == Status.PAUSED) {
				downloads.add(info);
				info.resume();
			}
			i++;
		}

		toRemove.forEach(queue::remove);

		startNextDownload();
	}

	public void retryAll() {
		globalStop = false;

		ArrayList<DownloadInfo> toRemove = new ArrayList<>();

		for (int i = errQueue.size() - 1; i > -1; i--) {
			DownloadInfo info = errQueue.get(i);
			toRemove.add(info);
			if (downloads.size() < MAX_DOWNLOAD) {
				downloads.add(info);
				info.retry();
			} else queue.add(0, info);
		}

		toRemove.forEach(errQueue::remove);
		startNextDownload();
	}

	public void restartAll() {
		globalStop = false;

		ArrayList<DownloadInfo> toRemove = new ArrayList<>();

		for (int i = errQueue.size() - 1; i > -1; i--) {
			DownloadInfo info = errQueue.get(i);
			toRemove.add(info);

			if (downloads.size() < MAX_DOWNLOAD) {
				if (!downloads.contains(info)) downloads.add(info);
				info.restart();
			} else {
				queue.add(0, info);
				info.cancel();
				info.setStatus(Status.PENDING);
			}
		}

		toRemove.forEach(errQueue::remove);

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

			ArrayList<DownloadInfo> toRemove = new ArrayList<>();
			int last = MAX_DOWNLOAD - tmp;
			if (last > queue.size()) last = queue.size();

			for (int i = 0; i < last; i++) {
				DownloadInfo info = queue.get(i);
				addDownload(info);
				toRemove.add(info);
				if (info.getSize() > -1) {
					switch (info.getStatus()) {
					case PENDING:
						info.restart();
						break;
					case PAUSED:
						info.resume();
						break;
					default:
						info.retry();
						break;
					}
				} else startDownload(info);
			}

			toRemove.forEach(queue::remove);
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