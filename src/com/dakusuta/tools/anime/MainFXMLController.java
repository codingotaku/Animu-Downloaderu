package com.dakusuta.tools.anime;

import java.io.IOException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dakusuta.tools.anime.callback.DownloadObserver;
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
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Pair;

public class MainFXMLController implements DownloadObserver {
	// To search for specific anime
	@FXML private TextField search;

	// To download
	@FXML private Button download;

	// Display episodes of selected anime
	@FXML private Button showEpisodes;

	// For navigating through anime episodes
	@FXML private Button previous;
	@FXML private Button next;

	// For Anime summary
	@FXML private WebView webView;

	// For displaying downloads
	@FXML private ScrollPane scrollPane;
	@FXML private TableView<DownloadInfo> tableView;
	@FXML private TableColumn<DownloadInfo, String> fileName;
	@FXML private TableColumn<DownloadInfo, String> url;
	@FXML private TableColumn<DownloadInfo, Double> size;
	@FXML private TableColumn<DownloadInfo, Double> downloaded;
	@FXML private TableColumn<DownloadInfo, Double> progress;
	@FXML private TableColumn<DownloadInfo, Status> status;

	private VBox list;
	private CustomLabel prLbl;
	private WebEngine webEngine;
	private Document selectedDoc = null;
	private ArrayList<CustomLabel> animeList = new ArrayList<>();
	private List<CustomLabel> episodes = new ArrayList<>();
	private int current;
	private int last;
	private String epUrl;

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

		current = last = 1;

		Elements navBar = selectedDoc.select("a.last");
		String tmp;
		if (navBar.size() == 0) {
			navBar = selectedDoc.select("a.page");
			if (navBar.size() == 0) {
				Elements div = selectedDoc.select("div.postlist");
				Elements elements = div.select("a");
				elements.forEach(element -> episodes.add(new CustomLabel(element)));
				list.getChildren().addAll(episodes);
				return;
			}
			last = getPageNum(navBar.last().attr("href"));
			tmp = navBar.last().attr("href");
		} else tmp = navBar.first().attr("href");

		String url = tmp.substring(0, tmp.lastIndexOf('/'));
		last = getPageNum(tmp);
		epUrl = url + "/";
		current = last;

		next.setDisable(false);
		loadEpisodes(last);
	}

	private int getPageNum(String path) {
		path = path.substring(path.lastIndexOf('/') + 1);
		return Integer.parseInt(path);
	}

	private void loadEpisodes(int page) {
		list.getChildren().clear();
		episodes.clear();
		try {
			Document doc = Jsoup.parse(new URL(epUrl + page), 60000);
			Elements div = doc.select("div.postlist");
			Elements elements = div.select("a");
			elements.forEach(element -> episodes.add(new CustomLabel(element)));
			list.getChildren().addAll(episodes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void download(ActionEvent event) {
		List<CustomLabel> ep = Utils.copyList(episodes);
		Optional<Pair<Integer, Integer>> result = new DownloadDialog(ep).showAndWait();
		result.ifPresent(pair -> {
			download(pair.getKey(), pair.getValue());
		});
	}

	@FXML
	protected void previous(ActionEvent event) {
		if (current < last) {
			current++;
			if (current == last) previous.setDisable(true);
			loadEpisodes(current);
			next.setDisable(false);
		}
	}

	@FXML
	protected void next(ActionEvent event) {
		if (current > 1) {
			current--;
			loadEpisodes(current);
			if (current == 1) next.setDisable(true);
			previous.setDisable(false);
		}
	}

	private void loadAnime() {
		if (animeList.isEmpty()) {
			new Thread(() -> {
				LoadDialog.setMessage("Fetching website");
				try {
					Document doc = Jsoup.parse(new URL("http://www.gogoanime.to/watch-anime-list"), 60000);
					LoadDialog.setMessage("Finding anime collection");
					Elements elements = doc.select("li.cat-item > a");
					LoadDialog.setMessage("Loading List");
					elements.forEach(element -> {
						CustomLabel label = new CustomLabel(element);
						label.setOnMouseClicked(this::getSynopsys);
						Platform.runLater(() -> {
							LoadDialog.setMessage("Found " + label.getText());
							animeList.add(label);
						});
					});
					LoadDialog.setMessage("Finished loading");
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
			Document doc = Jsoup.parse(new URL(label.getValue()), 60000);
			selectedDoc = doc;
			Element description = doc.select("div.catdescription").first();
			String content = "<body bgcolor='#323232'><font color=\"red\"><b><u><center>" + label.getText()
					+ "</center></u></b><br><br>";
			content += description.text().replace("Plot Summary:", "<b>Plot Summary:</b></font><font color=\"white\">");
			webEngine.loadContent(content + "</font></body>");
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
		url.setCellValueFactory(new PropertyValueFactory<DownloadInfo, String>("url"));
		size.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("size"));
		downloaded.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("downloaded"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Double>("progress"));
		progress.setCellFactory(ProgressBarTableCell.<DownloadInfo>forTableColumn());
		status.setCellValueFactory(new PropertyValueFactory<DownloadInfo, Status>("status"));

		tableView.setRowFactory(new TableSelectListener());
		loadAnime();

		search.textProperty().addListener((observable, oldValue, newValue) -> {
			search(newValue);
		});
	}

	private void search(String text) {
		showEpisodes.setDisable(true);
		download.setDisable(true);
		previous.setDisable(true);
		next.setDisable(true);
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

		new Thread(() -> { // because it will take a hell lot of time to execute my spaghetti code
			List<CustomLabel> toDownload = Utils.copyList(episodes);
			for (int i = s; i >= e; i--) {
				CustomLabel episode = toDownload.get(i);
				manager.addDownloadURL(episode.getValue());
			}
		}).start();
	}

	@Override
	public void paused(DownloadInfo download) {
		tableView.refresh();

	}

	@Override
	public void finished(DownloadInfo download) {
		tableView.refresh();

	}

	@Override
	public void error(DownloadInfo download) {
		tableView.refresh();

	}

	@Override
	public void resumed(DownloadInfo download) {
		tableView.refresh();

	}

	@Override
	public void cancelled(DownloadInfo download) {
		tableView.refresh();

	}

	@Override
	public void pending(DownloadInfo download) {
		tableView.getItems().add(download);

	}

	@Override
	public void downloading(DownloadInfo download, double progress) {
		tableView.refresh();
	}

}