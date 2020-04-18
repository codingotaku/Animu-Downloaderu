package com.codingotaku.apps;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codingotaku.apis.animecrawler.Anime;
import com.codingotaku.apis.animecrawler.Result.Status;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardController {
	private static Logger logger = Logger.getLogger(CardController.class.getName());
	@FXML private ImageView poster;
	@FXML private Label title;
	private Anime anime;
	private EpisodeController episodeController;
	private BookmarkController bookmarkController;

	void load(Anime anime) {
		this.anime = anime;
		Platform.runLater(() -> title.setText(anime.getName()));
		anime.getPosterUrl((url, res) -> {
			if (res.getStatus() == Status.OK) {
				new Thread(() -> poster.setImage(new Image(url))).start();
			}
		});
	}

	@FXML private void showEpisodes() {
		if (episodeController == null) {
			return;
		}

		new Thread(() -> anime.listAllEpisodes((list, res) -> {
			if (res.getStatus() == Status.OK) {
				episodeController.loadEpisodes(list);
			}
		})).start();
	}
	
	@FXML private void removeBookmark() {
		File folder = new File("anime");
		if (!folder.exists()) {
			return;
		}
		String name = anime.getName().replaceAll("[^a-zA-Z0-9 \\.\\-]", "_");
		File[] files = folder.listFiles();

		logger.log(Level.FINE, name);
		for (File file : files) {
			logger.log(Level.FINE, file.getName());
			if(file.getName().equals(name+".json")) {
				if(file.delete()) {
					bookmarkController.removeBookmarkIfExists(anime);
					logger.log(Level.FINE, "Removed Anime "+ anime.getName());
				} else {
					logger.log(Level.SEVERE, "Unable to remove Anime "+ anime.getName());
				}
				break;
			}
		}
	}

	public void init(EpisodeController episodeController, DownloadController downloadController, BookmarkController bookmarkController) {
		this.episodeController = episodeController;
		this.episodeController.setDownloadController(downloadController);
		this.bookmarkController = bookmarkController;
	}
}
