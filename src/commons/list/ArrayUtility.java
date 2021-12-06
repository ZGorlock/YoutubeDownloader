/*
 * File:    ArrayUtility.java
 * Package: commons.list
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.list;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import commons.math.BoundUtility;
import commons.math.MathUtility;
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
    
    
    //Functions
    
    /**
     * Converts a list to an array.
     *
     * @param list The list.
     * @param type The type of the list.
     * @param <T>  The type of the list.
     * @return The array built from the list, or null if the list is empty.
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(List<T> list, Class<?> type) {
        return list.toArray((T[]) Array.newInstance(type, list.size()));
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
    @SuppressWarnings("unchecked")
    public static <T> T[] merge(T[] array1, T[] array2, Class<?> type) {
        T[] result = (T[]) Array.newInstance(type, (array1.length + array2.length));
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
    @SuppressWarnings("unchecked")
    public static <T> T[][] split(T[] array, int length, Class<?> type) {
        length = BoundUtility.truncateNum(length, 1, array.length).intValue();
        
        T[][] result = (T[][]) Array.newInstance(type, (int) Math.ceil(array.length / (double) length), length);
        
        for (int i = 0; i < array.length; i++) {
            result[i / length][i % length] = array[i];
        }
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
        T[] reversed = array.clone();
        for (int i = 0; i < array.length / 2; i++) {
            T tmp = reversed[i];
            reversed[i] = reversed[reversed.length - i - 1];
            reversed[reversed.length - i - 1] = tmp;
        }
        return reversed;
    }
    
    /**
     * Determines whether an element exists in an array.
     *
     * @param array   The array.
     * @param element The element.
     * @param <T>     The type of the array.
     * @return Whether the array contains the specified element or not.
     */
    public static <T> boolean contains(T[] array, T element) {
        return Arrays.asList(array).contains(element);
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
        return Arrays.asList(array).indexOf(element);
    }
    
    /**
     * Determines if any element in an array is null.
     *
     * @param array The array.
     * @param <T>   The type of the array.
     * @return Whether or not any element in the array is null.
     */
    public static <T> boolean anyNull(T[] array) {
        for (T entry : array) {
            if (entry == null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes null elements from an array.
     *
     * @param array The list.
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
    public static <T> T[] removeDuplicates(T[] array, Class<?> type) {
        return toArray(Arrays.stream(array).distinct().collect(Collectors.toList()), type);
    }
    
    /**
     * Selects a random element from an array.
     *
     * @param array The array to select from.
     * @param <T>   The type of the array.
     * @return A random element from the array.
     */
    public static <T> T selectRandom(T[] array) {
        if (array.length == 0) {
            return null;
        }
        
        return array[MathUtility.random(array.length - 1)];
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
    @SuppressWarnings({"unchecked", "StatementWithEmptyBody"})
    public static <T> T[] selectN(T[] array, int n, Class<?> type) {
        if (n >= array.length) {
            List<T> list = Arrays.asList(array);
            Collections.shuffle(list);
            return toArray(list, type);
        }
        
        List<Integer> previousChoices = new ArrayList<>();
        T[] choices = (T[]) Array.newInstance(type, Math.max(n, 0));
        for (int i = 0; i < n; i++) {
            int index;
            while (previousChoices.contains(index = MathUtility.random(array.length - 1))) {
            }
            choices[i] = array[index];
            previousChoices.add(index);
        }
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
    @SuppressWarnings("unchecked")
    public static <T> T[] duplicateInOrder(T[] array, int times, Class<?> type) {
        if (times <= 0) {
            return toArray(Collections.emptyList(), type);
        }
        if (times == 1) {
            return clone(array);
        }
        
        T[] finalArray = (T[]) Array.newInstance(type, (array.length * times));
        for (int time = 0; time < times; time++) {
            System.arraycopy(array, 0, finalArray, (array.length * time), array.length);
        }
        return finalArray;
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
    public static <T> T[] duplicateInOrder(T[] array, Class<?> type) {
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
    @SuppressWarnings("unchecked")
    public static <T> T[] sortByNumberOfOccurrences(T[] array, boolean reverse, Class<?> type) {
        Map<T, Integer> store = new LinkedHashMap<>();
        for (T entry : array) {
            if (store.containsKey(entry)) {
                store.replace(entry, store.get(entry) + 1);
            } else {
                store.put(entry, 1);
            }
        }
        
        T[] sorted = (T[]) Array.newInstance(type, array.length);
        final AtomicInteger index = new AtomicInteger(0);
        if (reverse) {
            store.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> {
                for (int i = 0; i < e.getValue(); i++) {
                    sorted[index.getAndIncrement()] = e.getKey();
                }
            });
        } else {
            store.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEach(e -> {
                for (int i = 0; i < e.getValue(); i++) {
                    sorted[index.getAndIncrement()] = e.getKey();
                }
            });
        }
        return sorted;
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
    public static <T> T[] sortByNumberOfOccurrences(T[] array, Class<?> type) {
        return sortByNumberOfOccurrences(array, false, type);
    }
    
}
