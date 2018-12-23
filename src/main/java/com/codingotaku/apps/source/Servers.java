package com.codingotaku.apps.source;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.codingotaku.apps.callback.Crawler;
import com.codingotaku.apps.custom.AlertDialog;
import com.codingotaku.apps.custom.AnimeLabel;
import com.codingotaku.apps.custom.EpisodeLabel;
import com.codingotaku.apps.custom.LoadDialog;

import javafx.application.Platform;
import javafx.stage.Window;

public class Servers {
	private static Servers instance = null;
	private Source source = Source.SERVER_1;
	private Crawler webCrawler;

	private Map<Source, IServer> serverMap = new HashMap<>();

	public void setSource(Source source) {
		this.source = source;
	}

	private Servers(Crawler crawler) {
		webCrawler = crawler;
		serverMap.put(Source.SERVER_1, new Server1());
		serverMap.put(Source.SERVER_2, new Server2());
	}

	public static Servers getInstance(Crawler crawler) {
		if (instance == null) instance = new Servers(crawler);
		return instance;
	}

	private class Server1 extends IServer {
		private Server1() {
			path = "http://www.anime1.com/content/list/";
		}

		@Override
		public List<AnimeLabel> loadAnime(Document doc) {
			if (!animeList.isEmpty()) return animeList;

			Elements elements = doc.select("div.alph-list-box > h4 > a[name]");
			elements.forEach(element -> {
				List<Element> nodes = element.parent().nextElementSibling().children();
				nodes.forEach(node -> {
					AnimeLabel label = new AnimeLabel(node.child(0));
					Platform.runLater(() -> {
						LoadDialog.setMessage("Found " + label.getText());
						animeList.add(label);
					});
				});
			});

			return animeList;
		}

		@Override
		public void getSynopsys(AnimeLabel label) {
			webCrawler.loading();
			new Thread(() -> {
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
		public List<EpisodeLabel> loadEpisodes() {
			episodes.clear();
			String title = selectedDoc.select("h1.blue-main-title").get(0).text();
			Elements nav = selectedDoc.select("div.left-left > ul.anime-list");
			Elements elements = nav.select("li >a");

			elements.forEach(element -> episodes.add(new EpisodeLabel(title, element)));

			Collections.reverse(episodes);
			return episodes;
		}
	}

	private class Server2 extends IServer {
		private Server2() {
			path = "http://www.animexd.me/home/anime-list";
		}

		@Override
		public List<AnimeLabel> loadAnime(Document doc) {
			if (!animeList.isEmpty()) return animeList;
			Elements elements = doc.select("div.container-left > div.container-item > div.ci-title");
			elements.forEach(element -> {
				List<Element> nodes = element.nextElementSibling().children();
				nodes.forEach(node -> {
					AnimeLabel label = new AnimeLabel(node.child(0));
					Platform.runLater(() -> {
						LoadDialog.setMessage("Found " + label.getText());
						animeList.add(label);
					});
				});
			});
			return animeList;
		}

		@Override
		public void getSynopsys(AnimeLabel label) {
			webCrawler.loading();
			new Thread(() -> {
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
		public List<EpisodeLabel> loadEpisodes() {
			episodes.clear();
			String title = selectedDoc.select("div.anime-title").get(0).text();
			Elements elements = selectedDoc.select("div.ci-contents > div.tnContent:nth-child(2) > ul > li > a");
			if (elements.isEmpty()) { // for new animes
				elements = selectedDoc.select("div.ci-contents > div.tnContent:nth-child(1) > ul > li > a");
			}
			elements.forEach(element -> episodes.add(new EpisodeLabel(title, element)));
			return episodes;
		}
	}
	public List<AnimeLabel> loadAnime(Window window) {
		try {
			if (!serverMap.get(source).animeList.isEmpty()) return serverMap.get(source).animeList;
			Platform.runLater(() -> {
				LoadDialog.showDialog(window, "Please wait", "Fetching website...");
			});
			IServer server = serverMap.get(source);
			Document doc = Jsoup.parse(new URL(server.getPath()), 60000);
			LoadDialog.setMessage("Finding anime collection");
			return serverMap.get(source).loadAnime(doc);
		} catch (Exception e) {
			Platform.runLater(() -> {
				LoadDialog.stopDialog();
				AlertDialog dialog = new AlertDialog("Connection Error", "Unable to connect! Please try again later.");
				dialog.showAndWait();
			});
			return null;
		} finally {
			LoadDialog.stopDialog();
		}
	}

	public List<EpisodeLabel> loadEpisodes() {
		return serverMap.get(source).loadEpisodes();
	}

	public void getSynopsys(AnimeLabel label) {
		serverMap.get(source).getSynopsys(label);
	}
}