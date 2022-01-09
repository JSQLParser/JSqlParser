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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
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
    private MySqlSqlCacheFlags mySqlCacheFlag = null;
    private String forXmlPath;
    private KSQLWindow ksqlWindow = null;
    private boolean noWait = false;
    private boolean emitChanges = false;

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

    public PlainSelect withFromItem(FromItem item) {
        this.setFromItem(item);
        return this;
    }

    public void setFromItem(FromItem item) {
        fromItem = item;
    }

    public void setIntoTables(List<Table> intoTables) {
        this.intoTables = intoTables;
    }

    public PlainSelect withSelectItems(List<SelectItem> list) {
        this.setSelectItems(list);
        return this;
    }

    public void setSelectItems(List<SelectItem> list) {
        selectItems = list;
    }

    public PlainSelect addSelectItems(SelectItem... items) {
        List<SelectItem> list = Optional.ofNullable(getSelectItems()).orElseGet(ArrayList::new);
        Collections.addAll(list, items);
        return withSelectItems(list);
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
        return withJoins(list);
    }

    public PlainSelect withJoins(List<Join> joins) {
        this.setJoins(joins);
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

    public void setEmitChanges(boolean emitChanges) {
        this.emitChanges = emitChanges;
    }

    public boolean isEmitChanges() {
        return emitChanges;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity" , "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
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
        if (mySqlCacheFlag != null) {
            sql.append(mySqlCacheFlag.name()).append(" ");
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
            if (emitChanges){
                sql.append(" EMIT CHANGES");
            }
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

            if (limit != null) {
                sql.append(limit);
            }
            if (offset != null) {
                sql.append(offset);
            }
            if (fetch != null) {
                sql.append(fetch);
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
        String comma = ",";
        if (!useComma) {
            comma = "";
        }
        if (list != null) {
            if (useBrackets) {
                ans.append("(");
            }

            for (int i = 0; i < list.size(); i++) {
                ans.append(list.get(i)).append( i < list.size() - 1 
                                                                            ? comma + " " 
                                                                            : "" );
            }

            if (useBrackets) {
                ans.append(")");
            }
        }

        return ans.toString();
    }

    public PlainSelect withMySqlSqlCalcFoundRows(boolean mySqlCalcFoundRows) {
        this.setMySqlSqlCalcFoundRows(mySqlCalcFoundRows);
        return this;
    }

    public PlainSelect withMySqlSqlNoCache(MySqlSqlCacheFlags mySqlCacheFlag) {
        this.setMySqlSqlCacheFlag(mySqlCacheFlag);
        return this;
    }

    public void setMySqlSqlCalcFoundRows(boolean mySqlCalcFoundRows) {
        this.mySqlSqlCalcFoundRows = mySqlCalcFoundRows;
    }

    public void setMySqlSqlCacheFlag(MySqlSqlCacheFlags sqlCacheFlag) {
        this.mySqlCacheFlag = sqlCacheFlag;
    }

    public boolean getMySqlSqlCalcFoundRows() {
        return this.mySqlSqlCalcFoundRows;
    }

    public MySqlSqlCacheFlags getMySqlSqlCacheFlag() {
        return this.mySqlCacheFlag;
    }

    public void setNoWait(boolean noWait) {
        this.noWait = noWait;
    }

    public boolean isNoWait() {
        return this.noWait;
    }

    public PlainSelect withDistinct(Distinct distinct) {
        this.setDistinct(distinct);
        return this;
    }

    public PlainSelect withIntoTables(List<Table> intoTables) {
        this.setIntoTables(intoTables);
        return this;
    }

    public PlainSelect withWhere(Expression where) {
        this.setWhere(where);
        return this;
    }

    public PlainSelect withOrderByElements(List<OrderByElement> orderByElements) {
        this.setOrderByElements(orderByElements);
        return this;
    }

    public PlainSelect withLimit(Limit limit) {
        this.setLimit(limit);
        return this;
    }

    public PlainSelect withOffset(Offset offset) {
        this.setOffset(offset);
        return this;
    }

    public PlainSelect withFetch(Fetch fetch) {
        this.setFetch(fetch);
        return this;
    }

    public PlainSelect withOptimizeFor(OptimizeFor optimizeFor) {
        this.setOptimizeFor(optimizeFor);
        return this;
    }

    public PlainSelect withSkip(Skip skip) {
        this.setSkip(skip);
        return this;
    }

    public PlainSelect withMySqlHintStraightJoin(boolean mySqlHintStraightJoin) {
        this.setMySqlHintStraightJoin(mySqlHintStraightJoin);
        return this;
    }

    public PlainSelect withFirst(First first) {
        this.setFirst(first);
        return this;
    }

    public PlainSelect withTop(Top top) {
        this.setTop(top);
        return this;
    }

    public PlainSelect withOracleHierarchical(OracleHierarchicalExpression oracleHierarchical) {
        this.setOracleHierarchical(oracleHierarchical);
        return this;
    }

    public PlainSelect withOracleHint(OracleHint oracleHint) {
        this.setOracleHint(oracleHint);
        return this;
    }

    public PlainSelect withOracleSiblings(boolean oracleSiblings) {
        this.setOracleSiblings(oracleSiblings);
        return this;
    }

    public PlainSelect withForUpdate(boolean forUpdate) {
        this.setForUpdate(forUpdate);
        return this;
    }

    public PlainSelect withForUpdateTable(Table forUpdateTable) {
        this.setForUpdateTable(forUpdateTable);
        return this;
    }

    public PlainSelect withUseBrackets(boolean useBrackets) {
        this.setUseBrackets(useBrackets);
        return this;
    }

    public PlainSelect withForXmlPath(String forXmlPath) {
        this.setForXmlPath(forXmlPath);
        return this;
    }

    public PlainSelect withKsqlWindow(KSQLWindow ksqlWindow) {
        this.setKsqlWindow(ksqlWindow);
        return this;
    }

    public PlainSelect withNoWait(boolean noWait) {
        this.setNoWait(noWait);
        return this;
    }

    public PlainSelect withHaving(Expression having) {
        this.setHaving(having);
        return this;
    }

    public PlainSelect withWait(Wait wait) {
        this.setWait(wait);
        return this;
    }

    public PlainSelect addSelectItems(Collection<? extends SelectItem> selectItems) {
        List<SelectItem> collection = Optional.ofNullable(getSelectItems()).orElseGet(ArrayList::new);
        collection.addAll(selectItems);
        return this.withSelectItems(collection);
    }

    public PlainSelect addIntoTables(Table... intoTables) {
        List<Table> collection = Optional.ofNullable(getIntoTables()).orElseGet(ArrayList::new);
        Collections.addAll(collection, intoTables);
        return this.withIntoTables(collection);
    }

    public PlainSelect addIntoTables(Collection<? extends Table> intoTables) {
        List<Table> collection = Optional.ofNullable(getIntoTables()).orElseGet(ArrayList::new);
        collection.addAll(intoTables);
        return this.withIntoTables(collection);
    }

    public PlainSelect addJoins(Collection<? extends Join> joins) {
        List<Join> collection = Optional.ofNullable(getJoins()).orElseGet(ArrayList::new);
        collection.addAll(joins);
        return this.withJoins(collection);
    }

    public PlainSelect addOrderByElements(OrderByElement... orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        Collections.addAll(collection, orderByElements);
        return this.withOrderByElements(collection);
    }

    public PlainSelect addOrderByElements(Collection<? extends OrderByElement> orderByElements) {
        List<OrderByElement> collection = Optional.ofNullable(getOrderByElements()).orElseGet(ArrayList::new);
        collection.addAll(orderByElements);
        return this.withOrderByElements(collection);
    }

    public <E extends FromItem> E getFromItem(Class<E> type) {
        return type.cast(getFromItem());
    }

    public <E extends Expression> E getWhere(Class<E> type) {
        return type.cast(getWhere());
    }

    public <E extends Expression> E getHaving(Class<E> type) {
        return type.cast(getHaving());
    }
}
