package com.codingotaku.apps.download;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.codingotaku.apps.callback.DownloadObserver;
import com.codingotaku.apis.animecrawler.Episode;
import com.codingotaku.apps.util.Constants;

// This class downloads a file from a URL.
public class DownloadInfo implements Runnable {
	private static final int MAX_THREAD = 8; // Maximum threads allowed for each download
	private long size; // Size of download in bytes
	private long downloaded; // Number of bytes downloaded

	private ArrayList<Segment> segments = new ArrayList<>(); // Download segments

	private DownloadObserver observer = null; // Callback for download status
	private String fileName; // Download file name
	private String anime;
	private URL url;
	private Status status; // Current status of download
	private Callback callBack = new Callback(this) {
		@Override
		public void add(long progress, Status status) {
			addDownloaded(progress);
			if (status == Status.ERROR) {
				error();
			} else
				observer.downloading(info);
		}
	};
	private Episode episode;

	// For tracking download progress
	private synchronized void addDownloaded(long count) {
		downloaded += count;
	}

	// Constructor for Download.
	public DownloadInfo(Episode episode, DownloadObserver observer) {
		this.episode = episode;
		this.observer = observer;
		//sanitize file and folder names
		this.anime = episode.getTitle().replaceAll("[^a-zA-Z0-9\\-]", "_");
		
		var folderName = Constants.downloadFolder + "/" + anime;
		this.fileName = folderName + File.separator + (episode.toString().replaceAll("[^a-zA-Z0-9\\.\\-]", "_"));
		
		new File(folderName).mkdir();

		size = -1;
		downloaded = 0;
		status = Status.PENDING;
		observer.pending(this);
	}

	// Begin the download.
	public void startDownload() {
		String url;
		try {
			url = episode.getVideoUrl();
		} catch (IOException e) {
			error();
			return;
		}
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
		if (url == null)
			return null;
		// Only allow HTTP URLs.
		if (!url.toLowerCase().startsWith("http://"))
			return null;

		// Verify format of URL.
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
			// Make sure URL specifies a file.
			if (verifiedUrl.getFile().length() < 2)
				return null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return verifiedUrl;
	}

	// Returns total downloaded size
	public long getDownloaded() {
		return downloaded;
	}

	// Get this download's size.
	public long getSize() {
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
		if (status != Status.PAUSED)
			return;
		status = Status.DOWNLOADING;
		observer.resumed(this);
		download();
	}

	// Cancel this download.
	public void cancel() {
		if (status != Status.DOWNLOADING)
			return;
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

	private long setDownloadSize() {
		try {
			getContentSize();
		} catch (IOException e) {
			error();
			e.printStackTrace();
			return -1;
		}

		// Clear out finished download segments if any
		segments.removeIf(segment -> segment.isFinished());

		if (!segments.isEmpty())
			return size - downloaded;
		long partLen = size / MAX_THREAD;
		long start = 0;
		long end = partLen;
		for (long i = 0; i < MAX_THREAD - 1; i++) {
			segments.add(new Segment(start, end, fileName + ".part" + (i + 1)));
			start = end + 1;
			end += partLen;
		}

		// Just to make sure no bit is missed out in previous calculation
		segments.add(new Segment(start, end + (size % MAX_THREAD), fileName + ".part" + MAX_THREAD));

		// Delete previously downloaded file if it exists
		File tmp = new File(fileName);
		if (tmp.exists())
			tmp.delete();
		return size;
	}

	void setStatus(Status status) {
		this.status = status;
	}

	// Download file.
	public void run() {
		long downloadSize = setDownloadSize();
		if (downloadSize <= 0)
			return;

		ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREAD);
		ArrayList<File> files = new ArrayList<>(MAX_THREAD);

		for (Segment segment : segments) {
			try {
				File file = new File(segment.file);
				file.createNewFile();
				files.add(file);
				threadPool.submit(new Downloader(url, segment, status, callBack));
			} catch (IOException e) {
				error();
				e.printStackTrace();
				break;
			}
		}

		try {
			threadPool.shutdown();
			threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

			if (status == Status.PAUSED)
				return;

			if ((status == Status.ERROR || status == Status.DOWNLOADING) && getDownloaded() != size) {
				error();
			} else if (status == Status.CANCELLING) {
				// delete every segments
				segments.forEach(segment -> {
					File f = new File(segment.file);
					if (f.exists())
						f.delete();
				});
				segments.clear();
				status = Status.CANCELLED;
				observer.cancelled(this);
			} else {
				this.status = Status.MERGING_FILES;
				observer.mergingFiles(this);
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
			return;
		}

		// Check for valid content length.
		long contentLength = connection.getContentLength();
		if (contentLength < 1) {
			System.out.println("Content length " + contentLength);
			error();
			return;
		}

//		if (!fileName.endsWith(".mp4")||!fileName.endsWith(".flv")) { // if file type not provided
		String ext = url.getFile();
		ext = ext.substring(ext.lastIndexOf('.'), ext.indexOf('?'));
		fileName += ext;
//		}

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
		} else {
			observer.error(this);
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
		if (status == Status.DOWNLOADING)
			return;

		if (status == Status.ERROR) {
			status = Status.DOWNLOADING;
			String url = null;
			try {
				url = episode.getVideoUrl();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			URL verifiedUrl = verifyUrl(url);
			if (verifiedUrl == null) {
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
		downloaded = 0;

		startDownload();
	}

	public String getUrl() {
		return url.toString();
	}
}
