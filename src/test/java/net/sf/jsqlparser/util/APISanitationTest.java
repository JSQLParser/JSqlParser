/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

interface Visitor<T> {
    /**
     * @return {@code true} if the algorithm should visit more results, {@code false} if it should
     *         terminate now.
     */
    boolean visit(T t);
}


public class APISanitationTest {
    private final static TreeSet<Class<?>> CLASSES = new TreeSet<>(new Comparator<Class<?>>() {
        @Override
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });

    private final static Logger LOGGER = Logger.getLogger(APISanitationTest.class.getName());
    private final static Class<?>[] EXPRESSION_CLASSES =
            new Class[] {Expression.class, Column.class, Function.class};

    public static void findClasses(Visitor<String> visitor) {
        String classpath = System.getProperty("java.class.path");
        String[] paths = classpath.split(System.getProperty("path.separator"));
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                findClasses(file, file, visitor);
            }
        }
    }

    private static boolean findClasses(File root, File file, Visitor<String> visitor) {
        if (file.isDirectory()) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                if (!findClasses(root, child, visitor)) {
                    return false;
                }
            }
        } else if (file.getName().toLowerCase().endsWith(".class")) {
            return visitor.visit(createClassName(root, file));
        }

        return true;
    }

    private static String createClassName(File root, File file) {
        StringBuilder sb = new StringBuilder();
        String fileName = file.getName();
        sb.append(fileName, 0, fileName.lastIndexOf(".class"));
        File file1 = file.getParentFile();
        while (file1 != null && !file1.equals(root)) {
            sb.insert(0, '.').insert(0, file1.getName());
            file1 = file1.getParentFile();
        }
        return sb.toString();
    }

    /**
     * find all classes belonging to JSQLParser
     *
     */

    @BeforeAll
    static void findRelevantClasses() {
        findClasses(new Visitor<String>() {
            @Override
            public boolean visit(String clazz) {
                if (clazz.startsWith("net.sf.jsqlparser.statement")
                        || clazz.startsWith("net.sf.jsqlparser.expression")
                        || clazz.startsWith("net.sf.jsqlparser.schema")) {

                    int lastDotIndex = clazz.lastIndexOf(".");
                    int last$Index = clazz.lastIndexOf("$");

                    String className = last$Index > 0
                            ? clazz.substring(lastDotIndex, last$Index)
                            : clazz.substring(lastDotIndex);

                    if (!(className.toLowerCase().startsWith("test")
                            || className.toLowerCase().endsWith("test"))) {
                        try {
                            CLASSES.add(Class.forName(clazz));
                        } catch (ClassNotFoundException e) {
                            LOGGER.log(Level.SEVERE, "Class not found", e);
                        }
                    }
                }
                return true; // return false if you don't want to see any more classes
            }
        });
    }

    /**
     * find all field declarations for the classes belonging to JSQLParser
     *
     * @return the stream of fields
     */

    private static Stream<Field> fields() {
        TreeSet<Field> fields = new TreeSet<>(new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        for (Class<?> clazz : CLASSES) {
            // no enums
            if (!clazz.isEnum()) {
                for (Field field : clazz.getDeclaredFields()) {
                    // no final fields
                    if ((field.getModifiers() & Modifier.FINAL) != Modifier.FINAL) {
                        fields.add(field);
                    }
                }
            }
        }

        return fields.stream();
    }

    /**
     * Checks, if a field has Getters and Setters and Fluent Setter matching the naming conventions
     *
     * @param field the field to verify
     * @throws MethodNamingException a qualified exception pointing on the failing field
     */

    @ParameterizedTest(name = "{index} Field {0}")
    @MethodSource("fields")
    @Disabled
    void testFieldAccess(Field field) throws MethodNamingException {
        Class<?> clazz = field.getDeclaringClass();
        String fieldName = field.getName();

        if (!fieldName.equalsIgnoreCase("$jacocoData")) {

            boolean foundGetter = false;
            boolean foundSetter = false;
            boolean foundFluentSetter = false;

            for (Method method : clazz.getMethods()) {
                String methodName = method.getName();
                Class<?> typeClass = field.getType();
                boolean isBooleanType =
                        typeClass.equals(Boolean.class) || typeClass.equals(boolean.class);

                foundGetter |= ("get" + fieldName).equalsIgnoreCase(methodName)
                        | (isBooleanType && ("is" + fieldName).equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("is")
                                && fieldName.equalsIgnoreCase(methodName))
                        | (isBooleanType && ("has" + fieldName).equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && fieldName.equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("isUsing" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName));

                foundSetter |= ("set" + fieldName).equalsIgnoreCase(methodName)
                        | (isBooleanType && fieldName.startsWith("is")
                                && ("set" + fieldName.substring("is".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && ("set" + fieldName.substring("has".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && ("setHas" + fieldName.substring("has".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && ("setHaving" + fieldName.substring("has".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("set" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("setUse" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("setUsing" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName));

                foundFluentSetter |= ("with" + fieldName).equalsIgnoreCase(methodName)
                        | (isBooleanType && fieldName.startsWith("is")
                                && ("with" + fieldName.substring("is".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && ("with" + fieldName.substring("has".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && ("withHas" + fieldName.substring("has".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && ("withHaving" + fieldName.substring("has".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("with" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("withUse" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("use")
                                && ("withUsing" + fieldName.substring("use".length()))
                                        .equalsIgnoreCase(methodName));
            }

            if (!(foundGetter && foundSetter && foundFluentSetter)) {
                String message = fieldName + " "
                        + (!foundGetter ? "[Getter] " : "")
                        + (!foundSetter ? "[Setter] " : "")
                        + (!foundFluentSetter ? "[Fluent Setter] " : "")
                        + "missing";
                throwException(field, clazz, message);
            }
        }
    }

    /**
     * Test if a field declaration extends a certain class.
     *
     * @param field the declared field
     * @param boundClass the class, which the declaration extends
     * @return whether the field extends the class
     */

    boolean testGenericType(Field field, Class<?> boundClass) {
        Type listType = field.getGenericType();
        if (listType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) listType;
            for (Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                if (actualTypeArgument instanceof Class) {
                    Class<?> elementClass = (Class<?>) actualTypeArgument;
                    if (elementClass.isAssignableFrom(boundClass)) {
                        return true;
                    }
                }
            }
        }

        Type superclassType = field.getType().getGenericSuperclass();
        ParameterizedType parameterizedType = (ParameterizedType) superclassType;
        if (parameterizedType != null) {
            for (final Type actualTypeArgument : parameterizedType.getActualTypeArguments()) {
                if (actualTypeArgument instanceof TypeVariable<?>) {
                    final TypeVariable<?> typeVariable =
                            (TypeVariable<?>) actualTypeArgument;
                    for (Type type : typeVariable.getBounds()) {
                        if (type.getTypeName().equals(boundClass.getTypeName())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Scan for any occurrence of <code>List<Expression></code> and throw an Exception.
     *
     * @param field the field to test for <code>List<Expression></code>
     * @throws MethodNamingException the Exception pointing on the Class and location of the field
     */

    @ParameterizedTest(name = "{index} Field {0}")
    @MethodSource("fields")
    @SuppressWarnings({"PMD.NPath"})
    void testExpressionList(final Field field) throws MethodNamingException {
        Class<?> clazz = field.getType();
        String fieldName = field.getName();

        if (!fieldName.equalsIgnoreCase("$jacocoData")) {
            boolean isExpressionList = false;
            for (Class<?> boundClass : EXPRESSION_CLASSES) {
                if (Collection.class.isAssignableFrom(clazz)
                        && !ExpressionList.class.isAssignableFrom(clazz)) {
                    isExpressionList |= testGenericType(field, boundClass);
                }
            }

            if (isExpressionList) {
                String message = fieldName + " is an Expression List";
                throwException(field, clazz, message);
            }
        }
    }

    /**
     * Find the declaration of the offending field and throws a qualified exception.
     *
     * @param field the offending field
     * @param clazz the offending class declaring the field
     * @param message the information about the offense
     * @throws MethodNamingException the qualified exception pointing on the location
     */

    private static void throwException(Field field, Class<?> clazz, String message)
            throws MethodNamingException {
        String fieldName = field.getName();
        String pureFieldName = fieldName.lastIndexOf("$") > 0
                ? fieldName.substring(fieldName.lastIndexOf("$"))
                : fieldName;
        Class<?> declaringClazz = field.getDeclaringClass();
        while (declaringClazz.getDeclaringClass() != null) {
            declaringClazz = declaringClazz.getDeclaringClass();
        }
        String pureDeclaringClassName = declaringClazz.getCanonicalName();

        File file = new File(
                "src/main/java/"
                        + pureDeclaringClassName.replace(".", "/")
                                .concat(".java"));

        int position = 1;
        Pattern pattern = Pattern.compile(
                "\\s" + field.getType().getSimpleName() + "(<\\w*>)?(\\s*\\w*,?)*\\s*\\W",
                Pattern.MULTILINE);
        try (FileReader reader = new FileReader(file)) {
            List<String> lines = IOUtils.readLines(reader);
            StringBuilder builder = new StringBuilder();
            for (String s : lines) {
                builder.append(s).append("\n");
            }
            final Matcher matcher = pattern.matcher(builder);
            while (matcher.find()) {
                String group0 = matcher.group(0);
                if (group0.contains(pureFieldName)
                        && (group0.endsWith("=") || group0.endsWith(";"))) {
                    int pos = matcher.start(0);
                    int readCharacters = 0;
                    for (String line : lines) {
                        readCharacters += line.length() + 1;
                        if (readCharacters >= pos) {
                            break;
                        }
                        position++;
                    }
                    break;
                }
            }
        } catch (Exception ex) {
            LOGGER.warning(
                    "Could not find the field " + fieldName + " for " + clazz.getName());
        }

        StackTraceElement stackTraceElement = new StackTraceElement(
                field.getDeclaringClass().getName(), fieldName,
                file.toURI().normalize().toASCIIString(),
                position);

        throw new MethodNamingException(message, stackTraceElement);
    }

    public static class MethodNamingException extends Exception {
        public MethodNamingException(String message, StackTraceElement stackTrace) {
            super(message);
            super.setStackTrace(new StackTraceElement[] {stackTrace});
        }
    }
}
