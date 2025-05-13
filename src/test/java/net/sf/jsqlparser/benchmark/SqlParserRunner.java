package net.sf.jsqlparser.benchmark;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statements;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public interface SqlParserRunner {
    Statements parseStatements(String sql, ExecutorService executorService, Consumer<CCJSqlParser> consumer) throws Exception;
}
