/**
 * 
 */
package com.dakusuta.tools.anime.downloader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dakusuta.download.DownloadManager;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * @author Rahul Sivananda
 *
 */
public class Main extends javafx.application.Application {

	private VBox listPane = new VBox();
	private VBox downloadList = new VBox();
	private HBox navigation = new HBox();
	private VBox synopsys = new VBox();

	private MediaView playPane = new MediaView();

	private Button showEpisodes = new Button("Show Episodes");
	private Button download = new Button("Download");
	private Button next = new Button("Next");
	private Button previous = new Button("Previous");

	private WebView area = new WebView();
	private WebEngine webEngine = area.getEngine();
	private List<CustomLabel> animeList = new ArrayList<>();

	private Document selectedDoc;

	private int current = -1, last = -1;
	private String epUrl = null;
	private List<CustomLabel> episodes;

	@Override
	public void start(Stage primaryStage) throws Exception {
		VBox root = new VBox(10);
		HBox content = new HBox();
		TextField search = new TextField();
		Scene scene = new Scene(root, 300, 250);
		ScrollPane listScrollPane = new ScrollPane(listPane);

		search.setPromptText("Search");
		search.setMinWidth(300);
		search.setMaxWidth(300);

		listPane.setPadding(new Insets(10, 50, 50, 50));
		navigation.setPadding(new Insets(10, 50, 10, 50));

		listPane.setSpacing(10);
		navigation.setSpacing(10);

		webEngine.documentProperty().addListener(new WebDocumentListener(webEngine));
		webEngine.loadContent("<body style=\"background-color:#424242;\"");

		primaryStage.setWidth(1400);
		primaryStage.setHeight(750);
		primaryStage.setResizable(false);

		content.setMaxWidth(1400);
		listScrollPane.setMaxWidth(700);
		listScrollPane.setMinWidth(700);
		area.setMaxHeight(250);
		synopsys.setMaxWidth(600);
		synopsys.setMinWidth(600);
		synopsys.setMaxHeight(500);
		synopsys.setMinHeight(500);
		playPane.minWidth(200);
		playPane.minHeight(200);
		showEpisodes.setDisable(true);

		navigation.getChildren().addAll(search, download);
		synopsys.getChildren().addAll(area, showEpisodes, downloadList);
		content.getChildren().addAll(listScrollPane, synopsys);
		root.getChildren().addAll(navigation, content);

		Platform.runLater(() -> {
			listPane.getChildren().clear();
			listPane.getChildren().addAll(getAllAnime());
		});

		showEpisodes.setOnMouseClicked(event -> {
			listPane.getChildren().clear();
			if (showEpisodes.getText().equals("Show Episodes")) {
				episodes = loadEpisodes();
				listPane.getChildren().addAll(episodes);
				showEpisodes.setText("Back to Anime list");
			} else {
				webEngine.loadContent("<body style=\"background-color:#424242;\"");
				showEpisodes.setDisable(true);
				search(search.getText());
				showEpisodes.setText("Show Episodes");
			}
		});

		search.setOnKeyReleased(event -> search(search.getText()));
		scene.getStylesheets().add(Main.class.getResource("style.css").toExternalForm());

		previous.setOnMousePressed(e -> {
			if (current > 1) {
				current--;
				episodes = loadEpisodes(current);
				listPane.getChildren().addAll(episodes);
			}
		});
		next.setOnMouseClicked(e -> {
			if (current < last) {
				current++;
				episodes = loadEpisodes(current);
				listPane.getChildren().addAll(episodes);
			}
		});
		download.setOnMouseClicked(ev -> {
			List<CustomLabel> ep = Utils.copyList(episodes);
			Optional<Pair<Integer, Integer>> result = new DownloadDialog(ep).showAndWait();
			result.ifPresent(pair -> {
				download(pair.getKey(), pair.getValue());
			});
		});

		primaryStage.setTitle("Animu Downloaderu!");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	private void search(String text) {
		showEpisodes.setDisable(true);
		navigation.getChildren().removeAll(previous, next);
		if (!(animeList == null || animeList.isEmpty())) {
			listPane.getChildren().clear();
			listPane.getChildren().addAll(animeList.stream().filter(label -> label.hasValue(text)).collect(Collectors.toList()));
		}
	}

	private List<CustomLabel> getAllAnime() {
		if (!(animeList == null || animeList.isEmpty())) return animeList;
		animeList = new ArrayList<>();

		try {
			Document doc = Jsoup.parse(new URL("http://www.gogoanime.to/watch-anime-list"), 60000);
			Elements elements = doc.select("li.cat-item > a");

			elements.forEach(element -> {
				CustomLabel label = new CustomLabel(element);
				label.setOnMouseClicked(this::getSynopsys);
				animeList.add(label);
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		return animeList;
	}

	CustomLabel prLbl = null;

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
			String content = "<font color=\"red\"><b><u><center>" + label.getText() + "</center></u></b><br><br>";
			content += description.text().replace("Plot Summary:", "<b>Plot Summary:</b></font><font color=\"white\">");
			webEngine.loadContent(content + "</font>");
			showEpisodes.setText("Show Episodes");
			showEpisodes.setDisable(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getURL(String url) {
		try {
			Document doc = Jsoup.parse(new URL(url), 60000);
			Elements iframes = doc.select("iframe[src^=http://]");

			for (Element iframe : iframes) {
				Document source;
				source = Jsoup.parse(new URL(iframe.attr("src")), 60000);
				String lines[] = source.data().split("\\r?\\n");
				for (String line : lines) {
					if (line.contains("file: \"http://gateway")) { return line.replace("file: \"", "").replace("\",", "").trim(); }
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	DownloadManager manager = new DownloadManager();

	private void download(int start, int end) {
		new Thread(() -> {
			manager.setVisible(true);
			List<CustomLabel> toDownload= Utils.copyList(episodes);
			for (int i = start; i >= end; i--) {
				CustomLabel episode = toDownload.get(i);

				// Download download =
				manager.addUrl(getURL(episode.getValue()));
				// try {
				// while (download.getStatus() == Download.DOWNLOADING) {
				// Thread.sleep(1000);
				// }
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }

			}
		}).start();
	}

	private List<CustomLabel> loadEpisodes() {
		List<CustomLabel> episodeList = new ArrayList<>();
		Elements div = selectedDoc.select("div.postlist");
		Elements episodes = div.select("a");

		episodes.forEach(element -> episodeList.add(new CustomLabel(element)));
		current = last = 1;

		Elements navBar = selectedDoc.select("a.last");
		if (navBar.size() == 0) return episodeList;

		String tmp = navBar.first().attr("href");
		String url = tmp.substring(0, tmp.lastIndexOf('/'));
		tmp = tmp.substring(tmp.lastIndexOf('/') + 1);

		last = Integer.parseInt(tmp);
		epUrl = url + "/";
		if (last > 1) navigation.getChildren().addAll(previous, next);
		return episodeList;
	}

	private List<CustomLabel> loadEpisodes(int page) {
		listPane.getChildren().clear();
		List<CustomLabel> episodeList = new ArrayList<>();
		try {
			Document doc = Jsoup.parse(new URL(epUrl + page), 60000);
			Elements div = doc.select("div.postlist");
			Elements episodes = div.select("a");
			episodes.forEach(element -> episodeList.add(new CustomLabel(element)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return episodeList;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
