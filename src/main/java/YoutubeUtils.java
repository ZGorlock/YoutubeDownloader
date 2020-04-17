/*
 * File:    YoutubeUtils.java
 * Package: PACKAGE_NAME
 * Author:  Zachary Gill
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.Map;

public final class YoutubeUtils {
    
    public static boolean downloadYoutubeVideo(String video, File output, boolean asMp3, boolean outputCommand) throws Exception {
        String outputPath = output.getAbsolutePath();
        outputPath = outputPath.substring(0, outputPath.lastIndexOf('.'));
        
        String cmd = "youtube-dl.exe " +
                "--output \"" + outputPath + ".%(ext)s\" " +
                "--geo-bypass " +
                (asMp3 ? "--extract-audio --audio-format mp3 " :
                 "--format best ") +
                video;
        if (outputCommand) {
            System.out.println(cmd);
        }
        String result = executeProcess(cmd);
        
        return result.split("\r\n").length > 2;
    }
    
    public static String buildParameterString(Map<String, String> parameters) throws Exception {
        StringBuilder parameterString = new StringBuilder("?");
        for (Map.Entry<String, String> parameterEntry : parameters.entrySet()) {
            if (parameterString.length() > 1) {
                parameterString.append("&");
            }
            parameterString.append(URLEncoder.encode(parameterEntry.getKey(), "UTF-8"))
                           .append("=")
                           .append(URLEncoder.encode(parameterEntry.getValue(), "UTF-8"));
        }
        return parameterString.toString();
    }
    
    public static String cleanTitle(String title) {
        return title.replace("\\", "-")
                    .replace("/", "-")
                    .replace(":", "-")
                    .replace("*", "-")
                    .replace("?", "")
                    .replace("\"", "'")
                    .replace("<", "-")
                    .replace(">", "-")
                    .replace("|", "-")
                    .replace("‒", "-")
                    .replaceAll("[^\\x00-\\x7F]", "")
                    .replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
    }
    
    public static String executeProcess(String cmd) {
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", cmd);
            
            Process process = builder.start();
            BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            StringBuilder response = new StringBuilder();
            String line;
            while (true) {
                line = r.readLine();
                if (line == null) {
                    break;
                }
                response.append(line).append(System.lineSeparator());
            }
            
            process.waitFor();
            r.close();
            process.destroy();
            
            return response.toString();
            
        } catch (Exception e) {
            return "Failed";
        }
    }
    
}
