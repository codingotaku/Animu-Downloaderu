package com.codingotaku.apps;

import java.io.IOException;

import com.codingotaku.apis.animecrawler.Anime;
import com.codingotaku.apis.animecrawler.AnimeCrawlerAPI;
import com.codingotaku.apis.animecrawler.AnimeList;
import com.codingotaku.apis.animecrawler.Episode;
import com.codingotaku.apis.animecrawler.EpisodeList;
import com.codingotaku.apis.animecrawler.Result;
import com.codingotaku.apps.callback.Crawler;
import com.codingotaku.apps.custom.AlertDialog;
import com.codingotaku.apps.custom.DonateDialog;
import com.codingotaku.apps.custom.DownloadDialog;
import com.codingotaku.apps.custom.LoadDialog;
import com.codingotaku.apps.download.DownloadManager;
import com.codingotaku.apps.source.AnimeSources;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainFXMLController implements Crawler {
	@FXML
	private VBox root; // Root

	// Anime download and interactions
	@FXML
	private TextField search;
	@FXML
	private CheckBox cb;
	@FXML
	private ComboBox<String> sources;
	@FXML
	private Button download;

	// Anime information
	@FXML
	private Button showDownloads;
	@FXML
	private ImageView poster;
	@FXML
	private TextArea area;

	// For displaying downloads
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private ScrollPane epScrollPane;

	private final ObservableList<Episode> episodes = FXCollections.observableArrayList();
	private final ObservableList<Anime> animes = FXCollections.observableArrayList();
	private final DownloadManager manager = DownloadManager.getInstance();
	private final AnimeCrawlerAPI api = new AnimeCrawlerAPI();
	private final AnimeSources animeProviders = AnimeSources.getInstance();
	private ListView<Episode> episodeList;
	private ListView<Anime> animeList;

	private Window window;
	private Stage stage;
	private Stage downloads;
	private VBox animeBox;
	private VBox epBox;

	private Image defaultImg;

	@FXML
	private ImageView boxImage;

	@FXML
	private void showDownloads(ActionEvent event) {
		if (window == null)
			window = root.getScene().getWindow();
		if (downloads.isShowing()) {
			downloads.requestFocus();
		} else {
			downloads.show();
		}

	}

	@FXML
	private void download(ActionEvent event) {
		int count = episodeList.getSelectionModel().getSelectedItems().size();
		var dialog = new DownloadDialog(count);

		if (stage == null)
			stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
		// Calculate the center position of the parent Stage
		double centerXPosition = stage.getX() + stage.getWidth() / 2d;
		double centerYPosition = stage.getY() + stage.getHeight() / 2d;

		dialog.setOnShowing((e) -> {
			dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
			dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
		});

		var result = dialog.showAndWait();
		result.ifPresent(res -> {
			if (res)
				downloadSelected();
		});
	}

	public void loadAnime(Window window) {
		var source = animeProviders.values().get(sources.getSelectionModel().getSelectedIndex());

		animeList.getSelectionModel().clearSelection();
		LoadDialog.showDialog(window, "Loading..", "Loading Anime list.. please wait!");
		api.listAllAnime(source, this::loadedAnime);
	}

	@FXML
	private void initialize() {
		area.clear();
		animeBox = (VBox) scrollPane.getContent().lookup("#list");
		epBox = (VBox) epScrollPane.getContent().lookup("#epList");

		search.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));

		try {
			downloads = new Stage();
			var loader = new FXMLLoader(getClass().getResource("/fxml/table.fxml"));
			Parent root = loader.load();
			final double WIDTH = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width * 0.75;
			final double HEIGHT = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height * 0.50;

			var scene = new Scene(root, WIDTH, HEIGHT);
			var icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));

			downloads.setMinWidth(WIDTH);
			downloads.setMinHeight(HEIGHT);
			downloads.getIcons().add(icon);
			downloads.centerOnScreen();
			scene.getStylesheets().add(getClass().getResource("/css/table.css").toExternalForm());
			downloads.setTitle("Downloads");
			downloads.setScene(scene);
			var controller = (DownloadController) loader.getController();
			manager.setController(controller);

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		sources.getSelectionModel().select(0);
		defaultImg = new Image(getClass().getResourceAsStream("/icons/panda1.jpg"));
		poster.setImage(defaultImg);
		sources.valueProperty().addListener(e -> {
			if (window == null)
				window = root.getScene().getWindow();
			loadAnime(window);
			poster.setImage(defaultImg);
			area.clear();
		});

		cb.selectedProperty().addListener((paramObservableValue, old, flag) -> {
			if (episodeList == null)
				return;
			if (flag)
				episodeList.getSelectionModel().selectAll();
			else
				episodeList.getSelectionModel().clearSelection();
		});

		animeList = new ListView<>();
		animeList.getSelectionModel().selectedItemProperty().addListener((observable, oldV, newV) -> {
			if (newV != null) {
				if (window == null)
					window = root.getScene().getWindow();
				api.getSynopsys(newV, this::loadedSynopsys);
				api.listAllEpisodes(newV, this::loadedEpisodes);
				api.getPosterUrl(newV, this::loadedPoster);
			}
		});

	}

	@FXML
	private void donate() {
		var dialog = new DonateDialog();
		if (stage == null)
			stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
		// Calculate the center position of the parent Stage
		double centerXPosition = stage.getX() + stage.getWidth() / 2d;
		double centerYPosition = stage.getY() + stage.getHeight() / 2d;

		dialog.setOnShowing((e) -> {
			dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
			dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
		});

		dialog.showAndWait();
	}

	private void search(String text) {
		animeList.getSelectionModel().clearSelection();
		if (!(animes.isEmpty())) {
			if (text.isEmpty()) {
				animeList.setItems(animes);
			} else {
				animeList.setItems(animes.filtered(label -> {
					return label.getName().toLowerCase().contains(text.toLowerCase());
				}));
			}
		}
		if (!animeBox.getChildren().contains(animeList)) {
			animeBox.getChildren().setAll(animeList);
		}
	}

	private void downloadSelected() {
		new Thread(() -> {
			episodeList.getSelectionModel().getSelectedItems().forEach(episode -> manager.addDownloadURL(episode));
		}).start();
	}

	@Override
	public void loadedSynopsys(String content, Result result) {
		Platform.runLater(() -> {
			String text = content.replaceAll("([\\w]+ :)", "\n$1").trim();
			area.setText(text);
			area.setEditable(false);
			area.setWrapText(true);
		});
	}

	@Override
	public void loadedPoster(String url, Result result) {
		Platform.runLater(() -> {
			if (url == null) {
				poster.setImage(defaultImg);
			} else {
				poster.setImage(new Image(url));
			}

		});
	}

	@Override
	public void loadedAnime(AnimeList animesTmp, Result result) {
		LoadDialog.stopDialog();
		if (result.getStatus() == Result.Status.OK) {
			Platform.runLater(() -> {
				animes.setAll(animesTmp.getAnimes());
				search(search.getText());
				VBox.setVgrow(animeList, Priority.ALWAYS);
				animeBox.getChildren().setAll(animeList);
			});
		} else {

			Platform.runLater(() -> {
				var dialog = new AlertDialog("Error", "Error loading anime, " + result.getError().getMessage());
				if (stage == null)
					stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
				// Calculate the center position of the parent Stage
				double centerXPosition = stage.getX() + stage.getWidth() / 2d;
				double centerYPosition = stage.getY() + stage.getHeight() / 2d;

				dialog.setOnShowing((e) -> {
					dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
					dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
				});
				dialog.showAndWait();
			});

		}
	}

	@Override
	public void loadedEpisodes(EpisodeList episodesTmp, Result result) {
		LoadDialog.stopDialog();
		if (result.getStatus() == Result.Status.OK) {
			Platform.runLater(() -> {
				episodes.setAll(episodesTmp.episodes());
				episodeList = new ListView<Episode>();
				episodeList.setItems(episodes);
				episodeList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
				episodeList.setOnMouseClicked(event -> {
					int size = episodeList.getSelectionModel().getSelectedIndices().size();
					if (size == 0) {
						cb.setIndeterminate(false);
						cb.setSelected(false);
					}
					if (size < episodes.size()) {
						cb.setIndeterminate(true);
					} else {
						cb.setIndeterminate(false);
						cb.setSelected(true);
					}
				});
			});

			Platform.runLater(() -> {
				VBox.setVgrow(episodeList, Priority.ALWAYS);
				epBox.getChildren().setAll(episodeList);
			});
		} else {
			Platform.runLater(() -> {
				var dialog = new AlertDialog("Error", "Error loading anime, " + result.getError().getMessage());
				if (stage == null)
					stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
				// Calculate the center position of the parent Stage
				double centerXPosition = stage.getX() + stage.getWidth() / 2d;
				double centerYPosition = stage.getY() + stage.getHeight() / 2d;

				dialog.setOnShowing((e) -> {
					dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
					dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
				});
				dialog.showAndWait();
			});
		}
	}
}