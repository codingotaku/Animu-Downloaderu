package com.dakusuta.tools.anime.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.dakusuta.tools.anime.callback.DownloadObserver;
import com.dakusuta.tools.anime.custom.EpisodeLabel;

// This class downloads a file from a URL.
public class DownloadInfo implements Runnable {
	private static final int MAX_THREAD = 8; // Maximum threads allowed for each download
	private int size; // Size of download in bytes
	private int downloaded; // Number of bytes downloaded

	private ArrayList<Segment> segments = new ArrayList<>(); // Download segments

	private DownloadObserver observer = null; // Callback for download status
	private String fileName; // Download file name
	private String anime;
	private String pageUrl;

	private URL url; // Download URL
	private Status status; // Current status of download
	private Callback callBack = new Callback(this) {
		@Override
		public void add(int progress, Status status) {
			addDownloaded(progress);
			if (status == Status.ERROR) {
				error();
			} else observer.downloading(info);
		}
	};

	// For tracking download progress
	private synchronized void addDownloaded(int count) {
		downloaded += count;
	}

	// Constructor for Download.
	public DownloadInfo(EpisodeLabel episode, DownloadObserver observer) {
		this.observer = observer;
		try {
			this.pageUrl = episode.getUrl();
			this.url = new URL(pageUrl);
		} catch (MalformedURLException e) {
			observer.error(this);
			e.printStackTrace();
		}
		this.anime = episode.getAnime();

		String home = System.getProperty("user.home");
		String folder = home + "\\Downloads\\" + anime;
		new File(folder).mkdir();
		this.fileName = folder + "\\" + episode.toString();
		size = -1;
		downloaded = 0;
		status = Status.PENDING;
		observer.pending(this);
	}

	// Begin the download.
	public void startDownload() {
		String url = generateDownloadUrl();
		URL verifiedUrl = verifyUrl(url);
		if (verifiedUrl != null) {
			setUrl(verifiedUrl);
		} else {
			error();
			return;
		}
		status = Status.DOWNLOADING;
		download();
	}

	private URL verifyUrl(String url) {
		if (url == null) return null;
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

	// Returns total downloaded size
	public int getDownloaded() {
		return downloaded;
	}

	// Get this download's size.
	public int getSize() {
		return size;
	}

	// Get this download's progress.
	public String getProgress() {
		return String.format("%.2f%%", (downloaded / (float) size) * 100);
	}

	// Get this download's status.
	public Status getStatus() {
		return status;
	}

	// Pause this download.
	public void pause() {
		status = Status.PAUSED;
		observer.paused(this);
	}

	// Resume this download.
	public void resume() {
		// Do not allow resuming downloads if it is not paused
		if (status != Status.PAUSED) return;
		status = Status.DOWNLOADING;
		observer.resumed(this);
		download();
	}

	// Cancel this download.
	public void cancel() {
		if (status != Status.DOWNLOADING) return;
		status = Status.CANCELLING;
		observer.cancelling(this);
	}

	// Mark this download as having an error.
	private void error() {
		status = Status.ERROR;
		observer.error(this);
	}

	// Start or resume downloading.
	private void download() {
		Thread thread = new Thread(this);
		thread.start();
	}

	// Get file name portion of URL.
	public String getFileName() {
		return fileName;
	}

	private int setDownloadSize() {
		try {
			getContentSize();
		} catch (IOException e) {
			error();
			e.printStackTrace();
			return -1;
		}

		// Clear out finished download segments if any
		segments.removeIf(segment -> segment.isFinished());

		if (!segments.isEmpty()) return size - downloaded;
		int partLen = size / MAX_THREAD;
		int start = 0;
		int end = partLen;
		for (int i = 0; i < MAX_THREAD - 1; i++) {
			segments.add(new Segment(start, end, fileName + ".part" + (i + 1)));
			start = end + 1;
			end += partLen;
		}

		// Just to make sure no bit is missed out in previous calculation
		segments.add(new Segment(start, end + (size % MAX_THREAD), fileName + ".part" + MAX_THREAD));

		// Delete previously downloaded file if it exists
		File tmp = new File(fileName);
		if (tmp.exists()) tmp.delete();
		return size;
	}

	void setStatus(Status status) {
		this.status = status;
	}

	// Download file.
	public void run() {
		int downloadSize = setDownloadSize();
		if (downloadSize <= 0) return;

		ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREAD);
		ArrayList<File> files = new ArrayList<>(MAX_THREAD);

		segments.forEach(segment -> {
			File file = new File(segment.file);
			try {
				file.createNewFile();
			} catch (IOException e) {
				error();
				e.printStackTrace();
				return;
			}
			files.add(file);
			threadPool.submit(new Downloader(url, segment, status, callBack));
		});

		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

			if (status == Status.PAUSED) return;

			if ((status == Status.ERROR || status == Status.DOWNLOADING) && getDownloaded() != size) {
				error();
				return;
			}

			if (status == Status.CANCELLING) {
				// delete every segments
				segments.forEach(segment -> {
					File f = new File(segment.file);
					if (f.exists()) f.delete();
				});
				segments.clear();
				status = Status.CANCELLED;
				observer.cancelled(this);
				return;
			}

			this.status = Status.MERGING_FILES;
			observer.mergingFiles(this);
			Thread.sleep(5000); // Dirty delay for closing all files
			mergeSegments(files, new File(fileName));
		} catch (InterruptedException e) {
			error();
			e.printStackTrace();
		}
	}

	// Finds and sets total size of file from URL, this also sets file name
	private void getContentSize() throws IOException {
		System.out.println(url.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// Connect to server.
		connection.connect();

		// Make sure response code is in the 200 range.
		if (connection.getResponseCode() / 100 != 2) {
			System.out.println("Response code" + connection.getResponseCode());
			error();
			return;
		}

		// Check for valid content length.
		int contentLength = connection.getContentLength();
		if (contentLength < 1) {
			System.out.println("Content length " + contentLength);
			error();
			return;
		}

		if (!fileName.contains(".")) { // if file type not provided
			String ext = url.getFile();
			ext = ext.substring(ext.lastIndexOf('.'), ext.indexOf('?'));
			fileName += ext;
		}

		size = contentLength;
	}

	// Merge all downloaded segments
	public void mergeSegments(ArrayList<File> files, File mergedFile) {
		RandomAccessFile out;
		try {
			out = new RandomAccessFile(mergedFile, "rw");
			out.seek(0);
		} catch (IOException e) {
			error();
			e.printStackTrace();
			return;
		}

		for (File file : files) {
			if (!file.exists()) {
				System.err.println("File not found : " + file.getName());
				error();
				break;
			}

			byte[] data = new byte[8192];
			int c = 0;
			RandomAccessFile in = null;

			try {
				in = new RandomAccessFile(file, "r");
				while ((c = in.read(data)) != -1) {
					out.write(data, 0, c);
				}
			} catch (IOException e) {
				error();
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						error();
						e.printStackTrace();
					}
				}
			}
		}

		files.forEach(file -> file.delete());

		try {
			out.close();
		} catch (IOException e) {
			error();
			e.printStackTrace();
		}

		if (status != Status.ERROR) {
			status = Status.FINISHED;
			observer.finished(this);
		}
	}

	// Set download URL
	private void setUrl(URL url) {
		this.url = url;
	}

	// Returns current download segments
	public ArrayList<Segment> getSegments() {
		return segments;
	}

	public void retry() {
		if (status == Status.DOWNLOADING) return;

		if (status == Status.ERROR) {
			status = Status.DOWNLOADING;
			String url = generateDownloadUrl();

			URL verifiedUrl = verifyUrl(url);
			if (verifiedUrl == null) {
				error();
				return;
			}

			setUrl(verifiedUrl);
			download();
		}
	}

	public void restart() {
		if (status == Status.DOWNLOADING) return;

		cancel();
		downloaded = 0;

		startDownload();
	}

	private String generateDownloadUrl() {
		try {
			Document doc = Jsoup.parse(new URL(pageUrl), 60000);
			Pattern pattern = Pattern.compile("(http://.*(.mp4\\?)[^\"\']*)");
			Matcher matcher = pattern.matcher(doc.data());
			

			if (matcher.find()) {
				return matcher.group(0);
			} else {
				System.out.println("not found");
				if (pageUrl.contains("animexd")) {
					pageUrl = doc.select("div.sd-nav > a:contains(English Subbed)").last().attr("href");
					return generateAltDownloadUrl();
				}
			}
		} catch (IOException e) {
			error();
			e.printStackTrace();
		}
		return null;
	}

	private String generateAltDownloadUrl() {
		try {
			Document doc = Jsoup.parse(new URL(pageUrl), 60000);
			Pattern pattern = Pattern.compile("(http://.*(.mp4\\?)[^\"\']*)");
			Matcher matcher = pattern.matcher(doc.data());

			if (matcher.find()) { return matcher.group(0); }
		} catch (IOException e) {
			error();
			e.printStackTrace();
		}
		return null;
	}

}