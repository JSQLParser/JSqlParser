/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2014 JSQLParser
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
package net.sf.jsqlparser.util;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * Utility function for select statements.
 *
 * @author toben
 */
public final class SelectUtils {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";

    private SelectUtils() {
    }

    /**
     * Builds select expr1, expr2 from table.
     *
     * @param table
     * @param expr
     * @return
     */
    public static Select buildSelectFromTableAndExpressions(Table table, Expression... expr) {
        SelectItem[] list = new SelectItem[expr.length];
        for (int i = 0; i < expr.length; i++) {
            list[i] = new SelectExpressionItem(expr[i]);
        }
        return buildSelectFromTableAndSelectItems(table, list);
    }

    /**
     * Builds select expr1, expr2 from table.
     *
     * @param table
     * @param expr
     * @return
     * @throws net.sf.jsqlparser.JSQLParserException
     */
    public static Select buildSelectFromTableAndExpressions(Table table, String... expr) throws JSQLParserException {
        SelectItem[] list = new SelectItem[expr.length];
        for (int i = 0; i < expr.length; i++) {
            list[i] = new SelectExpressionItem(CCJSqlParserUtil.parseExpression(expr[i]));
        }
        return buildSelectFromTableAndSelectItems(table, list);
    }

    public static Select buildSelectFromTableAndSelectItems(Table table, SelectItem... selectItems) {
        Select select = new Select();
        PlainSelect body = new PlainSelect();
        body.addSelectItems(selectItems);
        body.setFromItem(table);
        select.setSelectBody(body);
        return select;
    }

    /**
     * Builds select * from table.
     *
     * @param table
     * @return
     */
    public static Select buildSelectFromTable(Table table) {
        return buildSelectFromTableAndSelectItems(table, new AllColumns());
    }

    /**
     * Adds an expression to select statements. E.g. a simple column is an expression.
     *
     * @param select
     * @param expr
     */
    public static void addExpression(Select select, final Expression expr) {
        select.getSelectBody().accept(new SelectVisitor() {

            @Override
            public void visit(PlainSelect plainSelect) {
                plainSelect.getSelectItems().add(new SelectExpressionItem(expr));
            }

            @Override
            public void visit(SetOperationList setOpList) {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
            }

            @Override
            public void visit(WithItem withItem) {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
            }
        });
    }

    /**
     * Adds a simple join to a select statement. The introduced join is returned for more
     * configuration settings on it (e.g. left join, right join).
     *
     * @param select
     * @param table
     * @param onExpression
     * @return
     */
    public static Join addJoin(Select select, final Table table, final Expression onExpression) {
        if (select.getSelectBody() instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            List<Join> joins = plainSelect.getJoins();
            if (joins == null) {
                joins = new ArrayList<Join>();
                plainSelect.setJoins(joins);
            }
            Join join = new Join();
            join.setRightItem(table);
            join.setOnExpression(onExpression);
            joins.add(join);
            return join;
        }
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    /**
     * Adds group by to a plain select statement.
     *
     * @param select
     * @param expr
     */
    public static void addGroupBy(Select select, final Expression expr) {
        select.getSelectBody().accept(new SelectVisitor() {

            @Override
            public void visit(PlainSelect plainSelect) {
                plainSelect.addGroupByColumnReference(expr);
            }

            @Override
            public void visit(SetOperationList setOpList) {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
            }

            @Override
            public void visit(WithItem withItem) {
                throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
            }
        });
    }
}
