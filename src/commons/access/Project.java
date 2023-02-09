/*
 * File:    Project.java
 * Package: commons.access
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.access;

import java.io.File;
import java.util.regex.Matcher;

import commons.object.string.EntityStringUtility;

/**
 * Defines directories for the project.
 */
public final class Project {
    
    //Constants
    
    /**
     * The project source directory.
     */
    public static final File SOURCE_DIR = new File("src");
    
    /**
     * The project test directory.
     */
    public static final File TEST_DIR = new File("test");
    
    /**
     * The project data directory.
     */
    public static final File DATA_DIR = new File("data");
    
    /**
     * The project resources directory.
     */
    public static final File RESOURCES_DIR = new File("resources");
    
    /**
     * The project test resources directory.
     */
    public static final File TEST_RESOURCES_DIR = new File("test-resources");
    
    /**
     * The project output directory.
     */
    public static final File OUTPUT_DIR = new File("bin");
    
    /**
     * The project source classes directory.
     */
    public static final File SOURCE_CLASSES_DIR = new File(OUTPUT_DIR, "classes");
    
    /**
     * The project test classes directory.
     */
    public static final File TEST_CLASSES_DIR = new File(OUTPUT_DIR, "test-classes");
    
    /**
     * The project log directory.
     */
    public static final File LOG_DIR = new File("log");
    
    /**
     * The project temporary directory.
     */
    public static final File TMP_DIR = new File("tmp");
    
    
    //Static Methods
    
    /**
     * Ensures the project directories exist and creates them if needed.
     *
     * @return Whether the project directories are successfully initialized.
     */
    public static boolean initializeProjectDirectories() {
        return ((Filesystem.exists(SOURCE_DIR) || Filesystem.createDirectory(SOURCE_DIR)) &&
                (Filesystem.exists(TEST_DIR) || Filesystem.createDirectory(TEST_DIR)) &&
                (Filesystem.exists(DATA_DIR) || Filesystem.createDirectory(DATA_DIR)) &&
                (Filesystem.exists(RESOURCES_DIR) || Filesystem.createDirectory(RESOURCES_DIR)) &&
                (Filesystem.exists(TEST_RESOURCES_DIR) || Filesystem.createDirectory(TEST_RESOURCES_DIR)) &&
                (Filesystem.exists(OUTPUT_DIR) || Filesystem.createDirectory(OUTPUT_DIR)) &&
                (Filesystem.exists(SOURCE_CLASSES_DIR) || Filesystem.createDirectory(SOURCE_CLASSES_DIR)) &&
                (Filesystem.exists(TEST_CLASSES_DIR) || Filesystem.createDirectory(TEST_CLASSES_DIR)) &&
                (Filesystem.exists(LOG_DIR) || Filesystem.createDirectory(LOG_DIR)) &&
                (Filesystem.exists(TMP_DIR) || Filesystem.createDirectory(TMP_DIR)) && Filesystem.clearDirectory(TMP_DIR));
    }
    
    /**
     * Returns the source directory for a particular class.
     *
     * @param clazz The class.
     * @return The source directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File sourceDir(Class<?> clazz) {
        return classDir(SOURCE_DIR, "", clazz, false);
    }
    
    /**
     * Returns the test directory for a particular class.
     *
     * @param clazz The class.
     * @return The test directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File testDir(Class<?> clazz) {
        return classDir(TEST_DIR, "", clazz, false);
    }
    
    /**
     * Returns the data directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project data directory.
     * @return The data directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File dataDir(Class<?> clazz, String prefix) {
        return classDir(DATA_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the data directory for a particular class.
     *
     * @param clazz The class.
     * @return The data directory for the specified class.
     * @see #dataDir(Class, String)
     */
    public static File dataDir(Class<?> clazz) {
        return dataDir(clazz, "");
    }
    
    /**
     * Returns the resources directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project resources directory.
     * @return The resources directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File resourcesDir(Class<?> clazz, String prefix) {
        return classDir(RESOURCES_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the resources directory for a particular class.
     *
     * @param clazz The class.
     * @return The resources directory for the specified class.
     * @see #resourcesDir(Class, String)
     */
    public static File resourcesDir(Class<?> clazz) {
        return resourcesDir(clazz, "");
    }
    
    /**
     * Returns the test resources directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project test resources directory.
     * @return The test resources directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File testResourcesDir(Class<?> clazz, String prefix) {
        return classDir(TEST_RESOURCES_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the test resources directory for a particular class.
     *
     * @param clazz The class.
     * @return The test resources directory for the specified class.
     * @see #testResourcesDir(Class, String)
     */
    public static File testResourcesDir(Class<?> clazz) {
        return testResourcesDir(clazz, "");
    }
    
    /**
     * Returns the source classes directory for a particular class.
     *
     * @param clazz The class.
     * @return The source classes directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File sourceClassesDir(Class<?> clazz) {
        return classDir(SOURCE_CLASSES_DIR, "", clazz, false);
    }
    
    /**
     * Returns the test classes directory for a particular class.
     *
     * @param clazz The class.
     * @return The test classes directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File testClassesDir(Class<?> clazz) {
        return classDir(TEST_CLASSES_DIR, "", clazz, false);
    }
    
    /**
     * Returns the log directory for a particular class.
     *
     * @param clazz  The class.
     * @param prefix The prefix within the project log directory.
     * @return The log directory for the specified class.
     * @see #classDir(File, String, Class, boolean)
     */
    public static File logDir(Class<?> clazz, String prefix) {
        return classDir(LOG_DIR, prefix, clazz, true);
    }
    
    /**
     * Returns the log directory for a particular class.
     *
     * @param clazz The class.
     * @return The log directory for the specified class.
     * @see #logDir(Class, String)
     */
    public static File logDir(Class<?> clazz) {
        return logDir(clazz, "");
    }
    
    /**
     * Returns class directory for a particular project dir.
     *
     * @param projectDir The project directory.
     * @param prefix     The prefix within the project directory
     * @param clazz      The class.
     * @param classOwned Whether the class directory is private to the class or not.
     * @return The class directory for the specified project dir.
     */
    private static File classDir(File projectDir, String prefix, Class<?> clazz, boolean classOwned) {
        String justifiedPrefix = prefix.replaceAll("[\\\\/]+", Matcher.quoteReplacement(File.separator));
        String classPath = justifiedPrefix + (justifiedPrefix.endsWith(File.separator) ? "" : File.separator) +
                clazz.getPackage().getName().replace(".", File.separator) +
                (classOwned ? (File.separator + EntityStringUtility.simpleClassString(clazz)) : "");
        return new File(projectDir.getPath(), classPath);
    }
    
}
