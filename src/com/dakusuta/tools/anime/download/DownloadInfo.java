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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dakusuta.tools.anime.callback.DownloadObserver;

// This class downloads a file from a URL.
public class DownloadInfo implements Runnable {
	private static final int MAX_THREAD = 8; // Maximum threads allowed for each download
	private int size; // Size of download in bytes
	private int downloaded; // Number of bytes downloaded

	private ArrayList<Segment> segments = new ArrayList<>(); // Download segments

	private DownloadObserver observer = null; // Callback for download status
	private String fileName; // Download file name
	private String animeName;
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
	public DownloadInfo(String fileName, String pageUrl, DownloadObserver observer) {
		this.observer = observer;
		try {
			this.pageUrl = pageUrl;
			this.url = new URL(pageUrl);
		} catch (MalformedURLException e) {
			observer.error(this);
			e.printStackTrace();
		}
		animeName = pageUrl.substring(pageUrl.lastIndexOf("/") + 1, pageUrl.indexOf("episode") - 1);

		String home = System.getProperty("user.home");
		String folder = home + "\\Downloads\\" + animeName;
		new File(folder).mkdir();
		this.fileName = folder + "\\" + fileName;
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

	// Get this download's URL.
	public String getUrl() {
		return url.toString();
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
	public double getProgress() {
		return downloaded / (double) size;
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
		status = Status.CANCELLED;

		// So that download will start from the beginning next time
		// Delete all segment files
		new Thread(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			segments.forEach(segment -> {
				File f = new File(segment.file);
				if (f.exists()) f.delete();
			});
			segments.clear();
		}).start();

		observer.cancelled(this);
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
			}
			files.add(file);
			threadPool.submit(new Downloader(url, segment, status, callBack));
		});

		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			if ((status == Status.ERROR || status == Status.DOWNLOADING) && getDownloaded() < size) {
				error();
				return;
			}
			if (getDownloaded() >= size) {
				status = Status.FINISHED;
				observer.finished(this);
				Thread.sleep(5000); // Dirty delay for closing all files
				mergeSegments(files, new File(fileName));
			}
		} catch (InterruptedException e) {
			error();
			e.printStackTrace();
		}
	}

	// Finds and sets total size of file from URL, this also sets file name
	private void getContentSize() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// Connect to server.
		connection.connect();

		// Make sure response code is in the 200 range.
		if (connection.getResponseCode() / 100 != 2) {
			System.out.println("Response code" + connection.getResponseCode());
			error();
		}

		// Check for valid content length.
		int contentLength = connection.getContentLength();
		if (contentLength < 1) {
			System.out.println("Content length " + contentLength);
			error();
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

		files.forEach(file -> {
			byte[] data = new byte[8192];
			int c = 0;
			RandomAccessFile in = null;
			try {
				in = new RandomAccessFile(file, "r");
				while ((c = in.read(data)) != -1)
					out.write(data, 0, c);
			} catch (IOException e) {
				error();
				e.printStackTrace();
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				file.delete();
			}
		});

		try {
			out.close();
		} catch (IOException e) {
			error();
			e.printStackTrace();
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
		if (status == Status.ERROR) {
			status = Status.DOWNLOADING;
			String url = generateDownloadUrl();

			URL verifiedUrl = verifyUrl(url);
			if (verifiedUrl != null) {
				setUrl(verifiedUrl);
				download();
			} else {
				error();
			}
		}
	}

	public void restart() {
		cancel();
		downloaded = 0;

		startDownload();
	}

	private String generateDownloadUrl() {
		try {
			Document doc = Jsoup.parse(new URL(pageUrl), 60000);
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
			error();
			e.printStackTrace();
		}
		return null;
	}
}