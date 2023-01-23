/*
 * File:    ConfigData.java
 * Package: youtube.config.base
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.config.base;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import commons.lambda.function.checked.CheckedFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines Config Data.
 */
public class ConfigData {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ConfigData.class);
    
    
    //Fields
    
    /**
     * The raw json Config Data.
     */
    public Map<String, Object> configData;
    
    
    //Constructors
    
    /**
     * Creates a Config Data.
     *
     * @param configData The json Config Data.
     */
    public ConfigData(Map<String, Object> configData) {
        this.configData = configData;
    }
    
    /**
     * Creates an empty Config Data.
     */
    public ConfigData() {
    }
    
    
    //Methods
    
    /**
     * Parses a field from the Config Data.
     *
     * @param path The path to the requested field.
     * @param <R>  The type of the requested field.
     * @return The value of the field; or null if it does not exist.
     */
    protected <R> R parseData(String... path) {
        return fetch(getConfigData(), path);
    }
    
    /**
     * Parses a field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the value of the field.
     */
    protected Optional<Object> parseField(String... path) {
        return Optional.ofNullable(parseData(path));
    }
    
    /**
     * Parses a string field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the string value of the field.
     */
    protected Optional<String> parseString(String... path) {
        return parseField(path)
                .map(String::valueOf);
    }
    
    /**
     * Parses a boolean field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the boolean value of the field.
     */
    protected Optional<Boolean> parseBoolean(String... path) {
        return parseString(path)
                .map((CheckedFunction<String, Boolean>) Boolean::parseBoolean);
    }
    
    /**
     * Parses an integer field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the integer value of the field.
     */
    protected Optional<Integer> parseInteger(String... path) {
        return parseString(path)
                .map((CheckedFunction<String, Integer>) Integer::parseInt);
    }
    
    /**
     * Parses a long field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the long value of the field.
     */
    protected Optional<Long> parseLong(String... path) {
        return parseString(path)
                .map((CheckedFunction<String, Long>) Long::parseLong);
    }
    
    /**
     * Parses a double field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the double value of the field.
     */
    protected Optional<Double> parseDouble(String... path) {
        return parseString(path)
                .map((CheckedFunction<String, Double>) Double::parseDouble);
    }
    
    /**
     * Parses a float field from the Config Data.
     *
     * @param path The path to the requested field.
     * @return The optional containing the float value of the field.
     */
    protected Optional<Float> parseFloat(String... path) {
        return parseString(path)
                .map((CheckedFunction<String, Float>) Float::parseFloat);
    }
    
    /**
     * Parses a map from the Config Data.
     *
     * @param path The path to the requested map.
     * @return The optional containing the value of the map.
     */
    protected Optional<Map<String, Object>> parseMap(String... path) {
        return Optional.ofNullable(parseData(path));
    }
    
    /**
     * Parses a list from the Config Data.
     *
     * @param path The path to the requested list.
     * @return The optional containing the value of the list.
     */
    protected Optional<List<Object>> parseList(String... path) {
        return Optional.ofNullable(parseData(path));
    }
    
    
    //Getters
    
    /**
     * Returns the raw json Config Data.
     *
     * @return The raw json Config Data.
     */
    public Map<String, Object> getConfigData() {
        return configData;
    }
    
    
    //Static Methods
    
    /**
     * Fetches a field from a json data map.
     *
     * @param data The json data map.
     * @param path The path to the requested field.
     * @param <K>  The type of the keys of the map.
     * @param <V>  The type of the values of the map.
     * @param <R>  The type of the requested field.
     * @return The value of the field; or null if it does not exist.
     */
    @SuppressWarnings("unchecked")
    private static <K, V, R extends V> R fetch(Map<K, V> data, K... path) {
        return (data == null) ? null :
               (path.length == 0) ? (R) data :
               (path.length == 1) ? (R) data.get(path[0]) :
               fetch(Arrays.stream(path, 0, path.length - 1).sequential()
                               .reduce(data, ConfigData::fetch, (p, q) -> null),
                       path[path.length - 1]);
    }
    
}
