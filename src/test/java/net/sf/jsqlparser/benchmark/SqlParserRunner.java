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

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import net.sf.jsqlparser.parser.CCJSqlParser;

public interface SqlParserRunner extends AutoCloseable {
    enum Configuration {
        SIMPLE("withAllowComplexParsing", false,
                parser -> parser.withAllowComplexParsing(false)), BACKSLASH_ESCAPES(
                        "withBackslashEscapeCharacter", true,
                        parser -> parser.withBackslashEscapeCharacter(true));

        final String methodName;
        final boolean value;
        final Consumer<CCJSqlParser> currentParserConfiguration;

        Configuration(String methodName, boolean value, Consumer<CCJSqlParser> configuration) {
            this.methodName = methodName;
            this.value = value;
            this.currentParserConfiguration = configuration;
        }
    }

    Object parseStatements(String sql, ExecutorService executorService,
            Configuration configuration) throws Exception;

    @Override
    default void close() throws Exception {}
}
