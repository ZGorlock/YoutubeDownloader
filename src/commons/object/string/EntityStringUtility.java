/*
 * File:    EntityStringUtility.java
 * Package: commons.object.string
 * Author:  Zachary Gill
 * Repo:    https://github.com/ZGorlock/Java-Commons
 */

package commons.object.string;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import commons.object.CastUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A resource class that provides entity string functionality.
 */
public final class EntityStringUtility {
    
    //Logger
    
    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(EntityStringUtility.class);
    
    
    //Static Methods
    
    /**
     * Returns a string representing a class.
     *
     * @param simple Whether to use simple names or not.
     * @param clazz  The class.
     * @return The class string.
     */
    private static String generateClassString(boolean simple, Class<?> clazz) {
        return Optional.ofNullable(clazz)
                .map(e -> (simple ? e.getSimpleName() : e.getTypeName())).map(classString -> classString
                        .replaceAll("\\$MockitoMock\\$\\d*", "").replaceAll((simple ? "(?:.+\\$)+" : ""), ""))
                .orElse("null");
    }
    
    /**
     * Returns a string representing a class.
     *
     * @param clazz The class.
     * @return The class string.
     * @see #generateClassString(boolean, Class)
     */
    public static String classString(Class<?> clazz) {
        return generateClassString(false, clazz);
    }
    
    /**
     * Returns a simple string representing a class.
     *
     * @param clazz The class.
     * @return The simple class string.
     * @see #generateClassString(boolean, Class)
     */
    public static String simpleClassString(Class<?> clazz) {
        return generateClassString(true, clazz);
    }
    
    /**
     * Returns a string representing a class.
     *
     * @param object The object.
     * @return The class string.
     * @see #classString(Class)
     */
    public static String classString(Object object) {
        return classString(CastUtility.toClass(object));
    }
    
    /**
     * Returns a simple string representing a class.
     *
     * @param object The object.
     * @return The simple class string.
     * @see #simpleClassString(Class)
     */
    public static String simpleClassString(Object object) {
        return simpleClassString(CastUtility.toClass(object));
    }
    
    /**
     * Returns a string representing a super class.
     *
     * @param simple Whether to use simple names or not.
     * @param clazz  The class.
     * @return The super class string.
     * @see #generateClassString(boolean, Class)
     */
    private static String generateSuperClassString(boolean simple, Class<?> clazz) {
        return Optional.ofNullable(clazz)
                .map(e -> generateClassString(simple, clazz.getSuperclass()))
                .orElse("null");
    }
    
    /**
     * Returns a string representing a super class.
     *
     * @param clazz The class.
     * @return The super class string.
     * @see #generateSuperClassString(boolean, Class)
     */
    public static String superClassString(Class<?> clazz) {
        return generateSuperClassString(false, clazz);
    }
    
    /**
     * Returns a simple string representing a super class.
     *
     * @param clazz The class.
     * @return The simple super class string.
     * @see #generateSuperClassString(boolean, Class)
     */
    public static String simpleSuperClassString(Class<?> clazz) {
        return generateSuperClassString(true, clazz);
    }
    
    /**
     * Returns a string representing a super class.
     *
     * @param object The object.
     * @return The super class string.
     * @see #superClassString(Class)
     */
    public static String superClassString(Object object) {
        return superClassString(CastUtility.toClass(object));
    }
    
    /**
     * Returns a simple string representing a super class.
     *
     * @param object The object.
     * @return The simple super class string.
     * @see #simpleSuperClassString(Class)
     */
    public static String simpleSuperClassString(Object object) {
        return simpleSuperClassString(CastUtility.toClass(object));
    }
    
    /**
     * Returns a string representing an inner class.
     *
     * @param simple         Whether to use simple names or not.
     * @param clazz          The class that has the inner class.
     * @param innerClassName The name of the inner class.
     * @return The inner class string.
     */
    private static String generateInnerClassString(boolean simple, Class<?> clazz, String innerClassName) {
        return generateClassString(simple, clazz) + '$' + innerClassName;
    }
    
    /**
     * Returns a string representing an inner class.
     *
     * @param clazz          The class that has the inner class.
     * @param innerClassName The name of the inner class.
     * @return The inner class string.
     * @see #generateInnerClassString(boolean, Class, String)
     */
    public static String innerClassString(Class<?> clazz, String innerClassName) {
        return generateInnerClassString(false, clazz, innerClassName);
    }
    
    /**
     * Returns a simple string representing an inner class.
     *
     * @param clazz          The class that has the inner class.
     * @param innerClassName The name of the inner class.
     * @return The simple inner class string.
     * @see #generateInnerClassString(boolean, Class, String)
     */
    public static String simpleInnerClassString(Class<?> clazz, String innerClassName) {
        return generateInnerClassString(true, clazz, innerClassName);
    }
    
    /**
     * Returns a string representing an inner class.
     *
     * @param object         The object that has the inner class.
     * @param innerClassName The name of the inner class.
     * @return The inner class string.
     * @see #innerClassString(Class, String)
     */
    public static String innerClassString(Object object, String innerClassName) {
        return innerClassString(CastUtility.toClass(object), innerClassName);
    }
    
    /**
     * Returns a simple string representing an inner class.
     *
     * @param object         The object that has the inner class.
     * @param innerClassName The name of the inner class.
     * @return The simple inner class string.
     * @see #simpleInnerClassString(Class, String)
     */
    public static String simpleInnerClassString(Object object, String innerClassName) {
        return simpleInnerClassString(CastUtility.toClass(object), innerClassName);
    }
    
    /**
     * Returns a string representing a method.
     *
     * @param simple          Whether to use simple names or not.
     * @param clazz           The class that has the method.
     * @param methodName      The name of the method.
     * @param argumentClasses The classes of the arguments of the method.
     * @return The method string.
     */
    private static String generateMethodString(boolean simple, Class<?> clazz, String methodName, Class<?>... argumentClasses) {
        return generateClassString(simple, clazz) + "::" + methodName +
                Arrays.stream(argumentClasses).sequential().map(e -> generateClassString(simple, e))
                        .collect(Collectors.joining(", ", "(", ")"));
    }
    
    /**
     * Returns a string representing a method.
     *
     * @param clazz           The class that has the method.
     * @param methodName      The name of the method.
     * @param argumentClasses The classes of the arguments of the method.
     * @return The method string.
     * @see #generateMethodString(boolean, Class, String, Class[])
     */
    public static String methodString(Class<?> clazz, String methodName, Class<?>... argumentClasses) {
        return generateMethodString(false, clazz, methodName, argumentClasses);
    }
    
    /**
     * Returns a simple string representing a method.
     *
     * @param clazz           The class that has the method.
     * @param methodName      The name of the method.
     * @param argumentClasses The classes of the arguments of the method.
     * @return The simple method string.
     * @see #generateMethodString(boolean, Class, String, Class[])
     */
    public static String simpleMethodString(Class<?> clazz, String methodName, Class<?>... argumentClasses) {
        return generateMethodString(true, clazz, methodName, argumentClasses);
    }
    
    /**
     * Returns a string representing a method.
     *
     * @param object          The object that has the method.
     * @param methodName      The name of the method.
     * @param argumentClasses The classes of the arguments of the method.
     * @return The method string.
     * @see #methodString(Class, String, Class[])
     */
    public static String methodString(Object object, String methodName, Class<?>... argumentClasses) {
        return methodString(CastUtility.toClass(object), methodName, argumentClasses);
    }
    
    /**
     * Returns a simple string representing a method.
     *
     * @param object          The object that has the method.
     * @param methodName      The name of the method.
     * @param argumentClasses The classes of the arguments of the method.
     * @return The simple method string.
     * @see #simpleMethodString(Class, String, Class[])
     */
    public static String simpleMethodString(Object object, String methodName, Class<?>... argumentClasses) {
        return simpleMethodString(CastUtility.toClass(object), methodName, argumentClasses);
    }
    
    /**
     * Returns a string representing a method.
     *
     * @param method The method
     * @return The method string.
     * @see #methodString(Class, String, Class[])
     */
    public static String methodString(Method method) {
        return methodString(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }
    
    /**
     * Returns a simple string representing a method.
     *
     * @param method The method
     * @return The simple method string.
     * @see #simpleMethodString(Class, String, Class[])
     */
    public static String simpleMethodString(Method method) {
        return simpleMethodString(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }
    
    /**
     * Returns a string representing a constructor.
     *
     * @param simple          Whether to use simple names or not.
     * @param clazz           The class that has the constructor.
     * @param argumentClasses The classes of the arguments of the constructor.
     * @return The constructor string.
     * @see #generateMethodString(boolean, Class, String, Class[])
     */
    private static String generateConstructorString(boolean simple, Class<?> clazz, Class<?>... argumentClasses) {
        return generateMethodString(simple, clazz, generateClassString(true, clazz), argumentClasses);
    }
    
    /**
     * Returns a string representing a constructor.
     *
     * @param clazz           The class that has the constructor.
     * @param argumentClasses The classes of the arguments of the constructor.
     * @return The constructor string.
     * @see #generateConstructorString(boolean, Class, Class[])
     */
    public static String constructorString(Class<?> clazz, Class<?>... argumentClasses) {
        return generateConstructorString(false, clazz, argumentClasses);
    }
    
    /**
     * Returns a simple string representing a constructor.
     *
     * @param clazz           The class that has the constructor.
     * @param argumentClasses The classes of the arguments of the constructor.
     * @return The simple constructor string.
     * @see #generateConstructorString(boolean, Class, Class[])
     */
    public static String simpleConstructorString(Class<?> clazz, Class<?>... argumentClasses) {
        return generateConstructorString(true, clazz, argumentClasses);
    }
    
    /**
     * Returns a string representing a constructor.
     *
     * @param object          The object that has the constructor.
     * @param argumentClasses The classes of the arguments of the constructor.
     * @return The constructor string.
     * @see #constructorString(Class, Class[])
     */
    public static String constructorString(Object object, Class<?>... argumentClasses) {
        return constructorString(CastUtility.toClass(object), argumentClasses);
    }
    
    /**
     * Returns a simple string representing a constructor.
     *
     * @param object          The object that has the constructor.
     * @param argumentClasses The classes of the arguments of the constructor.
     * @return The simple constructor string.
     * @see #simpleConstructorString(Class, Class[])
     */
    public static String simpleConstructorString(Object object, Class<?>... argumentClasses) {
        return simpleConstructorString(CastUtility.toClass(object), argumentClasses);
    }
    
    /**
     * Returns a string representing a constructor.
     *
     * @param constructor The constructor.
     * @return The constructor string.
     * @see #constructorString(Class, Class[])
     */
    public static String constructorString(Constructor<?> constructor) {
        return constructorString(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }
    
    /**
     * Returns a simple string representing a constructor.
     *
     * @param constructor The constructor.
     * @return The simple constructor string.
     * @see #simpleConstructorString(Class, Class[])
     */
    public static String simpleConstructorString(Constructor<?> constructor) {
        return simpleConstructorString(constructor.getDeclaringClass(), constructor.getParameterTypes());
    }
    
    /**
     * Returns a string representing a field.
     *
     * @param simple    Whether to use simple names or not.
     * @param clazz     The class that has the field.
     * @param fieldName The name of the field.
     * @return The field string.
     */
    private static String generateFieldString(boolean simple, Class<?> clazz, String fieldName) {
        return generateClassString(simple, clazz) + "::" + fieldName;
    }
    
    /**
     * Returns a string representing a field.
     *
     * @param clazz     The class that has the field.
     * @param fieldName The name of the field.
     * @return The field string.
     * @see #generateFieldString(boolean, Class, String)
     */
    public static String fieldString(Class<?> clazz, String fieldName) {
        return generateFieldString(false, clazz, fieldName);
    }
    
    /**
     * Returns a simple string representing a field.
     *
     * @param clazz     The class that has the field.
     * @param fieldName The name of the field.
     * @return The simple field string.
     * @see #generateFieldString(boolean, Class, String)
     */
    public static String simpleFieldString(Class<?> clazz, String fieldName) {
        return generateFieldString(true, clazz, fieldName);
    }
    
    /**
     * Returns a string representing a field.
     *
     * @param object    The object that has the field.
     * @param fieldName The name of the field.
     * @return The field string.
     * @see #fieldString(Class, String)
     */
    public static String fieldString(Object object, String fieldName) {
        return fieldString(CastUtility.toClass(object), fieldName);
    }
    
    /**
     * Returns a simple string representing a field.
     *
     * @param object    The object that has the field.
     * @param fieldName The name of the field.
     * @return The simple field string.
     * @see #simpleFieldString(Class, String)
     */
    public static String simpleFieldString(Object object, String fieldName) {
        return simpleFieldString(CastUtility.toClass(object), fieldName);
    }
    
    /**
     * Returns a string representing a field.
     *
     * @param field The field.
     * @return The field string.
     * @see #fieldString(Class, String)
     */
    public static String fieldString(Field field) {
        return fieldString(field.getDeclaringClass(), field.getName());
    }
    
    /**
     * Returns a simple string representing a field.
     *
     * @param field The field.
     * @return The simple field string.
     * @see #simpleFieldString(Class, String)
     */
    public static String simpleFieldString(Field field) {
        return simpleFieldString(field.getDeclaringClass(), field.getName());
    }
    
}
