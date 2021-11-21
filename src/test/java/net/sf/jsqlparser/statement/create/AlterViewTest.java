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

import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertEqualsObjectTree;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlterViewTest {

    @Test
    public void testAlterView() throws JSQLParserException {
        String statement = "ALTER VIEW myview AS SELECT * FROM mytab";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        AlterView created = new AlterView().withView(new Table("myview")).withSelectBody(new PlainSelect()
                .addSelectItems(Collections.singleton(new AllColumns())).withFromItem(new Table("mytab")));
        assertDeparse(created, statement);
        assertEqualsObjectTree(parsed, created);
    }

    @Test
    public void testReplaceView() throws JSQLParserException {
        String statement = "REPLACE VIEW myview(a, b) AS SELECT a, b FROM mytab";
        Statement parsed = assertSqlCanBeParsedAndDeparsed(statement);
        AlterView alterView = new AlterView().withUseReplace(true).addColumnNames("a").addColumnNames(Collections.singleton("b"))
                .withView(new Table("myview"))
                .withSelectBody(new PlainSelect()
                        .addSelectItems(new SelectExpressionItem(new Column("a")),
                                new SelectExpressionItem(new Column("b")))
                        .withFromItem(new Table("mytab")));
        assertTrue(alterView.getSelectBody(PlainSelect.class) instanceof PlainSelect);
        assertDeparse(alterView, statement);
        assertEqualsObjectTree(parsed, alterView);
    }
}
