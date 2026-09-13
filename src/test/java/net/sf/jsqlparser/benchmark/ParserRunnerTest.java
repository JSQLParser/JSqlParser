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

import static org.junit.jupiter.api.Assertions.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.Executors;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.Test;
import net.sf.jsqlparser.benchmark.SqlParserRunner.Configuration;

class ParserRunnerTest {
    @Test
    void isolatedParserReturnsItsOwnAstAndAppliesTheSameOptions() throws Exception {
        URL classes = CCJSqlParserUtil.class.getProtectionDomain().getCodeSource().getLocation();
        var executor = Executors.newSingleThreadExecutor();
        try (var current = new LatestClasspathRunner();
                var isolated =
                        new DynamicParserRunner(new URLClassLoader(new URL[] {classes}, null))) {
            String sql = "SELECT 'it\\'s' AS value";
            Object expected =
                    current.parseStatements(sql, executor, Configuration.BACKSLASH_ESCAPES);
            Object actual =
                    isolated.parseStatements(sql, executor, Configuration.BACKSLASH_ESCAPES);
            assertEquals(expected.toString(), actual.toString());
            assertNotSame(expected.getClass(), actual.getClass());
            assertEquals(expected.getClass().getName(), actual.getClass().getName());
            assertEquals(
                    current.parseStatements("SELECT 1", executor, Configuration.SIMPLE).toString(),
                    isolated.parseStatements("SELECT 1", executor, Configuration.SIMPLE)
                            .toString());
            assertThrows(Exception.class,
                    () -> isolated.parseStatements("SELECT FROM", executor, Configuration.SIMPLE));
            assertFalse(executor.isShutdown());
        } finally {
            executor.shutdownNow();
        }
    }
}
