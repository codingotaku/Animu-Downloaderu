package com.dakusuta.tools.anime.source;

public enum Source {
	SERVER_1("Server 1"), SERVER_2("Server 2"), SERVER_3("Server 3"), SERVER_4("Server 4");
	private String path;
	Source(String text){
		
	}
	
	public void setPath(String path){
		this.path=path;
	}
	public String getPath() {
		return path;
	}
	
	
	
}
