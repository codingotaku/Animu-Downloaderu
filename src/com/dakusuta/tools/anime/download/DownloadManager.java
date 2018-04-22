/**
 * 
 */
package com.dakusuta.tools.anime.download;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	public void paused(DownloadInfo downloadInfo) {
		startNextDownload();
		observer.paused(downloadInfo);
	}

	@Override
	public void resumed(DownloadInfo downloadInfo) {
		observer.resumed(downloadInfo);
	}

	@Override
	public void cancelled(DownloadInfo downloadInfo) {
		startNextDownload();
		observer.cancelled(downloadInfo);
	}

	@Override
	public void downloading(DownloadInfo downloadInfo, double progress) {
		observer.downloading(downloadInfo, progress);
	}

	@Override
	public void error(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		queue.add(downloadInfo);
		startNextDownload();
		observer.error(downloadInfo);
	}

	@Override
	public void pending(DownloadInfo downloadInfo) {
		observer.pending(downloadInfo);
	}

	@Override
	public void finished(DownloadInfo downloadInfo) {
		downloads.remove(downloadInfo);
		startNextDownload();
		observer.finished(downloadInfo);
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
			if (downloads.size() < 4 && queue.contains(info) && info.getStatus() == Status.PAUSED) {
				info.resume();
				queue.remove(info);
				downloads.add(info);
			}
		}

		startNextDownload();
	}

	public void retryAll() {
		globalStop = false;
		for (int i = queue.size() - 1; i > -1; i--) {
			DownloadInfo info = queue.get(i);
			if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
				if (!downloads.contains(info)) downloads.add(info);
				if (queue.contains(info)) queue.remove(info);
				info.retry();
			}
		}
		startNextDownload();
	}

	public void restartAll() {
		globalStop = false;
		for (int i = queue.size() - 1; i > -1; i--) {
			DownloadInfo info = queue.get(i);
			if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
				if (!downloads.contains(info)) downloads.add(info);
				if (queue.contains(info)) queue.remove(info);
				info.restart();
			}
		}
		startNextDownload();
	}

	private void addDownload(DownloadInfo downloadInfo) {
		downloads.add(downloadInfo);
	}

	private void startDownload(DownloadInfo downloadInfo) {
		if (downloadInfo.getSize() == -1) {
			String url = getDownload(downloadInfo);
			if (url != null) {
				URL verifiedUrl = verifyUrl(url);
				if (verifiedUrl != null) {
					downloadInfo.setUrl(verifiedUrl);
				}
			}
		}
		downloadInfo.startDownload();
		addDownload(downloadInfo);
	}

	private URL verifyUrl(String url) {
		// Only allow HTTP URLs.
		if (!url.toLowerCase().startsWith("http://")) return null;

		// Verify format of URL.
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		// Make sure URL specifies a file.
		if (verifiedUrl.getFile().length() < 2) return null;

		return verifiedUrl;
	}

	private void startNextDownload() {
		if (globalStop) return;
		for (int i = downloads.size() - 1; i >= 0; i--) {
			DownloadInfo download = downloads.get(i);
			if (download.getStatus() != Status.DOWNLOADING) downloads.remove(i);
		}

		if (downloads.size() > 3) {
			for (int i = downloads.size() - 1; i >= 3; i--) {
				pause(downloads.get(i));
			}
		}
		if (!queue.isEmpty() && downloads.size() < 3) {
			startDownload(queue.get(0));
			queue.remove(0);
		}
	}

	private String getDownload(DownloadInfo downloadInfo) {
		try {
			Document doc = Jsoup.parse(new URL(downloadInfo.getUrl()), 60000);
			Elements iframes = doc.select("iframe[src^=http://]");
			for (Element iframe : iframes) {
				Document source;
				source = Jsoup.parse(new URL(iframe.attr("src")), 60000);
				String lines[] = source.data().split("\\r?\\n");
				for (String line : lines) {
					if (line.contains("file: \"http://gateway"))
						return line.replace("file: \"", "").replace("\",", "").trim();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void pause(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) {
			info.pause();
			queue.add(0, info);
			downloads.remove(info);
		}
	}

	public void resume(DownloadInfo info) {
		if (downloads.size() < 4 && queue.contains(info) && info.getStatus() == Status.PAUSED) {
			info.resume();
			queue.remove(info);
		}
	}

	public void cancel(DownloadInfo info) {
		if (info.getStatus() == Status.DOWNLOADING) {
			info.cancel();
			downloads.remove(info);
		}
	}

	public void retry(DownloadInfo info) {
		if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
			if (!downloads.contains(info)) downloads.add(info);
			if (queue.contains(info)) queue.remove(info);
			info.retry();
		}
	}

	public void restart(DownloadInfo info) {
		if (info.getStatus() == Status.CANCELLED || info.getStatus() == Status.ERROR) {
			if (!downloads.contains(info)) downloads.add(info);
			if (queue.contains(info)) queue.remove(info);
			info.restart();
		}
	}
}