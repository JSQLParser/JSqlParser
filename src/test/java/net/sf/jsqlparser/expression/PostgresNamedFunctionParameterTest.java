/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.validator.ExpressionValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 */
public class PostgresNamedFunctionParameterTest {

    /**
     * This test will parse and deparse the statement and assures the functional coverage by
     * JSQLParser.
     *
     * @throws JSQLParserException
     */
    @Test
    public void testExpression() throws JSQLParserException {
        String sqlStr =
                "SELECT concat_lower_or_upper(a := 'Hello', uppercase := true, b := 'World')";

        TestUtils.assertSqlCanBeParsedAndDeparsed(sqlStr, true);
    }

    /**
     * This test will trigger the method {@link ExpressionVisitorAdaptor#visit() Visit Method} in
     * the ExpressionVisitorAdaptor needed for the Code Coverage.
     *
     * @throws JSQLParserException
     */
    @Test
    public void testExpressionVisitorAdaptor() throws JSQLParserException {
        String sqlStr =
                "SELECT concat_lower_or_upper(a := 'Hello', uppercase := true, b := 'World')";

        CCJSqlParserUtil.parse(sqlStr).accept(new StatementVisitorAdapter());

        // alternatively, for the Expression only
        CCJSqlParserUtil.parseExpression("a := 'Hello'").accept(new ExpressionVisitorAdapter(),
                null);
    }

    /**
     * This test will trigger the method {@link TableNamesFinder#visit() Visit Method} in the
     * TableNamesFinder needed for the Code Coverage.
     *
     * @throws JSQLParserException
     */
    @Test
    public void testTableNamesFinder() throws JSQLParserException {
        String sqlStr =
                "SELECT concat_lower_or_upper(a := 'Hello', uppercase := true, b := 'World') FROM test_table";

        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        List<String> tables = new TablesNamesFinder<>().getTableList(statement);
        assertEquals(1, tables.size());
        assertTrue(tables.contains("test_table"));
    }

    /**
     * This test will trigger the method {@link ExpressionValidator#visit() Visit Method} in the
     * ExpressionValidator needed for the Code Coverage.
     *
     * @throws JSQLParserException
     */
    @Test
    public void testValidator() throws JSQLParserException {
        String sqlStr =
                "SELECT concat_lower_or_upper(a := 'Hello', uppercase := true, b := 'World') FROM test_table";

        ValidationTestAsserts.validateNoErrors(sqlStr, 1, DatabaseType.POSTGRESQL);
    }
}
