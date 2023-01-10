/*
 * File:    ChannelJsonFormatter.java
 * Package: youtube.channel.util
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/YoutubeDownloader
 */

package youtube.channel.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import commons.object.collection.ListUtility;
import commons.object.string.StringUtility;
import youtube.channel.ChannelConfig;
import youtube.channel.ChannelEntry;
import youtube.channel.ChannelGroup;

/**
 * Handles the formatting of Channel json strings.
 */
public final class ChannelJsonFormatter {
    
    //Constants
    
    /**
     * The map of default field values in the data of a Channel Entry; the default value of fields that are not included is null.
     */
    private static final Map<String, Object> DEFAULT_FIELD_VALUES = Map.of(
            "active", ChannelEntry.DEFAULT_ACTIVE,
            "saveAsMp3", ChannelEntry.DEFAULT_SAVE_AS_MP3,
            "savePlaylist", ChannelEntry.DEFAULT_SAVE_PLAYLIST,
            "reversePlaylist", ChannelEntry.DEFAULT_REVERSE_PLAYLIST,
            "ignoreGlobalLocations", ChannelEntry.DEFAULT_IGNORE_GLOBAL_LOCATIONS,
            "keepClean", ChannelEntry.DEFAULT_KEEP_CLEAN);
    
    /**
     * The number of spaces in an indent in a json string.
     */
    public static final int INDENT_WIDTH = 2;
    
    /**
     * The default indent to start with when formatting a json string.
     */
    public static final int DEFAULT_INDENT = 1;
    
    
    //Enums
    
    /**
     * An enumeration of Json Types.
     */
    public enum JsonType {
        FULL,
        BASE,
        MIN
    }
    
    
    //Static Methods
    
    /**
     * Produces a json string representing a Channel Entry.
     *
     * @param type         The type of the json string.
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @param indent       The indent of the json string.
     * @return The json string representing the Channel Entry.
     */
    public static String toJsonString(JsonType type, ChannelEntry channelEntry, boolean effective, int indent) {
        switch (type) {
            case FULL:
                return toFullJsonString(channelEntry, effective, indent);
            case BASE:
                return toBaseJsonString(channelEntry, effective, indent);
            case MIN:
                return toMinJsonString(channelEntry, effective, indent);
            default:
                return "";
        }
    }
    
    /**
     * Produces a json string representing a Channel Entry.
     *
     * @param type         The type of the json string.
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @return The json string representing the Channel Entry.
     */
    public static String toJsonString(JsonType type, ChannelEntry channelEntry, boolean effective) {
        return toJsonString(type, channelEntry, effective, DEFAULT_INDENT);
    }
    
    /**
     * Produces a json string representing a Channel Entry.
     *
     * @param type         The type of the json string.
     * @param channelEntry The Channel Entry.
     * @return The json string representing the Channel Entry.
     */
    public static String toJsonString(JsonType type, ChannelEntry channelEntry) {
        return toJsonString(type, channelEntry, false);
    }
    
    /**
     * Produces a full json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @param indent       The indent of the json string.
     * @return The full json string representing the Channel Entry.
     */
    public static String toFullJsonString(ChannelEntry channelEntry, boolean effective, int indent) {
        return formatJson(JsonType.FULL, getFields(channelEntry, effective), getFieldsList(channelEntry), effective, indent);
    }
    
    /**
     * Produces a full json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @return The full json string representing the Channel Entry.
     */
    public static String toFullJsonString(ChannelEntry channelEntry, boolean effective) {
        return toFullJsonString(channelEntry, effective, DEFAULT_INDENT);
    }
    
    /**
     * Produces a full json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @return The full json string representing the Channel Entry.
     */
    public static String toFullJsonString(ChannelEntry channelEntry) {
        return toFullJsonString(channelEntry, false);
    }
    
    /**
     * Produces a base json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @param indent       The indent of the json string.
     * @return The base json string representing the Channel Entry.
     */
    public static String toBaseJsonString(ChannelEntry channelEntry, boolean effective, int indent) {
        final Map<String, Object> fields = getFields(channelEntry, effective);
        final List<String> toInclude = getFieldsList(channelEntry).stream()
                .filter(field -> getBaseFieldList(channelEntry).contains(field) || !Objects.equals(fields.get(field), DEFAULT_FIELD_VALUES.get(field)))
                .collect(Collectors.toList());
        
        return formatJson(JsonType.BASE, fields, toInclude, effective, indent);
    }
    
    /**
     * Produces a base json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @return The base json string representing the Channel Entry.
     */
    public static String toBaseJsonString(ChannelEntry channelEntry, boolean effective) {
        return toBaseJsonString(channelEntry, effective, DEFAULT_INDENT);
    }
    
    /**
     * Produces a base json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @return The base json string representing the Channel Entry.
     */
    public static String toBaseJsonString(ChannelEntry channelEntry) {
        return toBaseJsonString(channelEntry, false);
    }
    
    /**
     * Produces a minimal json string representing a Channel Entry.
     *
     * @param channelEntry       The Channel Entry.
     * @param effective          Whether to produce the json string from the effective fields of the Channel Entry.
     * @param forceIncludeFields A list of fields to forcefully include in the json string.
     * @param forceExcludeFields A list of fields to forcefully exclude from the json string.
     * @param indent             The indent of the json string.
     * @return The minimal json string representing the Channel Entry.
     */
    public static String toMinJsonString(ChannelEntry channelEntry, boolean effective, List<String> forceIncludeFields, List<String> forceExcludeFields, int indent) {
        final Map<String, Object> fields = getFields(channelEntry, effective);
        final List<String> toInclude = getFieldsList(channelEntry).stream()
                .filter(field -> Objects.nonNull(fields.get(field)))
                .filter(field -> getRequiredFieldList(channelEntry).contains(field) ||
                        !Objects.equals(fields.get(field), DEFAULT_FIELD_VALUES.get(field)))
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        fieldList -> {
                            fieldList.addAll(forceIncludeFields);
                            fieldList.removeAll(forceExcludeFields);
                            return ListUtility.removeDuplicates(fieldList);
                        }));
        
        return formatJson(JsonType.MIN, fields, toInclude, effective, indent);
    }
    
    /**
     * Produces a minimal json string representing a Channel Entry.
     *
     * @param channelEntry       The Channel Entry.
     * @param effective          Whether to produce the json string from the effective fields of the Channel Entry.
     * @param forceIncludeFields A list of fields to forcefully include in the json string.
     * @param forceExcludeFields A list of fields to forcefully exclude from the json string.
     * @return The minimal json string representing the Channel Entry.
     */
    public static String toMinJsonString(ChannelEntry channelEntry, boolean effective, List<String> forceIncludeFields, List<String> forceExcludeFields) {
        return toMinJsonString(channelEntry, false, forceIncludeFields, forceExcludeFields, DEFAULT_INDENT);
    }
    
    /**
     * Produces a minimal json string representing a Channel Entry.
     *
     * @param channelEntry       The Channel Entry.
     * @param forceIncludeFields A list of fields to forcefully include in the json string.
     * @param forceExcludeFields A list of fields to forcefully exclude from the json string.
     * @return The minimal json string representing the Channel Entry.
     */
    public static String toMinJsonString(ChannelEntry channelEntry, List<String> forceIncludeFields, List<String> forceExcludeFields) {
        return toMinJsonString(channelEntry, false, forceIncludeFields, forceExcludeFields);
    }
    
    /**
     * Produces a minimal json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @param indent       The indent of the json string.
     * @return The minimal json string representing the Channel Entry.
     */
    public static String toMinJsonString(ChannelEntry channelEntry, boolean effective, int indent) {
        return toMinJsonString(channelEntry, effective, Collections.emptyList(), Collections.emptyList(), indent);
    }
    
    /**
     * Produces a minimal json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to produce the json string from the effective fields of the Channel Entry.
     * @return The minimal json string representing the Channel Entry.
     */
    public static String toMinJsonString(ChannelEntry channelEntry, boolean effective) {
        return toMinJsonString(channelEntry, effective, DEFAULT_INDENT);
    }
    
    /**
     * Produces a minimal json string representing a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @return The minimal json string representing the Channel Entry.
     */
    public static String toMinJsonString(ChannelEntry channelEntry) {
        return toMinJsonString(channelEntry, false);
    }
    
    /**
     * Formats a Channel Entry json string.
     *
     * @param type      The type of the json string.
     * @param fields    The map of fields of the Channel Entry.
     * @param toInclude The list of fields to include in the json string.
     * @param effective Whether to use the effective fields of the Channel Entry.
     * @param indent    The indent of the json string.
     * @return The formatted json string.
     */
    private static String formatJson(JsonType type, Map<String, Object> fields, List<String> toInclude, boolean effective, int indent) {
        return fields.entrySet().stream()
                .filter(e -> toInclude.contains(e.getKey()))
                .map(e -> StringUtility.spaces(indent * INDENT_WIDTH) +
                        StringUtility.quote(e.getKey()) + ": " + formatValue(type, e.getValue(), effective, indent))
                .collect(Collectors.joining(("," + System.lineSeparator()),
                        (StringUtility.spaces((indent - 1) * INDENT_WIDTH) + "{" + System.lineSeparator()),
                        (System.lineSeparator() + StringUtility.spaces((indent - 1) * INDENT_WIDTH) + "}")));
    }
    
    /**
     * Formats a Channel Entry json string.
     *
     * @param type      The type of the json string.
     * @param fields    The map of fields of the Channel Entry.
     * @param toInclude The list of fields to include in the json string.
     * @param effective Whether to use the effective fields of the Channel Entry.
     * @return The formatted json string.
     */
    private static String formatJson(JsonType type, Map<String, Object> fields, List<String> toInclude, boolean effective) {
        return formatJson(type, fields, toInclude, effective, DEFAULT_INDENT);
    }
    
    /**
     * Formats a Channel Entry json string.
     *
     * @param type      The type of the json string.
     * @param fields    The map of fields of the Channel Entry.
     * @param toInclude The list of fields to include in the json string.
     * @return The formatted json string.
     */
    private static String formatJson(JsonType type, Map<String, Object> fields, List<String> toInclude) {
        return formatJson(type, fields, toInclude, false);
    }
    
    /**
     * Formats a Channel Entry json value.
     *
     * @param type      The type of the json string.
     * @param value     The value.
     * @param effective Whether to use the effective fields of the Channel Entry.
     * @param indent    The indent of the json string.
     * @return The formatted json value.
     */
    @SuppressWarnings("unchecked")
    private static String formatValue(JsonType type, Object value, boolean effective, int indent) {
        if (value instanceof List) {
            return ((List<ChannelEntry>) value).stream()
                    .map(child -> toJsonString(type, child, effective, (indent + 2)))
                    .collect(Collectors.joining(("," + System.lineSeparator()),
                            ("[" + System.lineSeparator()),
                            (System.lineSeparator() + StringUtility.spaces(indent * INDENT_WIDTH) + "]")));
        } else if (value instanceof String) {
            return StringUtility.quote(String.valueOf(value));
        } else {
            return String.valueOf(value);
        }
    }
    
    /**
     * Returns the required fields of a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @return The required fields of a Channel Entry.
     */
    private static List<String> getRequiredFieldList(ChannelEntry channelEntry) {
        return channelEntry.isGroup() ? ChannelGroup.REQUIRED_FIELDS : ChannelConfig.REQUIRED_FIELDS;
    }
    
    /**
     * Returns the base fields of a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @return The base fields of a Channel Entry.
     */
    private static List<String> getBaseFieldList(ChannelEntry channelEntry) {
        return channelEntry.isGroup() ? ChannelGroup.BASE_FIELDS : ChannelConfig.BASE_FIELDS;
    }
    
    /**
     * Returns all fields of a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @return All fields of a Channel Entry.
     */
    private static List<String> getFieldsList(ChannelEntry channelEntry) {
        return channelEntry.isGroup() ? ChannelGroup.ALL_FIELDS : ChannelConfig.ALL_FIELDS;
    }
    
    /**
     * Returns the field map of a Channel Entry.
     *
     * @param channelEntry The Channel Entry.
     * @param effective    Whether to use the effective fields of the Channel Entry.
     * @return The field map of a Channel Entry.
     */
    private static Map<String, Object> getFields(ChannelEntry channelEntry, boolean effective) {
        return effective ? channelEntry.getEffectiveConfig() : channelEntry.getConfig();
    }
    
}
