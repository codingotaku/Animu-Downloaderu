package com.dakusuta.tools.anime.callback;

import javafx.scene.image.Image;

public interface Crawler {
	public void loading();
	public void loaded(String content);
	public void poster(Image image);
}
