
# Youtube Downloader


## Download Youtube Videos, Playlists, and Channels


----


### Table of Contents:

- [**Setup**](#setup)
  - [Downloading the Project](#downloading-the-project)
  - [Installing Dependencies](#installing-dependencies)

+ [**Usage**](#usage)
  + [Running the Project](#running-the-project)
  + [Executable Options](#executable-options)
  + [Updating the Project](#updating-the-project)

- [**The Youtube Downloader**](#the-youtube-downloader)
  - [Configuring the Youtube Downloader](#configuring-the-youtube-downloader)
    - [_Youtube Downloader Settings_](#youtube-downloader-settings)
    - [_Youtube Downloader Configuration (Sample)_](#youtube-downloader-configuration-sample)

+ [**The Youtube Channel Downloader**](#the-youtube-channel-downloader)
  + [Getting an API Key](#getting-an-api-key)
    + [_Setting up your API Key_](#setting-up-your-api-key)
    + [_Recovering your API Key_](#recovering-your-api-key)
  + [Configuring the Youtube Channel Downloader](#configuring-the-youtube-channel-downloader)
    + [_Youtube Channel Downloader Settings_](#youtube-channel-downloader-settings)
    + [_Youtube Channel Downloader Configuration (Sample)_](#youtube-channel-downloader-configuration-sample)
  + [Adding Channels](#adding-channels)
    + [_Channel Settings_](#channel-settings)
    + [_Channel Configuration (Sample)_](#channel-configuration-sample)
  + [Finding Youtube Playlist IDs](#finding-youtube-playlist-ids)
    + [_Getting the Playlist ID of a Youtube Channel_](#getting-the-playlist-id-of-a-youtube-channel)
    + [_Getting the Playlist ID of a Youtube Playlist_](#getting-the-playlist-id-of-a-youtube-playlist)
  + [Grouping Channels](#grouping-channels)
    + [_Channel Group Settings_](#channel-group-settings)
    + [_Channel Group Configuration (Sample)_](#channel-group-configuration-sample)
    + [_Channel Group Inheritance_](#channel-group-inheritance)
    + [_Channel Group Inheritance Configuration (Sample)_](#channel-group-inheritance-configuration-sample)
  + [Special Channel Processing](#special-channel-processing)

- [**Configuration**](#configuration)
  - [Configuration Overview](#configuration-overview)
    - [_Configuration Sections_](#configuration-sections)
    - [_Configuration (Sample)_](#configuration-sample)
  - [SponsorBlock Configuration](#sponsorblock-configuration)
    - [_SponsorBlock Settings_](#sponsorblock-settings)
    - [_SponsorBlock Configuration (Sample)_](#sponsorblock-configuration-sample)
  - [Color Configuration](#color-configuration)
    - [_Supported Colors_](#supported-colors)
    - [_Color Settings_](#color-settings)
    - [_Color Configuration (Sample)_](#color-configuration-sample)
  - [Logging Configuration](#logging-configuration)
    - [_Logging Settings_](#logging-settings)
    - [_Logging Configuration (Sample)_](#logging-configuration-sample)


----


# Setup

To get started, you will need to download the [project](#downloading-the-project), and install the required [dependencies](#installing-dependencies).


## Downloading the Project

Simply [**download**](https://github.com/ZGorlock/YoutubeDownloader/archive/refs/heads/master.zip) the latest project files from [_Github_](https://github.com/ZGorlock/YoutubeDownloader).
\
Then extract the files to a location of your choice.

Alternatively you can clone the repository with [**git**](https://git-scm.com/) using:

```shell
git clone "https://github.com/ZGorlock/YoutubeDownloader.git"
```


## Installing Dependencies

To run this project you will need to have the following dependencies installed:

| **Dependency** | **Minimum Version** |                         **Website**                         |                                                                           **Windows**                                                                           |                                                                             **Linux**                                                                             |
|:--------------:|:-------------------:|:-----------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|                |                     |                                                             |                                                                                                                                                                 |                                                                                                                                                                   |
|    **Java**    |      `13.0.2`       |       [_jdk.java.net_](https://jdk.java.net/archive/)       | [**openjdk-13.0.2_windows-x64_bin.zip**](https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_windows-x64_bin.zip) | [**openjdk-13.0.2_linux-x64_bin.tar.gz**](https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz) |
|   **Maven**    |       `3.8.7`       | [_maven.apache.org_](https://maven.apache.org/download.cgi) |                       [**apache-maven-3.8.7-bin.zip**](https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.zip)                        |                     [**apache-maven-3.8.7-bin.tar.gz**](https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.tar.gz)                      |
|   **FFmpeg**   |          -          |      [_ffmpeg.org_](https://ffmpeg.org/download.html)       |          [**ffmpeg-master-latest-win64-gpl.zip**](https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip)          |      [**ffmpeg-master-latest-linux64-gpl.tar.xz**](https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz)      |
|                |                     |                                                             |                                                                                                                                                                 |                                                                                                                                                                   |

If you do not already have these dependencies installed then download the packages for your operating system.
\
Then extract the packages to a location of your choice.

Add the `/bin` folder of each dependency to your `Path` environment variable so they are accessible on the system path.

You can verify that they are installed properly by executing the following commands and checking the versions:

```shell
java --version
```

```shell
mvn -v
```

```shell
ffmpeg
```


&nbsp;

----


# Usage

This project contains two programs:

- The [Youtube Downloader](#the-youtube-downloader)
- The [Youtube Channel Downloader](#the-youtube-channel-downloader)


## Running the Project

The project can be run by using the scripts in the project directory.
\
Depending on your operating system use either the _.bat_ scripts (Windows) or the _.sh_ scripts (Linux).

**On Windows:**

```shell
YoutubeDownloader.bat
```

```shell
YoutubeChannelDownloader.bat
```

**On Linux:**

```shell
./YoutubeDownloader.sh
```

```shell
./YoutubeChannelDownloader.sh
```

\
Alternatively you can run the project with [**Maven**](https://maven.apache.org/) using:

```shell
mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeDownloader"
```

```shell
mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeChannelDownloader"
```

\
Also, if you prefer, you could always run or debug the project in an IDE.


## Executable Options

You can configure this project to use either [**yt-dlp**](https://github.com/yt-dlp/yt-dlp/) or [**youtube-dl**](https://youtube-dl.org/).

- **_yt-dlp_**
  - A newer drop-in replacement of the Youtube Downloader executable
  - _Actively maintained by new developers_
  - _Includes additional features_

+ **_youtube-dl_**
  + The original Youtube Downloader executable
  + _As of the time of writing this it appears it may no longer be maintained_
  + _Throttling and failures often occur when downloading videos_

\
Specify your choice of **_executable_** in the project configuration as explained under:

- [Configuring the Youtube Downloader](#configuring-the-youtube-downloader)
- [Configuring the Youtube Channel Downloader](#configuring-the-youtube-channel-downloader)

The project will automatically download the executable that you choose.
\
At the beginning of each run it will check for updates to the executable and download the latest version if needed.


## Updating the Project

This project should work as it does now indefinitely, as long as **_youtube-dl_** and **_yt-dlp_** continue to exist on the same websites they do now.
\
However, additional improvements and features may be added from time to time.

If you wish to receive these updates then you can [**download**](https://github.com/ZGorlock/YoutubeDownloader/archive/refs/heads/master.zip) the latest project files from [_Github_](https://github.com/ZGorlock/YoutubeDownloader).
\
_More information about the download process is provided under:_ [Downloading the Project](#downloading-the-project).

If you cloned the repository with [**git**](https://git-scm.com/) then you can update to the latest code using:

```shell
git pull origin master
```

\
⚠️**WARNING**: Before updating the project, make sure you backup your configuration and data files:

- [ ] `conf.json`
- [ ] `channels.json`
- [ ] `src/youtube/channel/process/ChannelProcesses.java`
- [ ] `data/`

⚠️Updating may cause merge conflicts and could potentially overwrite your custom changes.


&nbsp;

----


# The Youtube Downloader

This program is just a simple Youtube video downloader.
\
It will download the videos from the Youtube urls entered into the terminal while the program is running.

Videos will be saved in a `~/Youtube` folder in your user directory.
\
This can be customized by defining **_location.output_** in the [Youtube Downloader Configuration](#configuring-the-youtube-downloader).

You can also create a list of Youtube urls in the file `data/downloadQueue.txt` and when the program is executed it will download the provided list of videos.

The program will continue to run until an empty line in entered: `Enter`.


## Configuring the Youtube Downloader

You can customize the operation of the _Youtube Downloader_ by changing the settings inside the file `conf.json`.

To configure the _Youtube Downloader_, find or create the `"YoutubeDownloader"` [json configuration](#youtube-downloader-configuration-sample) and customize the [settings](#youtube-downloader-settings) it contains.

### Youtube Downloader Settings:

| **SETTING**                       | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|:----------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                   |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **_executable_**                  | The name of the executable which will be used to download content from Youtube. <br/> The two valid options for this setting are `"yt-dlp"` and `"youtube-dl"`. <br/> _More information about these executables is provided under:_ [Executable Options](#executable-options).                                                                                                                                                                                                                                                                                                                                                                   |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_location_**                    | **The location settings for the _Youtube Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **_location.output_**             | The path of the directory where downloaded content will be saved. For example: `"C:/Users/User/Downloads"`. <br/> By leaving this blank (`""`), content will be saved in a `~/Youtube` folder in your user directory.                                                                                                                                                                                                                                                                                                                                                                                                                            |
| **_location.browser_**            | The name of the browser that you use locally to watch Youtube. <br/> This is the browser that cookies will be used from when attempting to retry certain failed downloads, assuming that **_flag.neverUseBrowserCookies_** is disabled. <br/> The acceptable values for this setting are `"Brave"`, `"Chrome"`, `"Chromium"`, `"Edge"`, `"Firefox"`, `"Opera"`, `"Safari"`, or `"Vivaldi"`. _(optional)_                                                                                                                                                                                                                                         |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_format_**                      | **The format settings for the _Youtube Downloader_ :** _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| **_format.asMp3_**                | When this setting is enabled, the _Youtube Downloader_ will download content as audio files (_.mp3_) instead of video files (_.mp4_). <br/> When this setting is enabled, you must have [**FFmpeg**](https://ffmpeg.org/) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                            |
| **_format.preMerged_**            | When this setting is enabled, the _Youtube Downloader_ will download videos in the best pre-merged format. <br/> When this setting is disabled, and when **_executable_** is set to `"yt-dlp"`, the _Youtube Downloader_ will download videos in the best possible format, not just the best pre-merged format. <br/> When this setting is disabled, videos will not necessarily be downloaded in _.mp4_ format. <br/> When this setting is disabled, you must have [**FFmpeg**](https://ffmpeg.org/) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_ |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_flag_**                        | **The flag settings for the _Youtube Downloader_ :** _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| **_flag.safeMode_**               | When this setting is enabled, **_flag.preventDownload_**, **_flag.preventVideoFetch_**, **_flag.preventExeAutoUpdate_**, and **_flag.preventExeVersionCheck_** will be enabled; overriding their individual values. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                 |
| **_flag.preventDownload_**        | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Downloader_ will not attempt to download any videos. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                             |
| **_flag.preventVideoFetch_**      | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Downloader_ will not attempt to fetch the video information _(title, publish date, etc.)_ prior to downloading. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                         |
| **_flag.preventExeAutoUpdate_**   | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Downloader_ will not attempt to download or automatically update the selected **_executable_**. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                         |
| **_flag.preventExeVersionCheck_** | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Downloader_ will not attempt to check the current or latest version of the selected **_executable_**. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                   |
| **_flag.neverUseBrowserCookies_** | When the _Youtube Downloader_ fails to download an age-restricted video, before marking that video as _blocked_, it can attempt one more time using the local browser cookies. <br/> When this setting is disabled, and when **_location.browser_** is properly set, the previously described functionality will be active. <br/> When this setting is enabled, the _Youtube Downloader_ will never attempt to use local browser cookies. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                   |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_sponsorBlock_**                | **The program-level SponsorBlock configuration for the _Youtube Downloader_ :** _(optional)_ <br/> _Instructions on how to define a SponsorBlock configuration can be found under:_ [SponsorBlock Configuration](#sponsorblock-configuration).                                                                                                                                                                                                                                                                                                                                                                                                   |
|                                   |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

### Youtube Downloader Configuration (Sample):

```json
"YoutubeDownloader": {
    "executable": "yt-dlp",
    "location": {
        "output": "C:/Users/User/Downloads",
        "browser": "Chrome"
    },
    "format": {
        "asMp3": false,
        "preMerged": true
    }
}
```


&nbsp;

----


# The Youtube Channel Downloader

This program is a more complex Youtube video downloader and the main focus of this project.
\
It will download and keep entire playlists and channels synchronized on your hard drive.

This document will explain everything you need to know to set up the _Youtube Channel Downloader_ to work for you.


## Getting an API Key

In order to use the _Youtube Channel Downloader_, you will need to get a Google API key.
\
A Google API key is completely free through your Google account.

The _Youtube Channel Downloader_ uses the Youtube Data API to query the video lists of channels and playlists.
\
The free quota for the Youtube Data API is 10,000 queries per day, which is more than you should ever need.

### Setting up your API Key:

- [ ] Go to: [_Google Cloud - APIs & Services_](https://console.cloud.google.com/projectselector2/apis/dashboard)
- [ ] Click **_Create Project_**
  - [ ] Set **Project name**: '`Youtube Downloader`'
  - [ ] Click **_Create_**
- [ ] Click **_Enable APIs and Services_**
- [ ] Search for: '`Youtube`'
  - [ ] Select `YouTube Data API v3`
- [ ] Click **_Enable_**
- [ ] Click **_Create Credentials_**
  - [ ] Select `YouTube Data API v3`
  - [ ] Click **_Public Data_**
  - [ ] Click **_Next_**
- [ ] Copy the **API Key** to the file `apiKey` in the project directory

### Recovering your API Key:

- [ ] Go to: [_Google Cloud - APIs & Services_](https://console.cloud.google.com/projectselector2/apis/dashboard)
- [ ] Click **_Select Project_**
  - [ ] Select `Youtube Downloader`
  - [ ] Click **_Open_**
- [ ] Click **_Credentials_**
  - [ ] Select your API key
  - [ ] Click **_Show Key_**
- [ ] Copy the **API Key** to the file `apiKey` in the project directory


## Configuring the Youtube Channel Downloader

You can customize the operation of the _Youtube Channel Downloader_ by changing the settings inside the file `conf.json`.

To configure the _Youtube Channel Downloader_, find or create the `"YoutubeChannelDownloader"` [json configuration](#youtube-channel-downloader-configuration-sample) and customize the [settings](#youtube-channel-downloader-settings) it contains.

### Youtube Channel Downloader Settings:

| **SETTING**                       | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|:----------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                   |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **_executable_**                  | The name of the executable which will be used to download content from Youtube. <br/> The two valid options for this setting are `"yt-dlp"` and `"youtube-dl"`. <br/> _More information about these executables is provided under:_ [Executable Options](#executable-options).                                                                                                                                                                                                                                                                                                                                                                                   |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_location_**                    | **The location settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **_location.storageDrive_**       | The path of the drive where downloaded content will be saved. For example: `"C:/"`. <br/> You may leave this blank (`""`) if you wish to specify the full path for each Channel in `channels.json`                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| **_location.musicDir_**           | The path of the directory where audio will be saved. For example: `"Users/User/Music/"`. <br/> This path is relative to **_location.storageDrive_**. <br/> You may leave this blank (`""`) if you wish to specify the full path for each Channel in `channels.json`. <br/> If you leave this blank you must also leave **_location.storageDrive_** and **_location.videoDir_** blank.                                                                                                                                                                                                                                                                            |
| **_location.videoDir_**           | The path of the directory where video will be saved. For example: `"Users/User/Videos/"`. <br/> This path is relative to **_location.storageDrive_**. <br/> You may leave this blank (`""`) if you wish to specify the full path for each Channel in `channels.json`. <br/> If you leave this blank you must also leave **_location.storageDrive_** and **_location.musicDir_** blank.                                                                                                                                                                                                                                                                           |
| **_location.browser_**            | The name of the browser that you use locally to watch Youtube. <br/> This is the browser that cookies will be used from when attempting to retry certain failed downloads, assuming that **_flag.neverUseBrowserCookies_** is disabled. <br/> The acceptable values for this setting are `"Brave"`, `"Chrome"`, `"Chromium"`, `"Edge"`, `"Firefox"`, `"Opera"`, `"Safari"`, or `"Vivaldi"`. _(optional)_                                                                                                                                                                                                                                                         |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_format_**                      | **The format settings for the _Youtube Channel Downloader_ :** _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **_format.preMerged_**            | When this setting is enabled, the _Youtube Channel Downloader_ will download videos in the best pre-merged format. <br/> When this setting is disabled, and when **_executable_** is set to `"yt-dlp"`, the _Youtube Channel Downloader_ will download videos in the best possible format, not just the best pre-merged format. <br/> When this setting is disabled, videos will not necessarily be downloaded in _.mp4_ format. <br/> When this setting is disabled, you must have [**FFmpeg**](https://ffmpeg.org/) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_ |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_filter_**                      | **The filter settings for the _Youtube Channel Downloader_ :** _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **_filter.channel_**              | There may be times where you want to process only a single Channel and not all of them. <br/> Set this to `"<YOUR_CHANNEL_KEY>"` to process only that Channel. <br/> To return to processing all Channels, set this back to `null`. _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **_filter.group_**                | There may be times where you want to process only a single group and not all of them. <br/> Set this to `"<YOUR_CHANNEL_GROUP>"` to process only that group. <br/> To return to processing all Channels, set this back to `null`. _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **_filter.startAt_**              | There may be times where you want to start processing at a specified Channel, skipping all Channels before it. <br/> The values for this setting work the same as for **_filter.channel_**. <br/> The Channels are processed in the order that they appear in your Channel configuration. <br/> To return to normal Channel processing, set this back to `null`. _(optional)_                                                                                                                                                                                                                                                                                    |
| **_filter.stopAt_**               | There may be times where you want to stop processing at a specified Channel, skipping all Channels after it. <br/> The values for this setting work the same as for **_filter.channel_**. <br/> The Channels are processed in the order that they appear in your Channel configuration. <br/> To return to normal Channel processing, set this back to `null`. _(optional)_                                                                                                                                                                                                                                                                                      |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_log_**                         | **The log settings for the _Youtube Channel Downloader_ :** _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **_log.printStats_**              | Whether to print statistics to the console at the end of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **_log.printChannels_**           | Whether to print the Channel list to the console at the start of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_flag_**                        | **The flag settings for the _Youtube Channel Downloader_ :** _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| **_flag.safeMode_**               | When this setting is enabled, **_flag.preventDeletion_**, **_flag.preventRenaming_**, **_flag.preventDownload_**, **_flag.preventPlaylistEdit_**, **_flag.preventChannelFetch_**, **_flag.preventExeAutoUpdate_**, and **_flag.preventExeVersionCheck_** will be enabled; overriding their individual values. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                       |
| **_flag.preventDownload_**        | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Channel Downloader_ will not attempt to download any videos. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                     |
| **_flag.preventDeletion_**        | When the _Youtube Channel Downloader_ detects that a video has been deleted off Youtube, from a Channel that has **_keepClean_** enabled, then it will also be deleted from your hard drive. <br/> When this setting is enabled, or when **_flag.safeMode_** is enabled, media deletion will be globally disabled; overriding the **_keepClean_** flag for all Channels. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                   |
| **_flag.preventRenaming_**        | When the _Youtube Channel Downloader_ detects that a video has been renamed on Youtube, or when you modify your [Channel Processes](#special-channel-processing), then it will also be renamed on your hard drive. <br/> When this setting is enabled, or when **_flag.safeMode_** is enabled, media renaming will be globally disabled. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                   |
| **_flag.preventPlaylistEdit_**    | When the _Youtube Channel Downloader_ detects that a video has been added, removed, or renamed on Youtube, or when you modify your [Channel Processes](#special-channel-processing), for a Channel that has a **_playlistFile_**, then the playlist file will be modified to reflect the changes. <br/> When this setting is enabled, or when **_flag.safeMode_** is enabled, playlist modification will be globally disabled. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                             |
| **_flag.preventChannelFetch_**    | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Channel Downloader_ will not attempt to fetch the latest data for Channels; this will result in the previously fetched data being used. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                          |
| **_flag.preventExeAutoUpdate_**   | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Channel Downloader_ will not attempt to download or automatically update the selected **_executable_**. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                 |
| **_flag.preventExeVersionCheck_** | When this setting is enabled, or when **_flag.safeMode_** is enabled, the _Youtube Channel Downloader_ will not attempt to check the current or latest version of the selected **_executable_**. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                           |
| **_flag.deleteToRecyclingBin_**   | When this setting is enabled, the _Youtube Channel Downloader_ will attempt to move files to the recycling bin instead of deleting them. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **_flag.neverUseBrowserCookies_** | When the _Youtube Channel Downloader_ fails to download an age-restricted video, before marking that video as _blocked_, it can attempt one more time using the local browser cookies. <br/> When this setting is disabled, and when **_location.browser_** is properly set, the previously described functionality will be active. <br/> When this setting is enabled, the _Youtube Channel Downloader_ will never attempt to use local browser cookies. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                   |
| **_flag.retryPreviousFailures_**  | When the _Youtube Channel Downloader_ fails to download a video, either because of a connection issue or because a video is "not available in your country", etc., it will mark that video as _blocked_ and will not automatically attempt to download it again; however sometimes the download will succeed if reattempted. <br/> When this setting is enabled, all previously failed downloads from all Channels will be reattempted. <br/> This should only be enabled occasionally and disabled after the run. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                         |
| &nbsp;                            | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_sponsorBlock_**                | **The program-level SponsorBlock configuration for the _Youtube Channel Downloader_ :** _(optional)_ <br/> _Instructions on how to define a SponsorBlock configuration can be found under:_ [SponsorBlock Configuration](#sponsorblock-configuration).                                                                                                                                                                                                                                                                                                                                                                                                           |
|                                   |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

### Youtube Channel Downloader Configuration (Sample):

```json
"YoutubeChannelDownloader": {
    "executable": "yt-dlp",
    "location": {
        "storageDrive": "C:/",
        "musicDir": "Users/User/Music/",
        "videoDir": "Users/User/Videos/",
        "browser": "Chrome"
    },
    "format": {
        "preMerged": true
    },
    "filter": {
        "channel": null,
        "group": null,
        "startAt": null,
        "stopAt": null
    }
}
```


## Adding Channels

You can specify the Youtube channels and playlists that you want to process by configuring Channels inside the file `channels.json`.

There are many examples in `channels-sample.json` that you can use as a reference.
\
The file `channels-sample.json` is also my personal Channel configuration; if you see any Channels that you like you can copy them to your `channels.json`.

To add a Channel, create a new Channel [json configuration](#channel-configuration-sample) and customize the [settings](#channel-settings) it contains.

### Channel Settings:

| **SETTING**                 | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|:----------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| **_key_**                   | The key which uniquely identifies the Channel within your configuration. For example: `"MY_CHANNEL"`. <br/> This setting is required and is an identifier which must be unique; a **_key_** used for a Channel must not be used by any other Channel in the configuration. <br/> As a best practice, only uppercase letters (`A-Z`), numbers (`0-9`), and underscores (`_`) should be used when defining a **_key_**. <br/> This setting should not contain spaces and should not be empty (`""`) or `null`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **_active_**                | When this setting is enabled, the Channel is _active_; meaning it will be processed when the _Youtube Channel Downloader_ runs and its data will be synced. <br/> When this setting is disabled, the Channel is _inactive_; meaning it will be ignored when the _Youtube Channel Downloader_ runs. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| **_name_**                  | The human readable name of the Channel. For example: `"MyChannel"`. <br/> This setting is an identifier which must be unique; a **_name_** used for a Channel must not be used by any other Channel in the configuration. <br/> If this setting is not provided, a **_name_** will be automatically generated for the Channel based on the **_key_**. <br/> This setting should not contain spaces or special characters and should not be empty (`""`) or `null`. _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| **_group_**                 | The user-defined group or category for the Channel. For example: `"FAVORITES"`. <br/> You may also define multiple groups or categories by separating them with delimiters. For example: `"FAVORITES : VIDEOS : GENERAL"`. <br/> As a best practice, only uppercase letters (`A-Z`), numbers (`0-9`), and underscores (`_`) should be used when defining a **_group_**. <br/> The following characters are valid for use as delimiters: (`.`, `,`, `;`, `:`, `+`, `&`). <br/>This setting should not contain spaces, except surrounding delimiters, and should not be empty (`""`) or `null`. _(optional)_ <br/> _**Note:** It is now recommended to use Channel Groups instead; instructions on how to define Channel Groups can be found under:_ [Grouping Channels](#grouping-channels).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| &nbsp;                      | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **_url_**                   | The url of the Youtube playlist or channel. For example: `"https://www.youtube.com/c/MyFavoriteChannel"`. <br/> This setting is not used by the program, but it can be useful when returning to the playlist or channel in the future. _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **_playlistId_**            | The id of the Youtube playlist or channel. For example: `"UU65aMyFavoriteChannelXY"`. <br/> _Instructions on how to obtain the playlist id can be found under:_ [Finding Youtube Playlist IDs](#finding-youtube-playlist-ids).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| &nbsp;                      | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **_outputFolder_**          | The path of the output directory of the Channel where downloaded content will be saved. For example: `"Youtube/My Channel"`. <br/> If this Channel is being used to download audio _(based on **_saveAsMp3_**)_, then this path is relative to **_location.musicDir_** defined in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader). <br/> If this Channel is being used to download video _(based on **_saveAsMp3_**)_, then this path is relative to **_location.videoDir_** defined in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader). <br/> If you chose to leave the **_location_** settings blank in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader), or if **_ignoreGlobalLocations_** is enabled, then this setting must specify the full path of the output directory. <br/> <br/> ⚠️**WARNING**: The output directory should be an empty directory initially! <br/> ⚠️**DO NOT** use the same directory for all your Channels and especially do not use directories where you already have other videos or data saved! <br/> ⚠️The **_Youtube Channel Downloader_ is able to delete files from this directory** in certain cases, so do not set this to a directory that contains existing files you do not want to lose! |
| **_playlistFile_**          | The path of the playlist file (_.m3u_) to be used to enumerate the downloaded content of the Channel. For example: `"Youtube/My Channel.m3u"`. <br/> This path is relative to the same base path as **_outputFolder_**, however it does not necessarily need to be in the same folder as **_outputFolder_**. <br/> If you chose to leave the **_location_** settings blank in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader), or if **_ignoreGlobalLocations_** is enabled, then this setting must specify the full path of the playlist file. <br/> If this setting is not provided, but **_savePlaylist_** is enabled, the playlist will be saved in the default location described by **_savePlaylist_**. <br/> If this setting is provided, but **_savePlaylist_** is disabled, **_savePlaylist_** will take precedence and the playlist file will not be created. _(optional)_                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| &nbsp;                      | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **_saveAsMp3_**             | When this setting is enabled, the content of the Channel will be downloaded as audio files (_.mp3_) instead of video files (_.mp4_). <br/> When this setting is enabled, you must have [**FFmpeg**](https://ffmpeg.org/) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| **_savePlaylist_**          | When this setting is enabled, a playlist file (_.m3u_) will be created to enumerate the downloaded content of the Channel. <br/> When this setting is disabled, no playlist file will be created; this setting takes precedence over **_playlistFile_**. <br/> The playlist file, if it is created, will be placed at the location specified by **_playlistFile_**. <br/> If no **_playlistFile_** is provided, then the playlist file will be placed in the parent folder of the **_outputFolder_** and have the same name as the **_outputFolder_**. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **_reversePlaylist_**       | When this setting is enabled, and when **_savePlaylist_** is enabled, the playlist will be saved in reverse order; newer content will be placed at the beginning of the playlist instead of the end. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **_ignoreGlobalLocations_** | When this setting is enabled, the **_location_** settings defined in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader) are disregarded for **_outputFolder_** and **_playlistFile_**. <br/> When this setting is enabled, you must specify the full path for **_outputFolder_** and **_playlistFile_**. <br/> When this setting is enabled, then **_outputFolder_** and **_playlistFile_** may contain `${D}`, `${V}`, or `${M}` which will translate to **_location.storageDrive_**, **_location.videoDir_**, and **_location.musicDir_** respectively. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **_keepClean_**             | When this setting is enabled, and when **_savePlaylist_** is enabled, the output directory of the Channel will be kept synchronized with the Youtube playlist or channel. <br/> If the output directory is synchronized then videos that are deleted off of Youtube will also be deleted locally. <br/> If the output directory is not synchronized then videos that are deleted off of Youtube will not be deleted locally, and will only be removed from the playlist file. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| &nbsp;                      | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| **_sponsorBlock_**          | **The SponsorBlock configuration for the Channel :** _(optional)_ <br/> _Instructions on how to define a SponsorBlock configuration can be found under:_ [SponsorBlock Configuration](#sponsorblock-configuration).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |

### Channel Configuration (Sample):

```json
{
    "key": "MY_CHANNEL",
    "active": true,
    "name": "MyChannel",
    "group": "FAVORITES",
    "url": "https://www.youtube.com/c/MyFavoriteChannel",
    "playlistId": "UU65aMyFavoriteChannelXY",
    "outputFolder": "Youtube/My Channel",
    "playlistFile": "Youtube/My Channel.m3u",
    "saveAsMp3": false,
    "savePlaylist": true,
    "reversePlaylist": true,
    "ignoreGlobalLocations": false,
    "keepClean": false
}
```

⚠️**WARNING:**
- Again, make sure you set the **_outputFolder_** for each Channel to an empty directory or a directory that does not exist yet
- These directories may have files deleted from them in certain circumstances
  - By default, files that are deleted in this way are not sent to the recycle bin and would be difficult, if not impossible, to recover
- Examples:
  - **BAD**: A directory that also contains your personal home videos
  - **BAD**: A directory that also contains important work documents
  - **BAD**: A directory that also contains videos that you have downloaded previously for this Channel before starting to use this program
  - **GOOD**: An empty directory or a directory that does not exist yet


## Finding Youtube Playlist IDs

When creating a [Channel Configuration](#adding-channels) you need to provide a setting called **_playlistId_**.
\
This is what links your Channel with the actual channel or playlist on Youtube.

This playlist id already exists and you just have to get it from Youtube and set it in your Channel.
\
The process of obtaining the playlist id depends on whether your Channel represents a [channel](#getting-the-playlist-id-of-a-youtube-channel) or a [playlist](#getting-the-playlist-id-of-a-youtube-playlist).

### Getting the Playlist ID of a Youtube Channel:

- [ ] Go to the home page of the channel on [_Youtube_](https://www.youtube.com/)
  - [ ] Right click the blank area of the page
    - [ ] Select **View page source**
    - [ ] Search for: '`externalId`'
- [ ] Copy the _external id_ from the page source:
  - `···,"externalId":"UC65aMyFavoriteChannelXY",···`
  - `··················UC65aMyFavoriteChannelXY·····`
- [ ] Change the second letter in the _external id_ from '`C`' to '`U`' to get the _playlist id_
- [ ] Set the _playlist id_ as **_playlistId_** in your Channel

### Getting the Playlist ID of a Youtube Playlist:

- [ ] Go to the home page of the playlist on [_Youtube_](https://www.youtube.com/)
- [ ] Copy the _playlist id_ from the url:
  - `https://www.youtube.com/watch?v=L_MupB3z1g4&list=PLUja9k1MyFavoriteChannel5Py_guZ3R`
  - `·················································PLUja9k1MyFavoriteChannel5Py_guZ3R`
- [ ] Set the _playlist id_ as **_playlistId_** in your Channel


## Grouping Channels

After you have finished adding [Channel Configurations](#adding-channels), you have the option to group your Channels; making them easier to read, search, and filter.

You can specify the groups that you want by configuring Channel Groups inside the file `channels.json`.

To add a Channel Group, create a new Channel Group [json configuration](#channel-group-configuration-sample) and customize the [settings](#channel-group-settings) it contains.

### Channel Group Settings:

| **SETTING**    | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
|:---------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| **_key_**      | The key which uniquely identifies the Channel Group within your configuration. For example: `"MY_GROUP"`. <br/> This setting is required and is an identifier which must be unique; a **_key_** used for a Channel Group must not be used by any other Channel Group in the configuration. <br/> As a best practice, only uppercase letters (`A-Z`), numbers (`0-9`), and underscores (`_`) should be used when defining a **_key_**. <br/> This setting should not contain spaces and should not be empty (`""`) or `null`.                                                                               |
| **_channels_** | The list of Channels and Channel Groups that are children of the Channel Group. <br/> This setting is an array and can contain any number of [Channel Configurations](#channel-configuration-sample) and [Channel Group Configurations](#channel-group-configuration-sample). <br/> <br/>  In the case of nested Channel Groups, a child will be considered a member of all of the enclosing Channel Groups. <br/> If the child is a Channel, it will also be considered a member of the group specified by its own **_group_** setting, if provided. <br/> Any of these groups may be used for filtering. |
|                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            | 

### Channel Group Configuration (Sample):

```json
{
    "key": "MY_GROUP",
    "channels": [
        ...
    ]
}
```

### Channel Group Inheritance:

When creating a [Channel Group Configuration](#grouping-channels) you can also define the default setting configuration for that group.

In addition to the required **_key_** and **_channels_** settings, any [setting](#channel-settings) available in a [Channel Configuration](#adding-channels) can also be configured in a Channel Group. _(excluding: **_playlistFile_**)_
\
When a setting is configured in this way, all children of that group will inherit the setting.

For example, if a Channel Group enables **_saveAsMp3_**, then all children of the group will save their downloaded content as audio (_.mp3_) files.
\
Even though **_saveAsMp3_** is usually disabled by default, within that group **_saveAsMp3_** will act as though it is enabled by default, even when that setting is not explicitly configured for a specific child configuration.
\
To disable **_saveAsMp3_** for a specific child, that child's configuration would have to explicitly disable **_saveAsMp3_**.

The **_active_** setting will behave slightly differently; instead of the standard inheritance described above, it will work in an _all-or-nothing_ manner.
\
For example, if a child is _active_, but its parent group is _inactive_, the child will not be processed. This will also be the case if _any_ of its enclosing groups are _inactive_.

If a group specifies an **_outputFolder_**, then its children may define their **_outputFolder_** and **_playlistFile_** relative to it using `~` to denote the relative path.

For example, if the parent **_outputFolder_** is configured as `"Youtube/Videos"`, then:

- Defining the child **_outputFolder_** as `"~/Favorites"` would be interpreted as `"Youtube/Videos/Favorites"`
- Defining the child **_playlistFile_** as `"~ - Favorites.m3u"` would be interpreted as `"Youtube/Videos - Favorites.m3u"`

### Channel Group Inheritance Configuration (Sample):

```json
{
    "key": "MY_GROUP",
    "active": true,
    "outputFolder": "Youtube",
    "saveAsMp3": false,
    "reversePlaylist": true,
    "keepClean": false,
    "channels": [
        {
            "key": "FAVORITE",
            "active": true,
            "url": "https://www.youtube.com/c/MyFavoriteChannel",
            "outputFolder": "~/Favorite",
            "channels": [
                {
                    "key": "FAVORITE_VIDEOS",
                    "name": "FavoriteVideos",
                    "playlistId": "PLUja9k1MyFavoriteVideos65Py_guZ3R",
                    "outputFolder": "~ Videos",
                    "playlistFile": "~/Favorite Videos.m3u",
                    "keepClean": true
                },
                {
                    "key": "FAVORITE_SONGS",
                    "active": false,
                    "name": "FavoriteSongs",
                    "playlistId": "PLUja9k1MyFavoriteSongs865Py_guZ3R",
                    "outputFolder": "~ Songs",
                    "playlistFile": "~/Favorite Songs.m3u",
                    "saveAsMp3": true
                }
            ]
        }
    ]
}
```

This would be equivalent to:

```json
{
    "key": "FAVORITE_VIDEOS",
    "active": true,
    "name": "FavoriteVideos",
    "group": "FAVORITE",
    "url": "https://www.youtube.com/c/MyFavoriteChannel",
    "playlistId": "PLUja9k1MyFavoriteVideos65Py_guZ3R",
    "outputFolder": "Youtube/Favorite Videos",
    "playlistFile": "Youtube/Favorite/Favorite Videos.m3u",
    "saveAsMp3": false,
    "reversePlaylist": true,
    "keepClean": true
},
{
    "key": "FAVORITE_SONGS",
    "active": false,
    "name": "FavoriteSongs",
    "group": "FAVORITE",
    "url": "https://www.youtube.com/c/MyFavoriteChannel",
    "playlistId": "PLUja9k1MyFavoriteSongs865Py_guZ3R",
    "outputFolder": "Youtube/Favorite Songs",
    "playlistFile": "Youtube/Favorite/Favorite Songs.m3u",
    "saveAsMp3": true,
    "reversePlaylist": true,
    "keepClean": false
}
```


## Special Channel Processing

There are certain instances where you may want to perform some additional processing on the Channel to filter which videos are downloaded, to rename the downloaded videos to something that you prefer, etc. This functionality is possible granted that you know a bit of Java.

There are two methods in the file `src/youtube/channel/process/ChannelProcesses.java`; **performSpecialPreConditions()** and **performSpecialPostConditions()**.

- **_performSpecialPreConditions()_**
  - Executed before the download queue is produced for the Channel
  - Used to rename the videos that will be downloaded

+ **_performSpecialPostConditions()_**
  + Executed after the download queue is produced for the Channel
  + Used to filter out videos that should not be downloaded; do not use this method to rename videos

In both of these methods, all you have to do is add another case for your Channel **_key_** to the switch statement.
\
You may also add whatever logic you like to the body of the method, or even call your own custom methods or classes, for special use cases.

These methods have no return value; any changes that you want to make should be achieved by modifying the following accessible objects:

| **OBJECT**              | **DESCRIPTION**                                                                                                     |
|:------------------------|:--------------------------------------------------------------------------------------------------------------------|
|                         |                                                                                                                     |
| `channel`               | The Channel object containing the details of your Channel.                                                          |
| `videoMap`              | A map of Video objects, containing the name, output file, etc. of all videos for the Channel; indexed by video ids. |
| `channel.state.queued`  | A list of video ids that are queued for download by the Channel.                                                    |
| `channel.state.saved`   | A list of video ids that have already been downloaded by the Channel and are currently on your hard drive.          |
| `channel.state.blocked` | A list of video ids that have not already been downloaded, but are also not queued for download.                    |
|                         |                                                                                                                     |

You can modify the video map and Channel state lists using your own custom logic


There is also a collection of macro methods provided that you can call to perform certain actions.
\
These macro methods are defined in the files `src/youtube/process/macro/RenameProcess.java` and `src/youtube/process/macro/FilterProcess.java`.
\
All of these methods will take the `videoMap` as their first argument, and the macro action will be performed on each Video in the video map.

There are many examples of these processes in the file `src/youtube/channel/process/ChannelProcesses_Sample.java` that you can use as a reference.
\
The file `src/youtube/channel/process/ChannelProcesses_Sample.java` contains the processes for my personal Channel configuration; if you copied some of my Channel configurations from `channels-sample.json` then you may also wish to copy the corresponding processes.


&nbsp;

----


# Configuration

All of the configuration settings for the project are stored in the file `conf.json`.


## Configuration Overview

### Configuration Sections:

| **NAME**                       | **DESCRIPTION**                                    | **DETAILS**                                                                               |                          **SAMPLE**                          |
|:-------------------------------|:---------------------------------------------------|:------------------------------------------------------------------------------------------|:------------------------------------------------------------:|
|                                |                                                    |                                                                                           |                                                              |
| **_YoutubeChannelDownloader_** | _Youtube Channel Downloader_ project configuration | [Configuring the Youtube Channel Downloader](#configuring-the-youtube-channel-downloader) | [_sample_](#youtube-channel-downloader-configuration-sample) |
| **_YoutubeDownloader_**        | _Youtube Downloader_ project configuration         | [Configuring the Youtube Downloader](#configuring-the-youtube-downloader)                 |     [_sample_](#youtube-downloader-configuration-sample)     |
| **_sponsorBlock_**             | Global SponsorBlock configuration _(optional)_     | [SponsorBlock Configuration](#sponsorblock-configuration)                                 |        [_sample_](#sponsorblock-configuration-sample)        |
| **_color_**                    | Global color configuration _(optional)_            | [Color Configuration](#color-configuration)                                               |           [_sample_](#color-configuration-sample)            |
| **_log_**                      | Global logging configuration _(optional)_          | [Logging Configuration](#logging-configuration)                                           |          [_sample_](#logging-configuration-sample)           |
|                                |                                                    |                                                                                           |                                                              |

### Configuration (Sample):

```json
{
    "YoutubeChannelDownloader": {
        ...
    },
    
    "YoutubeDownloader": {
        ...
    },
    
    "sponsorBlock": {
        ...
    },
    
    "color": {
        ...
    },
    
    "log": {
        ...
    }
}
```


## SponsorBlock Configuration

You can set up [_SponsorBlock_](https://sponsor.ajay.app/) by changing the settings inside the file `conf.json` or `channels.json`.
\
SponsorBlock is able to automatically cut out unwanted segments from the content you download. _(sponsored sections, self promotions, interaction reminders, etc.)_
\
_To use SponsorBlock, you must have [**FFmpeg**](https://ffmpeg.org/) installed and accessible on the path._
\
_This is only available when using **_yt-dlp_** as the **_executable_**._

SponsorBlock is capable of distinguishing and skipping several types of segments which can be enabled independently.
\
Details about these segments and what each covers can be found on the [_Category Breakdown_](https://wiki.sponsor.ajay.app/w/Guidelines#Category_Breakdown) page of the [_SponsorBlock Wiki_](https://wiki.sponsor.ajay.app/w/Main_Page).

You can set up a global SponsorBlock configuration for the entire project, or for the _Youtube Downloader_ or _Youtube Channel Downloader_ programs individually.
\
You can also have custom SponsorBlock configurations for individual Channel Groups or Channels.

To configure SponsorBlock, find or create a `"sponsorBlock"` [json configuration](#sponsorblock-configuration-sample) and customize the [settings](#sponsorblock-settings) it contains.

- A configuration in `conf.json` will function as a global configuration for the entire project.
- A configuration in `conf.json` inside the [Youtube Downloader Configuration](#configuring-the-youtube-downloader) or [Youtube Channel Downloader Configuration](#configuring-the-youtube-channel-downloader) will affect all content downloaded by the respective program.
- A configuration in `channels.json` inside a [Channel Configuration](#adding-channels) will affect only that Channel, and one inside a [Channel Group Configuration](#grouping-channels) will affect all Channels and Channel Groups which are children of the group.

### SponsorBlock Settings:

| **SETTING**             | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
|:------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **_enabled_**           | When this setting is enabled, the SponsorBlock configuration is _active_; meaning downloaded content will be post-processed and cleaned according to the provided settings. <br/> When this setting is disabled, the SponsorBlock configuration is _inactive_; meaning downloaded content will not be post-processed. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                                                |
| &nbsp;                  | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **_forceGlobally_**     | When this setting is enabled, this SponsorBlock configuration will be used in place of a specific lower-level configuration. <br/> In that case, this configuration will be used even if the lower-level configuration is _active_, provided that this configuration is _active_ and the lower-level configuration does not have **_overrideGlobal_** enabled. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_ <br/> _**Note:** This setting is only valid for global SponsorBlock configurations._ |
| **_overrideGlobal_**    | When this setting is enabled, this SponsorBlock configuration will be used regardless of the global configuration. <br/> When this configuration has **_overrideGlobal_** enabled and the global configuration has **_forceGlobally_** enabled, **_overrideGlobal_** will take precedence. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_ <br/> _**Note:** This setting is only valid for non-global SponsorBlock configurations._                                                                 |
| &nbsp;                  | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| **_skipAll_**           | When this setting is enabled, all segments recognized by SponsorBlock will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                                                     |
| **_skipSponsor_**       | When this setting is enabled, [_Sponsor_](https://wiki.sponsor.ajay.app/w/Sponsor) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                               |
| **_skipIntro_**         | When this setting is enabled, [_Intro_](https://wiki.sponsor.ajay.app/w/Intermission/Intro_Animation) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                            |
| **_skipOutro_**         | When this setting is enabled, [_Outro_](https://wiki.sponsor.ajay.app/w/Endcards/Credits) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                        |
| **_skipSelfPromo_**     | When this setting is enabled, [_Self-Promo_](https://wiki.sponsor.ajay.app/w/Unpaid/Self_Promotion) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                              |
| **_skipPreview_**       | When this setting is enabled, [_Preview_](https://wiki.sponsor.ajay.app/w/Preview/Recap) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                                         |
| **_skipInteraction_**   | When this setting is enabled, [_Interaction_](https://wiki.sponsor.ajay.app/w/Interaction_Reminder_(Subscribe)) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                  |
| **_skipMusicOffTopic_** | When this setting is enabled, [_Music Off-Topic_](https://wiki.sponsor.ajay.app/w/Music:_Non-Music_Section) segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_                                                                                                                                                                                                                                                                                      |
|                         |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |

### SponsorBlock Configuration (Sample):

```json
"sponsorBlock": {
    "enabled": true,
    "forceGlobally": false,
    "overrideGlobal": false,
    "skipAll": false,
    "skipSponsor": false,
    "skipIntro": false,
    "skipOutro": false,
    "skipSelfPromo": false,
    "skipPreview": false,
    "skipInteraction": false,
    "skipMusicOffTopic": false
}
```


## Color Configuration

You can customize the colors used by the project when printing output to the terminal by changing the settings inside the file `conf.json`.
\
Changing these settings will affect the colors for both the _Youtube Downloader_ and the _Youtube Channel Downloader_.

These colors will only work if your terminal supports _ANSI SGR codes_.
\
The project supports this list of [Supported Colors](#supported-colors).

To configure the colors, find or create the `"color"` [json configuration](#color-configuration-sample) and customize the [settings](#color-settings) it contains.

### Supported Colors:

| **OPTION**     |                          **COLOR**                           |  **HEX**  |      **RGB**      | **ANSI**  |
|:---------------|:------------------------------------------------------------:|:---------:|:-----------------:|:---------:|
|                |                                                              |           |                   |           |
| **WHITE**      | ![#ffffff](https://via.placeholder.com/15/ffffff/ffffff.png) | _#FFFFFF_ | _(255, 255, 255)_ | _Esc[97m_ |
| **GREY**       | ![#808080](https://via.placeholder.com/15/808080/808080.png) | _#808080_ | _(128, 128, 128)_ | _Esc[37m_ |
| **DARK_GREY**  | ![#595959](https://via.placeholder.com/15/595959/595959.png) | _#595959_ |  _(89, 89, 89)_   | _Esc[90m_ |
| **BLACK**      | ![#000000](https://via.placeholder.com/15/000000/000000.png) | _#000000_ |    _(0, 0, 0)_    | _Esc[30m_ |
| **DARK_RED**   | ![#f0524f](https://via.placeholder.com/15/f0524f/f0524f.png) | _#F0524F_ |  _(240, 82, 79)_  | _Esc[31m_ |
| **RED**        | ![#ff4050](https://via.placeholder.com/15/ff4050/ff4050.png) | _#FF4050_ |  _(255, 64, 80)_  | _Esc[91m_ |
| **ORANGE**     | ![#a68a0d](https://via.placeholder.com/15/a68a0d/a68a0d.png) | _#A68A0D_ | _(166, 138, 13)_  | _Esc[33m_ |
| **YELLOW**     | ![#e5bf00](https://via.placeholder.com/15/e5bf00/e5bf00.png) | _#E5BF00_ |  _(229, 191, 0)_  | _Esc[93m_ |
| **DARK_GREEN** | ![#5c962c](https://via.placeholder.com/15/5c962c/5c962c.png) | _#5C962C_ |  _(92, 150, 44)_  | _Esc[32m_ |
| **GREEN**      | ![#4fc414](https://via.placeholder.com/15/4fc414/4fc414.png) | _#4FC414_ |  _(79, 196, 20)_  | _Esc[92m_ |
| **TEAL**       | ![#00a3a3](https://via.placeholder.com/15/00a3a3/00a3a3.png) | _#00A3A3_ |  _(0, 163, 163)_  | _Esc[36m_ |
| **CYAN**       | ![#00e5e5](https://via.placeholder.com/15/00e5e5/00e5e5.png) | _#00E5E5_ |  _(0, 229, 229)_  | _Esc[96m_ |
| **DARK_BLUE**  | ![#3993d4](https://via.placeholder.com/15/3993d4/3993d4.png) | _#3993D4_ | _(57, 147, 212)_  | _Esc[34m_ |
| **BLUE**       | ![#1fb0ff](https://via.placeholder.com/15/1fb0ff/1fb0ff.png) | _#1FB0FF_ | _(31, 176, 255)_  | _Esc[94m_ |
| **PURPLE**     | ![#a771bf](https://via.placeholder.com/15/a771bf/a771bf.png) | _#A771BF_ | _(167, 113, 191)_ | _Esc[35m_ |
| **MAGENTA**    | ![#ed7eed](https://via.placeholder.com/15/ed7eed/ed7eed.png) | _#ED7EED_ | _(237, 126, 237)_ | _Esc[95m_ |
| **DEFAULT**    |                            &nbsp;                            | _varies_  |     _varies_      | _varies_  |
|                |                                                              |           |                   |           |

### Color Settings:

| **SETTING**            | **DESCRIPTION**                                                                                                                                                                                                                                                                             |
|:-----------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                        |                                                                                                                                                                                                                                                                                             |
| **_enableColors_**     | When this setting is enabled, colors will be used when printing output to the terminal. <br/> When this setting is disabled, all output will be printed in the default terminal color. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_ |
| &nbsp;                 | &nbsp;                                                                                                                                                                                                                                                                                      |
| **_base_**             | The base color to print in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"GREEN"` by default)_                                                                                                                           |
| **_good_**             | The color to print _"good" text_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"CYAN"` by default)_                                                                                                                   |
| **_bad_**              | The color to print _"bad" text_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"RED"` by default)_                                                                                                                     |
| **_log_**              | The color to print _logs_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"DARK_GREY"` by default)_                                                                                                                     |
| **_channel_**          | The color to print _Channel names_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"YELLOW"` by default)_                                                                                                               |
| **_video_**            | The color to print _Video titles_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"PURPLE"` by default)_                                                                                                                |
| **_number_**           | The color to print _numbers_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"WHITE"` by default)_                                                                                                                      |
| **_file_**             | The color to print _file names_ and _file paths_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"GREY"` by default)_                                                                                                   |
| **_exe_**              | The color to print the _name_ of the **_executable_** in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"ORANGE"` by default)_                                                                                            |
| **_link_**             | The color to print _links_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"TEAL"` by default)_                                                                                                                         |
| &nbsp;                 | &nbsp;                                                                                                                                                                                                                                                                                      |
| **_progressBar_**      | **The progress bar color settings.** _(optional)_                                                                                                                                                                                                                                           |
| **_progressBar.base_** | The base color to print progress bars in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"GREEN"` by default)_                                                                                                             |
| **_progressBar.good_** | The color to print the _"good" text_ of progress bars in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"CYAN"` by default)_                                                                                              |
| **_progressBar.bad_**  | The color to print the _"bad" text_ of progress bars in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(optional; `"RED"` by default)_                                                                                                |
|                        |                                                                                                                                                                                                                                                                                             |

### Color Configuration (Sample):

```json
"color": {
    "enableColors": true,
    "base": "GREEN",
    "good": "CYAN",
    "bad": "RED",
    "log": "DARK_GREY",
    "channel": "YELLOW",
    "video": "PURPLE",
    "number": "WHITE",
    "file": "GREY",
    "exe": "ORANGE",
    "link": "TEAL",
    "progressBar": {
        "base": "GREEN",
        "good": "CYAN",
        "bad": "RED"
    }
}
```


## Logging Configuration

You can customize what is printed to the terminal when running the project by changing the settings inside the file `conf.json`.
\
Changing these settings will affect the logging for both the _Youtube Downloader_ and the _Youtube Channel Downloader_.

To configure the logging, find or create the `"log"` [json configuration](#logging-configuration-sample) and customize the [settings](#logging-settings) it contains.

### Logging Settings:

| **SETTING**           | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                         |
|:----------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                       |                                                                                                                                                                                                                                                                                                                                         |
| **_printExeVersion_** | When this setting is enabled, the version of the **_executable_** will be printed to the terminal at the beginning of the run. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                     |
| **_logCommand_**      | When this setting is enabled, the commands sent to the **_executable_** to download each video will be printed to the terminal. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                                    |
| **_logWork_**         | When this setting is enabled, the work being done by the **_executable_** while downloading each video will be printed to the terminal. <br/> When this setting is enabled, **_showProgressBar_** will be automatically disabled. <br/> The acceptable values for this setting are `true` or `false`. _(optional; disabled by default)_ |
| **_showProgressBar_** | When this setting is enabled, and when **_logWork_** is disabled, a progress bar will be shown in the terminal while downloading each video. <br/> The acceptable values for this setting are `true` or `false`. _(optional; enabled by default)_                                                                                       |
|                       |                                                                                                                                                                                                                                                                                                                                         |

### Logging Configuration (Sample):

```json
"log": {
    "printExeVersion": true,
    "logCommand": true,
    "logWork": false,
    "showProgressBar": true
}
```


&nbsp;

----


~~ _This project is for educational and testing purposes only and is not intended to be used in any way that would violate the Youtube ToS or to download copyrighted material_ ~~


----
