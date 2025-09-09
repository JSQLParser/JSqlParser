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
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.piped.AggregatePipeOperator;
import net.sf.jsqlparser.statement.piped.AsPipeOperator;
import net.sf.jsqlparser.statement.piped.CallPipeOperator;
import net.sf.jsqlparser.statement.piped.DropPipeOperator;
import net.sf.jsqlparser.statement.piped.ExtendPipeOperator;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.piped.JoinPipeOperator;
import net.sf.jsqlparser.statement.piped.LimitPipeOperator;
import net.sf.jsqlparser.statement.piped.OrderByPipeOperator;
import net.sf.jsqlparser.statement.piped.PipeOperator;
import net.sf.jsqlparser.statement.piped.PipeOperatorVisitor;
import net.sf.jsqlparser.statement.piped.PivotPipeOperator;
import net.sf.jsqlparser.statement.piped.RenamePipeOperator;
import net.sf.jsqlparser.statement.piped.SelectPipeOperator;
import net.sf.jsqlparser.statement.piped.SetPipeOperator;
import net.sf.jsqlparser.statement.piped.TableSamplePipeOperator;
import net.sf.jsqlparser.statement.piped.UnPivotPipeOperator;
import net.sf.jsqlparser.statement.piped.SetOperationPipeOperator;
import net.sf.jsqlparser.statement.piped.WherePipeOperator;
import net.sf.jsqlparser.statement.piped.WindowPipeOperator;
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
import net.sf.jsqlparser.statement.select.SampleClause;
import net.sf.jsqlparser.statement.select.Select;
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
import net.sf.jsqlparser.statement.update.UpdateSet;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
public class SelectDeParser extends AbstractDeParser<PlainSelect>
        implements SelectVisitor<StringBuilder>, SelectItemVisitor<StringBuilder>,
        FromItemVisitor<StringBuilder>, PivotVisitor<StringBuilder>,
        PipeOperatorVisitor<StringBuilder, Void> {

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
        List<WithItem<?>> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, context);
                builder.append(" ");
            }
        }

        builder.append("(");
        select.getSelect().accept((SelectVisitor<StringBuilder>) this, context);
        builder.append(")");

        if (select.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, builder).deParse(select.isOracleSiblings(),
                    select.getOrderByElements());
        }

        Alias alias = select.getAlias();
        if (alias != null) {
            builder.append(alias);
        }

        SampleClause sampleClause = select.getSampleClause();
        if (sampleClause != null) {
            builder.append(sampleClause);
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
            new LimitDeparser(expressionVisitor, builder).deParse(select.getLimit());
        }
        if (select.getOffset() != null) {
            visit(select.getOffset());
        }
        if (select.getFetch() != null) {
            visit(select.getFetch());
        }
        if (select.getIsolation() != null) {
            builder.append(select.getIsolation().toString());
        }
        return builder;
    }

    public void visit(Top top) {
        builder.append(top).append(" ");
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength",
            "PMD.NPathComplexity"})
    public <S> StringBuilder visit(PlainSelect plainSelect, S context) {
        List<WithItem<?>> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor<?>) this, context);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        builder.append("SELECT ");

        if (plainSelect.getMySqlHintStraightJoin()) {
            builder.append("STRAIGHT_JOIN ");
        }

        OracleHint hint = plainSelect.getOracleHint();
        if (hint != null) {
            builder.append(hint).append(" ");
        }

        Skip skip = plainSelect.getSkip();
        if (skip != null) {
            builder.append(skip).append(" ");
        }

        First first = plainSelect.getFirst();
        if (first != null) {
            builder.append(first).append(" ");
        }

        deparseDistinctClause(plainSelect.getDistinct());

        if (plainSelect.getBigQuerySelectQualifier() != null) {
            switch (plainSelect.getBigQuerySelectQualifier()) {
                case AS_STRUCT:
                    builder.append("AS STRUCT ");
                    break;
                case AS_VALUE:
                    builder.append("AS VALUE ");
                    break;
            }
        }

        Top top = plainSelect.getTop();
        if (top != null) {
            visit(top);
        }

        if (plainSelect.getMySqlSqlCacheFlag() != null) {
            builder.append(plainSelect.getMySqlSqlCacheFlag().name()).append(" ");
        }

        if (plainSelect.getMySqlSqlCalcFoundRows()) {
            builder.append("SQL_CALC_FOUND_ROWS").append(" ");
        }

        deparseSelectItemsClause(plainSelect.getSelectItems());

        if (plainSelect.getIntoTables() != null) {
            builder.append(" INTO ");
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext();) {
                visit(iter.next(), context);
                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }
        }

        if (plainSelect.getFromItem() != null) {
            builder.append(" FROM ");
            if (plainSelect.isUsingOnly()) {
                builder.append("ONLY ");
            }
            plainSelect.getFromItem().accept(this, context);

            if (plainSelect.getFromItem() instanceof Table) {
                Table table = (Table) plainSelect.getFromItem();
                if (table.getSampleClause() != null) {
                    table.getSampleClause().appendTo(builder);
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
            builder.append(" FINAL");
        }

        if (plainSelect.getKsqlWindow() != null) {
            builder.append(" WINDOW ");
            builder.append(plainSelect.getKsqlWindow().toString());
        }

        deparseWhereClause(plainSelect);

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor, context);
        }

        if (plainSelect.getPreferringClause() != null) {
            builder.append(" ").append(plainSelect.getPreferringClause().toString());
        }

        if (plainSelect.getGroupBy() != null) {
            builder.append(" ");
            new GroupByDeParser(expressionVisitor, builder).deParse(plainSelect.getGroupBy());
        }

        if (plainSelect.getHaving() != null) {
            builder.append(" HAVING ");
            plainSelect.getHaving().accept(expressionVisitor, context);
        }
        if (plainSelect.getQualify() != null) {
            builder.append(" QUALIFY ");
            plainSelect.getQualify().accept(expressionVisitor, context);
        }
        if (plainSelect.getWindowDefinitions() != null) {
            builder.append(" WINDOW ");
            builder.append(plainSelect.getWindowDefinitions().stream()
                    .map(WindowDefinition::toString).collect(joining(", ")));
        }
        if (plainSelect.getForClause() != null) {
            plainSelect.getForClause().appendTo(builder);
        }

        Alias alias = plainSelect.getAlias();
        if (alias != null) {
            builder.append(alias);
        }
        Pivot pivot = plainSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this, context);
        }
        UnPivot unpivot = plainSelect.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this, context);
        }

        deparseOrderByElementsClause(plainSelect, plainSelect.getOrderByElements());

        if (plainSelect.isEmitChanges()) {
            builder.append(" EMIT CHANGES");
        }
        if (plainSelect.getLimitBy() != null) {
            new LimitDeparser(expressionVisitor, builder).deParse(plainSelect.getLimitBy());
        }
        if (plainSelect.getLimit() != null) {
            new LimitDeparser(expressionVisitor, builder).deParse(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            visit(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            visit(plainSelect.getFetch());
        }
        if (plainSelect.getIsolation() != null) {
            builder.append(plainSelect.getIsolation().toString());
        }
        if (plainSelect.getForMode() != null) {
            builder.append(" FOR ");
            builder.append(plainSelect.getForMode().getValue());

            if (plainSelect.getForUpdateTable() != null) {
                builder.append(" OF ").append(plainSelect.getForUpdateTable());
            }
            if (plainSelect.getWait() != null) {
                // wait's toString will do the formatting for us
                builder.append(plainSelect.getWait());
            }
            if (plainSelect.isNoWait()) {
                builder.append(" NOWAIT");
            } else if (plainSelect.isSkipLocked()) {
                builder.append(" SKIP LOCKED");
            }
        }
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeFor(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            builder.append(" FOR XML PATH(").append(plainSelect.getForXmlPath()).append(")");
        }
        if (plainSelect.getIntoTempTable() != null) {
            builder.append(" INTO TEMP ").append(plainSelect.getIntoTempTable());
        }
        if (plainSelect.isUseWithNoLog()) {
            builder.append(" WITH NO LOG");
        }



        return builder;
    }

    protected void deparseWhereClause(PlainSelect plainSelect) {
        if (plainSelect.getWhere() != null) {
            builder.append(" WHERE ");
            plainSelect.getWhere().accept(expressionVisitor, null);
        }
    }

    protected void deparseDistinctClause(Distinct distinct) {
        if (distinct != null) {
            if (distinct.isUseUnique()) {
                builder.append("UNIQUE ");
            } else if (distinct.isUseDistinctRow()) {
                builder.append("DISTINCTROW ");
            } else {
                builder.append("DISTINCT ");
            }
            if (distinct.getOnSelectItems() != null) {
                builder.append("ON (");
                for (Iterator<SelectItem<?>> iter = distinct.getOnSelectItems().iterator(); iter
                        .hasNext();) {
                    SelectItem<?> selectItem = iter.next();
                    selectItem.accept(this, null);
                    if (iter.hasNext()) {
                        builder.append(", ");
                    }
                }
                builder.append(") ");
            }
        }
    }

    protected void deparseSelectItemsClause(List<SelectItem<?>> selectItems) {
        if (selectItems != null) {
            for (Iterator<SelectItem<?>> iter = selectItems.iterator(); iter.hasNext();) {
                SelectItem<?> selectItem = iter.next();
                selectItem.accept(this, null);
                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }
        }
    }

    protected void deparseOrderByElementsClause(PlainSelect plainSelect,
            List<OrderByElement> orderByElements) {
        if (orderByElements != null) {
            new OrderByDeParser(expressionVisitor, builder).deParse(plainSelect.isOracleSiblings(),
                    orderByElements);
        }
    }

    @Override
    public <S> StringBuilder visit(SelectItem<?> selectItem, S context) {
        selectItem.getExpression().accept(expressionVisitor, context);
        if (selectItem.getAlias() != null) {
            builder.append(selectItem.getAlias().toString());
        }
        return builder;
    }


    @Override
    public <S> StringBuilder visit(Table table, S context) {
        builder.append(table.getFullyQualifiedName());
        if (table.getTimeTravel() != null) {
            builder.append(" ").append(table.getTimeTravel());
        }
        Alias alias = table.getAlias();
        if (alias != null) {
            builder.append(alias);
        }
        if (table.getTimeTravelStrAfterAlias() != null) {
            builder.append(" ").append(table.getTimeTravelStrAfterAlias());
        }
        Pivot pivot = table.getPivot();
        if (pivot != null) {
            pivot.accept(this, context);
        }
        UnPivot unpivot = table.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this, context);
        }
        MySQLIndexHint indexHint = table.getIndexHint();
        if (indexHint != null) {
            builder.append(indexHint);
        }
        SQLServerHints sqlServerHints = table.getSqlServerHints();
        if (sqlServerHints != null) {
            builder.append(sqlServerHints);
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Pivot pivot, S context) {
        // @todo: implement this as Visitor
        builder.append(" PIVOT (").append(PlainSelect.getStringList(pivot.getFunctionItems()));

        builder.append(" FOR ");
        pivot.getForColumns().accept(expressionVisitor, context);

        // @todo: implement this as Visitor
        builder.append(" IN ").append(PlainSelect.getStringList(pivot.getInItems(), true, true));

        builder.append(")");
        if (pivot.getAlias() != null) {
            builder.append(pivot.getAlias().toString());
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(UnPivot unpivot, S context) {
        boolean showOptions = unpivot.getIncludeNullsSpecified();
        boolean includeNulls = unpivot.getIncludeNulls();
        List<Column> unPivotClause = unpivot.getUnPivotClause();
        List<Column> unpivotForClause = unpivot.getUnPivotForClause();
        builder.append(" UNPIVOT").append(showOptions && includeNulls ? " INCLUDE NULLS" : "")
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
            builder.append(unpivot.getAlias().toString());
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(PivotXml pivot, S context) {
        List<Column> forColumns = pivot.getForColumns();
        builder.append(" PIVOT XML (").append(PlainSelect.getStringList(pivot.getFunctionItems()))
                .append(" FOR ").append(PlainSelect.getStringList(forColumns, true,
                        forColumns != null && forColumns.size() > 1))
                .append(" IN (");
        if (pivot.isInAny()) {
            builder.append("ANY");
        } else if (pivot.getInSelect() != null) {
            builder.append(pivot.getInSelect());
        } else {
            builder.append(PlainSelect.getStringList(pivot.getInItems()));
        }
        builder.append("))");
        return builder;
    }

    public void visit(Offset offset) {
        // OFFSET offset
        // or OFFSET offset (ROW | ROWS)
        builder.append(" OFFSET ");
        offset.getOffset().accept(expressionVisitor, null);
        if (offset.getOffsetParam() != null) {
            builder.append(" ").append(offset.getOffsetParam());
        }

    }

    public void visit(Fetch fetch) {
        builder.append(" FETCH ");
        if (fetch.isFetchParamFirst()) {
            builder.append("FIRST ");
        } else {
            builder.append("NEXT ");
        }
        if (fetch.getExpression() != null) {
            fetch.getExpression().accept(expressionVisitor, null);
        }

        for (String p : fetch.getFetchParameters()) {
            builder.append(" ").append(p);
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
            builder.append(" GLOBAL ");
        }

        if (join.isSimple() && join.isOuter()) {
            builder.append(", OUTER ");
        } else if (join.isSimple()) {
            builder.append(", ");
        } else {

            if (join.isNatural()) {
                builder.append(" NATURAL");
            }

            if (join.isRight()) {
                builder.append(" RIGHT");
            } else if (join.isFull()) {
                builder.append(" FULL");
            } else if (join.isLeft()) {
                builder.append(" LEFT");
            } else if (join.isCross()) {
                builder.append(" CROSS");
            }

            if (join.isOuter()) {
                builder.append(" OUTER");
            } else if (join.isInner()) {
                builder.append(" INNER");
            } else if (join.isSemi()) {
                builder.append(" SEMI");
            }

            if (join.isStraight()) {
                builder.append(" STRAIGHT_JOIN ");
            } else if (join.isApply()) {
                builder.append(" APPLY ");
            } else {
                if (join.getJoinHint() != null) {
                    builder.append(" ").append(join.getJoinHint());
                }
                builder.append(" JOIN ");
            }

        }

        FromItem fromItem = join.getFromItem();
        fromItem.accept(this, null);
        if (join.isWindowJoin()) {
            builder.append(" WITHIN ");
            builder.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            builder.append(" ON ");
            onExpression.accept(expressionVisitor, null);
        }
        if (!join.getUsingColumns().isEmpty()) {
            builder.append(" USING (");
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator
                    .hasNext();) {
                Column column = iterator.next();
                builder.append(column.toString());
                if (iterator.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        }

    }

    public void deparseLateralView(LateralView lateralView) {
        builder.append(" LATERAL VIEW");

        if (lateralView.isUsingOuter()) {
            builder.append(" OUTER");
        }

        builder.append(" ");
        lateralView.getGeneratorFunction().accept(expressionVisitor, null);

        if (lateralView.getTableAlias() != null) {
            builder.append(" ").append(lateralView.getTableAlias());
        }

        builder.append(" ").append(lateralView.getColumnAlias());
    }

    @Override
    public <S> StringBuilder visit(SetOperationList list, S context) {
        List<WithItem<?>> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor<?>) this, context);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        for (int i = 0; i < list.getSelects().size(); i++) {
            if (i != 0) {
                builder.append(' ').append(list.getOperations().get(i - 1)).append(' ');
            }
            list.getSelects().get(i).accept((SelectVisitor<StringBuilder>) this, context);
        }
        if (list.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, builder).deParse(list.getOrderByElements());
        }

        if (list.getLimit() != null) {
            new LimitDeparser(expressionVisitor, builder).deParse(list.getLimit());
        }
        if (list.getOffset() != null) {
            visit(list.getOffset());
        }
        if (list.getFetch() != null) {
            visit(list.getFetch());
        }
        if (list.getIsolation() != null) {
            builder.append(list.getIsolation().toString());
        }

        Alias alias = list.getAlias();
        if (alias != null) {
            builder.append(alias);
        }
        Pivot pivot = list.getPivot();
        if (pivot != null) {
            pivot.accept(this, context);
        }
        UnPivot unpivot = list.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this, context);
        }

        return builder;
    }

    @Override
    public <S> StringBuilder visit(WithItem<?> withItem, S context) {
        if (withItem.getWithFunctionDeclaration() == null) {
            if (withItem.isRecursive()) {
                builder.append("RECURSIVE ");
            }
            builder.append(withItem.getAlias().getName());
            if (withItem.getWithItemList() != null) {
                builder.append(" ")
                        .append(PlainSelect.getStringList(withItem.getWithItemList(), true, true));
            }
            builder.append(" AS ");
            if (withItem.isMaterialized()) {
                builder.append(withItem.isUsingNot()
                        ? "NOT MATERIALIZED "
                        : "MATERIALIZED ");
            }
            StatementDeParser statementDeParser =
                    new StatementDeParser((ExpressionDeParser) expressionVisitor, this, builder);
            statementDeParser.deParse(withItem.getParenthesedStatement());
        } else {
            builder.append(withItem.getWithFunctionDeclaration().toString());
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(LateralSubSelect lateralSubSelect, S context) {
        builder.append(lateralSubSelect.getPrefix());
        visit((ParenthesedSelect) lateralSubSelect, context);

        return builder;
    }

    @Override
    public <S> StringBuilder visit(TableStatement tableStatement, S context) {
        new TableStatementDeParser(expressionVisitor, builder).deParse(tableStatement);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(TableFunction tableFunction, S context) {
        if (tableFunction.getPrefix() != null) {
            builder.append(tableFunction.getPrefix()).append(" ");
        }
        tableFunction.getFunction().accept(this.expressionVisitor, context);

        if (tableFunction.getWithClause() != null) {
            builder.append(" WITH ").append(tableFunction.getWithClause());
        }

        if (tableFunction.getAlias() != null) {
            builder.append(tableFunction.getAlias());
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(ParenthesedFromItem fromItem, S context) {

        builder.append("(");
        fromItem.getFromItem().accept(this, context);
        List<Join> joins = fromItem.getJoins();
        if (joins != null) {
            for (Join join : joins) {
                if (join.isSimple()) {
                    builder.append(", ").append(join);
                } else {
                    builder.append(" ").append(join);
                }
            }
        }
        builder.append(")");

        if (fromItem.getAlias() != null) {
            builder.append(fromItem.getAlias().toString());
        }

        if (fromItem.getPivot() != null) {
            visit(fromItem.getPivot(), context);
        }

        if (fromItem.getUnPivot() != null) {
            visit(fromItem.getUnPivot(), context);
        }
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Values values, S context) {
        new ValuesStatementDeParser(expressionVisitor, builder).deParse(values);
        return builder;
    }

    @Override
    public <S> StringBuilder visit(Import imprt, S context) {
        builder.append(imprt.toString());
        return builder;
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

    public void visit(WithItem<?> withItem) {
        visit(withItem, null);
    }

    public void visit(LateralSubSelect lateralSubSelect) {
        visit(lateralSubSelect, null);
    }

    public void visit(TableStatement tableStatement) {
        visit(tableStatement, null);
    }

    @Override
    public <S> StringBuilder visit(FromQuery fromQuery, S context) {
        List<WithItem<?>> withItemsList = fromQuery.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor<?>) this, context);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        if (fromQuery.isUsingFromKeyword()) {
            builder.append("FROM ");
        }
        fromQuery.getFromItem().accept(this, context);
        builder.append("\n");

        if (fromQuery.getLateralViews() != null) {
            for (LateralView lateralView : fromQuery.getLateralViews()) {
                deparseLateralView(lateralView);
            }
        }

        if (fromQuery.getJoins() != null) {
            for (Join join : fromQuery.getJoins()) {
                deparseJoin(join);
            }
        }

        for (PipeOperator operator : fromQuery.getPipeOperators()) {
            operator.accept(this, null);
        }
        return builder;
    }

    public void visit(TableFunction tableFunction) {
        visit(tableFunction, null);
    }

    public void visit(ParenthesedFromItem fromItem) {
        visit(fromItem, null);
    }

    public void visit(Import imprt) {
        visit(imprt, null);
    }


    private void deparseOptimizeFor(OptimizeFor optimizeFor) {
        builder.append(" OPTIMIZE FOR ");
        builder.append(optimizeFor.getRowCount());
        builder.append(" ROWS");
    }

    @Override
    void deParse(PlainSelect statement) {
        statement.accept((SelectVisitor<StringBuilder>) this, Optional.ofNullable(null));
    }

    @Override
    public StringBuilder visit(AggregatePipeOperator aggregate, Void context) {
        builder.append("|> ").append("AGGREGATE");
        int i = 0;
        for (SelectItem<?> selectItem : aggregate.getSelectItems()) {
            builder.append(i > 0 ? ", " : " ");
            selectItem.accept(this, context);
            ArrayList<String> selectItemsOrderSuffices = aggregate.getSelectItemsOrderSuffices();
            if (i < selectItemsOrderSuffices.size() && selectItemsOrderSuffices.get(i) != null
                    && !selectItemsOrderSuffices.get(i).isEmpty()) {
                builder.append(" ").append(selectItemsOrderSuffices.get(i));
            }
            i++;
        }
        builder.append("\n");

        if (!aggregate.getGroupItems().isEmpty()) {
            builder.append("\t").append("GROUP");
            if (aggregate.isUsingShortHandOrdering()) {
                builder.append(" AND ORDER");
            }
            builder.append(" BY");
            i = 0;
            for (SelectItem<?> selectItem : aggregate.getGroupItems()) {
                builder.append(i > 0 ? ", " : " ");
                selectItem.accept(this, context);

                ArrayList<String> groupItemsOrderSuffices = aggregate.getGroupItemsOrderSuffices();
                if (i < groupItemsOrderSuffices.size() && groupItemsOrderSuffices.get(i) != null
                        && !groupItemsOrderSuffices.get(i).isEmpty()) {
                    builder.append(" ").append(groupItemsOrderSuffices.get(i));
                }

                i++;
            }
            builder.append("\n");
        }

        return builder;
    }

    @Override
    public StringBuilder visit(AsPipeOperator as, Void context) {
        builder.append("|> ").append(as.getAlias());
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(CallPipeOperator call, Void context) {
        builder.append("|> CALL ");
        call.getTableFunction().accept(this);
        if (call.getAlias() != null) {
            builder.append(" ").append(call.getAlias());
        }

        return builder;
    }

    @Override
    public StringBuilder visit(DropPipeOperator drop, Void context) {
        builder.append("|> ").append("DROP ");
        drop.getColumns().accept(expressionVisitor, context);
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(ExtendPipeOperator extend, Void context) {
        return visit((SelectPipeOperator) extend, context);
    }

    @Override
    public StringBuilder visit(JoinPipeOperator join, Void context) {
        builder.append("|> ");
        deparseJoin(join.getJoin());
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(LimitPipeOperator limit, Void context) {
        builder.append("|> ").append("LIMIT ").append(limit.getLimitExpression());
        if (limit.getOffsetExpression() != null) {
            builder.append(" OFFSET ").append(limit.getOffsetExpression());
        }
        return builder;
    }

    @Override
    public StringBuilder visit(OrderByPipeOperator orderBy, Void context) {
        builder.append("|> ");
        new OrderByDeParser(expressionVisitor, builder).deParse(orderBy.getOrderByElements());
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(PivotPipeOperator pivot, Void context) {
        builder
                .append("|> ")
                .append("PIVOT( ")
                .append(pivot.getAggregateExpression())
                .append(" FOR ")
                .append(pivot.getInputColumn())
                .append(" IN (")
                .append(Select.getStringList(pivot.getPivotColumns()))
                .append("))");
        if (pivot.getAlias() != null) {
            builder.append(" ").append(pivot.getAlias());
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(RenamePipeOperator rename, Void context) {
        return builder;
    }

    @Override
    public StringBuilder visit(SelectPipeOperator select, Void context) {
        builder.append("|> ").append(select.getOperatorName());
        if (select.getModifier() != null && !select.getModifier().isEmpty()) {
            builder.append(" ").append(select.getModifier());
        }

        int i = 0;
        for (SelectItem<?> selectItem : select.getSelectItems()) {
            builder.append(i++ > 0 ? ", " : " ").append(selectItem);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(SetPipeOperator set, Void context) {
        builder.append("|> ").append("SET");
        int i = 0;
        for (UpdateSet updateSet : set.getUpdateSets()) {
            builder.append(i++ > 0 ? ", " : " ").append(updateSet);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(TableSamplePipeOperator tableSample, Void context) {
        builder.append("|> ").append("TABLESAMPLE SYSTEM (").append(tableSample.getPercent())
                .append(" PERCENT)");
        return builder;
    }

    @Override
    public StringBuilder visit(SetOperationPipeOperator setOperationPipeOperator, Void context) {
        builder.append("|> ").append(setOperationPipeOperator.getSetOperationType());
        if (setOperationPipeOperator.getModifier() != null) {
            builder.append(" ").append(setOperationPipeOperator.getModifier());
        }

        int i = 0;
        for (ParenthesedSelect select : setOperationPipeOperator.getSelects()) {
            if (i++ > 0) {
                builder.append(", ");
            }
            builder.append(select);
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(UnPivotPipeOperator unPivot, Void context) {
        builder
                .append("|> ")
                .append("UNPIVOT( ")
                .append(unPivot.getValuesColumn())
                .append(" FOR ")
                .append(unPivot.getNameColumn())
                .append(" IN (")
                .append(Select.getStringList(unPivot.getPivotColumns()))
                .append("))");
        if (unPivot.getAlias() != null) {
            builder.append(" ").append(unPivot.getAlias());
        }
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(WherePipeOperator where, Void context) {
        builder.append("|> ")
                .append("WHERE ");
        where.getExpression().accept(expressionVisitor, context);
        builder.append("\n");
        return builder;
    }

    @Override
    public StringBuilder visit(WindowPipeOperator window, Void context) {
        return visit((SelectPipeOperator) window, context);
    }
}
