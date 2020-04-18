# Animu-Downloaderu

## Notes

1. Now has three Servers (Anime1, AnimeFreak, and GogoAnime1) all are in essence the same site (same content hosted differently),
1. Uses AnimeCrawlerAPI ([github](https://github.com/codingotaku/AnimeCrawlerAPI), [gitlab](https://gitlab.com/codingotaku/AnimeCrawlerAPI)) for fetching Anime.
1. Modified code so that it would be easier to add new servers in the future! ([this file](./src/main/java/com/codingotaku/apps/source/AnimeSources.java))
We might add more servers in the future! 
1. Android version is not the top priority as of now.

 **I do not support piracy!**, The only reason for me making this app was because of the DRM (Digital Restrictions Management) and licensing that mainstream anime hosing services has and the ad+tracking malware other websites adopted to.
___

## About the App

Animu Downloaderu is a java based software which helps you download your favorite anime as a batch. 

**To build from Source**
Run

```bash
 mvn javafx:run
```

**To get the native app image**
Run

```bash
  mvn javafx:jlink
```

## Screenshots

**Huge list of anime to choose from**
![screenshot](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/screenshot.png "screenshot")

**Search your favorite anime**
![Search anime](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/search.png "Search anime")

**Get all episodes in a single page**
![Selecting episodes](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/episodes.png "Selecting episodes")

**Download all episodes in a batch**
![Download](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/download.png "Download")

### It is still under development and the code is a bit spaghettified so feel free to contribute :)

Download the latest binary form [Releases](https://github.com/CodingOtaku/Animu-Downloaderu/releases)
