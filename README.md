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

### Configuration:

You can configure the operation of the Downloader project by changing the values inside the file *./conf.json*. There are two sections in the conf file, the settings for the Downloader project are under the "YoutubeDownloader" section:

* ***executable*** - This configures which Youtube Downloader executable should be used to download videos. The two valid options for this setting are 'yt-dlp' and 'youtube-dl'. More information about these options is provided in the Executable Options section below.
* ***asMp3*** - This is a boolean setting that determines whether the videos should be downloaded as mp3 (true) or mp4 (false).
* ***logCommand*** - When set to true the commands sent to the executable will be printed to the console for each video.
* ***logWork*** - When set to true the work done by the executable while downloading each video will be printed to the console.

 
***

## Channel Downloader:

This is a more complex Youtube Downloader and is the main focus of this repo. It will download and keep entire playlists or channels up to date on your hard drive. Everything you need to know for configuring the project to work for you will be explained below.

### Getting an API Key:

In order to use the Youtube Channel Downloader, you will need to have a Google API key. This is used to query the Youtube Data API for the video lists of channels and playlists.

Use the following steps to get your Google API key:

    1. Go to: https://console.cloud.google.com/projectselector2/apis/dashboard
    2. Click 'Create new Project' and name it 'Youtube Downloader'
    3. Click on 'Enable APIs and Services'
    4. Search 'Youtube' and select 'YouTube Data API v3'
    5. Click 'Enable'
    6. Click 'Create Credentials'
    7. Select 'YouTube Data API v3', click 'Public Data', then click 'Next'

Copy your API key to the file *./apiKey* in the project.

### Configuration:

You can configure the operation of the Channel Downloader project by changing the values inside the file *./conf.json*. There are two sections in the conf file, the settings for the Channel Downloader project are under the "YoutubeChannelDownloader" section:

* ***storageDrive*** - The drive where your music and videos will be stored. For example: "C:/". You may leave this blank ("") if you wish to specify the full path for each Channel in *./channels.json*; if you leave this blank you must also leave ***musicDir*** and ***videoDir*** blank.
* ***musicDir*** - The directory within the storageDrive where you want music to be saved. For Example: "Users/Me/My Music/". You may leave this blank ("") if you wish to specify the full path for each Channel in *./channels.json*; if you leave this blank you must also leave ***storageDrive*** and ***videoDir*** blank.
* ***videoDir*** - The directory within the storageDrive where you want videos to be saved. For Example: "Users/Me/My Videos/". You may leave this blank ("") if you wish to specify the full path for each Channel in *./channels.json*; if you leave this blank you must also leave ***storageDrive*** and ***musicDir*** blank.
* ***executable*** - This configures which Youtube Downloader executable should be used to download videos. The two valid options for this setting are 'yt-dlp' and 'youtube-dl'. More information about these options is provided in the Executable Options section below.
* ***channel*** - Once you have multiple Channels, there may be instances where you want to process only a single Channel and not all of them. Set this variable to '<YOUR_CHANNEL_KEY>' to process only that Channel. To return to processing all Channels, set this variable back to null.
* ***group*** - Once you have multiple Channels, and have organized them into groups, there may be instances where you want to process only a single group and not all of them. Set this variable to '<YOUR_CHANNEL_GROUP>' to process only that group. To return to processing all Channels, set this variable back to null.
* ***startAt*** - This will start processing at a specified Channel, skipping all Channels before it. The values for this variable work the same as for ***channel***. The Channels are processed in the order that they appear in the Channel enum. Except in special circumstances, this setting should be null.
* ***stopAt*** - This will stop processing at a specified Channel, skipping all Channels after it. The values for this variable work the same as for ***channel***. The Channels are processed in the order that they appear in the Channel enum. Except in special circumstances, this setting should be null.
* ***retryFailed*** - When the Youtube Channel Downloader fails to download a video (either because of a connection issue or because a video is "not available in your country") it will mark that video as blocked and will not automatically attempt to download it again. Sometimes the download will succeed if tried again though. You can set this variable to true or false, by default it is set to false. This should only be turned on occasionally because it will cause all previously failed downloads from all Channels to be reattempted in the next run. You do not want this to happen during every run, so set it back to false after the run.
* ***logCommand*** - When set to true the commands sent to the executable will be printed to the console for each video.
* ***logWork*** - When set to true the work done by the executable while downloading each video will be printed to the console.

### Executable Options:

You can configure this project to use either [youtube-dl](https://youtube-dl.org/) or [yt-dlp](https://github.com/yt-dlp/yt-dlp/).

* ***youtube-dl*** - The original Youtube Downloader executable; as of the time of writing this it appears it may no longer be maintained, and throttling often occurs when downloading videos.
* ***yt-dlp*** - A newer drop-in replacement of the Youtube Downloader executable; actively maintained by new developers and includes additional features.

Set your choice of executable in the configuration file as explain in the Configuration section above.
\
Whichever Executable you choose will be automatically downloaded by the project and it will ensure the latest version is always being used.

### Adding a Channel or Playlist:

Next add the channels or playlists that you want to process to the file *./channels.json*. There are many examples in *./channels-sample.json* that you can use as a reference.
\
The file *./channels-sample.json* is also my personal Channel configuration; if you see any Channels that you like you can copy them to your *./channels.json*.

When you add a new Channel you need to create a new json object in the json array and configure the values it contains. A good way to start is to simply copy another Channel object from your file or from *./channels-sample.json* and then modify it.
\
A "Channel object" is a section in the file that starts with '{' and ends with '}'.

* ***key*** - The Channel key can be anything as long as it is unique among all the Channels in your configuration.
* ***active*** - A boolean specifying whether the Channel will be active or inactive.
* ***name*** - A human readable name for the Channel, ideally unique and without spaces. Most likely this will be similar to ***key***.
* ***group*** - A group or category for the Channel; this could be anything you want it to be. By organizing your Channels into groups, you can process all Channels in a group independently of others.
* ***url*** - The url of the Youtube playlist or channel. This field is not used by the program, but is useful for returning to the playlist or channel in the future if you need to. 
* ***playlistId*** - This is the playlistId of the Channel or playlist; information about how to obtain this is discussed in the Finding YoutubePlaylist IDs section below.
* ***outputFolder*** - The output directory for the Channel; if this is a music Channel this will be relative to the ***musicDir*** from the Configuration section above, if it is a video Channel it will be relative to the ***videoDir***. If you left ***storageDir***, ***musicDir***, and ***videoDir*** blank in *./conf.json* then specify the full path to the output directory.
* ***saveAsMp3*** - A boolean specifying whether this Channel should be downloaded as mp3 (true) or as mp4 (false).
* ***playlistFile*** - An optional playlist file for enumerating the downloads from the Channel, this will also be relative to the ***musicDir*** or ***videoDir*** but does not need to be in the same folder where the music or videos are saved. If you left ***storageDir***, ***musicDir***, and ***videoDir*** blank in *./conf.json* then specify the full path to the playlist file. If you do not want to create a playlist for the Channel then set this field to null.
* ***keepClean*** - A boolean specifying whether or not to keep the Channel directory clean; if this is enabled then videos that are deleted off of Youtube will also be deleted from your hard drive.

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

### Special Channel Processing:

There are certain instances where you may want to perform some additional processing on the Channel to filter which videos are downloaded, to rename the downloaded videos to something that you prefer, etc. This functionality is possible granted that you know a bit of Java.

There are two methods in the file *./src/youtube/ChannelProcesses.java*, **performSpecialPreConditions()** and **performSpecialPostConditions()**.

* ***performSpecialPreConditions*** - Happens before the download queue is produced for the Channel. This can be used to rename the videos that will be downloaded.
* ***performSpecialPostConditions*** - Happens after the download queue is produced for the Channel. This can be used to filtering out videos that should not be downloaded; do not use this method for renaming the videos.

In both of these methods, all your have to do is add another case for your Channel key to the switch statement.
\
In these methods your have access to your Channel, the current video map containing all the information about the videos for the Channel, and the video ids in the queue, save, and blocked lists. These are provided as arguments to the method.
\
These methods have no return value, any changes that you want to make should be done on the maps and lists that are passed into the method. Any changes you make to these objects will persist when the method returns to the main program.

* ***videoMap*** - A map from video ids to the Video objects containing the name, output file, etc.
* ***queue*** - A list of video ids that are queued for download from the Channel.
* ***save*** - A list of video ids that have already been downloaded in the past and are currently on your hard drive.
* ***blocked*** - A list of video ids that have not already been downloaded, but are also not queued for download.

There are many examples of these processes in the file *./src/youtube/ChannelProcesses_Sample.java*. These are also the personal pre and post processes for my Channel configuration. If you did copy some of my Channels from *./channels-sample.json* then you should also copy the corresponding pre and post processes.

### Note about Updates:

The project should work as it does now indefinitely, as long as *youtube-dl* and *yt-dlp* continue to exist on the same websites they do now. However, additional improvements and features may be added from time to time.
\
If you wish to receive these updates then you would just have to do a *git pull* or re-download the master branch.
\
Before you do this though, make sure you back up your versions of the configuration files (*./conf.json*, *./channels.json*, and *./src/youtube/ChannelProcesses.java*). Updating may cause merge conflicts with your version if you have customized it.

 
***

--- This program is for educational and testing purposes only and is not intended to be used in any way that would violate the Youtube ToS or to download copyrighted material ---