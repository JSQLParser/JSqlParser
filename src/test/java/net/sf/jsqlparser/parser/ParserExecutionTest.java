/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.Executors;
import net.sf.jsqlparser.JSQLParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ParserExecutionTest {
    private static class QueuedExecutor extends AbstractExecutorService {
        Future<?> task;
        int submissions;
        boolean shutdown;

        @Override
        public void execute(Runnable command) {
            task = (Future<?>) command;
            submissions++;
        }

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown();
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown;
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cancelsTheSubmittedTaskAndPreservesCallerInterruption(boolean multiple) {
        QueuedExecutor executor = new QueuedExecutor();
        CCJSqlParser parser = CCJSqlParserUtil.newParser("SELECT 1");
        Thread.currentThread().interrupt();
        try {
            JSQLParserException error = assertThrows(JSQLParserException.class, () -> {
                if (multiple) {
                    CCJSqlParserUtil.parseStatements(parser, executor);
                } else {
                    CCJSqlParserUtil.parseStatement(parser, executor);
                }
            });
            assertInstanceOf(InterruptedException.class, error.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
            assertTrue(parser.interrupted);
            assertTrue(executor.task.isCancelled());
            assertFalse(executor.isShutdown());
        } finally {
            Thread.interrupted();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotRetryInterruptedConvenienceCalls(boolean multiple) {
        QueuedExecutor executor = new QueuedExecutor();
        Thread.currentThread().interrupt();
        try {
            JSQLParserException error = assertThrows(JSQLParserException.class, () -> {
                if (multiple) {
                    CCJSqlParserUtil.parseStatements("SELECT 1", executor, null);
                } else {
                    CCJSqlParserUtil.parse("SELECT 1", executor, null);
                }
            });
            assertInstanceOf(InterruptedException.class, error.getCause());
            assertEquals(1, executor.submissions);
            assertTrue(executor.task.isCancelled());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void timeoutCancelsOneTaskWithoutRetryingOrInterruptingTheCaller(boolean multiple) {
        QueuedExecutor executor = new QueuedExecutor();
        JSQLParserException error = assertThrows(JSQLParserException.class, () -> {
            if (multiple) {
                CCJSqlParserUtil.parseStatements("SELECT 1", executor,
                        parser -> parser.withTimeOut(0));
            } else {
                CCJSqlParserUtil.parse("SELECT 1", executor, parser -> parser.withTimeOut(0));
            }
        });
        assertInstanceOf(TimeoutException.class, error.getCause());
        assertEquals(1, executor.submissions);
        assertTrue(executor.task.isCancelled());
        assertFalse(Thread.currentThread().isInterrupted());
        assertFalse(executor.isShutdown());
    }

    @Test
    void successfulAndInvalidParsingKeepTheCallerExecutorUsable() throws Exception {
        var executor = Executors.newSingleThreadExecutor();
        try {
            assertEquals("SELECT 1", CCJSqlParserUtil.parse("SELECT 1", executor, null).toString());
            assertThrows(JSQLParserException.class,
                    () -> CCJSqlParserUtil.parse("SELECT FROM", executor, null));
            assertEquals("SELECT 2;\n",
                    CCJSqlParserUtil.parseStatements("SELECT 2", executor, null).toString());
            assertFalse(executor.isShutdown());
        } finally {
            executor.shutdownNow();
        }
    }
}
