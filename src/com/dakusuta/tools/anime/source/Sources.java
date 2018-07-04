package com.dakusuta.tools.anime.source;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.dakusuta.tools.anime.callback.Crawler;
import com.dakusuta.tools.anime.custom.CustomLabel;
import com.dakusuta.tools.anime.custom.LoadDialog;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public class Sources {
	private static Sources instance = null;
	private Source source = Source.SERVER_1;
	private Crawler webCrawler;

	private Map<Source, IServer> serverMap = new HashMap<>();

	public void setSource(Source source) {
		this.source = source;
	}

	private Sources(Crawler crawler) {
		webCrawler = crawler;
		serverMap.put(Source.SERVER_1, new Anime1());
		serverMap.put(Source.SERVER_2, new AnimeXD());
	}

	public static Sources getInstance(Crawler crawler) {
		if (instance == null) instance = new Sources(crawler);
		return instance;
	}

	private class Anime1 extends IServer {
		private Anime1() {
			path = "http://www.anime1.com/content/list/";
		}

		private final Background focusBackground = new Background(
				new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
		private final Background unfocusBackground = null;

		@Override
		public List<CustomLabel> loadAnime(Document doc) {
			if (!animeList.isEmpty()) return animeList;

			Elements elements = doc.select("div.alph-list-box > h4 > a[name]");
			elements.forEach(element -> {
				List<Element> nodes = element.parent().nextElementSibling().children();
				nodes.forEach(node -> {
					CustomLabel label = new CustomLabel(node.child(0));

					label.backgroundProperty().bind(Bindings
							.when(label.focusedProperty())
							.then(focusBackground)
							.otherwise(unfocusBackground));
					Platform.runLater(() -> {
						LoadDialog.setMessage("Found " + label.getText());
						animeList.add(label);
					});

					label.setOnMouseClicked(e -> {
						label.requestFocus();
						this.getSynopsys(e);
					});
				});
			});

			return animeList;
		}

		@Override
		public void getSynopsys(MouseEvent ev) {
			if (!ev.getButton().equals(MouseButton.PRIMARY)) return;
			webCrawler.loading();
			new Thread(() -> {
				CustomLabel label = (CustomLabel) ev.getSource();
				try {
					Document doc = Jsoup.parse(new URL(label.getUrl()), 60000);
					selectedDoc = doc;
					Element description = doc.select("div.detail-left").first();
					String html = description.html();

					// A brute force way to replace all unnecessary links and scripts

					html = html.replaceAll("<a[^>]*>([^<]+)</a>", "$1")
							.replaceAll("</span>", "</span><br><br>")
							.replaceAll("<div[^>]*onclick[^>]*>[^<]+</div>", "");

					webCrawler.poster(getPoster(doc.select("div.detail-cover >a >img").attr("src")));
					webCrawler.loaded("<body bgcolor=\"#424242\"><font color=\"white\">" + html + "</font></body>");
				} catch (IOException e) {
					e.printStackTrace();
					webCrawler.loaded("<body bgcolor=\"#424242\"><font color=\"white\"> Error :"
							+ e.getMessage() + "</font></body>");
				}
			}).start();

		}

		@Override
		public List<CustomLabel> loadEpisodes() {
			episodes.clear();
			String title = selectedDoc.select("h1.blue-main-title").get(0).text();
			Elements nav = selectedDoc.select("div.left-left > ul.anime-list");
			Elements elements = nav.select("li >a");

			elements.forEach(element -> episodes.add(new CustomLabel(title, element)));

			Collections.reverse(episodes);
			return episodes;
		}

		@Override
		Image getPoster(String imgUrl) {
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

	}

	private class AnimeXD extends IServer {
		//
		private AnimeXD() {
			path = "http://www.animexd.me/home/anime-list";
		}

		private final Background focusBackground = new Background(
				new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
		private final Background unfocusBackground = null;

		@Override
		public List<CustomLabel> loadAnime(Document doc) {
			if (!animeList.isEmpty()) return animeList;
			Elements elements = doc.select("div.container-left > div.container-item > div.ci-title");
			elements.forEach(element -> {
				List<Element> nodes = element.nextElementSibling().children();
				nodes.forEach(node -> {
					CustomLabel label = new CustomLabel(node.child(0));

					label.backgroundProperty().bind(Bindings
							.when(label.focusedProperty())
							.then(focusBackground)
							.otherwise(unfocusBackground));
					Platform.runLater(() -> {
						LoadDialog.setMessage("Found " + label.getText());
						animeList.add(label);
					});

					label.setOnMouseClicked(e -> {
						label.requestFocus();
						this.getSynopsys(e);
					});
				});
			});
			return animeList;
		}

		@Override
		public void getSynopsys(MouseEvent ev) {
			if (!ev.getButton().equals(MouseButton.PRIMARY)) return;
			webCrawler.loading();
			new Thread(() -> {
				CustomLabel label = (CustomLabel) ev.getSource();
				try {
					Document doc = Jsoup.parse(new URL(label.getUrl()), 60000);
					selectedDoc = doc;
					Element name = doc.select("div.anime-title").first();
					Element description = doc.select("p.anime-details").first();
					String html = "<h3>" + name.html() + "</h3>" + description.html();

					webCrawler.poster(getPoster(doc.select("div.animeDetail-image > img").attr("src")));
					webCrawler.loaded("<body bgcolor=\"#424242\"><font color=\"white\">" + html + "</font></body>");
				} catch (IOException e) {
					e.printStackTrace();
					webCrawler.loaded("<body bgcolor=\"#424242\"><font color=\"white\"> Error :"
							+ e.getMessage() + "</font></body>");
				}
			}).start();

		}

		@Override
		public List<CustomLabel> loadEpisodes() {
			episodes.clear();
			String title = selectedDoc.select("div.anime-title").get(0).text();
			Elements elements = selectedDoc.select("div.ci-contents > div.tnContent:nth-child(2) > ul > li > a");
			elements.forEach(element -> episodes.add(new CustomLabel(title, element)));
			return episodes;
		}

		@Override
		Image getPoster(String imgUrl) {
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
	}

	public List<CustomLabel> loadAnime(Window window) {
		try {
			IServer server = serverMap.get(source);
			Document doc = Jsoup.parse(new URL(server.getPath()), 60000);

			LoadDialog.setMessage("Finding anime collection");
			return serverMap.get(source).loadAnime(doc);
		} catch (IOException e) {
			Platform.runLater(() -> {
				LoadDialog.setMessage("Unable to connect please try again later");
			});
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

			e.printStackTrace();
			return null;
		} finally {
			LoadDialog.stopDialog();
		}
	}

	public List<CustomLabel> loadEpisodes() {
		return serverMap.get(source).loadEpisodes();
	}
}
