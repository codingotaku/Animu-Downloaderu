package com.codingotaku.apps.source;

public class Result {
	private Status status;
	private Exception error;

	Result() {
		status = Status.OK;
	}
	
	Result(Exception e) {
		this.error = e;
		status = e == null ? Status.OK : Status.ERROR;
	}

	public Status getStatus() {
		return status;
	}

	public Exception getError() {
		return error;
	}

	public enum Status {
		OK, ERROR
	}
}
