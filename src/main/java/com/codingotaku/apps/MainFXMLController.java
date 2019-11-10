package com.codingotaku.apps;

import java.util.Collections;

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
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainFXMLController implements Crawler {

	@FXML private VBox root; // Root

	// Anime download and interactions
	@FXML private TextField search;
	@FXML private CheckBox cb;
	@FXML private ComboBox<String> sources;
	@FXML private Button downloadEp;

	// Anime information
	@FXML
	private ImageView poster;
	@FXML
	private TextArea area;
	
	// For displaying downloads
	@FXML private ScrollPane scrollPane;
	@FXML private ScrollPane epScrollPane;
	@FXML private DownloadController downloadController;
	@FXML private Tab epTab;
	@FXML private Tab dwnTab;
	@FXML private TabPane tabPane;
	private final ObservableList<Episode> episodes = FXCollections.observableArrayList();
	private final ObservableList<Anime> animes = FXCollections.observableArrayList();
	private final DownloadManager manager = DownloadManager.getInstance();
	private final AnimeCrawlerAPI api = new AnimeCrawlerAPI();
	private final AnimeSources animeProviders = AnimeSources.getInstance();
	private ListView<Episode> episodeList;
	private ListView<Anime> animeList;

	private Window window;
	private Stage stage;

	private VBox animeBox;
	private VBox epBox;

	private Image defaultImg;

	@FXML private ImageView boxImage;


	@FXML
	private void download(ActionEvent event) {
		int count = episodeList.getSelectionModel().getSelectedItems().size();
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
				downloadSelected();
				tabPane.getSelectionModel().select(dwnTab);
			}
				
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
		animeBox = (VBox) scrollPane.getContent().lookup("#list");
		epBox = (VBox) epScrollPane.getContent().lookup("#epList");
		defaultImg = new Image(getClass().getResourceAsStream("/icons/panda1.jpg"));
		animeList = new ListView<>();

		sources.getSelectionModel().select(0);
		poster.setImage(defaultImg);

		initListeners();
	}

	private void initListeners() {
		search.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));
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
			if (Boolean.TRUE.equals(flag))
				episodeList.getSelectionModel().selectAll();
			else
				episodeList.getSelectionModel().clearSelection();
		});

		animeList.getSelectionModel().selectedItemProperty().addListener((observable, oldV, newV) -> {
			if (newV != null) {
				if (window == null)
					window = root.getScene().getWindow();
				api.getSynopsys(newV, this::loadedSynopsis);
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

		dialog.setOnShowing(e -> {
			dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
			dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
		});

		dialog.showAndWait();
	}

	@FXML
	private void settings() {
		
	}

	private void search(String text) {
		animeList.getSelectionModel().clearSelection();
		if (!(animes.isEmpty())) {
			if (text.isEmpty()) {
				animeList.setItems(animes);
			} else {
				animeList
						.setItems(animes.filtered(label -> label.getName().toLowerCase().contains(text.toLowerCase())));
			}
		}
		if (!animeBox.getChildren().contains(animeList)) {
			animeBox.getChildren().setAll(animeList);
		}
	}

	private void downloadSelected() {
		new Thread(() -> episodeList.getSelectionModel().getSelectedItems().forEach(manager::addDownloadURL)).start();
	}

	@Override
	public void loadedSynopsis(String content, Result result) {
		Platform.runLater(() -> {
			String text;
			manager.setController(downloadController);
			// Synopsis can be empty 
			if(content.length() > 0) {
				text = content.replaceAll("([\\w]+ :)", "\n$1").trim();
			}else {
				text = "Unable to load synopsys";
				if(result.getStatus() == Result.Status.ERROR) {
					text +=  " : "+result.getError().getMessage();
				}
			}
			
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

				dialog.setOnShowing(e -> {
					dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
					dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
				});
				dialog.showAndWait();
			});

		}
	}

	@FXML
	private void reverse() {
		Collections.reverse(episodeList.getItems());
	}

	@Override
	public void loadedEpisodes(EpisodeList episodesTmp, Result result) {
		LoadDialog.stopDialog();
		if (result.getStatus() == Result.Status.OK) {
			Platform.runLater(() -> {
				episodes.setAll(episodesTmp.episodes());
				episodeList = new ListView<>();
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
				tabPane.getSelectionModel().select(epTab);
			});
		} else {
			Platform.runLater(() -> {
				var dialog = new AlertDialog("Error", "Error loading anime, " + result.getError().getMessage());
				if (stage == null)
					stage = (Stage) root.getScene().getWindow(); // an ugly way of initializing stage
				// Calculate the center position of the parent Stage
				double centerXPosition = stage.getX() + stage.getWidth() / 2d;
				double centerYPosition = stage.getY() + stage.getHeight() / 2d;

				dialog.setOnShowing(e -> {				
					dialog.setX(centerXPosition - dialog.getDialogPane().getWidth() / 2d);
					dialog.setY(centerYPosition - dialog.getDialogPane().getHeight() / 2d);
				});
				dialog.showAndWait();
			});
		}
	}
}