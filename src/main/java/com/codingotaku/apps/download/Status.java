package com.codingotaku.apps.download;

/**
 * @author Rahul S <br>
 *         Contains download status
 */
public enum Status {
	PENDING, 
	PAUSED, 
	FINISHED, 
	ERROR, 
	RESUME, 
	CANCELLED, 
	DOWNLOADING, 
	MERGING_FILES, 
	CANCELLING;
}
