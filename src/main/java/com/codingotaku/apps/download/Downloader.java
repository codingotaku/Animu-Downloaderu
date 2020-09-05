package com.codingotaku.apps.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Rahul S
 */
public class Downloader implements Runnable {
	private static Logger logger = Logger.getLogger(Downloader.class.getName());
	private static final int DELAY_BETWEEN_RETRY = 10000; // Delay between retry after download error
	private static final int MAX_BUFFER_SIZE = 8192; // Max size of download buffer. 8KB
	private static final int MAX_RETRY = 8; // Maximum retry for each download thread

	private URL downloadURL; // Download URL
	private Segment segment; // Download segment (contains start and end bytes)

	private RandomAccessFile file; // temporary file for storing downloaded segment

	private Callback callBack; // Callback for tracking download progress
	private Status status; // Current segment status (Downloading, error, pause etc.)
	private HttpURLConnection httpURLConnection = null;

	// Downloader constructor
	public Downloader(URL downloadURL, Segment part, Status status, Callback callBack) {
		this.downloadURL = downloadURL;
		this.segment = part;

		this.status = status;
		this.callBack = callBack;
	}

	// Running download in a different thread
	@Override
	public void run() {
		try {
			downloadSegments();
		} catch (InterruptedException e) {
			error();
			Thread.currentThread().interrupt();
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	private boolean createDownloadFile() {
		try {
			this.file = new RandomAccessFile(segment.getFile(), "rw");
			return false;
		} catch (FileNotFoundException e) {
			error();
			closeSession();
			logger.log(Level.SEVERE, e.getMessage());
			return true;
		}
	}

	// Start downloading segment
	private void downloadSegments() throws InterruptedException {
		int retry = 0;
		boolean quit = false;

		while (!quit && callBack.getStaus().equals(Status.DOWNLOADING) && (segment.getStart() < segment.getEnd())) {

			try {
				this.httpURLConnection = (HttpURLConnection) downloadURL.openConnection();
				httpURLConnection.setRequestProperty("Range", "bytes=" + segment.getStart() + "-" + segment.getEnd());
				httpURLConnection.connect();

				quit = createDownloadFile();

				if (quit) {
					continue;
				}

				file.seek(segment.getDownloaded());

				InputStream is = httpURLConnection.getInputStream();

				int readNum;

				byte[] data = new byte[MAX_BUFFER_SIZE];
				while (callBack.getStaus().equals(Status.DOWNLOADING) && (readNum = is.read(data, 0, MAX_BUFFER_SIZE)) != -1
						&& (segment.getStart() < segment.getEnd())) { // Redundant
					// Write buffer to file.
					file.write(data, 0, readNum);
					segment.setStart(segment.getStart() + readNum );
					segment.setDownloaded(segment.getDownloaded() + readNum );
					callBack.add(readNum, Status.DOWNLOADING);
				}
				closeSession();
				quit = true;

			} catch (IOException e) {
				if (retry == MAX_RETRY) {
					logger.log(Level.SEVERE, "Maximum retry, failed request "+ e.getMessage());
					error();
				}

				Thread.sleep(DELAY_BETWEEN_RETRY);
				retry++;

				logger.log(Level.SEVERE, e.getMessage());
			} finally {
				closeSession();
				if (retry == MAX_RETRY) {
					quit = true;
				}

			}
		}
	}

	private void error() {
		status = Status.ERROR;
		callBack.add(0, status);
	}

	private void closeSession() {
		try {
			if (file != null)
				file.close();
			if (httpURLConnection != null)
				httpURLConnection.disconnect();
		} catch (IOException e) {
			logger.log(Level.SEVERE, e.getMessage());
			error();
		}
	}
}