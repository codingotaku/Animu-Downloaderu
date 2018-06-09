package com.dakusuta.tools.anime;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dakusuta.tools.anime.callback.TableObserver;
import com.dakusuta.tools.anime.callback.TableSelectListener;
import com.dakusuta.tools.anime.custom.CustomLabel;
import com.dakusuta.tools.anime.custom.DownloadDialog;
import com.dakusuta.tools.anime.custom.LoadDialog;
import com.dakusuta.tools.anime.download.DownloadInfo;
import com.dakusuta.tools.anime.download.DownloadManager;
import com.dakusuta.tools.anime.download.Status;
import com.dakusuta.tools.anime.util.Utils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;

public class MainFXMLController implements TableObserver {
	// To search for specific anime
	@FXML private TextField search;

	// To download
	@FXML private Button download;

	// Display episodes of selected anime
	@FXML private Button showEpisodes;

	@FXML ImageView poster;
	// For Anime summary
	@FXML private WebView webView;

	// For displaying downloads
	@FXML private ScrollPane scrollPane;
	@FXML private TableView<DownloadInfo> tableView;
	@FXML private TableColumn<DownloadInfo, String> fileName;
	@FXML private TableColumn<DownloadInfo, Double> size;
	@FXML private TableColumn<DownloadInfo, Double> downloaded;
	@FXML private TableColumn<DownloadInfo, String> progress;
	@FXML private TableColumn<DownloadInfo, Status> status;

	private VBox list;
	private CustomLabel prLbl;
	private WebEngine webEngine;
	private Document selectedDoc = null;
	private ArrayList<CustomLabel> animeList = new ArrayList<>();
	private List<CustomLabel> episodes = new ArrayList<>();
	DownloadManager manager = DownloadManager.getInstance();

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
			showEpisodes.setText("Show Episodes");
		}
	}

	private void loadEpisodes() {
		episodes.clear();
		list.getChildren().clear();

		String title = selectedDoc.select("h1.blue-main-title").get(0).text();
		Elements nav = selectedDoc.select("div.left-left > ul.anime-list");
		Elements elements = nav.select("li >a");

		elements.forEach(element -> episodes.add(new CustomLabel(title, element)));

		Collections.reverse(episodes);
		list.getChildren().addAll(episodes);
	}

	@FXML
	protected void download(ActionEvent event) {
		List<CustomLabel> ep = Utils.copyList(episodes);
		Optional<Pair<Integer, Integer>> result = new DownloadDialog(ep).showAndWait();
		result.ifPresent(pair -> download(pair.getKey(), pair.getValue()));
	}

	private void loadAnime() {
		if (animeList.isEmpty()) {
			new Thread(() -> {
				LoadDialog.setMessage("Fetching website");
				String animeListPath = "http://www.anime1.com/content/list/";
				try {
					Document doc = Jsoup.parse(new URL(animeListPath), 60000);
					LoadDialog.setMessage("Finding anime collection");
					Elements elements = doc.select("div.alph-list-box > h4 > a[name]");
					LoadDialog.setMessage("Loading List");
					elements.forEach(element -> {
						List<Element> nodes = element.parent().nextElementSibling().children();
						nodes.forEach(node -> {
							CustomLabel label = new CustomLabel(node.child(0));
							Platform.runLater(() -> {
								LoadDialog.setMessage("Found " + label.getText());
								animeList.add(label);
							});

							label.setOnMouseClicked(this::getSynopsys);
						});
					});
				} catch (UnknownHostException e) {
					Platform.runLater(() -> {
						LoadDialog.setMessage("Unable to connect please try again later");
					});
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					LoadDialog.stopDialog();
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}

				LoadDialog.stopDialog();
				Platform.runLater(() -> {
					list.getChildren().addAll(animeList);
				});
			}).start();
		} else {
			list.getChildren().addAll(animeList);
		}
	}

	private void getSynopsys(MouseEvent ev) {
		if (!ev.getButton().equals(MouseButton.PRIMARY)) return;
		if (prLbl != null) prLbl.setId("");

		CustomLabel label = (CustomLabel) ev.getSource();
		label.setId("selected");
		prLbl = label;
		try {
			Document doc = Jsoup.parse(new URL(label.getUrl()), 60000);
			selectedDoc = doc;
			Element description = doc.select("div.detail-left").first();
			String html = description.html();


			Platform.runLater(() -> {
					poster.setImage(getPoster(doc.select("div.detail-cover >a >img").attr("src")));
			});
			// A brute force way to replace all unnecessary links and scripts

			html = html.replaceAll("<a[^>]*>([^<]+)</a>", "$1")
					.replaceAll("</span>", "</span><br><br>")
					.replaceAll("<div[^>]*onclick[^>]*>[^<]+</div>", "");

			webEngine.loadContent("<body bgcolor=\"#424242\"><font color=\"white\">" + html + "</font></body>");
			showEpisodes.setText("Show Episodes");
			showEpisodes.setDisable(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void initialize() {
		webEngine = webView.getEngine();
		webEngine.loadContent("<html><body bgcolor='#424242'></body></html>");
		list = (VBox) scrollPane.getContent().lookup("#list");
		manager.setController(this);

		fileName.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("fileName"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("progress"));
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		tableView.setRowFactory(new TableSelectListener());
		loadAnime();

		search.textProperty().addListener((observable, oldValue, newValue) -> {
			search(newValue);
		});
	}

	private Image getPoster(String imgUrl) {
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
		return null;
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
}