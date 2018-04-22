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

import com.dakusuta.tools.anime.callback.DownloadObserver;

// This class downloads a file from a URL.
public class DownloadInfo implements Runnable {
	private static final int MAX_THREAD = 8; // Maximum threads allowed for each download
	DownloadObserver observer = null; // Callback for download status

	ArrayList<Segment> segments = new ArrayList<>(); // Download segments
	private String fileName; // Download file name
	private URL url; // Download URL
	private int size; // Size of download in bytes
	private int downloaded; // Number of bytes downloaded
	private Status status; // Current status of download
	private Callback callBack = new Callback(this) {
		@Override
		public void add(int progress, Status status) {
			addDownloaded(progress);
			if (status == Status.ERROR) {
				error();
			} else observer.downloading(info, getDownloaded());
		}
	};

	// For tracking download progress
	private synchronized void addDownloaded(int count) {
		downloaded += count;
	}

	// Constructor for Download.
	public DownloadInfo(String tmpUrl, DownloadObserver observer) {
		this.observer = observer;
		try {
			this.url = new URL(tmpUrl);
		} catch (MalformedURLException e) {
			observer.error(this);
			e.printStackTrace();
		}
		fileName = "";
		size = -1;
		downloaded = 0;
		status = Status.PENDING;
		observer.pending(this);
	}

	// Begin the download.
	public void startDownload() {
		status = Status.DOWNLOADING;
		download();
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
		if (status == Status.CANCELLED) downloaded = 0;
		if (size == -1) { // Do not allow resuming downloads from queue
			status = Status.PENDING;
			return;
		}
		status = Status.DOWNLOADING;
		observer.resume(this);
		download();
	}

	// Cancel this download.
	public void cancel() {
		status = Status.CANCELLED;
		segments.clear(); // So that download will start from the beginning next time
		// Delete all segment files

		for (int i = 0; i < MAX_THREAD; i++) {
			File f = new File(fileName + ".part" + (i + 1));
			if (f.exists()) f.delete();
		}

		observer.cancelled(this);
	}

	// Mark this download as having an error.
	private void error() {
		status = Status.ERROR;
		observer.error(this);
	}

	// Start or resume downloading.
	public void download() {
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
		}

		if (status == Status.ERROR) return -1;

		// Clear out finished download segments if any
		for (int i = segments.size() - 1; i > -1; i--) {
			if (segments.get(i).isFinished()) {
				segments.remove(i);
			}
		}

		if (!segments.isEmpty()) return size - downloaded;

		int partLen = size / MAX_THREAD;
		int start = 0;
		int end = partLen;
		for (int i = 0; i < MAX_THREAD - 1; i++) {
			segments.add(new Segment(start, end));
			start = end + 1;
			end += partLen;
		}

		// Just to make sure no bit is missed out in previous calculation
		segments.add(new Segment(start, end + (size % MAX_THREAD)));

		// Delete previously downloaded file if it exists
		String fName = fileName;
		File tmp = new File(fName);
		if (tmp.exists()) tmp.delete();
		return size;
	}

	// Download file.
	public void run() {
		try {
			int downloadSize = setDownloadSize();
			if (downloadSize < 0) return;

			ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREAD);
			File files[] = new File[MAX_THREAD];
			for (int i = 0; i < MAX_THREAD; i++) {
				String fName = fileName + ".part" + (i + 1);
				RandomAccessFile file = new RandomAccessFile(fName, "rw");
				files[i] = new File(fName);
				files[i].createNewFile();

				threadPool.submit(new Downloader(url, segments.get(i), file, status, callBack));
			}
			threadPool.shutdown();

			try {
				threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
				if ((status == Status.ERROR || status == Status.DOWNLOADING) && getDownloaded() < size) {
					error();
					return;
				}
				if (getDownloaded() >= size) {
					status = Status.FINISHED;
					observer.finished(this);
					mergeSegments(files, new File(fileName));
				}
			} catch (InterruptedException e) {
				error();
				e.printStackTrace();
			}

		} catch (IOException e) {
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
		size = contentLength;

		fileName = url.getFile();
		fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.indexOf('?'));
		fileName = "C:\\Users\\rahul\\Downloads\\files\\" + fileName;
	}

	// Merge all downloaded segments
	public void mergeSegments(File[] files, File mergedFile) {
		int readNum = 0;

		try {
			RandomAccessFile outfile = new RandomAccessFile(mergedFile, "rw");
			outfile.seek(0);
			for (File file : files) {
				byte[] data = new byte[8192];
				RandomAccessFile infile = new RandomAccessFile(file, "r");

				while ((readNum = infile.read(data)) != -1) {
					outfile.write(data, 0, readNum);
				}

				infile.close();
				file.delete();
			}

			outfile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Set download URL
	public void setUrl(URL url) {
		this.url = url;
	}

	// Returns current download segments
	public ArrayList<Segment> getSegments() {
		return segments;
	}

	public void retry() {
		if (status == Status.ERROR) startDownload();
	}

	public void restart() {
		cancel();
		startDownload();
	}
}