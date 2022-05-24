/*
 * File:    ArrayUtility.java
 * Package: commons.object.collection
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.collection;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import commons.math.BoundUtility;
import commons.math.MathUtility;
import commons.object.string.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides additional array functionality.
 */
public final class ArrayUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(ArrayUtility.class);
    
    
    //Static Methods
    
    /**
     * Creates a new array instance of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The array instance.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] emptyArray(Class<T> type) {
        return (T[]) Array.newInstance(type, 0);
    }
    
    /**
     * Creates a new array instance
     *
     * @return The array instance.
     * @see #emptyArray(Class)
     */
    public static Object[] emptyArray() {
        return emptyArray(Object.class);
    }
    
    /**
     * Creates a new array of a certain type and length, filled with a default value or null.
     *
     * @param type   The type of the array.
     * @param fill   The object to fill the array with, or null.
     * @param length The length of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     */
    @SuppressWarnings("unchecked")
    private static <T> T[] create(Class<T> type, T fill, int length) {
        final T[] array = (T[]) Array.newInstance(type, length);
        Arrays.fill(array, fill);
        return array;
    }
    
    /**
     * Creates a new array of a certain type and length, filled with null.
     *
     * @param type   The type of the array.
     * @param length The length of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create(Class, Object, int)
     */
    public static <T> T[] create(Class<T> type, int length) {
        return create(type, null, length);
    }
    
    /**
     * Creates a new array of a certain type and length, filled with a default value.
     *
     * @param fill   The object to fill the array with.
     * @param length The length of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create(Class, Object, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] create(T fill, int length) {
        return create((Class<T>) fill.getClass(), fill, length);
    }
    
    /**
     * Creates a new array of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create(Class, int)
     */
    public static <T> T[] create(Class<T> type) {
        return create(type, 0);
    }
    
    /**
     * Creates a new array of a certain length.
     *
     * @return The created array.
     * @see #create(Class, int)
     */
    public static Object[] create(int length) {
        return create(Object.class, length);
    }
    
    /**
     * Creates a new 2D array of a certain type and dimensions, filled with a default value, or null.
     *
     * @param type   The type of the array.
     * @param fill   The object to fill the array with, or null.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     */
    @SuppressWarnings("unchecked")
    private static <T> T[][] create2D(Class<T> type, T fill, int width, int height) {
        final T[][] array = (T[][]) Array.newInstance(type, width, height);
        IntStream.range(0, width).forEach(i -> array[i] = create(type, fill, height));
        return array;
    }
    
    /**
     * Creates a new 2D array of a certain type and dimensions, filled with null.
     *
     * @param type   The type of the array.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create2D(Class, Object, int, int)
     */
    public static <T> T[][] create2D(Class<T> type, int width, int height) {
        return create2D(type, null, width, height);
    }
    
    /**
     * Creates a new 2D array of a certain type and dimensions, filled with a default value.
     *
     * @param fill   The object to fill the array with.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create2D(Class, Object, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][] create2D(T fill, int width, int height) {
        return create2D((Class<T>) fill.getClass(), fill, width, height);
    }
    
    /**
     * Creates a new 2D array of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create2D(Class, int, int)
     */
    public static <T> T[][] create2D(Class<T> type) {
        return create2D(type, 0, 0);
    }
    
    /**
     * Creates a new 2D array of certain dimensions.
     *
     * @param width  The width of the array.
     * @param height The height of the array.
     * @return The created array.
     * @see #create2D(Class, int, int)
     */
    public static Object[][] create2D(int width, int height) {
        return create2D(Object.class, width, height);
    }
    
    /**
     * Creates a new 3D array of a certain type and dimensions, filled with a default value or null.
     *
     * @param type   The type of the array.
     * @param fill   The object to fill the array with, or null.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     */
    @SuppressWarnings({"unchecked", "SuspiciousNameCombination"})
    private static <T> T[][][] create3D(Class<T> type, T fill, int width, int height, int depth) {
        final T[][][] array = (T[][][]) Array.newInstance(type, width, height, depth);
        IntStream.range(0, width).forEach(i -> array[i] = create2D(type, fill, height, depth));
        return array;
    }
    
    /**
     * Creates a new 3D array of a certain type and dimensions, filled with null.
     *
     * @param type   The type of the array.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static <T> T[][][] create3D(Class<T> type, int width, int height, int depth) {
        return create3D(type, null, width, height, depth);
    }
    
    /**
     * Creates a new 3D array of a certain type and dimensions, filled with a default value.
     *
     * @param fill   The object to fill the array with.
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @param <T>    The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[][][] create3D(T fill, int width, int height, int depth) {
        return create3D((Class<T>) fill.getClass(), fill, width, height, depth);
    }
    
    /**
     * Creates a new 3D array of a certain type.
     *
     * @param type The type of the array.
     * @param <T>  The type of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static <T> T[][][] create3D(Class<T> type) {
        return create3D(type, 0, 0, 0);
    }
    
    /**
     * Creates a new 3D array of certain dimensions.
     *
     * @param width  The width of the array.
     * @param height The height of the array.
     * @param depth  The depth of the array.
     * @return The created array.
     * @see #create3D(Class, Object, int, int, int)
     */
    public static Object[][][] create3D(int width, int height, int depth) {
        return create3D(Object.class, width, height, depth);
    }
    
    /**
     * Creates and populates a new array.
     *
     * @param type     The type of the array.
     * @param elements The elements to populate the array with.
     * @param <T>      The type of the array.
     * @return The created and populated array.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(Class<T> type, T... elements) {
        final T[] result = create(type, elements.length);
        System.arraycopy(elements, 0, result, 0, elements.length);
        return result;
    }
    
    /**
     * Creates and populates a new array.
     *
     * @param elements The elements to populate the array with.
     * @param <T>      The type of the array.
     * @return The created and populated array.
     * @see #arrayOf(Class, Object[])
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] arrayOf(T... elements) {
        return arrayOf((Class<T>) Object.class, elements);
    }
    
    /**
     * Converts a collection to an array.
     *
     * @param collection The collection.
     * @param type       The type of the array.
     * @param <T>        The type of the array.
     * @return The array built from the collection.
     * @see #arrayOf(Class, Object[])
     */
    public static <T> T[] toArray(Collection<T> collection, Class<T> type) {
        return arrayOf(type, collection.toArray(i -> create(type, i)));
    }
    
    /**
     * Converts a collection to an array.
     *
     * @param collection The collection.
     * @param <T>        The type of the array.
     * @return The array built from the collection.
     * @see #toArray(Collection, Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> collection) {
        return toArray(collection, (Class<T>) Object.class);
    }
    
    /**
     * Clones an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The clone of the array.
     */
    public static <T> T[] clone(T[] array) {
        return Arrays.copyOf(array, array.length);
    }
    
    /**
     * Creates a sub array from an array.
     *
     * @param array The array.
     * @param from  The index to start the sub array at.
     * @param to    The index to end the sub array at.
     * @param <T>   The type of the array.
     * @return The sub array.
     * @throws IndexOutOfBoundsException When the from or to indices are out of bounds of the array.
     */
    public static <T> T[] subArray(T[] array, int from, int to) throws IndexOutOfBoundsException {
        if ((from > to) || (from < 0) || (to > array.length)) {
            throw new IndexOutOfBoundsException("The range [" + from + "," + to + ") is out of bounds of the array");
        }
        
        return Arrays.copyOfRange(array, from, to);
    }
    
    /**
     * Creates a sub array from an array.
     *
     * @param array The array.
     * @param from  The index to start the sub array at.
     * @param <T>   The type of the array.
     * @return The sub array.
     * @throws IndexOutOfBoundsException When the from or to indices are out of bounds of the array.
     * @see #subArray(Object[], int, int)
     */
    public static <T> T[] subArray(T[] array, int from) throws IndexOutOfBoundsException {
        return subArray(array, from, array.length);
    }
    
    /**
     * Merges two arrays.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @param type   The type of the arrays.
     * @param <T>    The type of the arrays.
     * @return The merged array.
     */
    public static <T> T[] merge(T[] array1, T[] array2, Class<T> type) {
        final T[] result = create(type, (array1.length + array2.length));
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
    
    /**
     * Splits an array into an array of arrays of a certain length.
     *
     * @param array  The array.
     * @param length The length of the resulting arrays.
     * @param type   The type of the array.
     * @param <T>    The type of the array.
     * @return The array of arrays of the specified length.
     */
    public static <T> T[][] split(T[] array, int length, Class<T> type) {
        final int split = BoundUtility.truncate(length, 1, array.length);
        final T[][] result = create2D(type, (int) Math.ceil(array.length / (double) split), split);
        IntStream.range(0, array.length).forEach(i ->
                result[i / split][i % split] = array[i]);
        return result;
    }
    
    /**
     * Reverses an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The reversed array.
     */
    public static <T> T[] reverse(T[] array) {
        final T[] reversed = clone(array);
        IntStream.range(0, array.length / 2).forEach(i -> {
            final T tmp = reversed[i];
            reversed[i] = reversed[reversed.length - i - 1];
            reversed[reversed.length - i - 1] = tmp;
        });
        return reversed;
    }
    
    /**
     * Shuffles an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The shuffled array.
     */
    public static <T> T[] shuffle(T[] array) {
        final T[] shuffled = clone(array);
        IntStream.range(0, array.length).forEach(i -> {
            final int index = MathUtility.random(i, (array.length - 1));
            final T tmp = array[index];
            array[index] = array[i];
            array[i] = tmp;
        });
        return shuffled;
    }
    
    /**
     * Determines if an array is null or empty.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return Whether the array is null or empty.
     */
    public static <T> boolean isNullOrEmpty(T[] array) {
        return (array == null) || (array.length == 0);
    }
    
    /**
     * Determines if an array equals another array.
     *
     * @param array1     The first array.
     * @param array2     The second array.
     * @param checkOrder Whether to check the order of the arrays or not.
     * @param <T>        The type of the first array.
     * @param <T2>       The type of the second array.
     * @return Whether the arrays are equal or not.
     */
    public static <T, T2> boolean equals(T[] array1, T2[] array2, boolean checkOrder) {
        return ((array1 == null) || (array2 == null)) ? ((array1 == null) && (array2 == null)) : ((array1.length == array2.length) &&
                (checkOrder ? IntStream.range(0, array1.length).allMatch(i -> Objects.equals(array1[i], array2[i])) :
                 Arrays.stream(array1).allMatch(e -> contains(array2, e) && (numberOfOccurrences(array1, e) == numberOfOccurrences(array2, e)))));
    }
    
    /**
     * Determines if an array equals another array.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @param <T>    The type of the first array.
     * @param <T2>   The type of the second array.
     * @return Whether the arrays are equal or not.
     * @see #equals(Object[], Object[], boolean)
     */
    public static <T, T2> boolean equals(T[] array1, T2[] array2) {
        return equals(array1, array2, true);
    }
    
    /**
     * Determines if an array of strings equals another array of strings, regardless of case.
     *
     * @param array1     The first array.
     * @param array2     The second array.
     * @param checkOrder Whether to check the order of the arrays or not.
     * @return Whether the arrays of strings are equal or not, regardless of case.
     */
    public static boolean equalsIgnoreCase(String[] array1, String[] array2, boolean checkOrder) {
        return ((array1 == null) || (array2 == null)) ? ((array1 == null) && (array2 == null)) : ((array1.length == array2.length) &&
                (checkOrder ? IntStream.range(0, array1.length).allMatch(i -> StringUtility.equalsIgnoreCase(array1[i], array2[i])) :
                 Arrays.stream(array1).allMatch(e -> containsIgnoreCase(array2, e) && (numberOfOccurrencesIgnoreCase(array1, e) == numberOfOccurrencesIgnoreCase(array2, e)))));
    }
    
    /**
     * Determines if an array of strings equals another array of strings, regardless of case.
     *
     * @param array1 The first array.
     * @param array2 The second array.
     * @return Whether the arrays of strings are equal or not, regardless of case.
     * @see #equalsIgnoreCase(String[], String[], boolean)
     */
    public static boolean equalsIgnoreCase(String[] array1, String[] array2) {
        return equalsIgnoreCase(array1, array2, true);
    }
    
    /**
     * Determines if an element exists in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return Whether the array contains the specified element or not.
     */
    public static <T> boolean contains(T[] array, T element) {
        return !isNullOrEmpty(array) && Arrays.asList(array).contains(element);
    }
    
    /**
     * Determines if a string exists in an array, regardless of case.
     *
     * @param array   The array.
     * @param element The element.
     * @return Whether the array contains the specified string or not, regardless of case.
     */
    public static boolean containsIgnoreCase(String[] array, String element) {
        return !isNullOrEmpty(array) && Arrays.stream(array).anyMatch(e -> StringUtility.equalsIgnoreCase(e, element));
    }
    
    /**
     * Determines the number of occurrences of an element in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return The number of occurrences of the specified element in the array.
     */
    public static <T> int numberOfOccurrences(T[] array, T element) {
        return isNullOrEmpty(array) ? 0 :
               (int) Arrays.stream(array).filter(e -> Objects.equals(e, element)).count();
    }
    
    /**
     * Determines the number of occurrences of a string element in an array, regardless of case.
     *
     * @param array   The array.
     * @param element The element.
     * @return The number of occurrences of the specified string element in the array, regardless of case.
     */
    public static int numberOfOccurrencesIgnoreCase(String[] array, String element) {
        return isNullOrEmpty(array) ? 0 :
               (int) Arrays.stream(array).filter(e -> StringUtility.equalsIgnoreCase(e, element)).count();
    }
    
    /**
     * Returns the index of an element in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return The index of the element in the array, or -1 if it does not exist.
     */
    public static <T> int indexOf(T[] array, T element) {
        return isNullOrEmpty(array) ? -1 :
               Arrays.asList(array).indexOf(element);
    }
    
    /**
     * Returns the index of a string in an array, regardless of case.
     *
     * @param array   The array.
     * @param element The element.
     * @return The index of the string in the array, regardless of case, or -1 if it does not exist.
     */
    public static int indexOfIgnoreCase(String[] array, String element) {
        return isNullOrEmpty(array) ? -1 :
               IntStream.range(0, array.length)
                       .filter(i -> StringUtility.equalsIgnoreCase(array[i], element))
                       .findFirst().orElse(-1);
    }
    
    /**
     * Returns an element from an array at a specified index, or a default value if the index is invalid.
     *
     * @param array        The array.
     * @param index        The index.
     * @param defaultValue The default value.
     * @param <T>          The type of the array.
     * @return The element in the array at the specified index, or the default value if the index is invalid.
     */
    public static <T> T getOrDefault(T[] array, int index, T defaultValue) {
        return (!isNullOrEmpty(array) && BoundUtility.inArrayBounds(index, array)) ?
               array[index] : defaultValue;
    }
    
    /**
     * Returns an element from an array at a specified index, or null if the index is invalid.
     *
     * @param array The array.
     * @param index The index.
     * @param <T>   The type of the array.
     * @return The element in the array at the specified index, or null if the index is invalid.
     * @see #getOrDefault(Object[], int, Object)
     */
    public static <T> T getOrNull(T[] array, int index) {
        return getOrDefault(array, index, null);
    }
    
    /**
     * Determines if any element in an array is null.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return Whether or not any element in the array is null.
     */
    public static <T> boolean anyNull(T[] array) {
        return Arrays.stream(array).anyMatch(Objects::isNull);
    }
    
    /**
     * Removes null elements from an array.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return The array with null elements removed.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] removeNull(T[] array) {
        return (T[]) Arrays.stream(array).filter(Objects::nonNull).toArray();
    }
    
    /**
     * Removes duplicate elements from an array.
     *
     * @param array The array to operate on.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return The array with duplicate elements removed.
     */
    public static <T> T[] removeDuplicates(T[] array, Class<T> type) {
        return Arrays.stream(array).distinct().toArray(i -> create(type, i));
    }
    
    /**
     * Selects a random element from an array.
     *
     * @param array The array to select from.
     * @param <T>   The type of the array.
     * @return A random element from the array.
     */
    public static <T> T selectRandom(T[] array) {
        return isNullOrEmpty(array) ? null :
               array[MathUtility.random(array.length - 1)];
    }
    
    /**
     * Selects a random subset of an array.
     *
     * @param array The array to select from.
     * @param n     The number of elements to select.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return A random subset of the array.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static <T> T[] selectN(T[] array, int n, Class<T> type) {
        if (n >= array.length) {
            return shuffle(array);
        }
        
        final List<Integer> previousChoices = ListUtility.emptyList();
        final T[] choices = create(type, Math.max(n, 0));
        IntStream.range(0, n).forEach(i -> {
            int index;
            while (previousChoices.contains(index = MathUtility.random(array.length - 1))) {
            }
            choices[i] = array[index];
            previousChoices.add(index);
        });
        return choices;
    }
    
    /**
     * Copies an array to the end of itself a number of times making an array n times the original length.
     *
     * @param array The array to duplicate.
     * @param times The number of copies of the array to add.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return An array of double size with duplicated elements.
     */
    public static <T> T[] duplicateInOrder(T[] array, int times, Class<T> type) {
        return (times <= 0) ? create(type) :
               (times == 1) ? clone(array) :
               IntStream.range(0, times).mapToObj(i -> array).flatMap(Arrays::stream).toArray(i -> create(type, i));
    }
    
    /**
     * Copies an array to the end of itself making an array double the original length.
     *
     * @param array The array to duplicate.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return A array of double size with duplicated elements.
     * @see #duplicateInOrder(Object[], int, Class)
     */
    public static <T> T[] duplicateInOrder(T[] array, Class<T> type) {
        return duplicateInOrder(array, 2, type);
    }
    
    /**
     * Sorts an array by the number of occurrences of each entry in the array.
     *
     * @param array   The array to sort.
     * @param reverse Whether to sort in reverse or not.
     * @param type    The type of the array.
     * @param <T>     The type of the array.
     * @return The array sorted by the number of occurrences of each entry in the array.
     */
    public static <T> T[] sortByNumberOfOccurrences(T[] array, boolean reverse, Class<T> type) {
        final Map<T, Integer> store = new LinkedHashMap<>();
        Arrays.stream(array).forEach(entry -> {
            store.putIfAbsent(entry, 0);
            store.replace(entry, store.get(entry) + 1);
        });
        
        return store.entrySet().stream()
                .sorted(reverse ? Map.Entry.comparingByValue() : Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(e -> create(e.getKey(), e.getValue()))
                .flatMap(Arrays::stream).toArray(i -> create(type, i));
    }
    
    /**
     * Sorts an array by the number of occurrences of each entry in the array.
     *
     * @param array The array to sort.
     * @param type  The type of the array.
     * @param <T>   The type of the array.
     * @return The array sorted by the number of occurrences of each entry in the array.
     * @see #sortByNumberOfOccurrences(Object[], boolean, Class)
     */
    public static <T> T[] sortByNumberOfOccurrences(T[] array, Class<T> type) {
        return sortByNumberOfOccurrences(array, false, type);
    }
    
}
