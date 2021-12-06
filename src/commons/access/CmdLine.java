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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.console.ProgressBar;
import commons.string.StringUtility;
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
    
    
    //Static Fields
    
    /**
     * A list of running processes that were started.
     */
    private static final Map<Process, String> runningProcesses = Collections.synchronizedMap(new HashMap<>());
    
    //Attempts to terminate synchronous cmd processes that were started and are still running.
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> runningProcesses.forEach((key, value) -> {
            if (!killProcess(key)) {
                Stream.of(Collections.singletonList(key.toHandle()), key.children().collect(Collectors.toList()))
                        .flatMap(List::stream).filter(ProcessHandle::isAlive).forEach(e ->
                                logger.error("{} with pid: {} could not be terminated: {}", ((e.pid() == key.pid()) ? "Process" : "Subprocess"),
                                        e.pid(), (((e.pid() == key.pid()) ? "" : "Subprocess of: ") + value)));
                
            }
        })));
    }
    
    
    //Functions
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd         The command to execute.
     * @param progressBar The progress bar to send the command output to.
     * @return The output; error lines are proceeded by '[*]'.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static String executeCmd(String cmd, ProgressBar progressBar) {
        try {
            final ProcessBuilder builder = buildProcess(cmd);
            if (builder == null) {
                return "";
            }
            
            final AtomicReference<Process> process = new AtomicReference<>(null);
            final List<String> response = Collections.synchronizedList(new ArrayList<>());
            
            final ExecutorService logReaders = Executors.newFixedThreadPool(2);
            final CountDownLatch logReadersLatch = new CountDownLatch(2);
            logReaders.execute(() -> {
                while (process.get() == null) {
                }
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.get().getErrorStream()))) {
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        response.add("[*]" + line);
                        if (progressBar != null) {
                            progressBar.processLog(line, true);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    logReadersLatch.countDown();
                }
            });
            logReaders.execute(() -> {
                while (process.get() == null) {
                }
                try (BufferedReader logReader = new BufferedReader(new InputStreamReader(process.get().getInputStream()))) {
                    String line;
                    while ((line = logReader.readLine()) != null) {
                        response.add(line);
                        if (progressBar != null) {
                            progressBar.processLog(line, false);
                        }
                    }
                } catch (Exception ignored) {
                } finally {
                    logReadersLatch.countDown();
                }
            });
            
            process.set(builder.start());
            runningProcesses.put(process.get(), cmd);
            
            process.get().waitFor();
            logReadersLatch.await();
            
            process.get().destroy();
            process.get().descendants().forEachOrdered(ProcessHandle::destroy);
            runningProcesses.remove(process.get());
            
            if (progressBar != null) {
                progressBar.complete();
            }
            return StringUtility.unsplitLines(response);
            
        } catch (IOException | InterruptedException e) {
            logger.warn("Error executing command: " + cmd);
        } catch (Exception e) {
            logger.error("Error executing command: " + cmd, e);
        }
        return "";
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd The command to execute.
     * @return The output; error lines are proceeded by '[*]'.
     * @see #executeCmd(String, ProgressBar)
     */
    public static String executeCmd(String cmd) {
        return executeCmd(cmd, null);
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
        if ((cmd == null) || cmd.isEmpty()) {
            return null;
        }
        
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
    
    /**
     * Attempts to kill a running process.
     *
     * @param process The process.
     * @return Whether the process was successfully killed or not.
     */
    public static boolean killProcess(Process process) {
        final List<ProcessHandle> processTree = new ArrayList<>(Collections.singletonList(process.toHandle()));
        processTree.addAll(process.descendants().collect(Collectors.toList()));
        if (processTree.stream().noneMatch(ProcessHandle::isAlive)) {
            return true;
        }
        
        processTree.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroy);
        try {
            Thread.sleep(250);
        } catch (InterruptedException ignored) {
        }
        if (processTree.stream().noneMatch(ProcessHandle::isAlive)) {
            return true;
        }
        
        processTree.stream().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
        try {
            Thread.sleep(750);
        } catch (InterruptedException ignored) {
        }
        if (processTree.stream().noneMatch(ProcessHandle::isAlive)) {
            return true;
        }
        
        Collections.reverse(processTree);
        if (OperatingSystem.getOperatingSystem() == OperatingSystem.OS.WINDOWS) {
            processTree.stream().filter(ProcessHandle::isAlive).forEachOrdered(e -> {
                try {
                    buildProcess("taskkill /F /PID " + e.pid()).start();
                } catch (Exception ignored) {
                }
            });
            
        } else {
            processTree.stream().filter(ProcessHandle::isAlive).forEachOrdered(e -> {
                try {
                    buildProcess("kill -SIGTERM " + e.pid()).start();
                } catch (Exception ignored) {
                }
            });
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
            }
            
            processTree.stream().filter(ProcessHandle::isAlive).forEachOrdered(e -> {
                try {
                    buildProcess("kill -SIGKILL " + e.pid()).start();
                } catch (Exception ignored) {
                }
            });
        }
        
        try {
            Thread.sleep(250);
        } catch (InterruptedException ignored) {
        }
        return processTree.stream().noneMatch(ProcessHandle::isAlive);
    }
    
}
