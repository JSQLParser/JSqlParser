/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.benchmark;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/** Owns the isolated class loader and keeps its AST types behind an Object boundary. */
public class DynamicParserRunner implements SqlParserRunner {
    private final URLClassLoader loader;
    private final Method parseStatementsMethod;
    private final Map<Configuration, Consumer<Object>> configurations =
            new EnumMap<>(Configuration.class);

    public DynamicParserRunner(URLClassLoader loader) throws Exception {
        this.loader = loader;
        Class<?> utilClass = loader.loadClass("net.sf.jsqlparser.parser.CCJSqlParserUtil");
        Class<?> parserClass = loader.loadClass("net.sf.jsqlparser.parser.CCJSqlParser");
        parseStatementsMethod = utilClass.getMethod("parseStatements", String.class,
                ExecutorService.class, Consumer.class);
        for (Configuration configuration : Configuration.values()) {
            Method method = parserClass.getMethod(configuration.methodName, boolean.class);
            configurations.put(configuration, parser -> {
                try {
                    method.invoke(parser, configuration.value);
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException(
                            "Cannot apply parser configuration " + configuration, ex);
                }
            });
        }
    }

    @Override
    public Object parseStatements(String sql, ExecutorService executorService,
            Configuration configuration) throws Exception {
        try {
            return parseStatementsMethod.invoke(null, sql, executorService,
                    configuration == null ? null : configurations.get(configuration));
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof Exception) {
                throw (Exception) ex.getCause();
            }
            throw ex;
        }
    }

    @Override
    public void close() throws java.io.IOException {
        loader.close();
    }
}
