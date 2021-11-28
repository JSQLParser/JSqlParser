/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.List;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.test.TestUtils;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.ValidationTestAsserts;
import net.sf.jsqlparser.util.validation.feature.DatabaseType;
import net.sf.jsqlparser.util.validation.validator.ExpressionValidator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author <a href="mailto:andreas@manticore-projects.com">Andreas Reichel</a>
 * @see <a href="https://docs.oracle.com/cd/B12037_01/server.101/b10759/statements_2013.htm">ALTER SESSION</a>
 */
public class AlterSystemTest {

    @Test
    public void testStatement() throws JSQLParserException {
        TestUtils.assertSqlCanBeParsedAndDeparsed("ALTER SYSTEM KILL SESSION '13, 8'", true);
    }

    /**
     * This test will trigger the method {@link StatementVisitorAdaptor#visit() Visit Method} in the
     * StatementVisitorAdaptor needed for the Code Coverage.
     *
     * @throws net.sf.jsqlparser.JSQLParserException
     */
    @Test
    public void testStatementVisitorAdaptor() throws JSQLParserException {
        String sqlStr = "ALTER SYSTEM KILL SESSION '13, 8'";

        CCJSqlParserUtil.parse(sqlStr).accept(new StatementVisitorAdapter());
    }

    /**
     * This test will trigger the method {@link TableNamesFinder#visit() Visit Method} in the TableNamesFinder needed
     * for the Code Coverage.
     *
     * @throws net.sf.jsqlparser.JSQLParserException
     */
    @Test
    public void testTableNamesFinder() throws JSQLParserException {
        String sqlStr = "ALTER SYSTEM KILL SESSION '13, 8'";

        Statement statement = CCJSqlParserUtil.parse(sqlStr);
        List<String> tables = new TablesNamesFinder().getTableList(statement);
        assertEquals(0, tables.size());
    }

    /**
     * This test will trigger the method {@link ExpressionValidator#visit() Visit Method} in the ExpressionValidator
     * needed for the Code Coverage.
     *
     * @throws net.sf.jsqlparser.JSQLParserException
     */
    @Test
    public void testValidator() throws JSQLParserException {
        String sqlStr = "ALTER SYSTEM KILL SESSION '13, 8'";

        ValidationTestAsserts.validateNoErrors(sqlStr, 1, DatabaseType.ORACLE);
    }

    @Test
    public void testObjectAccess() throws JSQLParserException {
        String sqlStr = "ALTER SYSTEM KILL SESSION '13, 8'";
        AlterSystemStatement statement = (AlterSystemStatement) CCJSqlParserUtil.parse(sqlStr);

        assertEquals(AlterSystemOperation.KILL_SESSION, statement.getOperation());
        assertEquals("'13, 8'", statement.getParameters().get(0));
    }
}
