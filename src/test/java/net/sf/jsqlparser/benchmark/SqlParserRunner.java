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

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statements;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface SqlParserRunner {
    Statements parseStatements(String sql, ExecutorService executorService,
            Consumer<CCJSqlParser> consumer) throws Exception;
}
