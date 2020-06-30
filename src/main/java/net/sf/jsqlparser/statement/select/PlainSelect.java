/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

public class PlainSelect extends ASTNodeAccessImpl implements SelectBody {

    private Distinct distinct = null;
    private List<SelectItem> selectItems;
    private List<Table> intoTables;
    private FromItem fromItem;
    private List<Join> joins;
    private Expression where;
    private GroupByElement groupBy;
    private List<OrderByElement> orderByElements;
    private Expression having;
    private Limit limit;
    private Offset offset;
    private Fetch fetch;
    private OptimizeFor optimizeFor;
    private Skip skip;
    private boolean mySqlHintStraightJoin;
    private First first;
    private Top top;
    private OracleHierarchicalExpression oracleHierarchical = null;
    private OracleHint oracleHint = null;
    private boolean oracleSiblings = false;
    private boolean forUpdate = false;
    private Table forUpdateTable = null;
    private boolean useBrackets = false;
    private Wait wait;
    private boolean mySqlSqlCalcFoundRows = false;
    private boolean sqlNoCacheFlag = false;
    private String forXmlPath;
    private KSQLWindow ksqlWindow = null;
    private boolean noWait = false;

    public boolean isUseBrackets() {
        return useBrackets;
    }

    public void setUseBrackets(boolean useBrackets) {
        this.useBrackets = useBrackets;
    }

    public FromItem getFromItem() {
        return fromItem;
    }

    public List<Table> getIntoTables() {
        return intoTables;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public Expression getWhere() {
        return where;
    }

    public PlainSelect fromItem(FromItem item) {
        setFromItem(item);
        return this;
    }

    public void setFromItem(FromItem item) {
        fromItem = item;
    }

    public void setIntoTables(List<Table> intoTables) {
        this.intoTables = intoTables;
    }

    public PlainSelect selectItems(SelectItem... items) {
        return selectItems(Arrays.asList(items));
    }

    public PlainSelect selectItems(List<SelectItem> list) {
        setSelectItems(list);
        return this;
    }

    public void setSelectItems(List<SelectItem> list) {
        selectItems = list;
    }

    public PlainSelect addSelectItems(SelectItem... items) {
        List<SelectItem> list = Optional.ofNullable(getSelectItems()).orElseGet(ArrayList::new);
        Collections.addAll(list, items);
        return selectItems(list);
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

    public PlainSelect addJoins(Join... joins) {
        List<Join> list = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        Collections.addAll(list, joins);
        return joins(list);
    }

    public PlainSelect joins(Join... joins) {
        return joins(Arrays.asList(joins));
    }

    public PlainSelect joins(List<Join> joins) {
        setJoins(joins);
        return this;
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

    public OptimizeFor getOptimizeFor() {
        return optimizeFor;
    }

    public void setOptimizeFor(OptimizeFor optimizeFor) {
        this.optimizeFor = optimizeFor;
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

    public boolean getMySqlHintStraightJoin() {
        return this.mySqlHintStraightJoin;
    }

    public void setMySqlHintStraightJoin(boolean mySqlHintStraightJoin) {
        this.mySqlHintStraightJoin = mySqlHintStraightJoin;
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
    public GroupByElement getGroupBy() {
        return this.groupBy;
    }

    public void setGroupByElement(GroupByElement groupBy) {
        this.groupBy = groupBy;
    }

    public PlainSelect addGroupByColumnReference(Expression expr) {
        groupBy = Optional.ofNullable(groupBy).orElseGet(GroupByElement::new);
        groupBy.addGroupByExpression(expr);
        return this;
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

    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
    }

    /**
     * Sets the {@link Wait} for this SELECT
     *
     * @param wait the {@link Wait} for this SELECT
     */
    public void setWait(final Wait wait) {
        this.wait = wait;
    }

    /**
     * Returns the value of the {@link Wait} set for this SELECT
     *
     * @return the value of the {@link Wait} set for this SELECT
     */
    public Wait getWait() {
        return wait;
    }

    public String getForXmlPath() {
        return forXmlPath;
    }

    public void setForXmlPath(String forXmlPath) {
        this.forXmlPath = forXmlPath;
    }

    public KSQLWindow getKsqlWindow() {
        return ksqlWindow;
    }

    public void setKsqlWindow(KSQLWindow ksqlWindow) {
        this.ksqlWindow = ksqlWindow;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        if (useBrackets) {
            sql.append("(");
        }
        sql.append("SELECT ");

        if (this.mySqlHintStraightJoin) {
            sql.append("STRAIGHT_JOIN ");
        }

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
        if (sqlNoCacheFlag) {
            sql.append("SQL_NO_CACHE").append(" ");
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

            if (ksqlWindow != null) {
                sql.append(" WINDOW ").append(ksqlWindow.toString());
            }
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
            if (oracleHierarchical != null) {
                sql.append(oracleHierarchical.toString());
            }
            if (groupBy != null) {
                sql.append(" ").append(groupBy.toString());
            }
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

                if (isNoWait()) {
                    sql.append(" NOWAIT");
                }
            }
            if (optimizeFor != null) {
                sql.append(optimizeFor);
            }
        } else {
            // without from
            if (where != null) {
                sql.append(" WHERE ").append(where);
            }
        }
        if (forXmlPath != null) {
            sql.append(" FOR XML PATH(").append(forXmlPath).append(")");
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
     * List the toString out put of the objects in the List comma separated. If the
     * List is null or empty an empty string is returned.
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
     * @param list        list of objects with toString methods
     * @param useComma    true if the list has to be comma separated
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

    public PlainSelect mySqlSqlCalcFoundRows(boolean mySqlCalcFoundRows) {
        setMySqlSqlCalcFoundRows(mySqlCalcFoundRows);
        return this;
    }

    public PlainSelect mySqlSqlNoCache(boolean sqlNoCacheFlagSet) {
        setMySqlSqlNoCache(sqlNoCacheFlagSet);
        return this;
    }

    public void setMySqlSqlCalcFoundRows(boolean mySqlCalcFoundRows) {
        this.mySqlSqlCalcFoundRows = mySqlCalcFoundRows;
    }

    public void setMySqlSqlNoCache(boolean sqlNoCacheFlagSet) {
        this.sqlNoCacheFlag = sqlNoCacheFlagSet;
    }

    public boolean getMySqlSqlCalcFoundRows() {
        return this.mySqlSqlCalcFoundRows;
    }

    public boolean getMySqlSqlNoCache() {
        return this.sqlNoCacheFlag;
    }

    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
    }

    public boolean isNoWait() {
        return this.noWait;
    }

    public static PlainSelect create() {
        return new PlainSelect();
    }

    public <T extends Expression> T getWhere(Class<T> type) {
        return Optional.ofNullable(where).map(type::cast).orElseGet(null);
    }

    public PlainSelect distinct(Distinct distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public PlainSelect intoTables(List<Table> intoTables) {
        this.setIntoTables(intoTables);
        return this;
    }

    public PlainSelect where(Expression where) {
        this.setWhere(where);
        return this;
    }

    public PlainSelect orderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public PlainSelect limit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public PlainSelect offset(Offset offset) {
        this.setOffset(offset);
        return this;
    }

    public PlainSelect fetch(Fetch fetch) {
        this.setFetch(fetch);
        return this;
    }

    public PlainSelect optimizeFor(OptimizeFor optimizeFor) {
        this.setOptimizeFor(optimizeFor);
        return this;
    }

    public PlainSelect skip(Skip skip) {
        this.setSkip(skip);
        return this;
    }

    public PlainSelect mySqlHintStraightJoin(boolean mySqlHintStraightJoin) {
        this.setMySqlHintStraightJoin(mySqlHintStraightJoin);
        return this;
    }

    public PlainSelect first(First first) {
        this.setFirst(first);
        return this;
    }

    public PlainSelect top(Top top) {
        this.setTop(top);
        return this;
    }

    public PlainSelect oracleHierarchical(OracleHierarchicalExpression oracleHierarchical) {
        this.setOracleHierarchical(oracleHierarchical);
        return this;
    }

    public PlainSelect oracleHint(OracleHint oracleHint) {
        this.setOracleHint(oracleHint);
        return this;
    }

    public PlainSelect oracleSiblings(boolean oracleSiblings) {
        this.setOracleSiblings(oracleSiblings);
        return this;
    }

    public PlainSelect forUpdate(boolean forUpdate) {
        this.setForUpdate(forUpdate);
        return this;
    }

    public PlainSelect forUpdateTable(Table forUpdateTable) {
        this.setForUpdateTable(forUpdateTable);
        return this;
    }

    public PlainSelect useBrackets(boolean useBrackets) {
        this.setUseBrackets(useBrackets);
        return this;
    }

    public PlainSelect forXmlPath(String forXmlPath) {
        this.setForXmlPath(forXmlPath);
        return this;
    }

    public PlainSelect ksqlWindow(KSQLWindow ksqlWindow) {
        this.setKsqlWindow(ksqlWindow);
        return this;
    }

    public PlainSelect noWait(boolean noWait) {
        this.setNoWait(noWait);
        return this;
    }
}
