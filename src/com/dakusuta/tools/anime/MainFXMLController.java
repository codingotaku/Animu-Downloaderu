package com.dakusuta.tools.anime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dakusuta.tools.anime.callback.Crawler;
import com.dakusuta.tools.anime.callback.TableObserver;
import com.dakusuta.tools.anime.callback.TableSelectListener;
import com.dakusuta.tools.anime.custom.CustomLabel;
import com.dakusuta.tools.anime.custom.DownloadDialog;
import com.dakusuta.tools.anime.custom.LoadDialog;
import com.dakusuta.tools.anime.download.DownloadInfo;
import com.dakusuta.tools.anime.download.DownloadManager;
import com.dakusuta.tools.anime.download.Status;
import com.dakusuta.tools.anime.source.Source;
import com.dakusuta.tools.anime.source.Sources;
import com.dakusuta.tools.anime.util.Utils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;
import javafx.util.Pair;

public class MainFXMLController implements TableObserver, Crawler {
	@FXML private VBox root;
	// To search for specific anime
	@FXML private TextField search;

	// To download
	@FXML private Button download;

	// Display episodes of selected anime
	@FXML private Button showEpisodes;

	@FXML ImageView poster;
	// For Anime summary
	@FXML private WebView webView;

	@FXML private ComboBox<String> servers;

	// For displaying downloads
	@FXML private ScrollPane scrollPane;
	@FXML private TableView<DownloadInfo> tableView;
	@FXML private TableColumn<DownloadInfo, String> fileName;
	@FXML private TableColumn<DownloadInfo, Double> size;
	@FXML private TableColumn<DownloadInfo, Double> downloaded;
	@FXML private TableColumn<DownloadInfo, String> progress;
	@FXML private TableColumn<DownloadInfo, Status> status;

	private VBox list;
	private WebEngine webEngine;
	private List<CustomLabel> animeList = new ArrayList<>();
	private List<CustomLabel> episodes = new ArrayList<>();
	DownloadManager manager = DownloadManager.getInstance();
	private Window window;
	private Sources sources;

	@FXML
	protected void showEpisodes(ActionEvent event) {
		list.getChildren().clear();
		if (showEpisodes.getText().equals("Show Episodes")) {
			loadEpisodes();
			download.setDisable(false);
			showEpisodes.setText("Back to Anime list");
		} else {
			webEngine.loadContent("<body bgcolor='#323232'>");
			showEpisodes.setDisable(true);
			search(search.getText());
			poster.setImage(null);
			showEpisodes.setText("Show Episodes");
		}
	}

	private void loadEpisodes() {
		episodes.clear();
		list.getChildren().clear();
		episodes.addAll(sources.loadEpisodes());
		list.getChildren().addAll(episodes);
	}

	@FXML
	protected void download(ActionEvent event) {
		List<CustomLabel> ep = Utils.copyList(episodes);
		Optional<Pair<Integer, Integer>> result = new DownloadDialog(ep).showAndWait();
		result.ifPresent(pair -> download(pair.getKey(), pair.getValue()));
	}

	void loadAnime(Window window) {
		if (this.window == null) this.window = window;
		sources.setSource(Source.values()[servers.getSelectionModel().getSelectedIndex()]);
		list.getChildren().clear();
		LoadDialog.showDialog(window, "Please wait", "Loading anime..");
		LoadDialog.setMessage("Fetching website");
		new Thread(() -> {
			animeList = sources.loadAnime(window);
			Platform.runLater(() -> {
				list.getChildren().addAll(animeList);
				search(search.getText());
			});
		}).start();

	}

	@FXML
	public void initialize() {
		webEngine = webView.getEngine();
		webEngine.loadContent("<html><body bgcolor='#424242'></body></html>");
		list = (VBox) scrollPane.getContent().lookup("#list");

		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		tableView.setRowFactory(new TableSelectListener());
		search.textProperty().addListener((observable, oldValue, newValue) -> {
			search(newValue);
		});
		sources = Sources.getInstance(this);
		manager.setController(this);
		servers.getSelectionModel().select(0);
		servers.valueProperty().addListener((e) -> {
			loadAnime(window);
			poster.setImage(null);
			webEngine.loadContent("<html><body bgcolor='#424242'></body></html>");
		});

	}

	private void search(String text) {
		showEpisodes.setDisable(true);
		download.setDisable(true);
		if (!(animeList.isEmpty())) {
			list.getChildren().clear();
			List<CustomLabel> anime = animeList.stream().filter(label -> label.hasValue(text))
					.collect(Collectors.toList());

			list.getChildren().addAll(anime);
		}
	}

	private void download(int start, int end) {
		int s, e; // To avoid the annoying "requires final" variable error

		if (end > start) { // This way it doesn't matter if user selected episodes in reverse order
			s = end;
			e = start;
		} else {
			s = start;
			e = end;
		}

		new Thread(() -> {// because it will take a hell lot of time to execute my spaghetti code
			List<CustomLabel> toDownload = Utils.copyList(episodes);
			for (int i = s; i >= e; i--) {
				CustomLabel episode = toDownload.get(i);
				manager.addDownloadURL(episode);
			}
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
	public void loaded(String content) {
		Platform.runLater(() -> {
			webEngine.loadContent(content);
			LoadDialog.stopDialog();
			showEpisodes.setDisable(false);
			showEpisodes.setText("Show Episodes");
		});
	}

	@Override
	public void poster(Image image) {
		Platform.runLater(() -> {
			poster.setImage(image);
		});

	}
}