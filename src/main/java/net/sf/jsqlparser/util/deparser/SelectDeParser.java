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

import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class SelectDeParser extends AbstractDeParser<PlainSelect>
        implements SelectVisitor, SelectItemVisitor, FromItemVisitor, PivotVisitor, ItemsListVisitor {

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
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity"})
    public void visit(PlainSelect plainSelect) {
        if (plainSelect.isUseBrackets()) {
            buffer.append("(");
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
                for (Iterator<SelectItem> iter = plainSelect.getDistinct().getOnSelectItems().iterator(); iter
                        .hasNext();) {
                    SelectItem selectItem = iter.next();
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

        for (Iterator<SelectItem> iter = plainSelect.getSelectItems().iterator(); iter.hasNext();) {
            SelectItem selectItem = iter.next();
            selectItem.accept(this);
            if (iter.hasNext()) {
                buffer.append(", ");
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
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                deparseJoin(join);
            }
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

        if (plainSelect.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(plainSelect.isOracleSiblings(),
                    plainSelect.getOrderByElements());
        }
        if (plainSelect.isEmitChanges()){
            buffer.append(" EMIT CHANGES");
        }
        if (plainSelect.getLimit() != null) {
            new LimitDeparser(buffer).deParse(plainSelect.getLimit());
        }
        if (plainSelect.getOffset() != null) {
            deparseOffset(plainSelect.getOffset());
        }
        if (plainSelect.getFetch() != null) {
            deparseFetch(plainSelect.getFetch());
        }
        if (plainSelect.getWithIsolation() != null) {
            buffer.append(plainSelect.getWithIsolation().toString());
        }
        if (plainSelect.isForUpdate()) {
            buffer.append(" FOR UPDATE");
            if (plainSelect.getForUpdateTable() != null) {
                buffer.append(" OF ").append(plainSelect.getForUpdateTable());
            }
            if (plainSelect.getWait() != null) {
                // wait's toString will do the formatting for us
                buffer.append(plainSelect.getWait());
            }
            if (plainSelect.isNoWait()) {
                buffer.append(" NOWAIT");
            }
        }
        if (plainSelect.getOptimizeFor() != null) {
            deparseOptimizeFor(plainSelect.getOptimizeFor());
        }
        if (plainSelect.getForXmlPath() != null) {
            buffer.append(" FOR XML PATH(").append(plainSelect.getForXmlPath()).append(")");
        }
        if (plainSelect.isUseBrackets()) {
            buffer.append(")");
        }
        
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        buffer.append(allTableColumns.getTable().getFullyQualifiedName()).append(".*");
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(expressionVisitor);
        if (selectExpressionItem.getAlias() != null) {
            buffer.append(selectExpressionItem.getAlias().toString());
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        buffer.append(subSelect.isUseBrackets() ? "(" : "");
        if (subSelect.getWithItemsList() != null && !subSelect.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = subSelect.getWithItemsList().iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                withItem.accept(this);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        subSelect.getSelectBody().accept(this);
        buffer.append(subSelect.isUseBrackets() ? ")" : "");
        Alias alias = subSelect.getAlias();
        if (alias != null) {
            buffer.append(alias);
        }
        Pivot pivot = subSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }

        UnPivot unPivot = subSelect.getUnPivot();
        if (unPivot != null) {
            unPivot.accept(this);
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
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT (").append(PlainSelect.getStringList(pivot.getFunctionItems())).append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
                .append(" IN ").append(PlainSelect.getStringList(pivot.getInItems(), true, true)).append(")");
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
        buffer
                .append(" UNPIVOT")
                .append(showOptions && includeNulls ? " INCLUDE NULLS" : "")
                .append(showOptions && !includeNulls ? " EXCLUDE NULLS" : "")
                .append(" (").append(PlainSelect.getStringList(unPivotClause, true,
                        unPivotClause != null && unPivotClause.size() > 1))
                .append(" FOR ").append(PlainSelect.getStringList(unpivotForClause, true,
                        unpivotForClause != null && unpivotForClause.size() > 1))
                .append(" IN ").append(PlainSelect.getStringList(unpivot.getUnPivotInClause(), true, true)).append(")");
        if (unpivot.getAlias() != null) {
            buffer.append(unpivot.getAlias().toString());
        }
    }

    @Override
    public void visit(PivotXml pivot) {
        List<Column> forColumns = pivot.getForColumns();
        buffer.append(" PIVOT XML (").append(PlainSelect.getStringList(pivot.getFunctionItems())).append(" FOR ")
                .append(PlainSelect.getStringList(forColumns, true, forColumns != null && forColumns.size() > 1))
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

    public void deparseOffset(Offset offset) {
        // OFFSET offset
        // or OFFSET offset (ROW | ROWS)
        buffer.append(" OFFSET ");
        buffer.append(offset.getOffset());
        if (offset.getOffsetParam() != null) {
            buffer.append(" ").append(offset.getOffsetParam());
        }

    }

    public void deparseFetch(Fetch fetch) {
        // FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
        buffer.append(" FETCH ");
        if (fetch.isFetchParamFirst()) {
            buffer.append("FIRST ");
        } else {
            buffer.append("NEXT ");
        }
        if (fetch.getFetchJdbcParameter() != null) {
            buffer.append(fetch.getFetchJdbcParameter().toString());
        } else {
            buffer.append(fetch.getRowCount());
        }
        buffer.append(" ").append(fetch.getFetchParam()).append(" ONLY");

    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }

    @Override
    public void visit(SubJoin subjoin) {
        buffer.append("(");
        subjoin.getLeft().accept(this);
        for (Join join : subjoin.getJoinList()) {
            deparseJoin(join);
        }
        buffer.append(")");

        if (subjoin.getPivot() != null) {
            subjoin.getPivot().accept(this);
        }
    }

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public void deparseJoin(Join join) {
        if (join.isSimple() && join.isOuter()) {
            buffer.append(", OUTER ");
        } else if (join.isSimple()) {
            buffer.append(", ");
        } else {

            if (join.isRight()) {
                buffer.append(" RIGHT");
            } else if (join.isNatural()) {
                buffer.append(" NATURAL");
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
                buffer.append(" JOIN ");
            }

        }

        FromItem fromItem = join.getRightItem();
        fromItem.accept(this);
        if (join.isWindowJoin()) {
            buffer.append(" WITHIN ");
            buffer.append(join.getJoinWindow().toString());
        }
        for (Expression onExpression : join.getOnExpressions()) {
            buffer.append(" ON ");
            onExpression.accept(expressionVisitor);
        }
        if (join.getUsingColumns().size()>0) {
            buffer.append(" USING (");
            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext();) {
                Column column = iterator.next();
                buffer.append(column.toString());
                if (iterator.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }

    }

    @Override
    public void visit(SetOperationList list) {
        for (int i = 0; i < list.getSelects().size(); i++) {
            if (i != 0) {
                buffer.append(' ').append(list.getOperations().get(i - 1)).append(' ');
            }
            boolean brackets = list.getBrackets() == null || list.getBrackets().get(i);
            if (brackets) {
                buffer.append("(");
            }
            list.getSelects().get(i).accept(this);
            if (brackets) {
                buffer.append(")");
            }
        }
        if (list.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(list.getOrderByElements());
        }

        if (list.getLimit() != null) {
            new LimitDeparser(buffer).deParse(list.getLimit());
        }
        if (list.getOffset() != null) {
            deparseOffset(list.getOffset());
        }
        if (list.getFetch() != null) {
            deparseFetch(list.getFetch());
        }
        if (list.getWithIsolation() != null) {
            buffer.append(list.getWithIsolation().toString());
        }
    }

    @Override
    public void visit(WithItem withItem) {
        if (withItem.isRecursive()) {
            buffer.append("RECURSIVE ");
        }
        buffer.append(withItem.getName());
        if (withItem.getWithItemList() != null) {
            buffer.append(" ").append(PlainSelect.getStringList(withItem.getWithItemList(), true, true));
        }
        buffer.append(" AS ");

        if (withItem.isUseValues()) {
            ItemsList itemsList = withItem.getItemsList();
            boolean useBracketsForValues = withItem.isUsingBracketsForValues();
            buffer.append("(VALUES ");

            ExpressionList expressionList = (ExpressionList) itemsList;
            buffer.append(
                    PlainSelect.getStringList(expressionList.getExpressions(), true, useBracketsForValues));
            buffer.append(")");
        } else {
            SubSelect subSelect = withItem.getSubSelect();
            if (!subSelect.isUseBrackets()) {
                buffer.append("(");
            }
            subSelect.accept((FromItemVisitor) this);
            if (!subSelect.isUseBrackets()) {
                buffer.append(")");
            }
        }
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        buffer.append(lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        buffer.append(valuesList.toString());
    }

    @Override
    public void visit(AllColumns allColumns) {
        buffer.append('*');
    }

    @Override
    public void visit(TableFunction tableFunction) {
        buffer.append(tableFunction.toString());
    }

    @Override
    public void visit(ParenthesisFromItem parenthesis) {
        buffer.append("(");
        parenthesis.getFromItem().accept(this);
        
        buffer.append(")");
        if (parenthesis.getAlias() != null) {
            buffer.append(parenthesis.getAlias().toString());
        }
    }

    @Override
    public void visit(ValuesStatement values) {
        new ValuesStatementDeParser(this, buffer).deParse(values);
    }

    private void deparseOptimizeFor(OptimizeFor optimizeFor) {
        buffer.append(" OPTIMIZE FOR ");
        buffer.append(optimizeFor.getRowCount());
        buffer.append(" ROWS");
    }

    @Override
    void deParse(PlainSelect statement) {
        statement.accept(this);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(expressionList.toString());
    }

    @Override
    public void visit(NamedExpressionList namedExpressionList) {
        buffer.append(namedExpressionList.toString());
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(multiExprList.toString());
    }
}
