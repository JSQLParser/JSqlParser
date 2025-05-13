package net.sf.jsqlparser.benchmark;

import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.statement.Statements;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class DynamicParserRunner implements SqlParserRunner {
    private final Method parseStatementsMethod;

    public DynamicParserRunner(URLClassLoader loader) throws Exception {
        Class<?> utilClass = loader.loadClass("net.sf.jsqlparser.parser.CCJSqlParserUtil");
        Class<?> ccjClass = loader.loadClass("net.sf.jsqlparser.parser.CCJSqlParser");
        Class<?> consumerClass = Class.forName("java.util.function.Consumer"); // interface OK
        parseStatementsMethod = utilClass.getMethod(
                "parseStatements",
                String.class,
                ExecutorService.class,
                consumerClass
        );
    }

    @Override
    public Statements parseStatements(String sql,
                                      ExecutorService executorService,
                                      Consumer<CCJSqlParser> consumer) throws Exception {
        return (Statements) parseStatementsMethod.invoke(null, sql, executorService, null);
    }
}
