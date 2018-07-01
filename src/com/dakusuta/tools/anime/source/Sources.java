package com.dakusuta.tools.anime.source;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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

	private Map<Source, ISource> sourceMap = new HashMap<>();

	public void setSource(Source source) {
		this.source = source;
	}

	private Sources(Crawler crawler) {
		webCrawler = crawler;
		sourceMap.put(Source.SERVER_1, new Anime1());
		sourceMap.put(Source.SERVER_2, new AnimeXD());
		sourceMap.put(Source.SERVER_3, new ChinaAnime());
		sourceMap.put(Source.SERVER_4, new AnimeFreak());

	}

	public static Sources getInstance(Crawler crawler) {
		if (instance == null) instance = new Sources(crawler);
		return instance;
	}

	public class Anime1 extends ISource {
		private final Background focusBackground = new Background(
				new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
		private final Background unfocusBackground = null;

		@Override
		public List<CustomLabel> loadAnime(Window window) {
			if (!animeList.isEmpty()) return animeList;
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
			} catch (UnknownHostException e) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
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

	public class AnimeXD extends ISource {
		// http://www.animexd.me/home/anime-list
		private AnimeXD() {

		}

		private final Background focusBackground = new Background(
				new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
		private final Background unfocusBackground = null;

		@Override
		public List<CustomLabel> loadAnime(Window window) {
			if (!animeList.isEmpty()) return animeList;
			String animeListPath = "http://www.animexd.me/home/anime-list";
			try {
				Document doc = Jsoup.parse(new URL(animeListPath), 60000);
				LoadDialog.setMessage("Finding anime collection");
				Elements elements = doc.select("div.container-left > div.container-item > div.ci-title");
				LoadDialog.setMessage("Loading List");
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
			} catch (UnknownHostException e) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
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
			Elements nav = selectedDoc.select("div.ci-contents:nth-child(2)");
			Elements elements = nav.select("li >a");

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

	public class AnimeFreak extends ISource {
		// http://animefreak.tv/book
		private final Background focusBackground = new Background(
				new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
		private final Background unfocusBackground = null;

		@Override
		public List<CustomLabel> loadAnime(Window window) {
			if (!animeList.isEmpty()) return animeList;
			String animeListPath = "http://animefreak.tv/book";
			try {
				Document doc = Jsoup.parse(new URL(animeListPath), 60000);
				LoadDialog.setMessage("Finding anime collection");
				Elements elements = doc.select("div.item-list > ul");
				LoadDialog.setMessage("Loading List");
				elements.forEach(element -> {
					List<Element> nodes = element.children();
					nodes.forEach(node -> {
						Element n = node.child(0);
						n.attr("href", "http://animefreak.tv" + n.attr("href"));
						CustomLabel label = new CustomLabel(n);

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
			} catch (UnknownHostException e) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
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
					Element name = doc.select("h1").first();
					Element description = doc.select("h2 ~ blockquote > p").first();
					String html = "<h3>" + name.html() + "</h3>" + description.html();

					webCrawler.poster(getPoster(doc.select("div.content > p > img").attr("src")));
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
			String title = selectedDoc.select("h1").get(0).text();
			Elements nav = selectedDoc.select("div.book-navigation > ul.menu");
			Elements elements = nav.select("li.leaf >a");

			elements.forEach(element -> {
				element.attr("href", "http://animefreak.tv" + element.attr("href"));
				episodes.add(new CustomLabel(title, element));

			});
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

	private class ChinaAnime extends ISource {
		private final Background focusBackground = new Background(
				new BackgroundFill(Color.RED, CornerRadii.EMPTY, Insets.EMPTY));
		private final Background unfocusBackground = null;

		// http://ww2.chia-anime.tv/index/
		public List<CustomLabel> loadAnime(Window window) {
			if (!animeList.isEmpty()) return animeList;
			String animeListPath = "http://ww2.chia-anime.tv/index/";
			try {
				Document doc = Jsoup.parse(new URL(animeListPath), 60000);
				LoadDialog.setMessage("Finding anime collection");
				Elements elements = doc.select("ul > li[itemtype=http://schema.org/TVSeries]");
				LoadDialog.setMessage("Loading List");
				elements.forEach(element -> {
					CustomLabel label = new CustomLabel(element.child(1));

					label.backgroundProperty().bind(Bindings
							.when(label.focusedProperty())
							.then(focusBackground)
							.otherwise(unfocusBackground));
					Platform.runLater(() -> {
						LoadDialog.setMessage("Found " + label.getText());
						animeList.add(label);

						label.setOnMouseClicked(e -> {
							label.requestFocus();
							this.getSynopsys(e);
						});
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

				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
			}
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
					Element name = doc.select("h1").first();
					Element description = doc.select("p[itemprop=description]").first();
					String html = "<h3>" + name.html() + "</h3>" + description.html();

					webCrawler.poster(getPoster(
							"http://ww2.chia-anime.tv/" + doc.select("img[itemprop=image]").first().attr("src")));
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
			String title = selectedDoc.select("h1").get(0).text();
			Elements elements = selectedDoc.select("div.post > h3[itemprop=episodeNumber] > a");
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
		return sourceMap.get(source).loadAnime(window);
	}

	public List<CustomLabel> loadEpisodes() {
		return sourceMap.get(source).loadEpisodes();
	}
}
