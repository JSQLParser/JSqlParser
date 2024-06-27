/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.SQLServerHints;
import net.sf.jsqlparser.expression.WindowDefinition;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Distinct;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.LateralView;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;

import static java.util.stream.Collectors.joining;

@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
public class SelectDeParser extends AbstractDeParser<PlainSelect>
        implements SelectVisitor<StringBuilder>, SelectItemVisitor<StringBuilder>,
        FromItemVisitor<StringBuilder>, PivotVisitor<StringBuilder> {

    private ExpressionVisitor<StringBuilder> expressionVisitor;

    public SelectDeParser() {
        this(new StringBuilder());
    }

    public SelectDeParser(StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = new ExpressionDeParser(this, buffer);
    }

    public SelectDeParser(Class<? extends ExpressionDeParser> expressionDeparserClass,
            StringBuilder builder) throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        super(builder);
        this.expressionVisitor =
                expressionDeparserClass.getConstructor(SelectDeParser.class, StringBuilder.class)
                        .newInstance(this, builder);
    }

    public SelectDeParser(Class<? extends ExpressionDeParser> expressionDeparserClass)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException,
            IllegalAccessException {
        this(expressionDeparserClass, new StringBuilder());
    }


    public SelectDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedSelect select, S context) {
        List<WithItem> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            buffer.append("WITH ");
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
                buffer.append(" ");
            }
        }

        buffer.append("(");
        select.getSelect().accept(this, context);
        buffer.append(")");

        if (select.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(select.isOracleSiblings(),
                    select.getOrderByElements());
        }

        Alias alias = select.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }
        Pivot pivot = select.getPivot();
        if (pivot != null) {
            pivot.accept(this, context);
        }
        UnPivot unpivot = select.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this, context);
        }

        if (select.getLimit() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(select.getLimit());
        }
        if (select.getOffset() != null) {
            visit(select.getOffset());
        }
        if (select.getFetch() != null) {
            visit(select.getFetch());
        }
        if (select.getIsolation() != null) {
            buffer.append(select.getIsolation().toString());
        }
        return buffer;
    }

    public void visit(Top top) {
        buffer.append(top).append(" ");
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength",
            "PMD.NPathComplexity"})
    public <S> StringBuilder visit(PlainSelect plainSelect, S context) {
        List<WithItem> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor<?>) this, context);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }

        buffer.append("SELECT ");

        if (plainSelect.getMySqlHintStraightJoin()) {
            buffer.append("STRAIGHT_JOIN ");
        }

        OracleHint hint = plainSelect.getOracleHint();
        if (hint != null) {
            buffer.append(hint).append(" ");
        }

        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            buffer.append(skip).append(" ");
        }

        First first = plainSelect.getFirst();
        if (first != null) {
            buffer.append(first).append(" ");
        }

        deparseDistinctClause(plainSelect.getDistinct());

        if (plainSelect.getBigQuerySelectQualifier() != null) {
            switch (plainSelect.getBigQuerySelectQualifier()) {
                case AS_STRUCT:
                    buffer.append("AS STRUCT ");
                    break;
                case AS_VALUE:
                    buffer.append("AS VALUE ");
                    break;
            }
        }

        Top top = plainSelect.getTop();
        if (top != null) {
            visit(top);
        }

        if (plainSelect.getMySqlSqlCacheFlag() != null) {
            buffer.append(plainSelect.getMySqlSqlCacheFlag().name()).append(" ");
        }

        if (plainSelect.getMySqlSqlCalcFoundRows()) {
            buffer.append("SQL_CALC_FOUND_ROWS").append(" ");
        }

        deparseSelectItemsClause(plainSelect.getSelectItems());

        if (plainSelect.getIntoTables() != null) {
            buffer.append(" INTO ");
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext();) {
                visit(iter.next(), context);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getFromItem() != null) {
            buffer.append(" FROM ");
            if (plainSelect.isUsingOnly()) {
                buffer.append("ONLY ");
            }
            plainSelect.getFromItem().accept(this, context);

            if (plainSelect.getFromItem() instanceof Table) {
                Table table = (Table) plainSelect.getFromItem();
                if (table.getSampleClause() != null) {
                    table.getSampleClause().appendTo(buffer);
                }
            }
        }

        if (plainSelect.getLateralViews() != null) {
            for (LateralView lateralView : plainSelect.getLateralViews()) {
                deparseLateralView(lateralView);
            }
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                deparseJoin(join);
            }
        }

        if (plainSelect.isUsingFinal()) {
            buffer.append(" FINAL");
        }

        if (plainSelect.getKsqlWindow() != null) {
            buffer.append(" WINDOW ");
            buffer.append(plainSelect.getKsqlWindow().toString());
        }

        deparseWhereClause(plainSelect);

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor, context);
        }

        if (plainSelect.getGroupBy() != null) {
            buffer.append(" ");
            new GroupByDeParser(expressionVisitor, buffer).deParse(plainSelect.getGroupBy());
        }

        if (plainSelect.getHaving() != null) {
            buffer.append(" HAVING ");
            plainSelect.getHaving().accept(expressionVisitor, context);
        }
        if (plainSelect.getQualify() != null) {
            buffer.append(" QUALIFY ");
            plainSelect.getQualify().accept(expressionVisitor, context);
        }
        if (plainSelect.getWindowDefinitions() != null) {
            buffer.append(" WINDOW ");
            buffer.append(plainSelect.getWindowDefinitions().stream()
                    .map(WindowDefinition::toString).collect(joining(", ")));
        }
        if (plainSelect.getForClause() != null) {
            plainSelect.getForClause().appendTo(buffer);
        }

        deparseOrderByElementsClause(plainSelect, plainSelect.getOrderByElements());
        if (plainSelect.isEmitChanges()) {
            buffer.append(" EMIT CHANGES");
        }
        if (plainSelect.getLimitBy() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(plainSelect.getLimitBy());
        }
        if (plainSelect.getLimit() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            visit(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            visit(plainSelect.getFetch());
        }
        if (plainSelect.getIsolation() != null) {
            buffer.append(plainSelect.getIsolation().toString());
        }
        if (plainSelect.getForMode() != null) {
            buffer.append(" FOR ");
            buffer.append(plainSelect.getForMode().getValue());

            if (plainSelect.getForUpdateTable() != null) {
                buffer.append(" OF ").append(plainSelect.getForUpdateTable());
            }
            if (plainSelect.getWait() != null) {
                // wait's toString will do the formatting for us
                buffer.append(plainSelect.getWait());
            }
            if (plainSelect.isNoWait()) {
                buffer.append(" NOWAIT");
            } else if (plainSelect.isSkipLocked()) {
                buffer.append(" SKIP LOCKED");
            }
        }
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeFor(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            buffer.append(" FOR XML PATH(").append(plainSelect.getForXmlPath()).append(")");
        }
        if (plainSelect.getIntoTempTable() != null) {
            buffer.append(" INTO TEMP ").append(plainSelect.getIntoTempTable());
        }
        if (plainSelect.isUseWithNoLog()) {
            buffer.append(" WITH NO LOG");
        }
        return buffer;
    }

    protected void deparseWhereClause(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null) {
            buffer.append(" WHERE ");
            plainSelect.getWhere().accept(expressionVisitor, null);
        }
    }

    protected void deparseDistinctClause(Distinct distinct) {
        if (distinct != null) {
            if (distinct.isUseUnique()) {
                buffer.append("UNIQUE ");
            } else {
                buffer.append("DISTINCT ");
            }
            if (distinct.getOnSelectItems() != null) {
                buffer.append("ON (");
                for (Iterator<SelectItem<?>> iter = distinct.getOnSelectItems().iterator(); iter
                        .hasNext();) {
                    SelectItem<?> selectItem = iter.next();
                    selectItem.accept(this, null);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
                buffer.append(") ");
            }
        }
    }

    protected void deparseSelectItemsClause(List<SelectItem<?>> selectItems) {
        if (selectItems != null) {
            for (Iterator<SelectItem<?>> iter = selectItems.iterator(); iter.hasNext();) {
                SelectItem<?> selectItem = iter.next();
                selectItem.accept(this, null);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }
    }

    protected void deparseOrderByElementsClause(PlainSelect plainSelect,
            List<OrderByElement> orderByElements) {
        if (orderByElements != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(plainSelect.isOracleSiblings(),
                    orderByElements);
        }
    }

    @Override
    public <S> StringBuilder visit(SelectItem<?> selectExpressionItem, S context) {
        selectExpressionItem.getExpression().accept(expressionVisitor, context);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }
        return buffer;
    }


    @Override
    public <S> StringBuilder visit(Table tableName, S context) {
        buffer.append(tableName.getFullyQualifiedName());
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }
        Pivot pivot = tableName.getPivot();
        if (pivot != null) {
            pivot.accept(this, context);
        }
        UnPivot unpivot = tableName.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this, context);
        }
        MySQLIndexHint indexHint = tableName.getIndexHint();
        if (indexHint != null) {
            buffer.append(indexHint);
        }
        SQLServerHints sqlServerHints = tableName.getSqlServerHints();
        if (sqlServerHints != null) {
            buffer.append(sqlServerHints);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Pivot pivot, S context) {
        // @todo: implement this as Visitor
        buffer.append(" PIVOT (").append(PlainSelect.getStringList(pivot.getFunctionItems()));

        buffer.append(" FOR ");
        pivot.getForColumns().accept(expressionVisitor, context);

        // @todo: implement this as Visitor
        buffer.append(" IN ").append(PlainSelect.getStringList(pivot.getInItems(), true, true));

        buffer.append(")");
        if (pivot.getAlias() != null) {
            buffer.append(pivot.getAlias().toString());
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(UnPivot unpivot, S context) {
        boolean showOptions = unpivot.getIncludeNullsSpecified();
        boolean includeNulls = unpivot.getIncludeNulls();
        List<Column> unPivotClause = unpivot.getUnPivotClause();
        List<Column> unpivotForClause = unpivot.getUnPivotForClause();
        buffer.append(" UNPIVOT").append(showOptions && includeNulls ? " INCLUDE NULLS" : "")
                .append(showOptions && !includeNulls ? " EXCLUDE NULLS" : "").append(" (")
                .append(PlainSelect.getStringList(unPivotClause, true,
                        unPivotClause != null && unPivotClause.size() > 1))
                .append(" FOR ")
                .append(PlainSelect.getStringList(unpivotForClause, true,
                        unpivotForClause != null && unpivotForClause.size() > 1))
                .append(" IN ")
                .append(PlainSelect.getStringList(unpivot.getUnPivotInClause(), true, true))
                .append(")");
        if (unpivot.getAlias() != null) {
            buffer.append(unpivot.getAlias().toString());
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(PivotXml pivot, S context) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT XML (").append(PlainSelect.getStringList(pivot.getFunctionItems()))
                .append(" FOR ").append(PlainSelect.getStringList(forColumns, true,
                        forColumns != null && forColumns.size() > 1))
                .append(" IN (");
        if (pivot.isInAny()) {
            buffer.append("ANY");
        } else if (pivot.getInSelect() != null) {
            buffer.append(pivot.getInSelect());
        } else {
            buffer.append(PlainSelect.getStringList(pivot.getInItems()));
        }
        buffer.append("))");
        return buffer;
    }

    public void visit(Offset offset) {
        // OFFSET offset
        // or OFFSET offset (ROW | ROWS)
        buffer.append(" OFFSET ");
        offset.getOffset().accept(expressionVisitor, null);
        if (offset.getOffsetParam() != null) {
            buffer.append(" ").append(offset.getOffsetParam());
        }

    }

    public void visit(Fetch fetch) {
        buffer.append(" FETCH ");
        if (fetch.isFetchParamFirst()) {
            buffer.append("FIRST ");
        } else {
            buffer.append("NEXT ");
        }
        if (fetch.getExpression() != null) {
            fetch.getExpression().accept(expressionVisitor, null);
        }

        for (String p : fetch.getFetchParameters()) {
            buffer.append(" ").append(p);
        }
    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deparseJoin(Join join) {
        if (join.isGlobal()) {
            buffer.append(" GLOBAL ");
        }

        if (join.isSimple() && join.isOuter()) {
            buffer.append(", OUTER ");
        } else if (join.isSimple()) {
            buffer.append(", ");
        } else {

            if (join.isNatural()) {
                buffer.append(" NATURAL");
            }

            if (join.isRight()) {
                buffer.append(" RIGHT");
            } else if (join.isFull()) {
                buffer.append(" FULL");
            } else if (join.isLeft()) {
                buffer.append(" LEFT");
            } else if (join.isCross()) {
                buffer.append(" CROSS");
            }

            if (join.isOuter()) {
                buffer.append(" OUTER");
            } else if (join.isInner()) {
                buffer.append(" INNER");
            } else if (join.isSemi()) {
                buffer.append(" SEMI");
            }

            if (join.isStraight()) {
                buffer.append(" STRAIGHT_JOIN ");
            } else if (join.isApply()) {
                buffer.append(" APPLY ");
            } else {
                if (join.getJoinHint() != null) {
                    buffer.append(" ").append(join.getJoinHint());
                }
                buffer.append(" JOIN ");
            }

        }

        FromItem fromItem = join.getFromItem();
        fromItem.accept(this, null);
        if (join.isWindowJoin()) {
            buffer.append(" WITHIN ");
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(" ON ");
            onExpression.accept(expressionVisitor, null);
        }
        if (!join.getUsingColumns().isEmpty()) {
            buffer.append(" USING (");
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator
                    .hasNext();) {
                Column column = iterator.next();
                buffer.append(column.toString());
                if (iterator.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }

    }

    public void deparseLateralView(LateralView lateralView) {
        buffer.append(" LATERAL VIEW");

        if (lateralView.isUsingOuter()) {
            buffer.append(" OUTER");
        }

        buffer.append(" ");
        lateralView.getGeneratorFunction().accept(expressionVisitor, null);

        if (lateralView.getTableAlias() != null) {
            buffer.append(" ").append(lateralView.getTableAlias());
        }

        buffer.append(" ").append(lateralView.getColumnAlias());
    }

    @Override
    public <S> StringBuilder visit(SetOperationList list, S context) {
        List<WithItem> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor<?>) this, context);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }

        for (int i = 0; i < list.getSelects().size(); i++) {
            if (i != 0) {
                buffer.append(' ').append(list.getOperations().get(i - 1)).append(' ');
            }
            list.getSelects().get(i).accept(this, context);
        }
        if (list.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(list.getOrderByElements());
        }

        if (list.getLimit() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(list.getLimit());
        }
        if (list.getOffset() != null) {
            visit(list.getOffset());
        }
        if (list.getFetch() != null) {
            visit(list.getFetch());
        }
        if (list.getIsolation() != null) {
            buffer.append(list.getIsolation().toString());
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(WithItem withItem, S context) {
        if (withItem.isRecursive()) {
            buffer.append("RECURSIVE ");
        }
        buffer.append(withItem.getAlias().getName());
        if (withItem.getWithItemList() != null) {
            buffer.append(" ")
                    .append(PlainSelect.getStringList(withItem.getWithItemList(), true, true));
        }
        buffer.append(" AS ");
        withItem.getSelect().accept(this, context);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(LateralSubSelect lateralSubSelect, S context) {
        buffer.append(lateralSubSelect.getPrefix());
        visit((ParenthesedSelect) lateralSubSelect, context);

        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TableStatement tableStatement, S context) {
        new TableStatementDeParser(expressionVisitor, buffer).deParse(tableStatement);
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(TableFunction tableFunction, S context) {
        buffer.append(tableFunction.toString());
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedFromItem fromItem, S context) {

        buffer.append("(");
        fromItem.getFromItem().accept(this, context);
        List<Join> joins = fromItem.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
                }
            }
        }
        buffer.append(")");

        if (fromItem.getAlias() != null) {
            buffer.append(fromItem.getAlias().toString());
        }

        if (fromItem.getPivot() != null) {
            visit(fromItem.getPivot(), context);
        }

        if (fromItem.getUnPivot() != null) {
            visit(fromItem.getUnPivot(), context);
        }
        return buffer;
    }

    @Override
    public <S> StringBuilder visit(Values values, S context) {
        new ValuesStatementDeParser(expressionVisitor, buffer).deParse(values);
        return buffer;
    }

    @Override
    public void visit(Values values) {
        SelectVisitor.super.visit(values);
    }

    public void visit(ParenthesedSelect select) {
        visit(select, null);
    }

    public void visit(PlainSelect plainSelect) {
        visit(plainSelect, null);
    }

    public void visit(SelectItem<?> selectExpressionItem) {
        visit(selectExpressionItem, null);
    }

    public void visit(Table tableName) {
        visit(tableName, null);
    }

    public void visit(Pivot pivot) {
        visit(pivot, null);
    }

    public void visit(UnPivot unpivot) {
        visit(unpivot, null);
    }

    public void visit(PivotXml pivot) {
        visit(pivot, null);
    }

    public void visit(SetOperationList list) {
        visit(list, null);
    }

    public void visit(WithItem withItem) {
        visit(withItem, null);
    }

    public void visit(LateralSubSelect lateralSubSelect) {
        visit(lateralSubSelect, null);
    }

    public void visit(TableStatement tableStatement) {
        visit(tableStatement, null);
    }

    public void visit(TableFunction tableFunction) {
        visit(tableFunction, null);
    }

    public void visit(ParenthesedFromItem fromItem) {
        visit(fromItem, null);
    }


    private void deparseOptimizeFor(OptimizeFor optimizeFor) {
        buffer.append(" OPTIMIZE FOR ");
        buffer.append(optimizeFor.getRowCount());
        buffer.append(" ROWS");
    }

    @Override
    void deParse(PlainSelect statement) {
        statement.accept(this, null);
    }

}
