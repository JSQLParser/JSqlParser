/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.opentest4j.TestAbortedException;

/**
 * @author gitmotte
 */
public class ReflectionTestUtils {

    public static final Predicate<Method> GETTER_METHODS = m -> !void.class.isAssignableFrom(m.getReturnType()) && m.getParameterCount() == 0 && (m.getName().startsWith("get") || m.getName().startsWith("is"));

    public static final Predicate<Method> SETTER_METHODS = m -> void.class.isAssignableFrom(m.getReturnType()) && m.getParameterCount() == 1 && m.getName().startsWith("set");

    public static final Predicate<Method> CHAINING_METHODS = m -> m.getDeclaringClass().isAssignableFrom(m.getReturnType()) && // could be prefixed with "with" or not, does not matter
    m.getParameterCount() == 1;

    /**
     * Testing of setters, getters, with-/add-methods by calling them with random parameter-values
     * <ul>
     * <li>testing, whether return-value is the specific type (not the parent)
     * <li>testing, whether calling the methods do not throw any exceptions
     * </ul>
     *
     * @param objs
     * @param testMethodFilter - additional filter to skip some methods (by returning <code>false</code>).
     * Default-Filters: null     {@link #notDeclaredInObjectClass(Method)},
     *                         {@link #GETTER_METHODS}, {@link #SETTER_METHODS},
     *                         {@link #CHAINING_METHODS}
     */
    @SafeVarargs
    public static void testGetterSetterChaining(List<Object> objs, Predicate<Method>... testMethodFilter) {
        RandomUtils.pushObjects(objs);
        objs.forEach(o -> {
            testMethodInvocation(o, ReflectionTestUtils::anyReturnType, ReflectionTestUtils::reflectiveNonNullArgs, ArrayUtils.insert(0, testMethodFilter, GETTER_METHODS, ReflectionTestUtils::notDeclaredInObjectClass));
            testMethodInvocation(o, ReflectionTestUtils::noReturnTypeValid, ReflectionTestUtils::reflectiveNonNullArgs, ArrayUtils.insert(0, testMethodFilter, SETTER_METHODS, ReflectionTestUtils::notDeclaredInObjectClass));
            testMethodInvocation(o, ReflectionTestUtils::returnTypeThis, ReflectionTestUtils::reflectiveNonNullArgs, ArrayUtils.insert(0, testMethodFilter, CHAINING_METHODS, ReflectionTestUtils::notDeclaredInObjectClass));
        });
    }

    private static boolean notDeclaredInObjectClass(Method m) {
        return !Object.class.equals(m.getDeclaringClass());
    }

    private static Object[] reflectiveNonNullArgs(Method m) {
        List<Object> params = new ArrayList<>();
        for (Parameter p : m.getParameters()) {
            Class<?> type = p.getType();
            Object value = RandomUtils.getRandomValueForType(type);
            Assumptions.assumeTrue(value != null, "cannot get random value for type " + type);
            params.add(value);
        }
        return params.toArray();
    }

    /**
     * @param returnValue
     * @param m
     * @return <code>true</code>, if the return-type is equals the method-declaring class
     */
    private static boolean returnTypeThis(Object returnValue, Method m) {
        return returnValue != null && m.getDeclaringClass().equals(returnValue.getClass());
    }

    /**
     * @param returnValue
     * @param m
     * @return always <code>true</code>
     */
    private static boolean anyReturnType(Object returnValue, Method m) {
        return true;
    }

    /**
     * @param returnValue
     * @param m
     * @return <code>true</code>, if returnValue is <code>null</code>
     */
    private static boolean noReturnTypeValid(Object returnValue, Method m) {
        return returnValue == null;
    }

    /**
     * @param object
     * @param argsFunction
     * @param methodFilters
     */
    @SafeVarargs
    public static void testMethodInvocation(Object object, BiPredicate<Object, Method> returnTypeCheck, Function<Method, Object[]> argsFunction, Predicate<Method>... methodFilters) {
        log(Level.INFO, "testing methods of class " + object.getClass());
        for (Method m : object.getClass().getMethods()) {
            boolean testMethod = true;
            for (Predicate<Method> f : methodFilters) {
                if (!f.test(m)) {
                    log(Level.FINE, "skip method " + m.toGenericString());
                    testMethod = false;
                    break;
                }
            }
            if (testMethod) {
                log(Level.INFO, "testing method " + m.toGenericString());
                try {
                    invoke(m, returnTypeCheck, argsFunction, object);
                } catch (Exception e) {
                    assertFalse(false, String.format("%s throws on invocation on object: %s", m.toGenericString(), object.getClass()));
                }
            }
        }
    }

    /**
     * Invoke one method of given object with args provided by #argsFunction, and test it's return-value
     *
     * @param method
     * @param returnValueCheck
     * @param argsFunction
     * @param object
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void invoke(Method method, BiPredicate<Object, Method> returnValueCheck, Function<Method, Object[]> argsFunction, Object object) throws IllegalAccessException, InvocationTargetException {
        try {
            Object returnValue = method.invoke(object, argsFunction.apply(method));
            if (!void.class.isAssignableFrom(method.getReturnType())) {
                assertTrue(returnValueCheck.test(returnValue, method), "unexpected return-value with type " + returnValue.getClass() + " for method " + method.toGenericString());
            }
        } catch (TestAbortedException tae) {
            log(Level.INFO, "skip methods " + method.toGenericString() + ", detail: " + tae.getMessage());
        }
    }

    private static void log(Level level, String string) {
        if (Logger.getAnonymousLogger().isLoggable(level)) {
            System.out.println(string);
        }
    }
}
