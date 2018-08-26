package com.dakusuta.tools.anime.download;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Rahul S
 */
public class Downloader implements Runnable {
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
		download();
	}

	// Start downloading segment
	private void download() {
		int retry = 0;
		while (callBack.getStaus() == Status.DOWNLOADING && (segment.start < segment.end)) {
			try {
				this.httpURLConnection = (HttpURLConnection) downloadURL.openConnection();
				httpURLConnection.setRequestProperty("Range", "bytes=" + segment.start + "-" + segment.end);
				httpURLConnection.connect();

				try {
					this.file = new RandomAccessFile(segment.file, "rw");
				} catch (FileNotFoundException e) {
					error();
					closeSession();
					e.printStackTrace();
					break;
				}

				file.seek(segment.downloaded);

				InputStream is = httpURLConnection.getInputStream();

				byte data[];
				int readNum;

				data = new byte[MAX_BUFFER_SIZE];
				while (callBack.getStaus() == Status.DOWNLOADING
						&& ((readNum = is.read(data, 0, MAX_BUFFER_SIZE)) != -1)
						&& (segment.start < segment.end)) { // Redundant
					// Write buffer to file.
					file.write(data, 0, readNum);
					segment.start += readNum;
					segment.downloaded += readNum;
					callBack.add(readNum, Status.DOWNLOADING);
				}
				closeSession();
				break;
			} catch (IOException e) {
				if (retry == MAX_RETRY) {
					error();
				} else {
					try {
						Thread.sleep(DELAY_BETWEEN_RETRY);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					retry++;
				}
				e.printStackTrace();
			} finally {
				closeSession();
				if (retry == MAX_RETRY) break;
			}
		}
	}

	private void error() {
		status = Status.ERROR;
		callBack.add(0, status);
	}
	private void closeSession() {
		try {
			if (file != null) file.close();
			if (httpURLConnection != null) httpURLConnection.disconnect();
		} catch (IOException e) {
			error();
		}
	}
}