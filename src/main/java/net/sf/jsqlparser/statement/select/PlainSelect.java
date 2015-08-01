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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;

/**
 * The core of a "SELECT" statement (no UNION, no ORDER BY)
 */
public class PlainSelect implements SelectBody {

    private Distinct distinct = null;
    private List<SelectItem> selectItems;
    private List<Table> intoTables;
    private FromItem fromItem;
    private List<Join> joins;
    private Expression where;
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
    private boolean oracleSiblings = false;
    private boolean forUpdate = false;
    private Table forUpdateTable = null;
    private boolean useBrackets = false;

    public boolean isUseBrackets() {
        return useBrackets;
    }

    public void setUseBrackets(boolean useBrackets) {
        this.useBrackets = useBrackets;
    }

    /**
     * The {@link FromItem} in this query
     *
     * @return the {@link FromItem}
     */
    public FromItem getFromItem() {
        return fromItem;
    }

    public List<Table> getIntoTables() {
        return intoTables;
    }

    /**
     * The {@link SelectItem}s in this query (for example the A,B,C in "SELECT
     * A,B,C")
     *
     * @return a list of {@link SelectItem}s
     */
    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public Expression getWhere() {
        return where;
    }

    public void setFromItem(FromItem item) {
        fromItem = item;
    }

    public void setIntoTables(List<Table> intoTables) {
        this.intoTables = intoTables;
    }

    public void setSelectItems(List<SelectItem> list) {
        selectItems = list;
    }

    public void addSelectItems(SelectItem... items) {
        if (selectItems == null) {
            selectItems = new ArrayList<SelectItem>();
        }
        Collections.addAll(selectItems, items);
    }

    public void setWhere(Expression where) {
        this.where = where;
    }

    /**
     * The list of {@link Join}s
     *
     * @return the list of {@link Join}s
     */
    public List<Join> getJoins() {
        return joins;
    }

    public void setJoins(List<Join> list) {
        joins = list;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public List<OrderByElement> getOrderByElements() {
        return orderByElements;
    }

    public void setOrderByElements(List<OrderByElement> orderByElements) {
        this.orderByElements = orderByElements;
    }

    public Limit getLimit() {
        return limit;
    }

    public void setLimit(Limit limit) {
        this.limit = limit;
    }

    public Offset getOffset() {
        return offset;
    }

    public void setOffset(Offset offset) {
        this.offset = offset;
    }

    public Fetch getFetch() {
        return fetch;
    }

    public void setFetch(Fetch fetch) {
        this.fetch = fetch;
    }

    public Top getTop() {
        return top;
    }

    public void setTop(Top top) {
        this.top = top;
    }

    public Skip getSkip() {
        return skip;
    }

    public void setSkip(Skip skip) {
        this.skip = skip;
    }

    public First getFirst() {
        return first;
    }

    public void setFirst(First first) {
        this.first = first;
    }
    
    public Distinct getDistinct() {
        return distinct;
    }

    public void setDistinct(Distinct distinct) {
        this.distinct = distinct;
    }

    public Expression getHaving() {
        return having;
    }

    public void setHaving(Expression expression) {
        having = expression;
    }

    /**
     * A list of {@link Expression}s of the GROUP BY clause. It is null in case
     * there is no GROUP BY clause
     *
     * @return a list of {@link Expression}s
     */
    public List<Expression> getGroupByColumnReferences() {
        return groupByColumnReferences;
    }

    public void setGroupByColumnReferences(List<Expression> list) {
        groupByColumnReferences = list;
    }

    public void addGroupByColumnReference(Expression expr) {
        if (groupByColumnReferences == null) {
            groupByColumnReferences = new ArrayList<Expression>();
        }
        groupByColumnReferences.add(expr);
    }

    public OracleHierarchicalExpression getOracleHierarchical() {
        return oracleHierarchical;
    }

    public void setOracleHierarchical(OracleHierarchicalExpression oracleHierarchical) {
        this.oracleHierarchical = oracleHierarchical;
    }

    public boolean isOracleSiblings() {
        return oracleSiblings;
    }

    public void setOracleSiblings(boolean oracleSiblings) {
        this.oracleSiblings = oracleSiblings;
    }

    public boolean isForUpdate() {
        return forUpdate;
    }

    public void setForUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
    }

    public Table getForUpdateTable() {
        return forUpdateTable;
    }

    public void setForUpdateTable(Table forUpdateTable) {
        this.forUpdateTable = forUpdateTable;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        if (useBrackets) {
            sql.append("(");
        }
        sql.append("SELECT ");
        
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
            // sql += getFormatedList(joins, "", false, false);
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
     * List the toString out put of the objects in the List comma separated. If
     * the List is null or empty an empty string is returned.
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
     * List the toString out put of the objects in the List that can be comma
     * separated. If the List is null or empty an empty string is returned.
     *
     * @param list list of objects with toString methods
     * @param useComma true if the list has to be comma separated
     * @param useBrackets true if the list has to be enclosed in brackets
     * @return comma separated list of the elements in the list
     */
    public static String getStringList(List<?> list, boolean useComma, boolean useBrackets) {
        String ans = "";
        String comma = ",";
        if (!useComma) {
            comma = "";
        }
        if (list != null) {
            if (useBrackets) {
                ans += "(";
            }

            for (int i = 0; i < list.size(); i++) {
                ans += "" + list.get(i) + ((i < list.size() - 1) ? comma + " " : "");
            }

            if (useBrackets) {
                ans += ")";
            }
        }

        return ans;
    }
}
