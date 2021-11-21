/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.mysql;

import java.util.Arrays;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;
import static net.sf.jsqlparser.test.TestUtils.assertDeparse;
import static net.sf.jsqlparser.test.TestUtils.assertEqualsObjectTree;
import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * @author sam
 */
public class MySqlSqlCalcFoundRowsTest {

    @Test
    public void testPossibleParsingWithSqlCalcFoundRowsHint() throws JSQLParserException {
        MySqlSqlCalcFoundRowRef ref = new MySqlSqlCalcFoundRowRef(false);
        String sqlCalcFoundRowsContainingSql = "SELECT SQL_CALC_FOUND_ROWS * FROM TABLE";
        String generalSql = "SELECT * FROM TABLE";

        accept(CCJSqlParserUtil.parse(sqlCalcFoundRowsContainingSql), ref);
        assertTrue(ref.sqlCalcFoundRows);

        accept(CCJSqlParserUtil.parse(generalSql), ref);
        assertFalse(ref.sqlCalcFoundRows);

        Statement parsed = assertSqlCanBeParsedAndDeparsed(sqlCalcFoundRowsContainingSql);
        assertSqlCanBeParsedAndDeparsed(generalSql);
        Select created = new Select().withSelectBody(new PlainSelect().addSelectItems(Arrays.asList(new AllColumns()))
                .withMySqlSqlCalcFoundRows(true).withFromItem(new Table("TABLE")));
        assertDeparse(created, sqlCalcFoundRowsContainingSql);
        assertEqualsObjectTree(parsed, created);
    }

    private void accept(Statement statement, final MySqlSqlCalcFoundRowRef ref) {
        statement.accept(new StatementVisitorAdapter() {
            @Override
            public void visit(Select select) {
                select.getSelectBody().accept(new SelectVisitorAdapter() {
                    @Override
                    public void visit(PlainSelect plainSelect) {
                        ref.sqlCalcFoundRows = plainSelect.getMySqlSqlCalcFoundRows();
                    }
                });
            }

        });
    }
}

class MySqlSqlCalcFoundRowRef {

    public boolean sqlCalcFoundRows = false;

    public MySqlSqlCalcFoundRowRef(boolean sqlCalcFoundRows) {
        this.sqlCalcFoundRows = sqlCalcFoundRows;
    }
}
