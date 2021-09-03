#!/bin/bash

mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeChannelDownloader"
read -p "Press Enter to continue . . . " < /dev/tty
