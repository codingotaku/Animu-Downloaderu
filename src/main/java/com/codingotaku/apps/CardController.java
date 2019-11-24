package com.codingotaku.apps;

import com.codingotaku.apis.animecrawler.Anime;
import com.codingotaku.apis.animecrawler.Result.Status;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class CardController {
	@FXML private ImageView poster;
	@FXML private Label title;
	private Anime anime;
	private EpisodeController episodeController;

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

	public void init(EpisodeController episodeController, DownloadController downloadController) {
		this.episodeController = episodeController;
		this.episodeController.setDownloadController(downloadController);
	}
}
