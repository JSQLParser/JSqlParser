/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

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

public final class SelectUtils {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";

    private SelectUtils() {}

    public static Select buildSelectFromTableAndExpressions(Table table, Expression... expr) {
        SelectItem[] list = new SelectItem[expr.length];
        for (int i = 0; i < expr.length; i++) {
            list[i] = new SelectExpressionItem(expr[i]);
        }
        return buildSelectFromTableAndSelectItems(table, list);
    }

    public static Select buildSelectFromTableAndExpressions(Table table, String... expr)
            throws JSQLParserException {
        SelectItem[] list = new SelectItem[expr.length];
        for (int i = 0; i < expr.length; i++) {
            list[i] = new SelectExpressionItem(CCJSqlParserUtil.parseExpression(expr[i]));
        }
        return buildSelectFromTableAndSelectItems(table, list);
    }

    public static Select buildSelectFromTableAndSelectItems(Table table,
            SelectItem... selectItems) {
        PlainSelect select = new PlainSelect().addSelectItems(selectItems).withFromItem(table);
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
        if (select instanceof PlainSelect) {
            ((PlainSelect) select).getSelectItems().add(new SelectExpressionItem(expr));
        } else {
            throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
        }
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
        if (select instanceof PlainSelect) {
            Join join = new Join().withRightItem(table).addOnExpression(onExpression);
            ((PlainSelect) select).addJoins(join);
            return join;
        } else {
            throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
        }
    }

    /**
     * Adds group by to a plain select statement.
     *
     * @param select
     * @param expr
     */
    public static void addGroupBy(Select select, final Expression expr) {
        if (select instanceof PlainSelect) {
            ((PlainSelect) select).addGroupByColumnReference(expr);
        } else {
            throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
        }
    }
}
