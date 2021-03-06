package com.codingotaku.apps.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codingotaku.apis.animecrawler.Episode;
import com.codingotaku.apps.callback.DownloadObserver;
import com.codingotaku.apps.util.Constants;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

// This class downloads a file from a URL.
public class DownloadInfo implements Runnable {
	private static Logger logger = Logger.getLogger(DownloadInfo.class.getName());
	private static final int MAX_THREAD = Constants.getThreadCount(); // Maximum threads allowed for each download
	private SimpleLongProperty size; // Size of download in bytes
	private SimpleLongProperty downloaded; // Number of bytes downloaded

	private ArrayList<Segment> segments = new ArrayList<>(); // Download segments

	private DownloadObserver observer = null; // Callback for download status
	private SimpleStringProperty fileName; // Download file name
	private SimpleStringProperty anime;
	private URL url;
	private SimpleStringProperty progress; // Progress in percentage
	private Status status; // Current status of download
	private SimpleStringProperty statusString; // Current status of download in string (for table view)
	private Callback callBack = new Callback(this) {
		@Override
		public void add(long progress, Status status) {
			addDownloaded(progress);
			if (status == Status.ERROR) {
				error();
			} else {
				observer.downloading(info);
			}

		}

		// For tracking download progress
		private synchronized void addDownloaded(long count) {
			progress.set(String.format("%.2f%%", (downloaded.get() / (float) size.get()) * 100));
			downloaded.set(downloaded.get() + count);
		}
	};
	private Episode episode;

	// Constructor for Download.
	public DownloadInfo(Episode episode, DownloadObserver observer) {
		this.episode = episode;
		this.observer = observer;
		// sanitize file and folder names
		this.anime = new SimpleStringProperty(episode.getanimeName().replaceAll("[^a-zA-Z0-9 \\-]", "_"));

		var folderName = Constants.getDownloadFolder() + "/" + anime.get();
		this.fileName = new SimpleStringProperty(
				folderName + File.separator + (episode.toString().replaceAll("[^a-zA-Z0-9 \\.\\-]", "_")));

		new File(folderName).mkdir();

		size = new SimpleLongProperty(-1);
		downloaded = new SimpleLongProperty(0);
		this.progress = new SimpleStringProperty("0%");
		statusString = new SimpleStringProperty();
		setStatus(Status.PENDING);
		observer.pending(this);
	}

	// Begin the download.
	public void startDownload() {
		String newUrl;
		try {
			newUrl = episode.getVideoUrl();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
			error();
			return;
		}
		logger.log(Level.INFO, "url is " + newUrl);
		URL verifiedUrl = verifyUrl(newUrl);
		if (verifiedUrl != null) {
			setUrl(verifiedUrl);
		} else {
			logger.log(Level.SEVERE, "verified url is null");
			error();
			return;
		}
		setStatus(Status.DOWNLOADING);
		download();
	}

	private URL verifyUrl(String url) {
		if (url == null)
			return null;
		// Verify format of URL.
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
			// Make sure URL specifies a file.
			if (verifiedUrl.getFile().length() < 2)
				return null;
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage());
		}

		return verifiedUrl;
	}

	// Returns total downloaded size
	public long getDownloaded() {
		return downloaded.get();
	}

	// Get this download's size.
	public long getSize() {
		return size.get();
	}

	// Get this download's progress.
	public String getProgress() {
		return progress.get();
	}

	// Get this download's status.
	public Status getStatus() {
		return status;
	}

	public void setStatusString(String status) {
		statusString.set(status);
	}

	public String getStatusString() {
		return statusString.get();
	}

	// Pause this download.
	public void pause() {
		setStatus(Status.PAUSED);
		observer.paused(this);
	}

	// Resume this download.
	public void resume() {
		// Do not allow resuming downloads if it is not paused
		if (status != Status.PAUSED)
			return;
		setStatus(Status.DOWNLOADING);
		observer.resumed(this);
		download();
	}

	// Cancel this download.
	public void cancel() {
		if (status != Status.DOWNLOADING)
			return;
		setStatus(Status.CANCELLING);
		observer.cancelling(this);
	}

	// Mark this download as having an error.
	private void error() {
		setStatus(Status.ERROR);
		observer.error(this);
	}

	// Start or resume downloading.
	private void download() {
		Thread thread = new Thread(this);
		thread.start();
	}

	// Get file name portion of URL.
	public String getFileName() {
		return fileName.get();
	}

	private long setDownloadSize() {
		try {
			getContentSize();
		} catch (IOException e) {
			error();
			logger.log(Level.SEVERE, e.getMessage());
			return -1;
		}

		// Clear out finished download segments if any
		segments.removeIf(Segment::isFinished);

		if (!segments.isEmpty())
			return size.get() - downloaded.get();
		long partLen = size.get() / MAX_THREAD;
		long start = 0;
		long end = partLen;
		for (long i = 0; i < MAX_THREAD - 1; i++) {
			segments.add(new Segment(start, end, fileName.get() + ".part" + (i + 1)));
			start = end + 1;
			end += partLen;
		}

		// Just to make sure no bit is missed out in previous calculation
		segments.add(new Segment(start, end + (size.get() % MAX_THREAD), fileName.get() + ".part" + MAX_THREAD));

		// Delete previously downloaded file if it exists
		File tmp = new File(fileName.get());
		if (tmp.exists()) {
			try {
				java.nio.file.Files.delete(tmp.toPath());
			} catch (IOException e) {
				error();
				logger.log(Level.SEVERE, e.getMessage());
			}
		}
		return size.get();
	}

	void setStatus(Status status) {
		this.status = status;
		setStatusString(status.name());
	}

	// Download file.
	public void run() {
		long downloadSize = setDownloadSize();
		if (downloadSize <= 0) {
			return;
		}

		ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREAD);
		ArrayList<File> files = new ArrayList<>(MAX_THREAD);

		for (Segment segment : segments) {
			try {
				File file = new File(segment.getFile());
				if (file.createNewFile()) {
					files.add(file);
					threadPool.submit(new Downloader(url, segment, status, callBack));
				} else {
					logger.log(Level.SEVERE, "Couldn't create new file!");
					error();
				}

			} catch (IOException e) {
				error();
				logger.log(Level.SEVERE, e.getMessage());
				break;
			}
		}

		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

			switch (status) {
			case PAUSED:
				return;
			case ERROR:
				error();
				break;
			case DOWNLOADING:
				if (getDownloaded() != size.get()) {
					logger.log(Level.SEVERE, "Error : Downloaded size is not same as actual size");
					error();
				} else {
					setStatus(Status.MERGING_FILES);
					observer.mergingFiles(this);
					Thread.sleep(5000); // Dirty delay for closing all files
					mergeSegments(files, new File(fileName.get()));
				}
				break;
			case CANCELLING:
				segments.forEach(segment -> {
					File f = new File(segment.getFile());
					try {
						java.nio.file.Files.delete(f.toPath());
					} catch (IOException e) {
						error();
						logger.log(Level.SEVERE, e.getMessage());
					}

				});
				segments.clear();
				setStatus(Status.CANCELLED);
				observer.cancelled(this);
				break;
			default:
				break;
			}
		} catch (InterruptedException e) {
			error();
			logger.log(Level.SEVERE, e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	// Finds and sets total size of file from URL, this also sets file name
	private void getContentSize() throws IOException {
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		// Connect to server.
		connection.connect();

		// Make sure response code is in the 200 range.
		if (connection.getResponseCode() / 100 != 2) {
			logger.log(Level.INFO, "Response code " + connection.getResponseCode());
			
			if(connection.getResponseCode() > 300 && connection.getResponseCode() < 400) {
				logger.log(Level.INFO, "Retrying");
				// Attempt one more time
				String newURL = "";
				try {
					newURL = episode.getVideoUrl();
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage());
				}
				URL verifiedUrl = verifyUrl(newURL);
				if (verifiedUrl == null) {
					logger.log(Level.SEVERE, "URl is null");
					error();
				} else {
					setUrl(verifiedUrl);
					connection = (HttpURLConnection) url.openConnection();
				}
			} else {
				error();
				return;
			}
			connection.connect();
			if (connection.getResponseCode() / 100 != 2) {
				logger.log(Level.SEVERE, "Response code " + connection.getResponseCode());
				error();
			}
			return;
		}

		// Check for valid content length.
		long contentLength = connection.getContentLength();
		if (contentLength < 1) {
			logger.log(Level.INFO, "Content length {0}", contentLength);
			error();
			return;
		}

		String ext = url.getFile();
		ext = ext.substring(ext.lastIndexOf('.'), ext.indexOf('?'));
		if (!fileName.get().endsWith(ext)) { // if retry or other operations calls, filename remains same
			fileName.set(fileName.get() + ext);
		}

		size.set(contentLength);
	}

	// Merge all downloaded segments
	public void mergeSegments(List<File> files, File mergedFile) {
		try (RandomAccessFile out = new RandomAccessFile(mergedFile, "rw")) {
			out.seek(0);

			for (File file : files) {
				if (!file.exists()) {
					logger.log(Level.SEVERE, "File not found : {0}", file.getName());
					error();
					break;
				}

				writeToFile(out, file);
			}

			files.forEach(file -> {
				try {
					java.nio.file.Files.delete(file.toPath());
				} catch (IOException e) {
					logger.log(Level.SEVERE, e.getMessage());
				}
			});
			closeFileStream(out);

			if (status != Status.ERROR) {
				setStatus(Status.FINISHED);
				observer.finished(this);
			} else {
				observer.error(this);
			}

		} catch (IOException e) {
			error();
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	private void writeToFile(RandomAccessFile out, File file) {
		byte[] data = new byte[8192];
		int c = 0;
		try (RandomAccessFile in = new RandomAccessFile(file, "r")) {
			while ((c = in.read(data)) != -1) {
				out.write(data, 0, c);
			}
		} catch (IOException e) {
			error();
			logger.log(Level.SEVERE, e.getMessage());
		}

	}

	private void closeFileStream(RandomAccessFile file) {
		if (file != null) {
			try {
				file.close();
			} catch (IOException e) {
				error();
				logger.log(Level.SEVERE, e.getMessage());
			}
		}

	}

	// Set download URL
	private void setUrl(URL url) {
		this.url = url;
	}

	// Returns current download segments
	public List<Segment> getSegments() {
		return segments;
	}

	public void retry() {
		if (status == Status.DOWNLOADING)
			return;

		if (status == Status.ERROR || status == Status.CANCELLED) {
			setStatus(Status.DOWNLOADING);
			String newURL = null;
			try {
				newURL = episode.getVideoUrl();
			} catch (IOException e) {
				logger.log(Level.SEVERE, e.getMessage());
			}

			URL verifiedUrl = verifyUrl(newURL);
			if (verifiedUrl == null) {
				logger.log(Level.SEVERE, "URL is null");
				error();
			} else {
				setUrl(verifiedUrl);
				download();
			}
		}
	}

	public void restart() {
		if (status == Status.DOWNLOADING)
			return;

		cancel();
		downloaded.set(0);

		startDownload();
	}

	public String getUrl() {
		return url.toString();
	}
}
