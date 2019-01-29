# Animu-Downloaderu

## Notes

1. Removed Server 2,
1. Modified code so that it would be easier to add new servers in the future! ([this file](https://github.com/codingotaku/Animu-Downloaderu/blob/master/src/main/java/com/codingotaku/apps/source/Source.java))

## Good bye

As you all might already know, most of the Anime websites are being taken down, and some websties added more annoying check to catch crawlers!  

 This means that we are limited by the websites that we have right now. and there are no APIs exposed for watching anime :/  
 We might add more servers in the future!  

 Anime1 server should work, but I cannot guarantee on when the videos gets blocked.  

 **I do not support piracy!**, The only reason for me making this app was because of the licensing crap that mainstream anime hosing services created and the ad+tracking bullshit in other websites adopted to.

 This might be a good bye, **for now**.. but I will try to create new app for similar tasks.
___

## About the App

Animu Downloaderu is a java based software which helps you download your favorite anime as a batch.  
More details at https://codingotaku.com/apps/AnimuDownloaderu/

**Requirements :** Java **11** or above.
1) Extract AnimeDownloader.zip/AnimeDownloader.tar to your preferred location
2) On Linux/Mac. go to AnimeDownloader/bin and run ./AnimeDownloader
   On Windows, go to AnimeDownloader/bin and run AnumeDownloader.bat
3) Wait for a few seconds for the app to fetch list of anime. (one time wait!)
4) Select the folder where you want to keep your anime.
5) Enter the anime you are looking for in the search box.
6) Select the anime from the results on the side panel to see the anime summary.
7) Click on Show Eppisodes to display available episodes.
8) Select the range of episodes to download and Click on Download button.
9) If you cannot find your favorite anime from the list, you can try changing the server!.
10) Enjoy ;)

**To build from Source**
Run

```bash
 ./gradlew jar
 #Runnable jar will be present at build/libs/ folder
```

To get the native app
Run

```bash
 ./gradlew jlink
 #Runnable jar will be present at build/distributions/ folder
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

Download the latest Jar form [Releases](https://github.com/CodingOtaku/Animu-Downloaderu/releases)
