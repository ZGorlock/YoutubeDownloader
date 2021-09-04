# Youtube Downloader
This repo contains two projects, the **Youtube Downloader** and the **Youtube Channel Downloader**.

To run these projects you will need to have [**Java 13.0.2**](https://jdk.java.net/archive/) or higher, as well as [**Maven 3.6.3**](https://maven.apache.org/download.cgi) or higher.

Both projects can be run within an IDE or by using the scripts in the project directory which compile and execute it using Maven.
\
Depending on your operating system you want to use either the *.bat* files (Windows) or the *.sh* files (Linux).

* YoutubeDownloader.bat
* YoutubeChannelDownloader.bat
* YoutubeDownloader.sh
* YoutubeChannelDownloader.sh

Alternatively you could run it with Maven directly from the command line using a command like:

    mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeChannelDownloader"

 
***

## Downloader:

This is just a simple Youtube Downloader.

You can create a list of Youtube urls in the file *./data/downloadQueue.txt* and when the program is executed it will download that list of videos to a directory called 'YoutubeDownloader' in your user home directory.

You can also enter additional Youtube urls while the program is running and they will be similarly downloaded. The program will continue to run until and empty line in received (Enter).

 
***

## Channel Downloader:

This is a more complex Youtube Downloader and is the main focus of this repo. It will download and keep entire playlists or channels up to date on your hard drive. You will need to be somewhat comfortable modifying java code in order to configure the Youtube Channel Downloader to work for you; everything you need to know will be explained below.

### Getting an API Key:

In order to use the Youtube Channel Downloader, you will need to have a Google API key. Use the following steps to get your Google API key:

    1. Go to: https://console.cloud.google.com/projectselector2/apis/dashboard
    2. Click 'Create new Project' and name it 'Youtube Downloader'
    3. Click on 'Enable APIs and Services'
    4. Search 'Youtube' and select 'YouTube Data API v3'
    5. Click 'Enable'
    6. Click 'Create Credentials'
    7. Select 'YouTube Data API v3', click 'Public Data', then click 'Next'

Copy your API key to the file *./apiKey* in the project.

### Setting your Storage Locations:

Open up *./src/youtube/Channel.java* and search for "//Constants". Here you can set the storageDrive, musicDir, and videoDir variables to the locations where you would like your music and videos to be saved.

* *storageDrive* - The drive where your music and videos will be stored. For example: "C:/"
* *musicDir* - The directory within the storageDrive where you want music to be saved. For Example: "Users/Me/My Music"
* *videoDir* - The directory within the storageDrive where you want videos to be saved. For Example: "Users/Me/My Videos"

### Adding a Channel or Playlist:

Next add the channels or playlists that you want to process to the Channel enum in *./src/youtube/Channel.java*. There are many examples there that you can use for reference. 
\
The first thing you will want to do is make inactive (set the first parameter to 'false'), comment out, or simply delete all of my Channels. Unless you also like those Channels, then you can keep them.

When you add a new Channel you need to create a new value in the Channel enum and configure the options for that Channel.

* First give your channel or playlist a unique name within the enum.
* The first parameter to the enum is a boolean; whether that Channel will be active or inactive.
* The second parameter is a unique name for the Channel within the project.
* The third parameter is the playlist ID; we will cover this in a moment.
* The fourth parameter is the output directory for that channel; if this is a music Channel this will be relative to the musicDir from the last section, if it is a video Channel it will be relative to the videoDir.
* The fifth parameter is a boolean specifying whether this Channel should be downloaded as mp3 (true) or as mp4 (false).
* The sixth parameter is an optional playlist file for the downloads from the Channel, this will also be relative to the musicDir or videoDir but does not need to be in the same folder where the music or videos are saved. If you do not want to create a playlist for the Channel then do not include this parameter, or put 'null'.
* The seventh parameter is an optional boolean specifying whether or not to keep the Channel directory clean; if this is enabled then videos that are deleted off of Youtube will also be deleted from your hard drive. You may also not include this parameter, by default the value is 'true'.

### Finding Youtube Playlist IDs:

Now you will need to set the playlist ID for your Channel. The way of obtaining this will be different depending on if your Channel is an actual Youtube channel, or if it is a playlist.

#### For a Youtube playlist:

    1. Go to the Youtube Playlist
    2. Simply copy it from the url:
        https://www.youtube.com/watch?v=3qiLI1ILMlU&list=PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr
                                                        <PLdE7uo_7KBkfAWkk7-Clm18krBuziKQfr>

#### For a Youtube channel:

    1. Go to the Youtube Channel
    2. Right click and View the Page Source
    3. Search for "externalId" and copy that value
    4. Change the second character from a 'C' to a 'U'

### Executable Options:

You can configure this project ot use either [youtube-dl](https://youtube-dl.org/) or [yt-dlp](https://github.com/yt-dlp/yt-dlp/).

* *youtube-dl* - The original Youtube Downloader executable; as of the time of writing this it appears it may no longer be maintained, and throttling often occurs when downloading videos.
* *yt-dlp* - A newer drop-in replacement of the Youtube Downloader executable; actively maintained by new developers and includes additional features.

By default the project is configured to use *yt-dlp*.
\
If you want to change this, open up *./src/youtube/YoutubeUtils.java* and search for "//Constants". Here you can set the variable 'EXECUTABLE' to either 'Executable.YOUTUBE_DL' or 'Executable.YT_DLP'.

Whichever Executable you choose will be automatically downloaded by the project and it will ensure the latest version is always being used.

### Additional Options:

There are a couple other options that you can enable for certain circumstances. All of these can be found in *./src/youtube/YoutubeChannelDownloader.java*. Just search for "< variableName > = " and change the value.

* *channel* - Once you have multiple Channels, there may be instances where you want to process only a single Channel and not all of them. Set this variable to 'Channel.YOUR_CHANNEL_NAME' to process only that Channel. To return to processing all Channels, set this variable back to 'null'.
* *startAt* - This will start processing at a specified Channel, skipping all Channels before it. The values for this variable work the same as for *channel*. The Channels are processed in the order that they appear in the Channel enum.
* *stopAt* - This will stop processing at a specified Channel, skipping all Channels after it. The values for this variable work the same as for *channel*. The Channels are processed in the order that they appear in the Channel enum.
* *retryFailed* - When the Youtube Channel Downloader fails to download a video (either because of a connection issue or because a video is "not available in your country") it will mark that video as blocked and will not automatically attempt to download it again. Sometimes the download will succeed if tried again though. You can set this variable to 'true' or 'false', by default it is set to 'false'. This should only be turned on occasionally because it will cause all previously failed downloads from all Channels to be reattempted in the next run. You do not want this to happen during every run, so set it back to 'false' after the run.

### Note about Updates:

The project should work as it does now indefinitely, as long as *youtube-dl* and *yt-dlp* continue to exist on the same websites they do now. However, additional improvements and features may be added from time to time.
\
If you wish to receive these updates then you would just have to do a *git pull* or re-download the master branch.
\
Before you do this though, make sure you back up your versions of the files (especially *./src/youtube/Channel.java*, at least your list of values from the enum) because I update my personal Channel list frequently and store it in the repo. This will cause merge conflicts with your version if you have customized it.

 
***

--- This program is for educational and testing purposes only and is not intended to be used in any way that would violate the Youtube ToS or to download copyrighted material ---