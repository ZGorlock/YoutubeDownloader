/*
 * File:    CastUtility.java
 * Package: commons.object
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import commons.object.collection.MapUtility;
import commons.object.collection.map.BiMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional object casting functionality.
 */
public final class CastUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(CastUtility.class);
    
    
    //Constants
    
    /**
     * A bidirectional map between boxed classes and the primitive equivalents.
     */
    @SuppressWarnings("unchecked")
    public static final BiMap<Class<?>, Class<?>> PRIMITIVE_CLASS_MAP = new BiMap<>(MapUtility.mapOf(
            new ImmutablePair<>(Boolean.class, boolean.class),
            new ImmutablePair<>(Byte.class, byte.class),
            new ImmutablePair<>(Short.class, short.class),
            new ImmutablePair<>(Integer.class, int.class),
            new ImmutablePair<>(Long.class, long.class),
            new ImmutablePair<>(Float.class, float.class),
            new ImmutablePair<>(Double.class, double.class),
            new ImmutablePair<>(Character.class, char.class)));
    
    
    //Static Methods
    
    /**
     * Casts an Object to a Boolean.
     *
     * @param object The Object.
     * @return The Boolean cast from the Object, or null if it cannot be cast.
     */
    public static Boolean toBoolean(Object object) {
        try {
            return Boolean.parseBoolean(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Byte.
     *
     * @param object The Object.
     * @return The Byte cast from the Object, or null if it cannot be cast.
     */
    public static Byte toByte(Object object) {
        try {
            return Byte.parseByte(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Short.
     *
     * @param object The Object.
     * @return The Short cast from the Object, or null if it cannot be cast.
     */
    public static Short toShort(Object object) {
        try {
            return Short.parseShort(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to an Integer.
     *
     * @param object The Object.
     * @return The Integer cast from the Object, or null if it cannot be cast.
     */
    public static Integer toInt(Object object) {
        try {
            return Integer.parseInt(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Long.
     *
     * @param object The Object.
     * @return The Long cast from the Object, or null if it cannot be cast.
     */
    public static Long toLong(Object object) {
        try {
            return Long.parseLong(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Float.
     *
     * @param object The Object.
     * @return The Float cast from the Object, or null if it cannot be cast.
     */
    public static Float toFloat(Object object) {
        try {
            return Float.parseFloat(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Double.
     *
     * @param object The Object.
     * @return The Double cast from the Object, or null if it cannot be cast.
     */
    public static Double toDouble(Object object) {
        try {
            return Double.parseDouble(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a BigInteger.
     *
     * @param object The Object.
     * @return The BigInteger cast from the Object, or null if it cannot be cast.
     */
    public static BigInteger toBigInteger(Object object) {
        try {
            return new BigInteger(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a BigDecimal.
     *
     * @param object The Object.
     * @return The BigDecimal cast from the Object, or null if it cannot be cast.
     */
    public static BigDecimal toBigDecimal(Object object) {
        try {
            return new BigDecimal(object.toString());
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Character.
     *
     * @param object The Object.
     * @return The Character cast from the Object, or null if it cannot be cast.
     */
    public static Character toChar(Object object) {
        try {
            return (object.toString().length() == 1) ? object.toString().charAt(0) : null;
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a String.
     *
     * @param object The Object.
     * @return The String cast from the Object, or null if it cannot be cast.
     */
    public static String toString(Object object) {
        try {
            return String.valueOf(object);
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts an Object to a Class.
     *
     * @param object The Object.
     * @return The Class cast from the Object, or the Object itself if it is a Class, or null if it cannot be cast.
     */
    public static Class<?> toClass(Object object) {
        try {
            return (object instanceof Class<?>) ? (Class<?>) object : object.getClass();
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts a Class to a primitive Class.
     *
     * @param clazz The Class.
     * @return The primitive Class cast from the Class, or the Class itself if there is no corresponding primitive Class, or null if it cannot be cast.
     */
    public static Class<?> toPrimitiveClass(Class<?> clazz) {
        try {
            return PRIMITIVE_CLASS_MAP.getOrDefault(clazz, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts a Class to a non-primitive Class.
     *
     * @param clazz The Class.
     * @return The non-primitive Class cast from the Class, or the Class itself if it is not a primitive Class, or null if it cannot be cast.
     */
    public static Class<?> toNonPrimitiveClass(Class<?> clazz) {
        try {
            return PRIMITIVE_CLASS_MAP.inverseGetOrDefault(clazz, clazz);
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts a Collection to a Collection subclass.
     *
     * @param collection The Collection.
     * @param type       The subclass to cast the Collection to.
     * @param valueType  The value type of the Collection.
     * @param <T>        The value type of the Collection.
     * @param <C>        The class of the Collection
     * @param <R>        The subclass to cast the Collection to.
     * @return The Collection subclass cast from the Collection, or null if it cannot be cast.
     */
    public static <T, C extends Collection<T>, R extends Collection<T>> R toCollectionType(C collection, Class<R> type, Class<T> valueType) {
        try {
            return type.equals(collection.getClass()) ? type.cast(collection) : type.getConstructor(Collection.class).newInstance(collection);
        } catch (Exception ignored) {
            return null;
        }
    }
    
    /**
     * Casts a Map to a Map subclass.
     *
     * @param map       The Map.
     * @param type      The subclass to cast the Map to.
     * @param keyType   The key type of the Map.
     * @param valueType The value type of the Map.
     * @param <T>       The key type of the Map.
     * @param <S>       The value type of the Map.
     * @param <M>       The class of the Map.
     * @param <R>       The subclass to cast the Map to.
     * @return The Map subclass cast from the Map, or null if it cannot be cast.
     */
    public static <T, S, M extends Map<T, S>, R extends Map<T, S>> R toMapType(M map, Class<R> type, Class<T> keyType, Class<S> valueType) {
        try {
            return type.equals(map.getClass()) ? type.cast(map) : type.getConstructor(Map.class).newInstance(map);
        } catch (Exception ignored) {
            return null;
        }
    }
    
}
