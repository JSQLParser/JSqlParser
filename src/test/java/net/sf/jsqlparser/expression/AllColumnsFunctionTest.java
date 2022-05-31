package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.JSqlParser;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;
import net.sf.jsqlparser.util.deparser.SelectDeParser;
import net.sf.jsqlparser.util.deparser.StatementDeParser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;

public class AllColumnsFunctionTest {
    @Test
    void shouldSetAllColumnsToTrue() throws Exception {
        JSqlParser jSqlParser = new CCJSqlParserManager();
        try (Reader statementReader = new StringReader("select f(*) from t")) {
            Statement parse = jSqlParser.parse(statementReader);
            StringBuilder builder = new StringBuilder();
            parse.accept(new StatementDeParser(new ExpressionDeParser() {
                @Override
                public void visit(Function function) {
                    super.visit(function);

                    // this is deprecated, please do not use it
                    Assertions.assertEquals(true, function.isAllColumns());

                    // this is the correct way since JSQLParser 4.4
                    Assertions.assertEquals(true, function.getParameters().getExpressions().get(0) instanceof AllColumns);
                }
            }, new SelectDeParser(), builder));
        }
    }
}
