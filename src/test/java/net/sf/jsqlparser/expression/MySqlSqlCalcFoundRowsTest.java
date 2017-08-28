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
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
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
        statement.accept(new StatementVisitor() {
            @Override
            public void visit(Commit commit) {

            }

            @Override
            public void visit(Delete delete) {

            }

            @Override
            public void visit(Update update) {

            }

            @Override
            public void visit(Insert insert) {

            }

            @Override
            public void visit(Replace replace) {

            }

            @Override
            public void visit(Drop drop) {

            }

            @Override
            public void visit(Truncate truncate) {

            }

            @Override
            public void visit(CreateIndex createIndex) {

            }

            @Override
            public void visit(CreateTable createTable) {

            }

            @Override
            public void visit(CreateView createView) {

            }

            @Override
            public void visit(AlterView alterView) {

            }

            @Override
            public void visit(Alter alter) {

            }

            @Override
            public void visit(Statements stmts) {

            }

            @Override
            public void visit(Execute execute) {

            }

            @Override
            public void visit(SetStatement set) {

            }

            @Override
            public void visit(Merge merge) {

            }

            @Override
            public void visit(Select select) {
                select.getSelectBody().accept(new SelectVisitor() {
                    @Override
                    public void visit(PlainSelect plainSelect) {
                        ref.set(plainSelect.getMySqlSqlCalcFoundRows());
                    }

                    @Override
                    public void visit(SetOperationList setOpList) {

                    }

                    @Override
                    public void visit(WithItem withItem) {

                    }
                });

            }

            @Override
            public void visit(Upsert upsert) {

            }
        });
    }
}
