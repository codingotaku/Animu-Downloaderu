package com.dakusuta.tools.anime.custom;

public interface BackupObserver {
	public void started();
	public void progress(int i);
	public void completed();
}
