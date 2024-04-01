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
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;

public final class SelectUtils {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";

    private SelectUtils() {}

    public static Select buildSelectFromTableAndExpressions(Table table, Expression... expr) {
        SelectItem[] list = new SelectItem[expr.length];
        for (int i = 0; i < expr.length; i++) {
            list[i] = new SelectItem(expr[i]);
        }
        return buildSelectFromTableAndSelectItems(table, list);
    }

    public static Select buildSelectFromTableAndExpressions(Table table, String... expr)
            throws JSQLParserException {
        SelectItem[] list = new SelectItem[expr.length];
        for (int i = 0; i < expr.length; i++) {
            list[i] = new SelectItem(CCJSqlParserUtil.parseExpression(expr[i]));
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
        return buildSelectFromTableAndSelectItems(table, SelectItem.from(new AllColumns()));
    }

    /**
     * Adds an expression to select statements. E.g. a simple column is an expression.
     *
     * @param select
     * @param expr
     */
    public static void addExpression(Select select, final Expression expr) {
        if (select instanceof PlainSelect) {
            ((PlainSelect) select).addSelectItem(expr);
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

    public static String getFormattedList(List<?> list, String expression) {
        return getFormattedList(list, expression, true, false);
    }

    public static String getFormattedList(List<?> list, String expression, boolean useComma,
                                          boolean useBrackets) {
        String sql = getStringList(list, useComma, useBrackets);

        if (sql.length() > 0) {
            if (expression.length() > 0) {
                sql = " " + expression + " " + sql;
            } else {
                sql = " " + sql;
            }
        }

        return sql;
    }

    /**
     * List the toString out put of the objects in the List comma separated. If the List is null or
     * empty an empty string is returned.
     * <p>
     * The same as getStringList(list, true, false)
     *
     * @see #getStringList(List, boolean, boolean)
     * @param list list of objects with toString methods
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list) {
        return getStringList(list, true, false);
    }

    /**
     * List the toString out put of the objects in the List that can be comma separated. If the List
     * is null or empty an empty string is returned.
     *
     * @param list list of objects with toString methods
     * @param useComma true if the list has to be comma separated
     * @param useBrackets true if the list has to be enclosed in brackets
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list, boolean useComma, boolean useBrackets) {
        return appendStringListTo(new StringBuilder(), list, useComma, useBrackets).toString();
    }

    /**
     * Append the toString out put of the objects in the List (that can be comma separated). If the
     * List is null or empty an empty string is returned.
     *
     * @param list list of objects with toString methods
     * @param useComma true if the list has to be comma separated
     * @param useBrackets true if the list has to be enclosed in brackets
     * @return comma separated list of the elements in the list
     */
    public static StringBuilder appendStringListTo(StringBuilder builder, List<?> list,
            boolean useComma, boolean useBrackets) {
        if (list != null) {
            String comma = useComma ? ", " : " ";

            if (useBrackets) {
                builder.append("(");
            }

            int size = list.size();
            for (int i = 0; i < size; i++) {
                builder.append(list.get(i)).append(i < size - 1 ? comma : "");
            }

            if (useBrackets) {
                builder.append(")");
            }
        }
        return builder;
    }
}
