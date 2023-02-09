/*
 * File:    CmdLine.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import commons.io.console.ProgressBar;
import commons.object.collection.ListUtility;
import commons.object.collection.MapUtility;
import commons.object.string.StringUtility;
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
     * A list of running processes that were started during this session.
     */
    private static final Map<Process, String> runningProcesses = MapUtility.synchronizedMap();
    
    //Attempts to terminate synchronous cmd processes that were started during this session and are still running
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
    
    
    //Static Methods
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd         The command to execute.
     * @param safeExecute If true, if exception occurs, null will be returned instead of the exception.
     * @param progressBar The progress bar to send the command output to.
     * @return The output; error lines are proceeded by '[*]'; or null if there was an error and safeExecute is enabled.
     * @throws RuntimeException When there is an error executing the command and safeExecute is not enabled.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static String executeCmd(String cmd, boolean safeExecute, ProgressBar progressBar) throws RuntimeException {
        try {
            final ProcessBuilder builder = buildProcess(cmd);
            if (builder == null) {
                return null;
            }
            
            final AtomicReference<Process> process = new AtomicReference<>(null);
            final List<String> response = ListUtility.synchronizedList();
            
            final ExecutorService logReaders = Executors.newFixedThreadPool(2, task -> {
                final Thread thread = Executors.defaultThreadFactory().newThread(task);
                thread.setDaemon(true);
                return thread;
            });
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
            logReaders.shutdown();
            
            process.get().destroy();
            process.get().descendants().forEachOrdered(ProcessHandle::destroy);
            runningProcesses.remove(process.get());
            
            if (progressBar != null) {
                progressBar.complete();
            }
            return StringUtility.unsplitLines(response);
            
        } catch (Exception e) {
            logger.error("Error executing command: " + StringUtility.quote(cmd), e);
            if (safeExecute) {
                return null;
            } else {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd         The command to execute.
     * @param safeExecute If true, if exception occurs, null will be returned instead of the exception.
     * @return The output; error lines are proceeded by '[*]'; or null if there was an error and safeExecute is enabled.
     * @throws RuntimeException When there is an error executing the command and safeExecute is not enabled.
     * @see #executeCmd(String, boolean, ProgressBar)
     */
    public static String executeCmd(String cmd, boolean safeExecute) throws RuntimeException {
        return executeCmd(cmd, safeExecute, null);
    }
    
    /**
     * Executes a command on the system command line.
     *
     * @param cmd         The command to execute.
     * @param progressBar The progress bar to send the command output to.
     * @return The output; error lines are proceeded by '[*]'.
     * @see #executeCmd(String, boolean, ProgressBar)
     */
    public static String executeCmd(String cmd, ProgressBar progressBar) {
        return executeCmd(cmd, true, progressBar);
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
            return buildProcess(cmd).start();
        } catch (Exception ignored) {
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
        if (StringUtility.isNullOrEmpty(cmd)) {
            return null;
        }
        
        if (useScriptCommand) {
            final OperatingSystem.OS operatingSystem = OperatingSystem.getOperatingSystem();
            switch (operatingSystem) {
                case WINDOWS:
                    return new ProcessBuilder("cmd.exe", "/c", cmd);
                case UNIX:
                    return new ProcessBuilder("bash", "-c", cmd);
                case MACOS:
                    return new ProcessBuilder("/usr/local/bin/nmap", cmd);
                case POSIX:
                case OTHER:
                default:
                    throw new RuntimeException("Operating system: " + System.getProperty("os.name").toUpperCase() + " is not supported!");
            }
        } else {
            return new ProcessBuilder(cmd);
        }
    }
    
    /**
     * Builds a process from a command.
     *
     * @param cmd The command to build a process for.
     * @return The process that was built, or null if it was not built.
     * @throws RuntimeException When called from an unsupported operating system.
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
     * @see ProcessKiller#kill(Process)
     */
    public static boolean killProcess(Process process) {
        return ProcessKiller.kill(process);
    }
    
    
    //Inner Classes
    
    /**
     * Handles the termination of running processes.
     */
    private final static class ProcessKiller {
        
        //Constants
        
        /**
         * The sequence of Kill Stages to perform when killing a process.
         */
        private static final Map<KillStage, Supplier<KillStage>> KILL_SEQUENCE = new LinkedHashMap<>();
        
        //Populates the Kill Stage sequence
        static {
            KILL_SEQUENCE.put(KillStage.DESTROY, () -> KillStage.DESTROY_FORCIBLY);
            KILL_SEQUENCE.put(KillStage.DESTROY_FORCIBLY, () -> (OperatingSystem.isWindows() ? KillStage.CMD_KILL_WINDOWS : KillStage.CMD_KILL));
            KILL_SEQUENCE.put(KillStage.CMD_KILL, () -> KillStage.CMD_KILL_HARD);
            KILL_SEQUENCE.put(KillStage.CMD_KILL_HARD, () -> null);
            KILL_SEQUENCE.put(KillStage.CMD_KILL_WINDOWS, () -> null);
        }
        
        /**
         * The default amount of time to wait after performing a Kill Stage before checking if the process has been killed or not, in milliseconds.
         */
        private static final long DEFAULT_VALIDATION_DELAY = 250L;
        
        
        //Enums
        
        /**
         * An enumeration of Process Killer Kill Stages.
         */
        private enum KillStage {
            
            //Values
            
            DESTROY(ProcessHandle::destroy, 1, false),
            DESTROY_FORCIBLY(ProcessHandle::destroyForcibly, 3, false),
            CMD_KILL(e -> executeCmdAsync("kill -SIGTERM " + e.pid()), 1, true),
            CMD_KILL_HARD(e -> executeCmdAsync("kill -SIGKILL " + e.pid()), 1, false),
            CMD_KILL_WINDOWS(e -> executeCmdAsync("taskkill /F /PID " + e.pid()), 1, true);
            
            
            //Fields
            
            /**
             * The action to perform.
             */
            private final Consumer<ProcessHandle> action;
            
            /**
             * The amount of time to wait before check if the process has been killed or not, in milliseconds.
             */
            private final long validationDelay;
            
            /**
             * Whether to iterate the process tree in reverse order or not.
             */
            private final boolean reverseTree;
            
            
            //Constructors
            
            /**
             * Constructs a Kill Stage.
             *
             * @param action      The action to perform.
             * @param delayFactor The factor to apply to the default validation delay.
             * @param reverseTree Whether to iterate the process tree in reverse order or not.
             */
            KillStage(Consumer<ProcessHandle> action, int delayFactor, boolean reverseTree) {
                this.action = action;
                this.validationDelay = DEFAULT_VALIDATION_DELAY * delayFactor;
                this.reverseTree = reverseTree;
            }
            
        }
        
        
        //Fields
        
        /**
         * The process tree of a process, including itself and all its descendants.
         */
        private final List<ProcessHandle> processTree;
        
        /**
         * The current Kill Stage of the Process Killer.
         */
        private KillStage stage = null;
        
        
        //Constructors
        
        /**
         * The private constructor for a Process Killer.
         *
         * @param process The process to kill.
         */
        private ProcessKiller(Process process) {
            this.processTree = Stream.of(Stream.of(process.toHandle()), process.descendants())
                    .flatMap(e -> e).collect(Collectors.toList());
        }
        
        
        //Methods
        
        /**
         * Executes the next Kill Stage of the Process Killer.
         */
        public void nextStage() {
            stage = (stage == null) ? KillStage.values()[0] : KILL_SEQUENCE.get(stage).get();
            if (stage == null) {
                return;
            }
            
            if (stage.reverseTree) {
                Collections.reverse(processTree);
            }
            try {
                processTree.stream().filter(ProcessHandle::isAlive).forEachOrdered(stage.action);
                Thread.sleep(stage.validationDelay);
            } catch (Exception ignored) {
            }
        }
        
        /**
         * Determines whether the Process Killer has finished or not.
         *
         * @return Whether the Process Killer has finished or not.
         */
        public boolean finished() {
            return ((stage != null) && (KILL_SEQUENCE.get(stage).get() == null)) || succeeded();
        }
        
        /**
         * Determines whether the Process Killer was successful or not.
         *
         * @return Whether the Process Killer was successful or not.
         */
        public boolean succeeded() {
            return processTree.stream().noneMatch(ProcessHandle::isAlive);
        }
        
        
        //Static Methods
        
        /**
         * Attempts to kill a running process.
         *
         * @param process The process.
         * @return Whether the process was successfully killed or not.
         * @see ProcessKiller
         */
        public static boolean kill(Process process) {
            final ProcessKiller processKiller = new ProcessKiller(process);
            while (!processKiller.finished()) {
                processKiller.nextStage();
            }
            return processKiller.succeeded();
        }
        
    }
    
}
