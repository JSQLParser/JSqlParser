/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class StatementImplTest {
    @Test
    public void testObject() throws JSQLParserException {
        Statement statement = CCJSqlParserUtil.parse("SELECT * FROM DUAL");
        Assert.assertEquals(Statement.StatementType.QUERY, statement.getStatementType());
        Assert.assertTrue(statement.isQuery());

        statement = CCJSqlParserUtil.parse("UPDATE foo SET bar=1");
        Assert.assertEquals(Statement.StatementType.DML, statement.getStatementType());
        Assert.assertTrue(statement.isDML());

        statement = CCJSqlParserUtil.parse("CREATE TABLE foo ( bar DECIMAL(1))");
        Assert.assertEquals(Statement.StatementType.DDL, statement.getStatementType());
        Assert.assertTrue(statement.isDDL());
    }

}
