@echo off

echo Youtube Channel Downloader

set mainClass=youtube.YoutubeChannelDownloader
set noCompile=false
set loggerLvl=warn
set waitAfter=true

mvn compile exec:exec ^
	-Dorg.slf4j.simpleLogger.defaultLogLevel=%loggerLvl% ^
	-Dmaven.main.skip="%noCompile%" ^
	-Dexec.executable="java" ^
	-Dexec.args="-Dfile.encoding=UTF8 -classpath %%classpath %mainClass%" && ^
if "%waitAfter%"=="true" (pause)
