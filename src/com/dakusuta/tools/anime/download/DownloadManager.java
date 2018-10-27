/**
 * 
 */
package com.dakusuta.tools.anime.download;

import java.util.ArrayList;

import com.dakusuta.tools.anime.callback.DownloadObserver;
import com.dakusuta.tools.anime.callback.TableObserver;
import com.dakusuta.tools.anime.custom.EpisodeLabel;

/**
 * @author Rahul Sivananda <br>
 * 
 */
public class DownloadManager implements DownloadObserver {

	private static DownloadManager instance;

	private boolean globalStop = false;
	private static final int MAX_DOWNLOAD = 3;

	private ArrayList<DownloadInfo> queue = new ArrayList<>();
	private ArrayList<DownloadInfo> downloads = new ArrayList<>();
	private ArrayList<DownloadInfo> errQueue = new ArrayList<>();
	private ArrayList<DownloadInfo> pauseQueue = new ArrayList<>();

	/*
	 * 
	 * Return DownloadManager instance, this makes sure that there is only one
	 * download manager per application
	 */
	public static DownloadManager getInstance() {
		if (instance == null) instance = new DownloadManager();
		return instance;
	}

	public void restoreDownload(DownloadInfo info) {
		switch (info.getStatus()) {
		case CANCELLED:
			errQueue.add(info);
			break;
		case CANCELLING:
			info.setStatus(Status.CANCELLED);
			errQueue.add(info);
			break;
		case DOWNLOADING:
			downloads.add(info);
			break;
		case ERROR:
			errQueue.add(info);
			break;
		case PAUSED:
			pauseQueue.add(info);
			break;
		case PENDING:
			queue.add(info);
			break;
		default:
			break;

		}
	}

	public ArrayList<DownloadInfo> getQueue(Status status) {
		ArrayList<DownloadInfo> ret = new ArrayList<>();
		switch (status) {
		case PENDING:
			ret = queue;
			break;
		case ERROR:
			ret = errQueue;
			break;
		case PAUSED:
			ret = pauseQueue;
			break;
		case DOWNLOADING:
			ret = downloads;
			break;
		default:
			break;
		}
		return ret;
	}

	public void addToQueue(DownloadInfo info) {
		boolean added = true;
		switch (info.getStatus()) {
		case PENDING:
			queue.add(info);
			break;
		case CANCELLED:
			errQueue.add(info);
			break;
		case ERROR:
			errQueue.add(info);
			break;
		case PAUSED:
			pauseQueue.add(info);
			break;
		default:
			added = false;
			break;
		}
		if (added) observer.added(info);
	}

	private DownloadManager() {
		// To make sure DownlodManager wont be reinitialized
	}

	private TableObserver observer;

	public void addDownloadURL(EpisodeLabel episode) {
		DownloadInfo downloadInfo = new DownloadInfo(episode, this);
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
	public void downloading(DownloadInfo downloadInfo) {
		observer.updated(downloadInfo);
	}

	@Override
	public void cancelling(DownloadInfo downloadInfo) {
		observer.updated(downloadInfo);
	}

	@Override
	public void cancelled(DownloadInfo downloadInfo) {
		errQueue.add(downloadInfo);
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
	public void mergingFiles(DownloadInfo downloadInfo) {
		observer.updated(downloadInfo);
	}

	@Override
	public void finished(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		observer.updated(downloadInfo);
		startNextDownload();
	}

	public void cancelAll() {
		globalStop = true;
		ArrayList<DownloadInfo> toCancel = new ArrayList<>();

		for (int i = downloads.size() - 1; i > -1; i--) {
			DownloadInfo info = downloads.get(i);
			toCancel.add(info);
			downloads.remove(info);
		}

		toCancel.forEach(info -> info.cancel());
	}

	public void pauseAll() {
		globalStop = true;

		ArrayList<DownloadInfo> toPause = new ArrayList<>();

		for (int i = downloads.size() - 1; i > -1; i--) {
			DownloadInfo info = downloads.get(i);
			if (info.getStatus() == Status.DOWNLOADING) {
				downloads.remove(info);
				toPause.add(info);
			}
		}

		toPause.forEach(info -> info.pause());
	}

	public void resumeAll() {
		globalStop = false;

		int i = 0;
		ArrayList<DownloadInfo> toRemove = new ArrayList<>();

		while (i < pauseQueue.size()) {
			DownloadInfo info = pauseQueue.get(i);
			toRemove.add(info);

			if (downloads.size() < MAX_DOWNLOAD) {
				downloads.add(info);
				info.resume();
			} else queue.add(0, info);
			i++;
		}

		toRemove.forEach(pauseQueue::remove);

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
				downloads.add(info);
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
		if (tmp >= MAX_DOWNLOAD || queue.isEmpty()) return;

		int last = MAX_DOWNLOAD - tmp;
		if (last > queue.size()) last = queue.size();

		for (int i = 0; i < last; i++) {
			DownloadInfo info = queue.get(i);
			addDownload(info);
			queue.remove(info);
			if (info.getSize() > -1) {
				if (info.getStatus() == Status.PENDING) info.restart();
				else if (info.getStatus() == Status.PAUSED) info.resume();
				else info.retry();
			} else startDownload(info);
		}
	}

	public void pause(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) {
			pauseQueue.add(info);
			downloads.remove(info);
			info.pause();
		}
		startNextDownload();
	}

	public void resume(DownloadInfo info) {
		if (info.getStatus() == Status.PAUSED) {
			if (downloads.size() < MAX_DOWNLOAD) {
				pauseQueue.remove(info);
				info.resume();
			} else {
				pauseQueue.remove(info);
				queue.add(0, info);
			}
		}
	}

	public void cancel(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) info.cancel();
		downloads.remove(info);
		startNextDownload();
	}

	public void retry(DownloadInfo info) {
		if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
			errQueue.remove(info);
			if (downloads.size() < MAX_DOWNLOAD) {
				downloads.add(info);
				info.retry();
			} else {
				queue.add(0, info);
			}
		}
	}

	public void restart(DownloadInfo info) {
		if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
			errQueue.remove(info);
			if (downloads.size() < MAX_DOWNLOAD) {
				downloads.add(info);
				info.restart();
			} else {
				info.cancel();
				info.setStatus(Status.PENDING);
				queue.add(0, info);
			}
		}
	}
}