package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.Expression;
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
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

interface Visitor<T> {
    /**
     * @return {@code true} if the algorithm should visit more results, {@code false} if it should
     *         terminate now.
     */
    public boolean visit(T t);
}


public class APISanitation {
    private final static TreeSet<Class> classes = new TreeSet<>(new Comparator<Class>() {
        @Override
        public int compare(Class o1, Class o2) {
            return o1.getName().compareTo(o2.getName());
        }
    });

    private final static Logger LOGGER = Logger.getLogger(APISanitation.class.getName());

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
            for (File child : file.listFiles()) {
                if (!findClasses(root, child, visitor)) {
                    return false;
                }
            }
        } else if (file.getName().toLowerCase().endsWith(".class")) {
            if (!visitor.visit(createClassName(root, file))) {
                return false;
            }
        }

        return true;
    }

    private static String createClassName(File root, File file) {
        StringBuffer sb = new StringBuffer();
        String fileName = file.getName();
        sb.append(fileName.substring(0, fileName.lastIndexOf(".class")));
        file = file.getParentFile();
        while (file != null && !file.equals(root)) {
            sb.insert(0, '.').insert(0, file.getName());
            file = file.getParentFile();
        }
        return sb.toString();
    }

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
                            classes.add(Class.forName(clazz));
                        } catch (ClassNotFoundException e) {
                            LOGGER.log(Level.SEVERE, "Class not found", e);
                        }
                    }
                }
                return true; // return false if you don't want to see any more classes
            }
        });
    }

    public static class MethodNamingException extends Exception {
        public MethodNamingException(String message, StackTraceElement stackTrace) {
            super(message);
            super.setStackTrace(new StackTraceElement[] {stackTrace});
        }
    }

    private static Stream<Field> fields() throws Exception {
        TreeSet<Field> fields = new TreeSet<>(new Comparator<Field>() {
            @Override
            public int compare(Field o1, Field o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });

        for (Class<?> clazz : classes) {
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
                                && (fieldName).equalsIgnoreCase(methodName))
                        | (isBooleanType && ("has" + fieldName).equalsIgnoreCase(methodName))
                        | (isBooleanType && fieldName.startsWith("has")
                                && (fieldName).equalsIgnoreCase(methodName))
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
                // clazz.getName().substring("net.sf.jsqlparser.".length()) + " " + fieldName + ":"
                String message = fieldName + " "
                        + (!foundGetter ? "[Getter] " : "")
                        + (!foundSetter ? "[Setter] " : "")
                        + (!foundFluentSetter ? "[Fluent Setter] " : "")
                        + "missing";

                File file = new File(
                        "src/main/java/"
                                + clazz.getCanonicalName().replace(".", "/").concat(".java"));

                int position = 1;
                try (FileReader reader = new FileReader(file)) {
                    for (String line : IOUtils.readLines(reader)) {
                        if (line.contains(fieldName)) {
                            break;
                        }
                        position++;
                    }
                } catch (Exception ex) {
                    LOGGER.warning(
                            "Could not find the field " + fieldName + " for " + clazz.getName());
                }

                StackTraceElement stackTraceElement = new StackTraceElement(
                        clazz.getName(), fieldName, file.toURI().normalize().toASCIIString(),
                        position);

                throw new MethodNamingException(message, stackTraceElement);
            }
        }
    }

    boolean testGenericType(Field field, Class boundClass) {
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
                final TypeVariable<Class<List>> typeVariable =
                        (TypeVariable<Class<List>>) actualTypeArgument;
                for (Type type : typeVariable.getBounds()) {
                    if (type.getTypeName().equals(boundClass.getTypeName()))
                        return true;
                }
            }
        }
        return false;
    }

    private final static Class[] EXPRESSION_CLASSES = new Class[] {Expression.class, Column.class};

    @ParameterizedTest(name = "{index} Field {0}")
    @MethodSource("fields")
    @Disabled
    void testExpressionList(final Field field) throws MethodNamingException {
        Class<?> clazz = field.getType();
        String fieldName = field.getName();

        if (!fieldName.equalsIgnoreCase("$jacocoData")) {
            boolean isExpressionList = false;
            for (Class boundClass : EXPRESSION_CLASSES) {
                if (Collection.class.isAssignableFrom(clazz)
                        && !ExpressionList.class.isAssignableFrom(clazz)) {
                    isExpressionList |= testGenericType(field, boundClass);
                }
            }

            if (isExpressionList) {
                String message = fieldName + " is an Expression List";

                File file = new File(
                        "src/main/java/"
                                + field.getDeclaringClass().getCanonicalName().replace(".", "/")
                                        .concat(".java"));

                int position = 1;
                try (FileReader reader = new FileReader(file)) {
                    for (String line : IOUtils.readLines(reader)) {
                        if (line.contains(fieldName)) {
                            break;
                        }
                        position++;
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
        }
    }
}
