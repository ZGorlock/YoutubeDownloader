/*
 * File:    CmdLine.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to the system command line.
 */
public final class CmdLine {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CmdLine.class);
    
    
    //Functions
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd The command to execute.
     * @return The output.
     */
    public static String executeCmd(String cmd) {
        try {
            ProcessBuilder builder = buildProcess(cmd);
            StringBuilder response = new StringBuilder();
            
            Process process = builder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                
                String line;
                while (true) {
                    line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    response.append(line).append(System.lineSeparator());
                }
            }
            process.waitFor();
            process.destroy();
            
            return response.toString();
            
        } catch (IOException | InterruptedException e) {
            logger.warn("Error executing command: " + cmd);
        } catch (Exception e) {
            logger.error("Error executing command: " + cmd, e);
        }
        return "";
    }
    
    /**
     * Executes a command on the system command line asynchronously.
     *
     * @param cmd The command to execute.
     * @return The process running the command execution, or null if there was an error.
     */
    public static Process executeCmdAsync(String cmd) {
        try {
            ProcessBuilder builder = buildProcess(cmd);
            
            return builder.start();
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Builds a process from a command.
     *
     * @param cmd              The command to build a process for.
     * @param useScriptCommand Whether or not to include the script command at the beginning ("cmd.exe /c", "bash -c", etc).
     * @return The process that was built, or null if it was not built.
     * @throws RuntimeException When called from an unsupported operating system.
     */
    public static ProcessBuilder buildProcess(String cmd, boolean useScriptCommand) throws RuntimeException {
        ProcessBuilder builder;
        if (useScriptCommand) {
            switch (OperatingSystem.getOperatingSystem()) {
                case WINDOWS:
                    builder = new ProcessBuilder("cmd.exe", "/c", cmd);
                    break;
                case UNIX:
                    builder = new ProcessBuilder("bash", "-c", cmd);
                    break;
                case MACOS:
                    builder = new ProcessBuilder("/usr/local/bin/nmap", cmd);
                    break;
                case POSIX:
                case OTHER:
                default:
                    throw new RuntimeException("Operating system: " + System.getProperty("os.name").toUpperCase() + " is not supported!");
            }
        } else {
            builder = new ProcessBuilder(cmd);
        }
        builder.redirectErrorStream(true);
        
        return builder;
    }
    
    /**
     * Builds a process from a command.
     *
     * @param cmd The command to build a process for.
     * @return The process that was built, or null if it was not built.
     * @throws RuntimeException When there is an unknown operating system.
     * @see #buildProcess(String, boolean)
     */
    public static ProcessBuilder buildProcess(String cmd) throws RuntimeException {
        return buildProcess(cmd, true);
    }
    
}
