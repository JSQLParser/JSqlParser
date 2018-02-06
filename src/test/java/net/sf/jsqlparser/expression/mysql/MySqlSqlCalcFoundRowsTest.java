/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.expression.mysql;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitorAdapter;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitorAdapter;

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

        assertSqlCanBeParsedAndDeparsed(sqlCalcFoundRowsContainingSql);
        assertSqlCanBeParsedAndDeparsed(generalSql);
    }

    private void accept(Statement statement, final MySqlSqlCalcFoundRowRef ref) {
        statement.accept(new StatementVisitorAdapter() {

            @Override
            public void visit(Select select) {
                select.getSelectBody().accept(new SelectVisitorAdapter() {

                    @Override
                    public void visit(PlainSelect plainSelect) {
                        ref.sqlCalcFoundRows = plainSelect.isMySqlSqlCalcFoundRows();
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
