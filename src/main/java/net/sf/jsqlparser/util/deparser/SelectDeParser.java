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

import static java.util.stream.Collectors.joining;

import java.util.Iterator;
import java.util.List;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.SQLServerHints;
import net.sf.jsqlparser.expression.WindowDefinition;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.LateralView;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
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

@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
public class SelectDeParser extends AbstractDeParser<PlainSelect> implements SelectVisitor,
        SelectItemVisitor, FromItemVisitor, PivotVisitor {

    private ExpressionVisitor expressionVisitor;

    public SelectDeParser() {
        this(new StringBuilder());
    }

    public SelectDeParser(StringBuilder buffer) {
        this(new ExpressionVisitorAdapter(), buffer);
    }

    public SelectDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void visit(ParenthesedSelect selectBody) {
        List<WithItem> withItemsList = selectBody.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            buffer.append("WITH ");
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
                buffer.append(" ");
            }
        }

        buffer.append("(");
        selectBody.getSelect().accept((SelectVisitor) this);
        buffer.append(")");

        if (selectBody.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(selectBody.isOracleSiblings(),
                    selectBody.getOrderByElements());
        }

        Alias alias = selectBody.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }
        Pivot pivot = selectBody.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        UnPivot unpivot = selectBody.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this);
        }

        if (selectBody.getLimit() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(selectBody.getLimit());
        }
        if (selectBody.getOffset() != null) {
            visit(selectBody.getOffset());
        }
        if (selectBody.getFetch() != null) {
            visit(selectBody.getFetch());
        }
        if (selectBody.getIsolation() != null) {
            buffer.append(selectBody.getIsolation().toString());
        }
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength",
            "PMD.NPathComplexity"})
    public void visit(PlainSelect plainSelect) {
        List<WithItem> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor) this);
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

        if (plainSelect.getDistinct() != null) {
            if (plainSelect.getDistinct().isUseUnique()) {
                buffer.append("UNIQUE ");
            } else {
                buffer.append("DISTINCT ");
            }
            if (plainSelect.getDistinct().getOnSelectItems() != null) {
                buffer.append("ON (");
                for (Iterator<SelectItem<?>> iter =
                        plainSelect.getDistinct().getOnSelectItems().iterator(); iter.hasNext();) {
                    SelectItem<?> selectItem = iter.next();
                    selectItem.accept(this);
                    if (iter.hasNext()) {
                        buffer.append(", ");
                    }
                }
                buffer.append(") ");
            }

        }

        Top top = plainSelect.getTop();
        if (top != null) {
            buffer.append(top).append(" ");
        }

        if (plainSelect.getMySqlSqlCacheFlag() != null) {
            buffer.append(plainSelect.getMySqlSqlCacheFlag().name()).append(" ");
        }

        if (plainSelect.getMySqlSqlCalcFoundRows()) {
            buffer.append("SQL_CALC_FOUND_ROWS").append(" ");
        }

        final List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
        if (selectItems != null) {
            for (Iterator<SelectItem<?>> iter = selectItems.iterator(); iter.hasNext();) {
                SelectItem<?> selectItem = iter.next();
                selectItem.accept(this);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getIntoTables() != null) {
            buffer.append(" INTO ");
            for (Iterator<Table> iter = plainSelect.getIntoTables().iterator(); iter.hasNext();) {
                visit(iter.next());
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
        }

        if (plainSelect.getFromItem() != null) {
            buffer.append(" FROM ");
            plainSelect.getFromItem().accept(this);

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

        if (plainSelect.getWhere() != null) {
            buffer.append(" WHERE ");
            plainSelect.getWhere().accept(expressionVisitor);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(expressionVisitor);
        }

        if (plainSelect.getGroupBy() != null) {
            buffer.append(" ");
            new GroupByDeParser(expressionVisitor, buffer).deParse(plainSelect.getGroupBy());
        }

        if (plainSelect.getHaving() != null) {
            buffer.append(" HAVING ");
            plainSelect.getHaving().accept(expressionVisitor);
        }
        if (plainSelect.getQualify() != null) {
            buffer.append(" QUALIFY ");
            plainSelect.getQualify().accept(expressionVisitor);
        }
        if (plainSelect.getWindowDefinitions() != null) {
            buffer.append(" WINDOW ");
            buffer.append(plainSelect.getWindowDefinitions().stream()
                    .map(WindowDefinition::toString).collect(joining(", ")));
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
        if (plainSelect.getForClause() != null) {
            plainSelect.getForClause().appendTo(buffer);
        }

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(plainSelect.isOracleSiblings(),
                    plainSelect.getOrderByElements());
        }
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
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeFor(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            buffer.append(" FOR XML PATH(").append(plainSelect.getForXmlPath()).append(")");
        }

    }

    @Override
    public void visit(SelectItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(expressionVisitor);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }
    }


    @Override
    public void visit(Table tableName) {
        buffer.append(tableName.getFullyQualifiedName());
        Alias alias = tableName.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }
        Pivot pivot = tableName.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        UnPivot unpivot = tableName.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this);
        }
        MySQLIndexHint indexHint = tableName.getIndexHint();
        if (indexHint != null) {
            buffer.append(indexHint);
        }
        SQLServerHints sqlServerHints = tableName.getSqlServerHints();
        if (sqlServerHints != null) {
            buffer.append(sqlServerHints);
        }
    }

    @Override
    public void visit(Pivot pivot) {
        // @todo: implement this as Visitor
        buffer.append(" PIVOT (").append(PlainSelect.getStringList(pivot.getFunctionItems()));

        buffer.append(" FOR ");
        pivot.getForColumns().accept(expressionVisitor);

        // @todo: implement this as Visitor
        buffer.append(" IN ").append(PlainSelect.getStringList(pivot.getInItems(), true, true));

        buffer.append(")");
        if (pivot.getAlias() != null) {
            buffer.append(pivot.getAlias().toString());
        }
    }

    @Override
    public void visit(UnPivot unpivot) {
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
    }

    @Override
    public void visit(PivotXml pivot) {
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
    }

    public void visit(Offset offset) {
        // OFFSET offset
        // or OFFSET offset (ROW | ROWS)
        buffer.append(" OFFSET ");
        offset.getOffset().accept(expressionVisitor);
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
            fetch.getExpression().accept(expressionVisitor);
        }

        for (String p : fetch.getFetchParameters()) {
            buffer.append(" ").append(p);
        }
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
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
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            buffer.append(" WITHIN ");
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(" ON ");
            onExpression.accept(expressionVisitor);
        }
        if (join.getUsingColumns().size() > 0) {
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
        lateralView.getGeneratorFunction().accept(expressionVisitor);

        if (lateralView.getTableAlias() != null) {
            buffer.append(" ").append(lateralView.getTableAlias());
        }

        buffer.append(" ").append(lateralView.getColumnAlias());
    }

    @Override
    public void visit(SetOperationList list) {
        List<WithItem> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                iter.next().accept((SelectVisitor) this);
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
            list.getSelects().get(i).accept(this);
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
    }

    @Override
    public void visit(WithItem withItem) {
        if (withItem.isRecursive()) {
            buffer.append("RECURSIVE ");
        }
        buffer.append(withItem.getAlias().getName());
        if (withItem.getWithItemList() != null) {
            buffer.append(" ")
                    .append(PlainSelect.getStringList(withItem.getWithItemList(), true, true));
        }
        buffer.append(" AS ");
        withItem.getSelect().accept(this);
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        buffer.append(lateralSubSelect.getPrefix());
        visit((ParenthesedSelect) lateralSubSelect);
    }

    @Override
    public void visit(TableStatement tableStatement) {
        new TableStatementDeParser(expressionVisitor, buffer).deParse(tableStatement);
    }

    @Override
    public void visit(TableFunction tableFunction) {
        buffer.append(tableFunction.toString());
    }

    @Override
    public void visit(ParenthesedFromItem fromItem) {

        buffer.append("(");
        fromItem.getFromItem().accept(this);
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
            visit(fromItem.getPivot());
        }

        if (fromItem.getUnPivot() != null) {
            visit(fromItem.getUnPivot());
        }
    }

    @Override
    public void visit(Values values) {
        new ValuesStatementDeParser(expressionVisitor, buffer).deParse(values);
    }

    private void deparseOptimizeFor(OptimizeFor optimizeFor) {
        buffer.append(" OPTIMIZE FOR ");
        buffer.append(optimizeFor.getRowCount());
        buffer.append(" ROWS");
    }

    @Override
    void deParse(PlainSelect statement) {
        statement.accept((SelectVisitor) this);
    }

}
