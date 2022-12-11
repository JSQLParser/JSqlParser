/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create;

import java.io.StringReader;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.select.PlainSelect;
import static net.sf.jsqlparser.test.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class CreateViewTest {

    private CCJSqlParserManager parserManager = new CCJSqlParserManager();

    @Test
    public void testCreateView() throws JSQLParserException {
        String statement = "CREATE VIEW myview AS SELECT * FROM mytab";
        CreateView createView = (CreateView) parserManager.parse(new StringReader(statement));
        assertFalse(createView.isOrReplace());
        assertEquals("myview", createView.getView().getName());
        assertEquals("mytab", ((Table) ((PlainSelect) createView.getSelect().getSelectBody()).getFromItem()).getName());
        assertEquals(statement, createView.toString());
    }

    @Test
    public void testCreateView2() throws JSQLParserException {
        String stmt = "CREATE VIEW myview AS SELECT * FROM mytab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateView3() throws JSQLParserException {
        String stmt = "CREATE OR REPLACE VIEW myview AS SELECT * FROM mytab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateView4() throws JSQLParserException {
        String stmt = "CREATE OR REPLACE VIEW view2 AS SELECT a, b, c FROM testtab INNER JOIN testtab2 ON testtab.col1 = testtab2.col2";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateViewWithColumnNames1() throws JSQLParserException {
        String stmt = "CREATE OR REPLACE VIEW view1(col1, col2) AS SELECT a, b FROM testtab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateView5() throws JSQLParserException {
        String statement = "CREATE VIEW myview AS (SELECT * FROM mytab)";
        String statement2 = "CREATE VIEW myview AS (SELECT * FROM mytab)";
        CreateView createView = (CreateView) parserManager.parse(new StringReader(statement));
        assertFalse(createView.isOrReplace());
        assertEquals("myview", createView.getView().getName());
        assertEquals("mytab", ((Table) ((PlainSelect) createView.getSelect().getSelectBody()).getFromItem()).
                getName());
        assertEquals(statement2, createView.toString());
    }

    @Test
    public void testCreateViewUnion() throws JSQLParserException {
        String stmt = "CREATE VIEW view1 AS (SELECT a, b FROM testtab) UNION (SELECT b, c FROM testtab2)";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateMaterializedView() throws JSQLParserException {
        String stmt = "CREATE MATERIALIZED VIEW view1 AS SELECT a, b FROM testtab";
        assertSqlCanBeParsedAndDeparsed(stmt);
    }

    @Test
    public void testCreateForceView() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE FORCE VIEW view1 AS SELECT a, b FROM testtab");
    }

    @Test
    public void testCreateForceView1() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE NO FORCE VIEW view1 AS SELECT a, b FROM testtab");
    }

    @Test
    public void testCreateForceView2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE OR REPLACE FORCE VIEW view1 AS SELECT a, b FROM testtab");
    }

    @Test
    public void testCreateForceView3() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE OR REPLACE NO FORCE VIEW view1 AS SELECT a, b FROM testtab");
    }

    @Test
    public void testCreateTemporaryViewIssue604() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TEMPORARY VIEW myview AS SELECT * FROM mytable");
    }

    @Test
    public void testCreateTemporaryViewIssue604_2() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE TEMP VIEW myview AS SELECT * FROM mytable");
    }

    @Test
    public void testCreateTemporaryViewIssue665() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE VIEW foo(\"BAR\") AS WITH temp AS (SELECT temp_bar FROM foobar) SELECT bar FROM temp");
    }

    @Test
    public void testCreateWithReadOnlyViewIssue838() throws JSQLParserException {
        assertSqlCanBeParsedAndDeparsed("CREATE VIEW v14(c1, c2) AS SELECT c1, C2 FROM t1 WITH READ ONLY");
    }

    @Test
    public void testCreateViewIfNotExists() throws JSQLParserException {
        String stmt = "CREATE VIEW myview IF NOT EXISTS AS SELECT * FROM mytab";
        CreateView createView = (CreateView) assertSqlCanBeParsedAndDeparsed(stmt);
        assertTrue(createView.isIfNotExists());
    }
    
    @Test
    public void testCreateMaterializedViewIfNotExists() throws JSQLParserException {
        String stmt = "CREATE MATERIALIZED VIEW myview IF NOT EXISTS AS SELECT * FROM mytab";
        CreateView createView = (CreateView) assertSqlCanBeParsedAndDeparsed(stmt);
        assertTrue(createView.isMaterialized());
        assertTrue(createView.isIfNotExists());
    }
}
