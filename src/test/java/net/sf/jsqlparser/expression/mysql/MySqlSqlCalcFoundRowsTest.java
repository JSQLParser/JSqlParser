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
import net.sf.jsqlparser.expression.MySqlSqlCalcFoundRows;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author sam
 */
public class MySqlSqlCalcFoundRowsTest {
    @Test
    public void testPossibleParsingWithSqlCalcFoundRowsHint() throws JSQLParserException {
        AtomicReference<MySqlSqlCalcFoundRows> ref = new AtomicReference<MySqlSqlCalcFoundRows>();

        accept(CCJSqlParserUtil.parse("SELECT SQL_CALC_FOUND_ROWS * FROM TABLE"), ref);
        assertNotNull(ref);
        assertEquals(MySqlSqlCalcFoundRows.DESCRIPTION, ref.get().getDescription());

        ref.set(null);
        accept(CCJSqlParserUtil.parse("SELECT * FROM TABLE"), ref);
        assertNull(ref.get());
    }

    public void accept(Statement statement, final AtomicReference<MySqlSqlCalcFoundRows> ref) {
        statement.accept(StatementVisitorFactory.create(ref));
    }
}
