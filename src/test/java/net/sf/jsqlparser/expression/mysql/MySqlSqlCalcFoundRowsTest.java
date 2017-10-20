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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import static net.sf.jsqlparser.test.TestUtils.assertSqlCanBeParsedAndDeparsed;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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

    public void accept(Statement statement, MySqlSqlCalcFoundRowRef ref) {
        statement.accept(SqlCalcFoundRowsStatementVisitorFactory.create(ref));
    }
}
