package com.codingotaku.apps;

import java.util.Collections;

import com.codingotaku.apis.animecrawler.Episode;
import com.codingotaku.apis.animecrawler.EpisodeList;
import com.codingotaku.apps.callback.NotificationListener;
import com.codingotaku.apps.custom.DownloadDialog;
import com.codingotaku.apps.custom.NotificationController;
import com.codingotaku.apps.download.DownloadManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EpisodeController {
	@FXML private Button downloadEp;
	@FXML private CheckBox cb;
	@FXML private ScrollPane epScrollPane;
	@FXML VBox root;

	private ListView<Episode> episodeList;
	private Stage stage;
	private VBox episodeBox;

	private final ObservableList<Episode> episodes = FXCollections.observableArrayList();
	private final DownloadManager manager = DownloadManager.getInstance();
	private NotificationListener notificationListener;

	@FXML private void initialize() {
		episodeBox = (VBox) epScrollPane.getContent().lookup("#epList");
		cb.selectedProperty().addListener((paramObservableValue, old, flag) -> {
			if (episodeList == null)
				return;
			if (Boolean.TRUE.equals(flag))
				episodeList.getSelectionModel().selectAll();
			else
				episodeList.getSelectionModel().clearSelection();
		});
	}

	@FXML private void download(ActionEvent event) {
		int count = 0;

		if (episodeList != null) {
			count = episodeList.getSelectionModel().getSelectedItems().size();
		}

		var dialog = new DownloadDialog(count);

		if (stage == null)
			stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
		// Calculate the center position of the parent Stage
		double centerXPosition = stage.getX() + stage.getWidth() / 2d;
		double centerYPosition = stage.getY() + stage.getHeight() / 2d;

		dialog.setOnShowing(e -> {
			dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
			dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
		});

		var result = dialog.showAndWait();
		result.ifPresent(res -> {
			if (Boolean.TRUE.equals(res)) {
				ObservableList<Episode> selectedItems = episodeList.getSelectionModel().getSelectedItems();
				new Thread(() -> selectedItems.forEach(manager::addDownloadURL)).start();
				notificationListener.created(NotificationController.Type.INFO, String.format("Downloading %d episode(s) of %s.", selectedItems.size(), selectedItems.get(0).getanimeName()));
			}
		});
	}

	@FXML private void reverse() {
		if (episodeList != null) {
			ObservableList<Integer> list = episodeList.getSelectionModel().getSelectedIndices();
			Integer[] selected = new Integer[list.size()];
			list.toArray(selected);

			int size = episodeList.getItems().size() - 1;
			episodeList.getSelectionModel().clearSelection();
			Collections.reverse(episodeList.getItems());

			for (int sel : selected) { // Reverse selection
				episodeList.getSelectionModel().select(size - sel);
			}
		}
	}

	void setCheckBoxStatus(int selSize, int epSize) {
		if (selSize == 0) {
			cb.setIndeterminate(false);
			cb.setSelected(false);
		}

		if (selSize < epSize) {
			cb.setIndeterminate(true);
		} else {
			cb.setIndeterminate(false);
			cb.setSelected(true);
		}
	}

	void loadEpisodes(EpisodeList episodesTmp) {
		Platform.runLater(() -> {
			episodes.setAll(episodesTmp.episodes());
			episodeList = new ListView<>();
			episodeList.setItems(episodes);
			episodeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
			episodeList.setOnMouseClicked(event -> {
				int selSize = episodeList.getSelectionModel().getSelectedIndices().size();
				this.setCheckBoxStatus(selSize, episodes.size());
			});
		});

		Platform.runLater(() -> {
			VBox.setVgrow(episodeList, Priority.ALWAYS);
			episodeBox.getChildren().setAll(episodeList);
		});

	}

	void setDownloadController(DownloadController downloadController) {
		manager.setController(downloadController);
	}
	
	void setNotificationListener(NotificationListener notificationListener) {
		this.notificationListener = notificationListener;
	}
}
