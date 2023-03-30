
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

To get started, you will need to download the [Project](#downloading-the-project), and install the required [Dependencies](#installing-dependencies).


## Downloading the Project

Simply [<u>**_download_**</u>](https://github.com/ZGorlock/YoutubeDownloader/archive/refs/heads/master.zip "https://github.com/ZGorlock/YoutubeDownloader/archive/refs/heads/master.zip") the latest project files from [<u>**Github**</u>](https://github.com/ZGorlock/YoutubeDownloader "https://github.com/ZGorlock/YoutubeDownloader").
\
Then extract the files to a location of your choice.

Alternatively you can clone the repository with [**_Git_**](#_dep_git) using:

```shell
git clone "https://github.com/ZGorlock/YoutubeDownloader.git"
```


## Installing Dependencies

To run this project you will need to have the following dependencies installed:

|               **Dependency**               | **Minimum Version** |                                               **Website**                                                |                                                                                                                                           **Windows**                                                                                                                                            |                                                                                                                                              **Linux**                                                                                                                                              |
|:------------------------------------------:|:-------------------:|:--------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|                                            |                     |                                                                                                          |                                                                                                                                                                                                                                                                                                  |                                                                                                                                                                                                                                                                                                     |
| <span id="_dep_java">   **Java**   </span> |      `13.0.2`       |           [<u>jdk.java.net</u>](https://jdk.java.net/archive/ "https://jdk.java.net/archive/")           | [<u>**_openjdk-13.0.2_windows-x64_bin.zip_**</u>](https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_windows-x64_bin.zip "https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_windows-x64_bin.zip") | [<u>**_openjdk-13.0.2_linux-x64_bin.tar.gz_**</u>](https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz "https://download.java.net/java/GA/jdk13.0.2/d4173c853231432d94f001e99d882ca7/8/GPL/openjdk-13.0.2_linux-x64_bin.tar.gz") |
| <span id="_dep_maven">  **Maven**  </span> |       `3.8.7`       | [<u>maven.apache.org</u>](https://maven.apache.org/download.cgi "https://maven.apache.org/download.cgi") |                                          [<u>**_apache-maven-3.8.7-bin.zip_**</u>](https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.zip "https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.zip")                                          |                                       [<u>**_apache-maven-3.8.7-bin.tar.gz_**</u>](https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.tar.gz "https://dlcdn.apache.org/maven/maven-3/3.8.7/binaries/apache-maven-3.8.7-bin.tar.gz")                                       |
| <span id="_dep_ffmpeg"> **FFmpeg** </span> |    _recommended_    |         [<u>ffmpeg.org</u>](https://ffmpeg.org/download.html "https://ffmpeg.org/download.html")         |                   [<u>**_ffmpeg-master-latest-win64-gpl.zip_**</u>](https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-win64-gpl.zip")                   |             [<u>**_ffmpeg-master-latest-linux64-gpl.tar.xz_**</u>](https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz "https://github.com/yt-dlp/FFmpeg-Builds/releases/download/latest/ffmpeg-master-latest-linux64-gpl.tar.xz")             |
| <span id="_dep_git">    **Git**    </span> |     _optional_      |           [<u>git-scm.com</u>](https://git-scm.com/downloads "https://git-scm.com/downloads")            |                            [<u>**_Git-2.40.0-64-bit.exe_**</u>](https://github.com/git-for-windows/git/releases/download/v2.40.0.windows.1/Git-2.40.0-64-bit.exe "https://github.com/git-for-windows/git/releases/download/v2.40.0.windows.1/Git-2.40.0-64-bit.exe")                             |                                                                   [<u>**_git-2.40.0.tar.gz_**</u>](https://www.kernel.org/pub/software/scm/git/git-2.40.0.tar.gz "https://www.kernel.org/pub/software/scm/git/git-2.40.0.tar.gz")                                                                   |
|                                            |                     |                                                                                                          |                                                                                                                                                                                                                                                                                                  |                                                                                                                                                                                                                                                                                                     |

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
ffmpeg -version
```

```shell
git --version
```


&nbsp;

----


# Usage

This project contains two programs:

- The [Youtube Downloader](#the-youtube-downloader)
- The [Youtube Channel Downloader](#the-youtube-channel-downloader)


## Running the Project

The project can be run by using the scripts provided in the project directory:

- [ ] [`YoutubeDownloader.bat`](./YoutubeDownloader.bat)
- [ ] [`YoutubeChannelDownloader.bat`](./YoutubeChannelDownloader.bat)
- [ ] [`YoutubeDownloader.sh`](./YoutubeDownloader.sh)
- [ ] [`YoutubeChannelDownloader.sh`](./YoutubeChannelDownloader.sh)

\
Depending on your operating system use either the _.bat_ scripts (Windows) or the _.sh_ scripts (Linux):


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
Alternatively you can run the project with [**_Maven_**](#_dep_maven) using:

```shell
mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeDownloader"
```

```shell
mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeChannelDownloader"
```

\
Also, if you prefer, you could always run or debug the project in an IDE.


## Executable Options

You can configure this project to use either [<u>**yt-dlp**</u>](https://github.com/yt-dlp/yt-dlp/ "https://github.com/yt-dlp/yt-dlp/") or [<u>**youtube-dl**</u>](https://youtube-dl.org/ "https://youtube-dl.org/").

- <span id="_exe_yt_dlp">**_yt-dlp_**</span>
  - A newer drop-in replacement of the Youtube Downloader executable
  - _Actively maintained by new developers_
  - _Includes additional features_

+ <span id="_exe_youtube_dl">**_youtube-dl_**</span>
  + The original Youtube Downloader executable
  + _As of the time of writing this it appears it may no longer be maintained_
  + _Throttling and failures often occur when downloading videos_

\
Specify your choice of executable in the project configuration as explained under:

- [Configuring the Youtube Downloader](#configuring-the-youtube-downloader)
- [Configuring the Youtube Channel Downloader](#configuring-the-youtube-channel-downloader)

The project will automatically download the executable that you choose.
\
At the beginning of each run it will check for updates to the executable and download the latest version if needed.


## Updating the Project

This project should work as it does now indefinitely, as long as [**_yt-dlp_**](#_exe_yt_dlp) and [**_youtube-dl_**](#_exe_youtube_dl) continue to exist on the same websites they do now.
\
However, additional improvements and features may be added from time to time.

If you wish to receive these updates then you can [<u>**_download_**</u>](https://github.com/ZGorlock/YoutubeDownloader/archive/refs/heads/master.zip "https://github.com/ZGorlock/YoutubeDownloader/archive/refs/heads/master.zip") the latest project files from [<u>**Github**</u>](https://github.com/ZGorlock/YoutubeDownloader "https://github.com/ZGorlock/YoutubeDownloader").
\
_More information about the download process is provided under:_ [Downloading the Project](#downloading-the-project).

If you cloned the repository with [**_Git_**](#_dep_git) then you can update to the latest code using:

```shell
git pull origin master
```

\
⚠️**WARNING**: Before updating the project, make sure you backup your configuration and data files:

- [ ] [`conf.json`](./conf.json)
- [ ] [`channels.json`](./channels.json)
- [ ] [`src/youtube/channel/process/ChannelProcesses.java`](./src/youtube/channel/process/ChannelProcesses.java)
- [ ] `data/*`

⚠️Updating may cause merge conflicts and could potentially overwrite your custom changes.


&nbsp;

----


# The Youtube Downloader

This program is just a simple Youtube video downloader.
\
It will download the videos from the Youtube urls entered into the console while the program is running.

Videos will be saved in a `~/Youtube` folder in your user directory.
\
This can be customized by defining [_location.outputDir_](#_yd_location_outputDir) in the [Youtube Downloader Configuration](#configuring-the-youtube-downloader).

You can also create a list of Youtube urls in the file [`data/downloadQueue.txt`](./data/downloadQueue.txt) and when the program is executed it will download the provided list of videos.

The program will continue to run until an empty line in entered: `Enter`.


## Configuring the Youtube Downloader

You can customize the operation of the _Youtube Downloader_ by changing the settings inside the file [`conf.json`](./conf.json).

To configure the _Youtube Downloader_, find or create the `"YoutubeDownloader"` [**json configuration**](#youtube-downloader-configuration-sample) and customize the [Settings](#youtube-downloader-settings) it contains.

### Youtube Downloader Settings:

| **SETTING**                                                                                     | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|:------------------------------------------------------------------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                                                                 |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| <span id="_yd_executable">                       **_executable_**                       </span> | The name of the executable which will be used to download content from Youtube. <br/> The two valid options for this setting are `"yt-dlp"` and `"youtube-dl"`. <br/> _More information about these executables is provided under:_ [Executable Options](#executable-options).                                                                                                                                                                                                                                                                                                                                                                   |
| &nbsp;                                                                                          | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_location">                         **_location_**                         </span> | **The location settings for the _Youtube Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| <span id="_yd_location_outputDir">               **_location.outputDir_**               </span> | The path of the directory where downloaded content will be saved. For example: `"C:/Users/User/Downloads"`. <br/> By leaving this blank (`""` _or_ `null`), content will be saved in a `~/Youtube` folder in your user directory.                                                                                                                                                                                                                                                                                                                                                                                                                |
| <span id="_yd_location_browser">                 **_location.browser_**                 </span> | The name of the browser that you use locally to watch Youtube. <br/> This is the browser that cookies will be used from when attempting to retry certain failed downloads, assuming that [_flag.neverUseBrowserCookies_](#_yd_flag_neverUseBrowserCookies) is disabled. <br/> The acceptable values for this setting are `"Brave"`, `"Chrome"`, `"Chromium"`, `"Edge"`, `"Firefox"`, `"Opera"`, `"Safari"`, or `"Vivaldi"`.                                                                                                                                                                                                                      |
| &nbsp;                                                                                          | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_format">                           **_format_**                           </span> | **The format settings for the _Youtube Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_format_preMerged">                 **_format.preMerged_**                 </span> | When this setting is enabled, the _Youtube Downloader_ will download videos in the best pre-merged format. <br/> When this setting is disabled, and when [_executable_](#_yd_executable) is set to `"yt-dlp"`, the _Youtube Downloader_ will download videos in the best possible format, not just the best pre-merged format. <br/> When this setting is disabled, videos will not necessarily be downloaded in _.mp4_ format. <br/> When this setting is disabled, you must have [**_FFmpeg_**](#_dep_ffmpeg) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_ |
| <span id="_yd_format_asMp3">                     **_format.asMp3_**                     </span> | When this setting is enabled, the _Youtube Downloader_ will download content as audio files (_.mp3_) instead of video files (_.mp4_). <br/> When this setting is enabled, you must have [**_FFmpeg_**](#_dep_ffmpeg) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                           |
| &nbsp;                                                                                          | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_flag">                             **_flag_**                             </span> | **The flag settings for the _Youtube Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| <span id="_yd_flag_neverUseBrowserCookies">      **_flag.neverUseBrowserCookies_**      </span> | When the _Youtube Downloader_ fails to download an age-restricted video, before marking that video as _blocked_, it can attempt one more time using the local browser cookies. <br/> When this setting is disabled, and when [_location.browser_](#_yd_location_browser) is properly set, the previously described functionality will be active. <br/> When this setting is enabled, the _Youtube Downloader_ will never attempt to use local browser cookies. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                        |
| &nbsp;                                                                                          | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_flag_test">                        **_flag.test_**                        </span> | **The test flag settings for the _Youtube Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_yd_flag_test_safeMode">               **_flag.test.safeMode_**               </span> | When this setting is enabled, [_flag.test.preventDownload_](#_yd_flag_test_preventDownload), [_flag.test.preventVideoFetch_](#_yd_flag_test_preventVideoFetch), [_flag.test.preventExeAutoUpdate_](#_yd_flag_test_preventExeAutoUpdate), and [_flag.test.preventExeVersionCheck_](#_yd_flag_test_preventExeVersionCheck) will be enabled; overriding their individual values. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                 |
| <span id="_yd_flag_test_preventRun">             **_flag.test.preventRun_**             </span> | When this setting is enabled, the _Youtube Downloader_ will not attempt to run the main code; it will only initialize and then shutdown. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                      |
| <span id="_yd_flag_test_preventDownload">        **_flag.test.preventDownload_**        </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_yd_flag_test_safeMode) is enabled, the _Youtube Downloader_ will not attempt to download any videos. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_flag_test_preventVideoFetch">      **_flag.test.preventVideoFetch_**      </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_yd_flag_test_safeMode) is enabled, the _Youtube Downloader_ will not attempt to fetch the video information _(title, publish date, etc.)_ prior to downloading. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                       |
| <span id="_yd_flag_test_preventExeAutoUpdate">   **_flag.test.preventExeAutoUpdate_**   </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_yd_flag_test_safeMode) is enabled, the _Youtube Downloader_ will not attempt to download or automatically update the selected [_executable_](#_yd_executable). <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                        |
| <span id="_yd_flag_test_preventExeVersionCheck"> **_flag.test.preventExeVersionCheck_** </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_yd_flag_test_safeMode) is enabled, the _Youtube Downloader_ will not attempt to check the current or latest version of the selected [_executable_](#_yd_executable). <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                  |
| &nbsp;                                                                                          | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_yd_sponsorBlock">                     **_sponsorBlock_**                     </span> | **The program-level SponsorBlock configuration for the _Youtube Downloader_ :** <br/> _Instructions on how to define a SponsorBlock configuration can be found under:_ [SponsorBlock Configuration](#sponsorblock-configuration).                                                                                                                                                                                                                                                                                                                                                                                                                |
|                                                                                                 |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

### Youtube Downloader Configuration (Sample):

```json
"YoutubeDownloader": {
    "executable": "yt-dlp",
    "location": {
        "outputDir": "C:/Users/User/Downloads",
        "browser": "Chrome"
    },
    "format": {
        "preMerged": true,
        "asMp3": false
    },
    "flag": {
        "neverUseBrowserCookies": true,
        "test": {
            "safeMode": false,
            "preventRun": false,
            "preventDownload": false,
            "preventVideoFetch": false,
            "preventExeAutoUpdate": false,
            "preventExeVersionCheck": false
        }
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

- [ ] Go to: [<u>**Google Cloud - APIs & Services**</u>](https://console.cloud.google.com/projectselector2/apis/dashboard "https://console.cloud.google.com/projectselector2/apis/dashboard")
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
- [ ] Copy the **API Key** to the file [`apiKey`](./apiKey) in the project directory

### Recovering your API Key:

- [ ] Go to: [<u>**Google Cloud - APIs & Services**</u>](https://console.cloud.google.com/projectselector2/apis/dashboard "https://console.cloud.google.com/projectselector2/apis/dashboard")
- [ ] Click **_Select Project_**
  - [ ] Select `Youtube Downloader`
  - [ ] Click **_Open_**
- [ ] Click **_Credentials_**
  - [ ] Select your API key
  - [ ] Click **_Show Key_**
- [ ] Copy the **API Key** to the file [`apiKey`](./apiKey) in the project directory


## Configuring the Youtube Channel Downloader

You can customize the operation of the _Youtube Channel Downloader_ by changing the settings inside the file [`conf.json`](./conf.json).

To configure the _Youtube Channel Downloader_, find or create the `"YoutubeChannelDownloader"` [**json configuration**](#youtube-channel-downloader-configuration-sample) and customize the [Settings](#youtube-channel-downloader-settings) it contains.

### Youtube Channel Downloader Settings:

| **SETTING**                                                                                          | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|:-----------------------------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                                                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| <span id="_ycd_executable">                         **_executable_**                         </span> | The name of the executable which will be used to download content from Youtube. <br/> The two valid options for this setting are `"yt-dlp"` and `"youtube-dl"`. <br/> _More information about these executables is provided under:_ [Executable Options](#executable-options).                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_location">                           **_location_**                           </span> | **The location settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| <span id="_ycd_location_storageDrive">              **_location.storageDrive_**              </span> | The path of the drive where downloaded content will be saved. For example: `"C:/"`. <br/> You may leave this blank (`""` _or_ `null`) if you wish to specify the full path for each Channel in [`channels.json`](./channels.json).                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| <span id="_ycd_location_musicDir">                  **_location.musicDir_**                  </span> | The path of the directory where audio will be saved. For example: `"Users/User/Music/"`. <br/> This path is relative to [_location.storageDrive_](#_ycd_location_storageDrive). <br/> You may leave this blank (`""` _or_ `null`) if you wish to specify the full path for each Channel in [`channels.json`](./channels.json). <br/> If you leave this blank you must also leave [_location.storageDrive_](#_ycd_location_storageDrive) and [_location.videoDir_](#_ycd_location_videoDir) blank.                                                                                                                                                                                                                |
| <span id="_ycd_location_videoDir">                  **_location.videoDir_**                  </span> | The path of the directory where video will be saved. For example: `"Users/User/Videos/"`. <br/> This path is relative to [_location.storageDrive_](#_ycd_location_storageDrive). <br/> You may leave this blank (`""` _or_ `null`) if you wish to specify the full path for each Channel in [`channels.json`](./channels.json). <br/> If you leave this blank you must also leave [_location.storageDrive_](#_ycd_location_storageDrive) and [_location.musicDir_](#__ycd_location_musicDir) blank.                                                                                                                                                                                                              |
| <span id="_ycd_location_browser">                   **_location.browser_**                   </span> | The name of the browser that you use locally to watch Youtube. <br/> This is the browser that cookies will be used from when attempting to retry certain failed downloads, assuming that [_flag.neverUseBrowserCookies_](#_ycd_flag_neverUseBrowserCookies) is disabled. <br/> The acceptable values for this setting are `"Brave"`, `"Chrome"`, `"Chromium"`, `"Edge"`, `"Firefox"`, `"Opera"`, `"Safari"`, or `"Vivaldi"`.                                                                                                                                                                                                                                                                                     |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_format">                             **_format_**                             </span> | **The format settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| <span id="_ycd_format_preMerged">                   **_format.preMerged_**                   </span> | When this setting is enabled, the _Youtube Channel Downloader_ will download videos in the best pre-merged format. <br/> When this setting is disabled, and when [_executable_](#_ycd_executable) is set to `"yt-dlp"`, the _Youtube Channel Downloader_ will download videos in the best possible format, not just the best pre-merged format. <br/> When this setting is disabled, videos will not necessarily be downloaded in _.mp4_ format. <br/> When this setting is disabled, you must have [**_FFmpeg_**](#_dep_ffmpeg) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_filter">                             **_filter_**                             </span> | **The filter settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| <span id="_ycd_filter_enableFiltering">             **_filter.enableFiltering_**             </span> | When this setting is enabled, the specified [_filter_](#_ycd_filter) settings will used to be determine which Channels should be processed. <br/> When this setting is disabled, the [_filter_](#_ycd_filter) settings will be ignored and all Channels will be processed. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                                            |
| <span id="_ycd_filter_channel">                     **_filter.channel_**                     </span> | There may be times where you want to process only a single Channel and not all of them. <br/> Set this to `"<YOUR_CHANNEL_KEY>"` to process only that Channel. <br/> To return to processing all Channels, set this back to `null`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_ycd_filter_group">                       **_filter.group_**                       </span> | There may be times where you want to process only a single group and not all of them. <br/> Set this to `"<YOUR_CHANNEL_GROUP>"` to process only that group. <br/> To return to processing all Channels, set this back to `null`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| <span id="_ycd_filter_startAt">                     **_filter.startAt_**                     </span> | There may be times where you want to start processing at a specified Channel, skipping all Channels before it. <br/> The values for this setting work the same as for [_filter.channel_](#_ycd_filter_channel). <br/> The Channels are processed in the order that they appear in your Channel configuration. <br/> To return to normal Channel processing, set this back to `null`.                                                                                                                                                                                                                                                                                                                             |
| <span id="_ycd_filter_stopAt">                      **_filter.stopAt_**                      </span> | There may be times where you want to stop processing at a specified Channel, skipping all Channels after it. <br/> The values for this setting work the same as for [_filter.channel_](#_ycd_filter_channel). <br/> The Channels are processed in the order that they appear in your Channel configuration. <br/> To return to normal Channel processing, set this back to `null`.                                                                                                                                                                                                                                                                                                                               |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_log">                                **_log_**                                </span> | **The log settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| <span id="_ycd_log_printStats">                     **_log.printStats_**                     </span> | Whether to print statistics to the console at the end of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_ycd_log_printChannels">                  **_log.printChannels_**                  </span> | Whether to print the Channel list to the console at the start of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_backup">                             **_backup_**                             </span> | **The backup settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| <span id="_ycd_backup_enableBackups">               **_backup.enableBackups_**               </span> | When this setting is enabled, the specified [_backup_](#_ycd_backup) settings will be used to create periodic snapshots of the project state. <br/> When this setting is disabled, the [_backup_](#_ycd_backup) settings will be ignored and no backups will be created. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                             |
| <span id="_ycd_backup_backupDir">                   **_backup.backupDir_**                   </span> | The path of the directory where backups will be saved. For example: `"C:/Users/User/Backups"`. <br/> By leaving this blank (`""` _or_ `null`), backups will be saved in the `backup/` folder in the project directory.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_backup_compressBackups">             **_backup.compressBackups_**             </span> | When this setting is enabled, backups that are created will be compressed to a zip archive (_.zip_). <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| <span id="_ycd_backup_daysBetweenBackups">          **_backup.daysBetweenBackups_**          </span> | The number of days in between backups. <br/> Once the most recent backup has become older than the value specified, a new backup will be created the next time the program runs. <br/> You may set this setting to `-1` to indicate that a backup should be created every time the program runs. <br/> The acceptable values for this setting are any positive integer, or `-1`. _(`7` by default)_                                                                                                                                                                                                                                                                                                              |
| <span id="_ycd_backup_daysToKeepBackups">           **_backup.daysToKeepBackups_**           </span> | The number of days to retain backups before deleting them. <br/> Once a backup has become older than the value specified, it will be deleted the next time the program runs. <br/> You may set this setting to `-1` to indicate that backups should never be automatically deleted. <br/> The acceptable values for this setting are any positive integer, or `-1`. _(`30` by default)_                                                                                                                                                                                                                                                                                                                          |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_backup_files">                       **_backup.files_**                       </span> | **The backup file inclusion settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| <span id="_ycd_backup_files_includeConfigs">        **_backup.files.includeConfigs_**        </span> | When this setting is enabled, _configuration_ files ([`conf.json`](./conf.json) _and_ [`channels.json`](./channels.json)) will be included in any backups that are created. <br/> When this setting is disabled, _configuration_ files will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                            |
| <span id="_ycd_backup_files_includeData">           **_backup.files.includeData_**           </span> | When this setting is enabled, _data_ files (`data/*`) will be included in any backups that are created. <br/> When this setting is disabled, _data_ files will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                         |
| <span id="_ycd_backup_files_includeLogs">           **_backup.files.includeLogs_**           </span> | When this setting is enabled, _log_ files (`log/*`) will be included in any backups that are created. <br/> When this setting is disabled, _log_ files will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                            |
| <span id="_ycd_backup_files_includeSourceCode">     **_backup.files.includeSourceCode_**     </span> | When this setting is enabled, _source code_ files (`src/*`) will be included in any backups that are created. <br/> When this setting is disabled, _source code_ files will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_backup_files_includeCompiledSource"> **_backup.files.includeCompiledSource_** </span> | When this setting is enabled, _compiled source code_ files (`bin/*`) will be included in any backups that are created. <br/> When this setting is disabled, _compiled source code_ files will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                         |
| <span id="_ycd_backup_files_includeApiKey">         **_backup.files.includeApiKey_**         </span> | When this setting is enabled, your _api key_ file ([`apiKey`](./apiKey)) will be included in any backups that are created. <br/> When this setting is disabled, your _api key_ file will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_ycd_backup_files_includeExecutable">     **_backup.files.includeExecutable_**     </span> | When this setting is enabled, the selected [_executable_](#_ycd_executable) file (_e.g._ `yt-dlp.exe`) will be included in any backups that are created. <br/> When this setting is disabled, the selected [_executable_](#_ycd_executable) file will be excluded from any backups that are created. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                 |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_flag">                               **_flag_**                               </span> | **The flag settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| <span id="_ycd_flag_deleteToRecyclingBin">          **_flag.deleteToRecyclingBin_**          </span> | When this setting is enabled, the _Youtube Channel Downloader_ will attempt to move files to the recycling bin instead of deleting them. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| <span id="_ycd_flag_neverUseBrowserCookies">        **_flag.neverUseBrowserCookies_**        </span> | When the _Youtube Channel Downloader_ fails to download an age-restricted video, before marking that video as _blocked_, it can attempt one more time using the local browser cookies. <br/> When this setting is disabled, and when [_location.browser_](#_ycd_location_browser) is properly set, the previously described functionality will be active. <br/> When this setting is enabled, the _Youtube Channel Downloader_ will never attempt to use local browser cookies. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                       |
| <span id="_ycd_flag_retryPreviousFailures">         **_flag.retryPreviousFailures_**         </span> | When the _Youtube Channel Downloader_ fails to download a video, either because of a connection issue or because a video is "not available in your country", etc., it will mark that video as _blocked_ and will not automatically attempt to download it again; however sometimes the download will succeed if reattempted. <br/> When this setting is enabled, all previously failed downloads from all Channels will be reattempted. <br/> This should only be enabled occasionally and disabled after the run. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                   |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_flag_test">                          **_flag.test_**                          </span> | **The test flag settings for the _Youtube Channel Downloader_ :**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| <span id="_ycd_flag_test_safeMode">                 **_flag.test.safeMode_**                 </span> | When this setting is enabled, [_flag.test.preventDownload_](#_ycd_flag_test_preventDownload), [_flag.test.preventDeletion_](#_ycd_flag_test_preventDeletion), [_flag.test.preventRenaming_](#_ycd_flag_test_preventRenaming), [_flag.test.preventPlaylistEdit_](#_ycd_flag_test_preventPlaylistEdit), [_flag.test.preventChannelFetch_](#_ycd_flag_test_preventChannelFetch), [_flag.test.preventExeAutoUpdate_](#_ycd_flag_test_preventExeAutoUpdate), and [_flag.test.preventExeVersionCheck_](#_ycd_flag_test_preventExeVersionCheck) will be enabled; overriding their individual values. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_ |
| <span id="_ycd_flag_test_preventRun">               **_flag.test.preventRun_**               </span> | When this setting is enabled, the _Youtube Channel Downloader_ will not attempt to run the main code; it will only initialize and then shutdown. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_ycd_flag_test_preventProcess">           **_flag.test.preventProcess_**           </span> | When this setting is enabled, the _Youtube Channel Downloader_ will not attempt to process any Channels. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| <span id="_ycd_flag_test_preventDownload">          **_flag.test.preventDownload_**          </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, the _Youtube Channel Downloader_ will not attempt to download any videos. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                  |
| <span id="_ycd_flag_test_preventDeletion">          **_flag.test.preventDeletion_**          </span> | When the _Youtube Channel Downloader_ detects that a video has been deleted off Youtube, from a Channel that has [_keepClean_](#_channel_keepClean) enabled, then it will also be deleted from your hard drive. <br/> When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, media deletion will be globally disabled; overriding the [_keepClean_](#_channel_keepClean) flag for all Channels. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                          |
| <span id="_ycd_flag_test_preventRenaming">          **_flag.test.preventRenaming_**          </span> | When the _Youtube Channel Downloader_ detects that a video has been renamed on Youtube, or when you modify your [Channel Processes](#special-channel-processing), then it will also be renamed on your hard drive. <br/> When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, media renaming will be globally disabled. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                |
| <span id="_ycd_flag_test_preventPlaylistEdit">      **_flag.test.preventPlaylistEdit_**      </span> | When the _Youtube Channel Downloader_ detects that a video has been added, removed, or renamed on Youtube, or when you modify your [Channel Processes](#special-channel-processing), for a Channel that has a [_playlistFile_](#__channel_playlistFile), then the playlist file will be modified to reflect the changes. <br/> When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, playlist modification will be globally disabled. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                   |
| <span id="_ycd_flag_test_preventChannelFetch">      **_flag.test.preventChannelFetch_**      </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, the _Youtube Channel Downloader_ will not attempt to fetch the latest data for Channels; this will result in the previously fetched data being used. _(for use in testing)_ <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                       |
| <span id="_ycd_flag_test_preventExeAutoUpdate">     **_flag.test.preventExeAutoUpdate_**     </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, the _Youtube Channel Downloader_ will not attempt to download or automatically update the selected [_executable_](#_ycd_executable). <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_ycd_flag_test_preventExeVersionCheck">   **_flag.test.preventExeVersionCheck_**   </span> | When this setting is enabled, or when [_flag.test.safeMode_](#_ycd_flag_test_safeMode) is enabled, the _Youtube Channel Downloader_ will not attempt to check the current or latest version of the selected [_executable_](#_ycd_executable). <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                        |
| &nbsp;                                                                                               | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| <span id="_ycd_sponsorBlock">                       **_sponsorBlock_**                       </span> | **The program-level SponsorBlock configuration for the _Youtube Channel Downloader_ :** <br/> _Instructions on how to define a SponsorBlock configuration can be found under:_ [SponsorBlock Configuration](#sponsorblock-configuration).                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                                                                                      |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |

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
        "enableFiltering": true,
        "channel": null,
        "group": null,
        "startAt": null,
        "stopAt": null
    },
    "log": {
        "printStats": true,
        "printChannels": false
    },
    "backup": {
        "enableBackups": false,
        "backupDir": null,
        "compressBackups": true,
        "daysBetweenBackups": 7,
        "daysToKeepBackups": 30,
        "files": {
            "includeConfigs": true,
            "includeData": true,
            "includeLogs": true,
            "includeSourceCode": false,
            "includeCompiledSource": false,
            "includeApiKey": false,
            "includeExecutable": false
        }
    },
    "flag": {
        "deleteToRecyclingBin": true,
        "neverUseBrowserCookies": true,
        "retryPreviousFailures": false,
        "test": {
            "safeMode": false,
            "preventRun": false,
            "preventProcess": false,
            "preventDownload": false,
            "preventDeletion": false,
            "preventRenaming": false,
            "preventPlaylistEdit": false,
            "preventChannelFetch": false,
            "preventExeAutoUpdate": false,
            "preventExeVersionCheck": false
        }
    }
}
```


## Adding Channels

You can specify the Youtube channels and playlists that you want to process by configuring Channels inside the file [`channels.json`](./channels.json).

There are many examples in [`channels-sample.json`](./channels-sample.json) that you can use as a reference.
\
The file [`channels-sample.json`](./channels-sample.json) is also my personal Channel configuration; if you see any Channels that you like you can copy them to your [`channels.json`](./channels.json).

To add a Channel, create a new Channel [**json configuration**](#channel-configuration-sample) and customize the [Settings](#channel-settings) it contains.

### Channel Settings:

| **SETTING**                                                                    | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|:-------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                                                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| <span id="_channel_key">                   **_key_**                   </span> | The key which uniquely identifies the Channel within your configuration. For example: `"MY_CHANNEL"`. <br/> This setting is required and is an identifier which must be unique; a [_key_](#_channel_key) used for a Channel must not be used by any other Channel in the configuration. <br/> As a best practice, only uppercase letters (`A-Z`), numbers (`0-9`), and underscores (`_`) should be used when defining a [_key_](#_channel_key). <br/> This setting should not contain spaces and should not be blank (`""` _or_ `null`).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| <span id="_channel_active">                **_active_**                </span> | When this setting is enabled, the Channel is _active_; meaning it will be processed when the _Youtube Channel Downloader_ runs and its data will be synced. <br/> When this setting is disabled, the Channel is _inactive_; meaning it will be ignored when the _Youtube Channel Downloader_ runs. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| <span id="_channel_name">                  **_name_**                  </span> | The human readable name of the Channel. For example: `"MyChannel"`. <br/> This setting is an identifier which must be unique; a [_name_](#_channel_name) used for a Channel must not be used by any other Channel in the configuration. <br/> If this setting is not provided, a [_name_](#_channel_name) will be automatically generated for the Channel based on the [_key_](#_channel_key). <br/> This setting should not contain spaces or special characters and should not be blank (`""` _or_ `null`).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| <span id="_channel_group">                 **_group_**                 </span> | The user-defined group or category for the Channel. For example: `"FAVORITES"`. <br/> You may also define multiple groups or categories by separating them with delimiters. For example: `"FAVORITES : VIDEOS : GENERAL"`. <br/> As a best practice, only uppercase letters (`A-Z`), numbers (`0-9`), and underscores (`_`) should be used when defining a [_group_](#_channel_group). <br/> The following characters are valid for use as delimiters: (`.`, `,`, `;`, `:`, `+`, `&`). <br/>This setting should not contain spaces, except surrounding delimiters, and should not be blank (`""` _or_ `null`). <br/> _**Note:** It is now recommended to use Channel Groups instead; instructions on how to define Channel Groups can be found under:_ [Grouping Channels](#grouping-channels).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| &nbsp;                                                                         | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_channel_url">                   **_url_**                   </span> | The url of the Youtube playlist or channel. For example: `"https://www.youtube.com/c/MyFavoriteChannel"`. <br/> This setting is not used by the program, but it can be useful when returning to the playlist or channel in the future.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_channel_playlistId">            **_playlistId_**            </span> | The id of the Youtube playlist or channel. For example: `"UU65aMyFavoriteChannelXY"`. <br/> _Instructions on how to obtain the playlist id can be found under:_ [Finding Youtube Playlist IDs](#finding-youtube-playlist-ids).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| &nbsp;                                                                         | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_channel_outputFolder">          **_outputFolder_**          </span> | The path of the output directory of the Channel where downloaded content will be saved. For example: `"Youtube/My Channel"`. <br/> If this Channel is being used to download audio _(based on [_saveAsMp3_](#_channel_saveAsMp3))_, then this path is relative to [_location.musicDir_](#_ycd_location_musicDir) defined in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader). <br/> If this Channel is being used to download video _(based on [_saveAsMp3_](#_channel_saveAsMp3))_, then this path is relative to [_location.videoDir_](#_ycd_location_videoDir) defined in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader). <br/> If you chose to leave the [_location_](#_ycd_location) settings blank in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader), or if [_ignoreGlobalLocations_](#_channel_ignoreGlobalLocations) is enabled, then this setting must specify the full path of the output directory. <br/> <br/> ⚠️**WARNING**: The output directory should be an empty directory initially! <br/> ⚠️**DO NOT** use the same directory for all your Channels and especially do not use directories where you already have other videos or data saved! <br/> ⚠️The _Youtube Channel Downloader_ is able to **delete files from this directory** in certain cases, so do not set this to a directory that contains existing files you do not want to lose! |
| <span id="_channel_playlistFile">          **_playlistFile_**          </span> | The path of the playlist file (_.m3u_) to be used to enumerate the downloaded content of the Channel. For example: `"Youtube/My Channel.m3u"`. <br/> This path is relative to the same base path as [_outputFolder_](#_channel_outputFolder), however it does not necessarily need to be in the same folder as [_outputFolder_](#_channel_outputFolder). <br/> If you chose to leave the [_location_](#_ycd_location) settings blank in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader), or if [_ignoreGlobalLocations_](#_channel_ignoreGlobalLocations) is enabled, then this setting must specify the full path of the playlist file. <br/> If this setting is not provided, but [_savePlaylist_](#_channel_savePlaylist) is enabled, the playlist will be saved in the default location described by [_savePlaylist_](#_channel_savePlaylist). <br/> If this setting is provided, but [_savePlaylist_](#_channel_savePlaylist) is disabled, [_savePlaylist_](#_channel_savePlaylist) will take precedence and the playlist file will not be created.                                                                                                                                                                                                                                                                                                                                                                                                        |
| &nbsp;                                                                         | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_channel_saveAsMp3">             **_saveAsMp3_**             </span> | When this setting is enabled, the content of the Channel will be downloaded as audio files (_.mp3_) instead of video files (_.mp4_). <br/> When this setting is enabled, you must have [**_FFmpeg_**](#_dep_ffmpeg) installed and accessible on the path. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| <span id="_channel_savePlaylist">          **_savePlaylist_**          </span> | When this setting is enabled, a playlist file (_.m3u_) will be created to enumerate the downloaded content of the Channel. <br/> When this setting is disabled, no playlist file will be created; this setting takes precedence over [_playlistFile_](#_channel_playlistFile). <br/> The playlist file, if it is created, will be placed at the location specified by [_playlistFile_](#_channel_playlistFile). <br/> If no [_playlistFile_](#_channel_playlistFile) is provided, then the playlist file will be placed in the parent folder of the [_outputFolder_](#_channel_outputFolder) and have the same name as the [_outputFolder_](#_channel_outputFolder). <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_channel_reversePlaylist">       **_reversePlaylist_**       </span> | When this setting is enabled, and when [_savePlaylist_](#_channel_savePlaylist) is enabled, the playlist will be saved in reverse order; newer content will be placed at the beginning of the playlist instead of the end. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_channel_ignoreGlobalLocations"> **_ignoreGlobalLocations_** </span> | When this setting is enabled, the [_location_](#_ycd_location) settings defined in the _Youtube Channel Downloader_ [Configuration](#configuring-the-youtube-channel-downloader) are disregarded for [_outputFolder_](#_channel_outputFolder) and [_playlistFile_](#_channel_playlistFile). <br/> When this setting is enabled, you must specify the full path for [_outputFolder_](#_channel_outputFolder) and [_playlistFile_](#_channel_playlistFile). <br/> When this setting is enabled, then [_outputFolder_](#_channel_outputFolder) and [_playlistFile_](#_channel_playlistFile) may contain `${D}`, `${V}`, or `${M}` which will translate to [_location.storageDrive_](#_ycd_location_storageDrive), [_location.videoDir_](#_ycd_location_videoDir), and [_location.musicDir_](#_ycd_location_musicDir) respectively. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| <span id="_channel_keepClean">             **_keepClean_**             </span> | When this setting is enabled, and when [_savePlaylist_](#_channel_savePlaylist) is enabled, the output directory of the Channel will be kept synchronized with the Youtube playlist or channel. <br/> If the output directory is synchronized then videos that are deleted off of Youtube will also be deleted locally. <br/> If the output directory is not synchronized then videos that are deleted off of Youtube will not be deleted locally, and will only be removed from the playlist file. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| &nbsp;                                                                         | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| <span id="_channel_sponsorBlock">          **_sponsorBlock_**          </span> | **The SponsorBlock configuration for the Channel :** <br/> _Instructions on how to define a SponsorBlock configuration can be found under:_ [SponsorBlock Configuration](#sponsorblock-configuration).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
|                                                                                |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |

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
- Again, make sure you set the [_outputFolder_](#_channel_outputFolder) for each Channel to an empty directory or a directory that does not exist yet
- These directories may have files deleted from them in certain circumstances
  - By default, files that are deleted in this way are not sent to the recycle bin and would be difficult, if not impossible, to recover
- Examples:
  - **BAD**: A directory that also contains your personal home videos
  - **BAD**: A directory that also contains important work documents
  - **BAD**: A directory that also contains videos that you have downloaded previously for this Channel before starting to use this program
  - **GOOD**: An empty directory or a directory that does not exist yet


## Finding Youtube Playlist IDs

When creating a [Channel Configuration](#adding-channels) you need to provide a setting called [_playlistId_](#_channel_playlistId).
\
This is what links your Channel with the actual channel or playlist on Youtube.

This _playlist id_ already exists and you just have to get it from Youtube and set it in your Channel.
\
The process of obtaining the _playlist id_ depends on whether your Channel represents a [Youtube Channel](#getting-the-playlist-id-of-a-youtube-channel) or a [Youtube Playlist](#getting-the-playlist-id-of-a-youtube-playlist).

### Getting the Playlist ID of a Youtube Channel:

- [ ] Go to the home page of the channel on [<u>**Youtube**</u>](https://www.youtube.com/ "https://www.youtube.com/")
  - [ ] Right click the blank area of the page
    - [ ] Select **View page source**
    - [ ] Search for: '`externalId`'
- [ ] Copy the _external id_ from the page source:
  - `···,"externalId":"UC65aMyFavoriteChannelXY",···`
  - `··················UC65aMyFavoriteChannelXY·····`
- [ ] Change the second letter in the _external id_ from '`C`' to '`U`' to get the _playlist id_
- [ ] Set the _playlist id_ as [_playlistId_](#_channel_playlistId) in your Channel

### Getting the Playlist ID of a Youtube Playlist:

- [ ] Go to the home page of the playlist on [<u>**Youtube**</u>](https://www.youtube.com/ "https://www.youtube.com/")
- [ ] Copy the _playlist id_ from the url:
  - `https://www.youtube.com/watch?v=L_MupB3z1g4&list=PLUja9k1MyFavoriteChannel5Py_guZ3R`
  - `·················································PLUja9k1MyFavoriteChannel5Py_guZ3R`
- [ ] Set the _playlist id_ as [_playlistId_](#_channel_playlistId) in your Channel


## Grouping Channels

After you have finished adding [Channel Configurations](#adding-channels), you have the option to group your Channels; making them easier to read, search, and filter.

You can specify the groups that you want by configuring Channel Groups inside the file [`channels.json`](./channels.json).

To add a Channel Group, create a new Channel Group [**json configuration**](#channel-group-configuration-sample) and customize the [Settings](#channel-group-settings) it contains.

### Channel Group Settings:

| **SETTING**                                        | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
|:---------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                    |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| <span id="_group_key">      **_key_**      </span> | The key which uniquely identifies the Channel Group within your configuration. For example: `"MY_GROUP"`. <br/> This setting is required and is an identifier which must be unique; a [_key_](#_group_key) used for a Channel Group must not be used by any other Channel Group in the configuration. <br/> As a best practice, only uppercase letters (`A-Z`), numbers (`0-9`), and underscores (`_`) should be used when defining a [_key_](#_group_key). <br/> This setting should not contain spaces and should not be blank (`""` _or_ `null`).                                                                                        |
| <span id="_group_channels"> **_channels_** </span> | The list of Channels and Channel Groups that are children of the Channel Group. <br/> This setting is an array and can contain any number of Channel [**json configurations**](#channel-configuration-sample) and Channel Group [**json configurations**](#channel-group-configuration-sample). <br/> <br/>  In the case of nested Channel Groups, a child will be considered a member of all of the enclosing Channel Groups. <br/> If the child is a Channel, it will also be considered a member of the group specified by its own [_group_](#_channel_group) setting, if provided. <br/> Any of these groups may be used for filtering. |
|                                                    |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | 

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

In addition to the required [_key_](#_group_key) and [_channels_](#_group_channels) settings, any [Setting](#channel-settings) available in a [Channel Configuration](#adding-channels) can also be configured in a Channel Group. _(excluding: [_playlistFile_](#_channel_playlistFile))_
\
When a setting is configured in this way, all children of that group will inherit the setting.

For example, if a Channel Group enables [_saveAsMp3_](#_channel_saveAsMp3), then all children of the group will save their downloaded content as audio (_.mp3_) files.
\
Even though [_saveAsMp3_](#_channel_saveAsMp3) is usually disabled by default, within that group [_saveAsMp3_](#_channel_saveAsMp3) will act as though it is enabled by default, even when that setting is not explicitly configured for a specific child configuration.
\
To disable [_saveAsMp3_](#_channel_saveAsMp3) for a specific child, that child's configuration would have to explicitly disable [_saveAsMp3_](#_channel_saveAsMp3).

The [_active_](#_channel_active) setting will behave slightly differently; instead of the standard inheritance described above, it will work in an _all-or-nothing_ manner.
\
For example, if a child is _active_, but its parent group is _inactive_, the child will not be processed. This will also be the case if _any_ of its enclosing groups are _inactive_.

If a group specifies an [_outputFolder_](#_channel_outputFolder), then its children may define their [_outputFolder_](#_channel_outputFolder) and [_playlistFile_](#_channel_playlistFile) relative to it using `~` to denote the relative path.

For example, if the parent [_outputFolder_](#_channel_outputFolder) is configured as `"Youtube/Videos"`, then:

- Defining the child [_outputFolder_](#_channel_outputFolder) as `"~/Favorites"` would be interpreted as `"Youtube/Videos/Favorites"`
- Defining the child [_playlistFile_](#_channel_playlistFile) as `"~ - Favorites.m3u"` would be interpreted as `"Youtube/Videos - Favorites.m3u"`

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

There are two methods in the file [`src/youtube/channel/process/ChannelProcesses.java`](./src/youtube/channel/process/ChannelProcesses.java); **performSpecialPreConditions()** and **performSpecialPostConditions()**.

- **_performSpecialPreConditions()_**
  - Executed before the download queue is produced for the Channel
  - Used to rename the videos that will be downloaded

+ **_performSpecialPostConditions()_**
  + Executed after the download queue is produced for the Channel
  + Used to filter out videos that should not be downloaded; do not use this method to rename videos

In both of these methods, all you have to do is add another case for your Channel [_key_](#_channel_key) to the switch statement.
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
These macro methods are defined in the files [`src/youtube/channel/process/macro/RenameProcess.java`](./src/youtube/channel/process/macro/RenameProcess.java) and [`src/youtube/channel/process/macro/FilterProcess.java`](./src/youtube/channel/process/macro/FilterProcess.java).
\
All of these methods will take the `videoMap` as their first argument, and the macro action will be performed on each Video in the video map.

There are many examples of these processes in the file [`src/youtube/channel/process/ChannelProcesses_Sample.java`](./src/youtube/channel/process/ChannelProcesses_Sample.java) that you can use as a reference.
\
The file [`src/youtube/channel/process/ChannelProcesses_Sample.java`](./src/youtube/channel/process/ChannelProcesses_Sample.java) contains the processes for my personal Channel configuration; if you copied some of my Channel configurations from [`channels-sample.json`](./channels-sample.json) then you may also wish to copy the corresponding processes.


&nbsp;

----


# Configuration

All of the configuration settings for the project are stored in the file [`conf.json`](./conf.json).


## Configuration Overview

### Configuration Sections:

| **NAME**                       | **DESCRIPTION**                                    | **DETAILS**                                                                               |                           **SAMPLE**                           |
|:-------------------------------|:---------------------------------------------------|:------------------------------------------------------------------------------------------|:--------------------------------------------------------------:|
|                                |                                                    |                                                                                           |                                                                |
| **_YoutubeChannelDownloader_** | _Youtube Channel Downloader_ project configuration | [Configuring the Youtube Channel Downloader](#configuring-the-youtube-channel-downloader) | [**sample**](#youtube-channel-downloader-configuration-sample) |
| **_YoutubeDownloader_**        | _Youtube Downloader_ project configuration         | [Configuring the Youtube Downloader](#configuring-the-youtube-downloader)                 |     [**sample**](#youtube-downloader-configuration-sample)     |
| **_sponsorBlock_**             | Global SponsorBlock configuration                  | [SponsorBlock Configuration](#sponsorblock-configuration)                                 |        [**sample**](#sponsorblock-configuration-sample)        |
| **_color_**                    | Global color configuration                         | [Color Configuration](#color-configuration)                                               |           [**sample**](#color-configuration-sample)            |
| **_log_**                      | Global logging configuration                       | [Logging Configuration](#logging-configuration)                                           |          [**sample**](#logging-configuration-sample)           |
|                                |                                                    |                                                                                           |                                                                |

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

You can set up [<u>**SponsorBlock**</u>](https://sponsor.ajay.app/ "https://sponsor.ajay.app/") by changing the settings inside the file [`conf.json`](./conf.json) or [`channels.json`](./channels.json).
\
SponsorBlock is able to automatically cut out unwanted segments from the content you download. _(sponsored sections, self promotions, interaction reminders, etc.)_
\
_To use SponsorBlock, you must have_ [**_FFmpeg_**](#_dep_ffmpeg) _installed and accessible on the path._
\
_This is only available when using_ [**_yt-dlp_**](#_exe_yt_dlp) _as the_ [_executable_](#_ycd_executable)_._

SponsorBlock is capable of distinguishing and skipping several types of segments which can be enabled independently.
\
Details about these segments and what each covers can be found on the [<u>**Category Breakdown**</u>](https://wiki.sponsor.ajay.app/w/Guidelines#Category_Breakdown "https://wiki.sponsor.ajay.app/w/Guidelines#Category_Breakdown") page of the [<u>**SponsorBlock Wiki**</u>](https://wiki.sponsor.ajay.app/w/Main_Page "https://wiki.sponsor.ajay.app/w/Main_Page").

You can set up a global SponsorBlock configuration for the entire project, or for the _Youtube Downloader_ or _Youtube Channel Downloader_ programs individually.
\
You can also have custom SponsorBlock configurations for individual Channel Groups or Channels.

To configure SponsorBlock, find or create a `"sponsorBlock"` [**json configuration**](#sponsorblock-configuration-sample) and customize the [Settings](#sponsorblock-settings) it contains.

- A configuration in [`conf.json`](./conf.json) will function as a global configuration for the entire project.
- A configuration in [`conf.json`](./conf.json) inside the [Youtube Downloader Configuration](#configuring-the-youtube-downloader) or [Youtube Channel Downloader Configuration](#configuring-the-youtube-channel-downloader) will affect all content downloaded by the respective program.
- A configuration in [`channels.json`](./channels.json) inside a [Channel Configuration](#adding-channels) will affect only that Channel, and one inside a [Channel Group Configuration](#grouping-channels) will affect all Channels and Channel Groups which are children of the group.

### SponsorBlock Settings:

| **SETTING**                                                                 | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
|:----------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_sponsorBlock_enabled">           **_enabled_**           </span> | When this setting is enabled, the SponsorBlock configuration is _active_; meaning downloaded content will be post-processed and cleaned according to the provided settings. <br/> When this setting is disabled, the SponsorBlock configuration is _inactive_; meaning downloaded content will not be post-processed. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                             |
| &nbsp;                                                                      | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| <span id="_sponsorBlock_forceGlobally">     **_forceGlobally_**     </span> | When this setting is enabled, this SponsorBlock configuration will be used in place of a specific lower-level configuration. <br/> In that case, this configuration will be used even if the lower-level configuration is _active_, provided that this configuration is _active_ and the lower-level configuration does not have [_overrideGlobal_](#_sponsorBlock_overrideGlobal) enabled. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_ <br/> _**Note:** This setting is only valid for global SponsorBlock configurations._ |
| <span id="_sponsorBlock_overrideGlobal">    **_overrideGlobal_**    </span> | When this setting is enabled, this SponsorBlock configuration will be used regardless of the global configuration. <br/> When this configuration has [_overrideGlobal_](#_sponsorBlock_overrideGlobal) enabled and the global configuration has [_forceGlobally_](#_sponsorBlock_forceGlobally) enabled, [_overrideGlobal_](#_sponsorBlock_overrideGlobal) will take precedence. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_ <br/> _**Note:** This setting is only valid for non-global SponsorBlock configurations._        |
| &nbsp;                                                                      | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| <span id="_sponsorBlock_skipAll">           **_skipAll_**           </span> | When this setting is enabled, all segments recognized by SponsorBlock will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                                                                                                  |
| <span id="_sponsorBlock_skipSponsor">       **_skipSponsor_**       </span> | When this setting is enabled, [<u>**Sponsor**</u>](https://wiki.sponsor.ajay.app/w/Sponsor "https://wiki.sponsor.ajay.app/w/Sponsor") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                                         |
| <span id="_sponsorBlock_skipIntro">         **_skipIntro_**         </span> | When this setting is enabled, [<u>**Intro**</u>](https://wiki.sponsor.ajay.app/w/Intermission/Intro_Animation "https://wiki.sponsor.ajay.app/w/Intermission/Intro_Animation") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                 |
| <span id="_sponsorBlock_skipOutro">         **_skipOutro_**         </span> | When this setting is enabled, [<u>**Outro**</u>](https://wiki.sponsor.ajay.app/w/Endcards/Credits "https://wiki.sponsor.ajay.app/w/Endcards/Credits") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                         |
| <span id="_sponsorBlock_skipSelfPromo">     **_skipSelfPromo_**     </span> | When this setting is enabled, [<u>**Self-Promo**</u>](https://wiki.sponsor.ajay.app/w/Unpaid/Self_Promotion "https://wiki.sponsor.ajay.app/w/Unpaid/Self_Promotion") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                          |
| <span id="_sponsorBlock_skipPreview">       **_skipPreview_**       </span> | When this setting is enabled, [<u>**Preview**</u>](https://wiki.sponsor.ajay.app/w/Preview/Recap "https://wiki.sponsor.ajay.app/w/Preview/Recap") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                                                             |
| <span id="_sponsorBlock_skipInteraction">   **_skipInteraction_**   </span> | When this setting is enabled, [<u>**Interaction**</u>](https://wiki.sponsor.ajay.app/w/Interaction_Reminder_(Subscribe) "https://wiki.sponsor.ajay.app/w/Interaction_Reminder_(Subscribe)") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                   |
| <span id="_sponsorBlock_skipMusicOffTopic"> **_skipMusicOffTopic_** </span> | When this setting is enabled, [<u>**Music Off-Topic**</u>](https://wiki.sponsor.ajay.app/w/Music:_Non-Music_Section "https://wiki.sponsor.ajay.app/w/Music:_Non-Music_Section") segments will be skipped when downloading content. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_                                                                                                                                                                                                                                               |
|                                                                             |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |

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

You can customize the colors used by the project when printing output to the console by changing the settings inside the file [`conf.json`](./conf.json).
\
Changing these settings will affect the colors for both the _Youtube Downloader_ and the _Youtube Channel Downloader_.

These colors will only work if your console supports _ANSI SGR codes_.
\
The project supports this list of [Supported Colors](#supported-colors).

To configure the colors, find or create the `"color"` [**json configuration**](#color-configuration-sample) and customize the [Settings](#color-settings) it contains.

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

| **SETTING**                                                        | **DESCRIPTION**                                                                                                                                                                                                                                                                           |
|:-------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                                    |                                                                                                                                                                                                                                                                                           |
| <span id="_color_enableColors">     **_enableColors_**     </span> | When this setting is enabled, colors will be used when printing output to the console. <br/> When this setting is disabled, all output will be printed to the console using its default color. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_ |
| &nbsp;                                                             | &nbsp;                                                                                                                                                                                                                                                                                    |
| <span id="_color_base">             **_base_**             </span> | The base color to print in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"GREEN"` by default)_                                                                                                                                   |
| <span id="_color_good">             **_good_**             </span> | The color to print _"good" text_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"CYAN"` by default)_                                                                                                                           |
| <span id="_color_bad">              **_bad_**              </span> | The color to print _"bad" text_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"RED"` by default)_                                                                                                                             |
| <span id="_color_log">              **_log_**              </span> | The color to print _logs_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"DARK_GREY"` by default)_                                                                                                                             |
| <span id="_color_channel">          **_channel_**          </span> | The color to print _Channel names_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"YELLOW"` by default)_                                                                                                                       |
| <span id="_color_video">            **_video_**            </span> | The color to print _Video titles_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"PURPLE"` by default)_                                                                                                                        |
| <span id="_color_number">           **_number_**           </span> | The color to print _numbers_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"WHITE"` by default)_                                                                                                                              |
| <span id="_color_file">             **_file_**             </span> | The color to print _file names_ and _file paths_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"GREY"` by default)_                                                                                                           |
| <span id="_color_exe">              **_exe_**              </span> | The color to print the _name_ of the [_executable_](#_ycd_executable) in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"ORANGE"` by default)_                                                                                    |
| <span id="_color_link">             **_link_**             </span> | The color to print _links_ in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"TEAL"` by default)_                                                                                                                                 |
| &nbsp;                                                             | &nbsp;                                                                                                                                                                                                                                                                                    |
| <span id="_color_progressBar">      **_progressBar_**      </span> | **The progress bar color settings :**                                                                                                                                                                                                                                                     |
| <span id="_color_progressBar_base"> **_progressBar.base_** </span> | The base color to print progress bars in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"GREEN"` by default)_                                                                                                                     |
| <span id="_color_progressBar_good"> **_progressBar.good_** </span> | The color to print the _"good" text_ of progress bars in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"CYAN"` by default)_                                                                                                      |
| <span id="_color_progressBar_bad">  **_progressBar.bad_**  </span> | The color to print the _"bad" text_ of progress bars in. <br/> The acceptable values for this setting are any of the [Supported Colors](#supported-colors). _(`"RED"` by default)_                                                                                                        |
|                                                                    |                                                                                                                                                                                                                                                                                           |

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

You can customize what is printed to the console when running the project by changing the settings inside the file [`conf.json`](./conf.json).
\
Changing these settings will affect the logging for both the _Youtube Downloader_ and the _Youtube Channel Downloader_.

To configure the logging, find or create the `"log"` [**json configuration**](#logging-configuration-sample) and customize the [Settings](#logging-settings) it contains.

### Logging Settings:

| **SETTING**                                                                        | **DESCRIPTION**                                                                                                                                                                                                                                                                                                                                                                          |
|:-----------------------------------------------------------------------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|                                                                                    |                                                                                                                                                                                                                                                                                                                                                                                          |
| <span id="_log_printSettings">              **_printSettings_**            </span> | Whether to print the active configuration settings to the console at the start of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                             |
| <span id="_log_printExeVersion">            **_printExeVersion_**          </span> | Whether to print the version of the [_executable_](#_ycd_executable) to the console at the start of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                           |
| <span id="_log_printExecutionTime">         **_printExecutionTime_**       </span> | Whether to print the time elapsed during execution of the run to the console at the end of the run or not. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                                                    |
| &nbsp;                                                                             | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                   |
| <span id="_log_download">                   **_download_**                 </span> | **The download log settings :**                                                                                                                                                                                                                                                                                                                                                          |
| <span id="_log_download_showCommand">       **_download.showCommand_**     </span> | When this setting is enabled, the commands sent to the [_executable_](#_ycd_executable) to download each video will be printed to the console. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                                                |
| <span id="_log_download_showWork">          **_download.showWork_**        </span> | When this setting is enabled, the work being done by the [_executable_](#_ycd_executable) while downloading each video will be printed to the console. <br/> When this setting is enabled, [_download.showProgressBar_](#_log_download_showProgressBar) will be automatically disabled. <br/> The acceptable values for this setting are `true` or `false`. _(disabled by default)_      |
| <span id="_log_download_showProgressBar">   **_download.showProgressBar_** </span> | When this setting is enabled, and when [_download.showWork_](#_log_download_showWork) is disabled, a progress bar will be shown in the console while downloading each video. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                  |
| &nbsp;                                                                             | &nbsp;                                                                                                                                                                                                                                                                                                                                                                                   |
| <span id="_log_file">                       **_file_**                     </span> | **The file log settings :**                                                                                                                                                                                                                                                                                                                                                              |
| <span id="_log_file_file_allowFileLogging"> **_file.allowFileLogging_**    </span> | When this setting is enabled, logging to files in the `log/` directory will be permitted. <br/> When this setting is disabled, all file logging will be disabled. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                             |
| <span id="_log_file_file_writeMainLog">     **_file.writeMainLog_**        </span> | When this setting is enabled, the program will be permitted to write to the main log file. <br/> When this setting is disabled, logging to the main log file will be disabled. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                |
| <span id="_log_file_file_writeDownloadLog"> **_file.writeDownloadLog_**    </span> | When this setting is enabled, the program will be permitted to write to the download log file. <br/> When this setting is disabled, logging to the download log file will be disabled. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                        |
| <span id="_log_file_file_writeApiLog">      **_file.writeApiLog_**         </span> | When this setting is enabled, the program will be permitted to write to the api log file. <br/> When this setting is disabled, logging to the api log file will be disabled. <br/> The acceptable values for this setting are `true` or `false`. _(enabled by default)_                                                                                                                  |
| <span id="_log_file_file_daysToKeepLogs">   **_file.daysToKeepLogs_**      </span> | The number of days to retain log files before deleting them. <br/> Once a log file has become older than the value specified, it will be deleted the next time the program runs. <br/> You may set this setting to `-1` to indicate that logs should never be automatically deleted. <br/> The acceptable values for this setting are any positive integer, or `-1`. _(`30` by default)_ |
|                                                                                    |                                                                                                                                                                                                                                                                                                                                                                                          |

### Logging Configuration (Sample):

```json
"log": {
    "printSettings": true,
    "printExeVersion": true,
    "printExecutionTime": true,
    "download": {
        "showCommand": true,
        "showWork": false,
        "showProgressBar": true,
    },
    "file": {
        "allowFileLogging": true,
        "writeMainLog": true,
        "writeDownloadLog": true,
        "writeApiLog": true,
        "daysToKeepLogs": 30
    }
}
```


&nbsp;

----


~~ _This project is for educational and testing purposes only and is not intended to be used in any way that would violate the Youtube ToS or to download copyrighted material_ ~~


----
