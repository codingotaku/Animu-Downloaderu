package com.dakusuta.tools.anime.source;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;

import com.dakusuta.tools.anime.AnimuDownloaderu;
import com.dakusuta.tools.anime.custom.AnimeLabel;
import com.dakusuta.tools.anime.custom.EpisodeLabel;

import javafx.scene.image.Image;

public abstract class IServer {
	protected String path = null;
	protected Document selectedDoc = null;
	protected List<AnimeLabel> animeList = new ArrayList<>();
	protected List<EpisodeLabel> episodes = new ArrayList<>();

	protected String getPath() {
		return path;
	}

	public abstract List<AnimeLabel> loadAnime(Document doc);

	public abstract void getSynopsys(AnimeLabel label);

	public abstract List<EpisodeLabel> loadEpisodes();

	Image getPoster(String imgUrl) {
		try {
			URL url = new URL(imgUrl);

			final HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:60.0) Gecko/20100101 Firefox/60.0");
			return new Image(connection.getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Image(AnimuDownloaderu.class.getResourceAsStream("/icons/icon.png"));
	}
}
