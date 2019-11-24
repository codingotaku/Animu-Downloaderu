package com.codingotaku.apps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.codingotaku.apis.animecrawler.Anime;
import com.fasterxml.jackson.databind.ObjectMapper;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BookmarkController {
	private static Logger logger = Logger.getLogger(BookmarkController.class.getName());
	@FXML private VBox bookmarkList;
	@FXML EpisodeController episodeController;
	private List<Anime> animeList = new ArrayList<>();

	private ObservableList<HBox> bookmarks = FXCollections.observableArrayList();
	private DownloadController downloadController;

	void loadBookmarks() {
		listBookmarks();
		bookmarks.clear();
		for (int i = 0; i < animeList.size(); i++) {
			Anime anime = animeList.get(i);
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Card.fxml"));
			try {
				HBox box = fxmlLoader.load();
				CardController controller = fxmlLoader.getController();
				controller.init(episodeController, downloadController);
				bookmarks.add(box);
				new Thread(() -> controller.load(anime)).start();
			} catch (IOException e) {
				logger.severe(e.getMessage());
			}
		}
		bookmarkList.getChildren().clear();
		Platform.runLater(() -> bookmarkList.getChildren().addAll(bookmarks));
	}

	private void listBookmarks() {
		animeList.clear();
		ObjectMapper mapper = new ObjectMapper();
		File folder = new File("anime");
		if (!folder.exists()) {
			return;
		}

		File[] files = folder.listFiles();
		for (File file : files) {
			try {
				Anime anime = mapper.readValue(file, Anime.class);
				if (!animeList.contains(anime)) {
					animeList.add(anime);
				}
			} catch (IOException e) {
				logger.severe(e.getMessage());
			}
		}
	}

	public void setDownloadController(DownloadController downloadController) {
		this.downloadController = downloadController;
	}

	public List<Anime> getAnimeList() {
		return animeList;
	}
}
