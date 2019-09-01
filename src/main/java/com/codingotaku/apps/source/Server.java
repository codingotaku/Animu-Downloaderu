package com.codingotaku.apps.source;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.codingotaku.apps.callback.AnimeFetchListener;
import com.codingotaku.apps.callback.EpisodeListListener;

class Server {
	static void listAllAnime(Source source, AnimeFetchListener listener) {
		new Thread(() -> {
			try {
				Document doc = Jsoup.connect(source.listUrl()).userAgent(
						"Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
						.referrer("http://www.google.com").timeout(60000).get();
				Elements elements = doc.select(source.listRegex());
				listener.loaded(generateAnimeList(source, elements), new Result());
			} catch (IOException e) {
				listener.loaded(null, new Result(e));
			}
		}).start();
	}

	static String getSynopsys(Anime anime) throws IOException {
		return anime.getDoc().select(anime.source.docRegex()).text().replaceAll("([\\w]+ :)", "\n\n$1").trim();
	}

	static void listAllEpisodes(Anime anime, EpisodeListListener listener) {
		new Thread(() -> {
			try {
				String name = anime.getName();
				Source source = anime.source;
				Document doc = anime.getDoc();
				Elements elements = doc.select(source.epRegex());
				listener.loaded(generateEpList(source, elements, name), new Result());
			} catch (IOException e) {
				listener.loaded(null, new Result(e));
			}
		}).start();
	}

	static String getPosterUrl(Anime anime) throws IOException {
		return anime.getDoc().select(anime.source.posterRegex()).first().attr("src");
	}

	static String generateVideoUrl(Episode episode) throws IOException {
		Document doc = Jsoup.connect(episode.episodeUrl)
				.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
				.referrer("http://www.google.com").timeout(60000).get();
		Pattern pattern = Pattern.compile(episode.source.vidRegex());
		Matcher matcher = pattern.matcher(doc.data());
		if (matcher.find())
			return matcher.group();
		return null;
	}

	private static List<Episode> generateEpList(Source source, Elements elements, String name) {
		List<Episode> episodes = new ArrayList<>();
		elements.forEach(element -> episodes.add(new Episode(source, element, name)));
		return Collections.unmodifiableList(episodes);
	}

	private static List<Anime> generateAnimeList(Source source, Elements elements) {
		List<Anime> list = new ArrayList<Anime>();
		elements.forEach(element -> list.add(new Anime(source, element)));
		return Collections.unmodifiableList(list);
	}

	public static String getName(Anime anime) throws IOException {
		return anime.getDoc().select(anime.source.nameRegex()).text();
	}
}
