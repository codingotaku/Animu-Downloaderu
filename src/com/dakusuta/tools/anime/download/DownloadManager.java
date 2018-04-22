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
	public void resume(DownloadInfo downloadInfo) {
		observer.resume(downloadInfo);
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
		startNextDownload();
		observer.error(downloadInfo);
	}

	@Override
	public void pending(DownloadInfo downloadInfo) {
		observer.pending(downloadInfo);
	}

	@Override
	public void finished(DownloadInfo downloadInfo) {
		startNextDownload();
		observer.finished(downloadInfo);
	}

	public void cancelAll() {
		globalStop = true;
		downloads.forEach(download -> download.cancel());
		queue.forEach(download -> download.cancel());
	}

	public void pauseAll() {
		globalStop = true;
		downloads.forEach(download -> download.pause());
		queue.forEach(download -> download.pause());
	}

	public void resumeAll() {
		globalStop = false;
		downloads.forEach(download -> download.resume());
		startNextDownload();
	}

	public void retryAll() {
		globalStop = false;
		downloads.forEach(download -> download.retry());
		startNextDownload();
	}

	public void restartAll() {
		globalStop = false;
		downloads.forEach(download -> download.restart());
		startNextDownload();
	}

	private void addDownload(DownloadInfo downloadInfo) {
		downloads.add(downloadInfo);
	}

	private void startDownload(DownloadInfo downloadInfo) {
		String url = getDownload(downloadInfo);
		if (url != null) {
			URL verifiedUrl = verifyUrl(url);
			if (verifiedUrl != null) {
				downloadInfo.setUrl(verifiedUrl);
				downloadInfo.startDownload();
				addDownload(downloadInfo);
			}
		}
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
		if (downloads.size() > 3) {
			for (int i = downloads.size() - 1; i >= 3; i--) {
				downloads.get(i).pause();
				queue.add(downloads.get(i));
				downloads.remove(i);
			}
		}
		if (!globalStop && !queue.isEmpty() && downloads.size() < 3) {
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

}