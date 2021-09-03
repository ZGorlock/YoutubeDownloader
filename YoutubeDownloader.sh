#!/bin/bash

mvn compile && mvn exec:java -Dexec.mainClass="youtube.YoutubeDownloader"
read -p "Press Enter to continue . . . " < /dev/tty
