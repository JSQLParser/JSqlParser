package net.sf.jsqlparser.benchmark;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statements;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class LatestClasspathRunner implements SqlParserRunner {

    @Override
    public Statements parseStatements(String sql,
            ExecutorService executorService,
            Consumer<CCJSqlParser> consumer) throws Exception {
        return net.sf.jsqlparser.parser.CCJSqlParserUtil.parseStatements(sql, executorService,
                consumer);
    }
}

