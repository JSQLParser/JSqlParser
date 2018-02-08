/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
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
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import lombok.Data;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

/**
 * The core of a "SELECT" statement (no UNION, no ORDER BY)
 */
@Data
public class PlainSelect extends ASTNodeAccessImpl implements SelectBody {

    private Distinct distinct = null;
    /**
     * The {@link SelectItem}s in this query (for example the A,B,C in "SELECT A,B,C")
     */
    private List<SelectItem> selectItems;
    private List<Table> intoTables;
    /**
     * The {@link FromItem} in this query
     */
    private FromItem fromItem;
    /**
     * The list of {@link Join}s
     */
    private List<Join> joins;
    private Expression where;
    /**
     * A list of {@link Expression}s of the GROUP BY clause. It is null in case there is no GROUP BY
     * clause
     */
    private List<Expression> groupByColumnReferences;
    private List<OrderByElement> orderByElements;
    private Expression having;
    private Limit limit;
    private Offset offset;
    private Fetch fetch;
    private Skip skip;
    private First first;
    private Top top;
    private OracleHierarchicalExpression oracleHierarchical = null;
    private OracleHint oracleHint = null;
    private boolean oracleSiblings = false;
    private boolean forUpdate = false;
    private Table forUpdateTable = null;
    private boolean useBrackets = false;
    /**
     * Sets the {@link Wait} for this SELECT
     */
    private Wait wait;
    private boolean mySqlSqlCalcFoundRows = false;

    public void addSelectItems(SelectItem... items) {
        if (selectItems == null) {
            selectItems = new ArrayList<SelectItem>();
        }
        Collections.addAll(selectItems, items);
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public void addGroupByColumnReference(Expression expr) {
        if (groupByColumnReferences == null) {
            groupByColumnReferences = new ArrayList<Expression>();
        }
        groupByColumnReferences.add(expr);
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        if (useBrackets) {
            sql.append("(");
        }
        sql.append("SELECT ");

        if (oracleHint != null) {
            sql.append(oracleHint).append(" ");
        }

        if (skip != null) {
            sql.append(skip).append(" ");
        }

        if (first != null) {
            sql.append(first).append(" ");
        }

        if (distinct != null) {
            sql.append(distinct).append(" ");
        }
        if (top != null) {
            sql.append(top).append(" ");
        }
        if (mySqlSqlCalcFoundRows) {
            sql.append("SQL_CALC_FOUND_ROWS").append(" ");
        }
        sql.append(getStringList(selectItems));

        if (intoTables != null) {
            sql.append(" INTO ");
            for (Iterator<Table> iter = intoTables.iterator(); iter.hasNext();) {
                sql.append(iter.next().toString());
                if (iter.hasNext()) {
                    sql.append(", ");
                }
            }
        }

        if (fromItem != null) {
            sql.append(" FROM ").append(fromItem);
            if (joins != null) {
                Iterator<Join> it = joins.iterator();
                while (it.hasNext()) {
                    Join join = it.next();
                    if (join.isSimple()) {
                        sql.append(", ").append(join);
                    } else {
                        sql.append(" ").append(join);
                    }
                }
            }
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
            if (oracleHierarchical != null) {
                sql.append(oracleHierarchical.toString());
            }
            sql.append(getFormatedList(groupByColumnReferences, "GROUP BY"));
            if (having != null) {
                sql.append(" HAVING ").append(having);
            }
            sql.append(orderByToString(oracleSiblings, orderByElements));
            if (limit != null) {
                sql.append(limit);
            }
            if (offset != null) {
                sql.append(offset);
            }
            if (fetch != null) {
                sql.append(fetch);
            }
            if (isForUpdate()) {
                sql.append(" FOR UPDATE");

                if (forUpdateTable != null) {
                    sql.append(" OF ").append(forUpdateTable);
                }

                if (wait != null) {
                    // Wait's toString will do the formatting for us
                    sql.append(wait);
                }
            }
        } else {
            //without from
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
        }
        if (useBrackets) {
            sql.append(")");
        }
        return sql.toString();
    }

    public static String orderByToString(List<OrderByElement> orderByElements) {
        return orderByToString(false, orderByElements);
    }

    public static String orderByToString(boolean oracleSiblings, List<OrderByElement> orderByElements) {
        return getFormatedList(orderByElements, oracleSiblings ? "ORDER SIBLINGS BY" : "ORDER BY");
    }

    public static String getFormatedList(List<?> list, String expression) {
        return getFormatedList(list, expression, true, false);
    }

    public static String getFormatedList(List<?> list, String expression, boolean useComma, boolean useBrackets) {
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
     *
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
        StringBuilder ans = new StringBuilder();
        //        String ans = "";
        String comma = ",";
        if (!useComma) {
            comma = "";
        }
        if (list != null) {
            if (useBrackets) {
                ans.append("(");
                //                ans += "(";
            }

            for (int i = 0; i < list.size(); i++) {
                ans.append(list.get(i)).append((i < list.size() - 1) ? comma + " " : "");
                //                ans += "" + list.get(i) + ((i < list.size() - 1) ? comma + " " : "");
            }

            if (useBrackets) {
                ans.append(")");
                //                ans += ")";
            }
        }

        return ans.toString();
    }
}
