/*
 * File:    Desktop.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.File;
import java.net.URI;
import java.util.Objects;

import commons.lambda.function.checked.CheckedSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides access to the desktop.
 */
public final class Desktop {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(Desktop.class);
    
    
    //Static Fields
    
    /**
     * The handle to the desktop.
     */
    private static java.awt.Desktop desktop = CheckedSupplier.invoke(java.awt.Desktop::getDesktop);
    
    
    //Static Methods
    
    /**
     * Determines if the desktop is supported.
     *
     * @return Whether the desktop if supported or not.
     */
    public static boolean isDesktopSupported() {
        return (desktop != null);
    }
    
    /**
     * Opens a file with the associated application.
     *
     * @param file The file.
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean open(File file) {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.open(Objects.requireNonNull(file));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Edits a file with the associated application.
     *
     * @param file The file.
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean edit(File file) {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.edit(Objects.requireNonNull(file));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Prints a file with the native desktop printing facility.
     *
     * @param file The file.
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean print(File file) {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.print(Objects.requireNonNull(file));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Browses to a file and selects it with the native file explorer.
     *
     * @param file The file.
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean browse(File file) {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.browse(Objects.requireNonNull(file));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Moves a file to the trash.
     *
     * @param file The file.
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean trash(File file) {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.trash(Objects.requireNonNull(file));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Navigates to a url with the default browser.
     *
     * @param uri the url.
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean navigate(URI uri) {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.navigate(Objects.requireNonNull(uri));
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Launches the default mail client.
     *
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean mail() {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.mail();
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    /**
     * Launches the default help viewer application.
     *
     * @return Whether the desktop operation was performed or not.
     */
    public static boolean help() {
        if (isDesktopSupported()) {
            try {
                DesktopWrapper.help();
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
    
    
    //Inner Classes
    
    /**
     * Wraps the desktop calls from Desktop.
     */
    private static final class DesktopWrapper {
        
        //Static Methods
        
        /**
         * Opens a file with the associated application.
         *
         * @param file The file.
         * @throws Exception When there is an exception.
         */
        private static void open(File file) throws Exception {
            desktop.open(file);
        }
        
        /**
         * Edits a file with the associated application.
         *
         * @param file The file.
         * @throws Exception When there is an exception.
         */
        private static void edit(File file) throws Exception {
            desktop.edit(file);
        }
        
        /**
         * Prints a file with the native desktop printing facility.
         *
         * @param file The file.
         * @throws Exception When there is an exception.
         */
        private static void print(File file) throws Exception {
            desktop.print(file);
        }
        
        /**
         * Browses to a file and selects it with the native file explorer.
         *
         * @param file The file.
         * @throws Exception When there is an exception.
         */
        private static void browse(File file) throws Exception {
            desktop.browseFileDirectory(file);
        }
        
        /**
         * Moves a file to the trash.
         *
         * @param file The file.
         * @throws Exception When there is an exception.
         */
        private static void trash(File file) throws Exception {
            desktop.moveToTrash(file);
        }
        
        /**
         * Navigates to a url with the default browser.
         *
         * @param uri the url.
         * @throws Exception When there is an exception.
         */
        private static void navigate(URI uri) throws Exception {
            desktop.browse(uri);
        }
        
        /**
         * Launches the default mail client.
         *
         * @throws Exception When there is an exception.
         */
        private static void mail() throws Exception {
            desktop.mail();
        }
        
        /**
         * Launches the default help viewer application.
         *
         * @throws Exception When there is an exception.
         */
        private static void help() throws Exception {
            desktop.openHelpViewer();
        }
        
    }
    
}
