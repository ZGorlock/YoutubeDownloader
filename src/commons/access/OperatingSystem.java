/*
 * File:    OperatingSystem.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.lang.management.ManagementFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to the operating system.
 */
public final class OperatingSystem {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(OperatingSystem.class);
    
    
    //Constants
    
    /**
     * Whether the JVM is running in debug mode or not.
     */
    private static final boolean DEBUGGING = ManagementFactory.getRuntimeMXBean().getInputArguments().toString().contains("jdwp");
    
    
    //Enums
    
    /**
     * An enumeration of operating systems.
     */
    public enum OS {
        
        //Values
        
        WINDOWS,
        UNIX,
        MACOS,
        POSIX,
        OTHER
        
    }
    
    
    //Functions
    
    /**
     * Determines the current operating system.
     *
     * @return The current operating system.
     * @see #getOperatingSystemName()
     */
    public static OS getOperatingSystem() {
        String osName = getOperatingSystemName().toUpperCase();
        
        if (osName.contains("WINDOWS")) {
            return OS.WINDOWS;
        } else if (osName.contains("LINUX") ||
                osName.contains("MPE/IX") ||
                osName.contains("FREEBSD") ||
                osName.contains("IRIX") ||
                osName.contains("UNIX")) {
            return OS.UNIX;
        } else if (osName.contains("MAC")) {
            return OS.MACOS;
        } else if (osName.contains("SUN") ||
                osName.contains("SOL") ||
                osName.contains("HP-UX") ||
                osName.contains("AIX")) {
            return OS.POSIX;
        } else {
            return OS.OTHER;
        }
    }
    
    /**
     * Returns the name of the current operating system.
     *
     * @return The name of the current operating system.
     * @see System#getProperty(String)
     */
    public static String getOperatingSystemName() {
        return System.getProperty("os.name");
    }
    
    /**
     * Determines if the current operating system is Windows.
     *
     * @return Whether the current operating system is Windows or not.
     * @see #getOperatingSystem()
     */
    public static boolean isWindows() {
        return getOperatingSystem().equals(OS.WINDOWS);
    }
    
    /**
     * Determines if the current operating system is Unix.
     *
     * @return Whether the current operating system is Unix or not.
     * @see #getOperatingSystem()
     */
    public static boolean isUnix() {
        return getOperatingSystem().equals(OS.UNIX);
    }
    
    /**
     * Determines if the current operating system is macOS.
     *
     * @return Whether the current operating system is macOS or not.
     * @see #getOperatingSystem()
     */
    public static boolean isMacOS() {
        return getOperatingSystem().equals(OS.MACOS);
    }
    
    /**
     * Determines if the current operating system is POSIX.
     *
     * @return Whether the current operating system is POSIX or not.
     * @see #getOperatingSystem()
     */
    public static boolean isPosix() {
        return getOperatingSystem().equals(OS.POSIX);
    }
    
    /**
     * Determines if the current operating system is Other.
     *
     * @return Whether the current operating system is Other or not.
     * @see #getOperatingSystem()
     */
    public static boolean isOther() {
        return getOperatingSystem().equals(OS.OTHER);
    }
    
    /**
     * Determines if the current operating system is a particular operating system.
     *
     * @param os The operating system to test for.
     * @return Whether the current operating system is the particular operating system or not.
     * @see #getOperatingSystem()
     */
    public static boolean is(OS os) {
        return getOperatingSystem().equals(os);
    }
    
    /**
     * Determines if the current operating system is 32-bit or not.
     *
     * @return Whether the current operating system is 32-bit or not.
     */
    public static boolean is32Bit() {
        return System.getProperty("os.arch").contains("86");
    }
    
    /**
     * Determines if the current operating system is 64-bit or not.
     *
     * @return Whether the current operating system is 64-bit or not.
     */
    public static boolean is64Bit() {
        return System.getProperty("os.arch").contains("64");
    }
    
    /**
     * Returns the number of processors available.
     *
     * @return The number of processors available.
     */
    public static int getProcessorCount() {
        return Runtime.getRuntime().availableProcessors();
    }
    
    /**
     * Returns the maximum amount of memory the JVM will attempt to use.
     *
     * @return The maximum amount of memory the JVM will attempt to use, in bytes.
     */
    public static long getMaximumMemory() {
        return Runtime.getRuntime().maxMemory();
    }
    
    /**
     * Returns the total amount of memory available in the JVM.
     *
     * @return The total amount of memory available in the JVM, in bytes.
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }
    
    /**
     * Returns the amount of free memory available in the JVM.
     *
     * @return The amount of free memory available in the JVM, in bytes.
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
    
    /**
     * Returns the amount of memory used by the JVM.
     *
     * @return The amount of memory used by the JVM, in bytes.
     */
    public static long getUsedMemory() {
        return getTotalMemory() - getFreeMemory();
    }
    
    /**
     * Returns whether the JVM is running in debug mode or not.
     *
     * @return Whether the JVM is running in debug mode or not.
     */
    public static boolean isDebugging() {
        return DEBUGGING;
    }
    
}
