/*
 * File:    Internet.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import commons.object.string.StringUtility;
import org.apache.commons.io.FileUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to the internet.
 */
public final class Internet {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Internet.class);
    
    
    //Constants
    
    /**
     * The default host to use for checking for internet connectivity.
     */
    public static final String DEFAULT_TEST_HOST = "google.com";
    
    /**
     * The encoding for url strings.
     */
    public static final String URL_ENCODING = "UTF-8";
    
    
    //Static Fields
    
    /**
     * The host to use for checking for internet connectivity.
     */
    private static String testHost = DEFAULT_TEST_HOST;
    
    
    //Static Methods
    
    /**
     * Determines if the system has access to the internet.
     *
     * @return Whether the system has access to the internet or not.
     */
    public static boolean isOnline() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(testHost, 80), 200);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Encodes a string for use as a url.
     *
     * @param url The string to encode as a url.
     * @return The encoded url, or null if there was an error.
     */
    public static String encodeUrl(String url) {
        try {
            return URLEncoder.encode(url, URL_ENCODING);
        } catch (UnsupportedEncodingException ignored) {
            logger.trace("Unable to encode string: " + url + " to URL");
            return null;
        }
    }
    
    /**
     * Downloads an html from a url and returns the retrieved Document.
     *
     * @param url The url address to download the html from.
     * @return The retrieved Document, or null if there was an error.
     * @see Jsoup#connect(String)
     */
    public static Document getHtml(String url) {
        try {
            if (logInternet()) {
                logger.trace("Fetching data from url: {}", url);
            }
            
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .followRedirects(true)
                    .execute()
                    .parse();
            
        } catch (IOException ignored) {
            logger.trace("Unable to download html from URL: " + url);
            return null;
        }
    }
    
    /**
     * Downloads a file from a url to the specified file and returns the file.<br>
     * This is a blocking operation and should be called from a thread.
     *
     * @param url      The url to the file to download.
     * @param download The file to download to.
     * @return The downloaded file, or null if there was an error.
     * @see FileUtils#copyURLToFile(URL, File, int, int)
     */
    public static File downloadFile(String url, File download) {
        try {
            if (logInternet()) {
                logger.trace("Downloading file from url: {}", url);
            }
            if (!isOnline()) {
                throw new IOException();
            }
            
            FileUtils.copyURLToFile(new URL(url), download, 200, Integer.MAX_VALUE);
            return download;
            
        } catch (IOException ignored) {
            logger.trace("Unable to download file from URL: " + url);
            return null;
        }
    }
    
    /**
     * Downloads a file from a url to a temporary file and returns the temporary file.<br>
     * This is a blocking operation and should be called from a thread.
     *
     * @param url The url to the file to download.
     * @return The downloaded file, or null if there was an error.
     * @see #downloadFile(String, File)
     */
    public static File downloadFile(String url) {
        return downloadFile(url, Filesystem.createTemporaryFile(".download"));
    }
    
    /**
     * Parses the JMP functions inside an HTML Document and fills the Document.
     *
     * @param document The Document.
     * @return The Document with the JMP functions parsed.
     */
    public static Document parseHtmlJmpFunctions(Document document) {
        String htmlText = document.toString();
        StringBuilder parsedHtml = new StringBuilder(htmlText);
        
        final Pattern jslDhFunctionPattern = Pattern.compile("window\\.jsl\\.dh\\('(?<id>[^']+)',\\s*'(?<content>[^']+)'");
        final Pattern escapeCodePattern = Pattern.compile("\\\\(?<code>(x[0-9a-fA-F]{2})|(u[0-9a-fA-F]{4}))(?<clear>[^\\\\]*)");
        
        final Matcher jslDhFunctionMatcher = jslDhFunctionPattern.matcher(htmlText);
        while (jslDhFunctionMatcher.find()) {
            String id = jslDhFunctionMatcher.group("id");
            String content = jslDhFunctionMatcher.group("content");
            
            StringBuilder contentBuilder = new StringBuilder();
            Matcher escapeCodeMatcher = escapeCodePattern.matcher(content);
            while (escapeCodeMatcher.find()) {
                String code = escapeCodeMatcher.group("code");
                String clear = escapeCodeMatcher.group("clear");
                if (code.startsWith("x")) {
                    code = String.valueOf((char) Integer.parseInt(code.substring(1), 16));
                } else if (code.startsWith("u")) {
                    code = StringEscapeUtils.unescapeJava("\\" + code);
                }
                contentBuilder.append(code).append(clear);
            }
            
            int jmpIndex = parsedHtml.indexOf("id=" + StringUtility.quote(id));
            int insertIndex = parsedHtml.indexOf(">", jmpIndex) + 1;
            parsedHtml.insert(insertIndex, contentBuilder);
        }
        
        return Jsoup.parse(parsedHtml.toString());
    }
    
    /**
     * Opens a url in the default web browser of the system.
     *
     * @param url The url to open.
     * @return Whether the url was opened or not.
     */
    public static boolean openUrl(String url) throws IOException {
        if (logInternet()) {
            logger.trace("Opening url: {}", url);
        }
        
        if (!Desktop.navigate(URI.create(url))) {
            logger.trace("Unable to open URL: " + url + " in local web browser");
            return false;
        }
        return true;
    }
    
    /**
     * Determines if internet logging is enabled or not.
     *
     * @return Whether internet logging is enabled or not.
     */
    public static boolean logInternet() {
        return false;
    }
    
}
