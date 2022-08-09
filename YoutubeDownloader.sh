#!/bin/bash

echo Youtube Downloader

mainClass="youtube.YoutubeDownloader"
noCompile="false"
loggerLvl="warn"
waitAfter="true"

mvn compile exec:exec \
	-Dorg.slf4j.simpleLogger.defaultLogLevel=$loggerLvl \
	-Dmaven.main.skip=$noCompile \
	-Dexec.executable="java" \
	-Dexec.args="-Dfile.encoding=UTF8 -classpath %classpath $mainClass" && \
if $waitAfter; then read -p "Press Enter to continue . . . " < /dev/tty; fi
