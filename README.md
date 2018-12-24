# Animu-Downloaderu

Animu Downloaderu is a java based software which helps you download your favorite anime as a batch.<br/>
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
```
 ./gradlew jar
 #Runnable jar will be present at build/libs/ folder
```
To get the native app
Run
```
 ./gradlew jlink
 #Runnable jar will be present at build/distributions/ folder
```

**Screenshots**

**Huge list of anime to choose from**
![screenshot](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/screenshot.png "screenshot")


**Multiple server support**
![Servers](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/servers.png "Servers")


**Search your favorite anime**
![Search anime](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/search.png "Search anime")


**Get all episodes in a single page**
![Selecting episodes](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/episodes.png "Selecting episodes")


**Download all episodes in a batch**
![Download](https://github.com/codingotaku/Animu-Downloaderu/raw/master/screenshots/download.png "Download")
		

**It is still under development and the code is a bit spaghettified so feel free to contribute :)**

Download the latest Jar form [Releases](https://github.com/CodingOtaku/Animu-Downloaderu/releases)
