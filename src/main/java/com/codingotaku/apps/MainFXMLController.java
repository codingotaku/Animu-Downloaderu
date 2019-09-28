package com.codingotaku.apps;

import java.io.File;

import com.codingotaku.apis.animecrawler.Anime;
import com.codingotaku.apis.animecrawler.AnimeCrawlerAPI;
import com.codingotaku.apis.animecrawler.AnimeList;
import com.codingotaku.apis.animecrawler.Episode;
import com.codingotaku.apis.animecrawler.EpisodeList;
import com.codingotaku.apis.animecrawler.Result;
import com.codingotaku.apps.callback.Crawler;
import com.codingotaku.apps.callback.TableObserver;
import com.codingotaku.apps.callback.TableSelectListener;
import com.codingotaku.apps.custom.AlertDialog;
import com.codingotaku.apps.custom.DonateDialog;
import com.codingotaku.apps.custom.DownloadDialog;
import com.codingotaku.apps.custom.LoadDialog;
import com.codingotaku.apps.download.DownloadInfo;
import com.codingotaku.apps.download.DownloadManager;
import com.codingotaku.apps.download.Status;
import com.codingotaku.apps.source.AnimeSources;
import com.codingotaku.apps.util.Backup;
import com.codingotaku.apps.util.Constants;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

public class MainFXMLController implements TableObserver, Crawler {
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
	private Button showEpisodes;
	@FXML
	private ImageView poster;
	@FXML
	private TextArea area;

	// For displaying downloads
	@FXML
	private ScrollPane scrollPane;
	@FXML
	private TableView<DownloadInfo> tableView;
	@FXML
	private TableColumn<DownloadInfo, String> fileName;
	@FXML
	private TableColumn<DownloadInfo, Double> size;
	@FXML
	private TableColumn<DownloadInfo, Double> downloaded;
	@FXML
	private TableColumn<DownloadInfo, String> progress;
	@FXML
	private TableColumn<DownloadInfo, Status> status;

	private final ObservableList<Episode> episodes = FXCollections.observableArrayList();
	private final ObservableList<Anime> animes = FXCollections.observableArrayList();
	private final DownloadManager manager = DownloadManager.getInstance();
	private final AnimeCrawlerAPI api = new AnimeCrawlerAPI();
	private final AnimeSources animeProviders = AnimeSources.getInstance();
	private ListView<Episode> episodeList;
	private ListView<Anime> animeList;

	private Window window;
	private Stage stage;
	private VBox vBox;

	private Image defaultImg;

	@FXML
	private ImageView boxImage;

	@FXML
	private void showEpisodes(ActionEvent event) {
		if (window == null)
			window = showEpisodes.getScene().getWindow();
		if (showEpisodes.getText().equals("Show Episodes")) {
			loadEpisodes(window);
			download.setDisable(false);
			showEpisodes.setText("Back to Anime list");
		} else {
			area.clear();
			showEpisodes.setDisable(true);
			loadAnime(window);
			poster.setImage(defaultImg);
			showEpisodes.setText("Show Episodes");
		}
	}

	private void loadEpisodes(Window window) {
		if (window == null)
			window = showEpisodes.getScene().getWindow();
		cb.setIndeterminate(false);
		cb.setSelected(false);
		LoadDialog.showDialog(window, "Loading..", "Loading Episodes.. please wait!");
		api.listAllEpisodes(animeList.getSelectionModel().getSelectedItem(), this::loadedEpisodes);
	}

	@FXML
	private void chooseFolder() {
		if (window == null)
			window = showEpisodes.getScene().getWindow();

		DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Select Download folder");
		File defaultDirectory;

		defaultDirectory = new File(Constants.downloadFolder);
		if (!defaultDirectory.exists()) {// If the path was external HD or it doesn't exist.
			Constants.downloadFolder = System.getProperty("user.home") + File.separator + "Downloads";
			defaultDirectory = new File(Constants.downloadFolder);
		}

		chooser.setInitialDirectory(defaultDirectory);
		File selectedDir = chooser.showDialog(window);
		if (selectedDir != null && selectedDir.exists()) {
			Constants.downloadFolder = selectedDir.getAbsolutePath();
			Backup.saveDownloadFolder();
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
		LoadDialog.showDialog(window, "Loading..", "Loading Anime.. please wait!");
		api.listAllAnime(source, this::loadedAnime);
	}

	@FXML
	private void initialize() {
		area.clear();
		vBox = (VBox) scrollPane.getContent().lookup("#list");

		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		tableView.setRowFactory(new TableSelectListener());
		search.textProperty().addListener((observable, oldValue, newValue) -> search(newValue));
		manager.setController(this);
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
				api.getSynopsys(newV, this::loadedSynopsys);
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
		showEpisodes.setDisable(true);
		download.setDisable(true);
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
		if (!vBox.getChildren().contains(animeList)) {
			vBox.getChildren().setAll(animeList);
		}
	}

	private void downloadSelected() {
		new Thread(() -> {
			episodeList.getSelectionModel().getSelectedItems().forEach(episode -> manager.addDownloadURL(episode));
		}).start();
	}

	@Override
	public void added(DownloadInfo download) {
		tableView.getItems().add(download);
	}

	@Override
	public void updated(DownloadInfo download) {
		tableView.refresh();
	}

	@Override
	public void loading() {
		LoadDialog.showDialog(window, "Please wait", "Fetching anime details");
	}

	@Override
	public void loadedSynopsys(String content, Result result) {
		Platform.runLater(() -> {
			String text = content.replaceAll("([\\w]+ :)", "\n$1").trim();
			area.setText(text);
			area.setEditable(false);
			area.setWrapText(true);
			LoadDialog.stopDialog();
			showEpisodes.setDisable(false);
			showEpisodes.setText("Show Episodes");
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
				vBox.getChildren().setAll(animeList);
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
			Platform.runLater(() -> {
				VBox.setVgrow(episodeList, Priority.ALWAYS);
				vBox.getChildren().setAll(episodeList);
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