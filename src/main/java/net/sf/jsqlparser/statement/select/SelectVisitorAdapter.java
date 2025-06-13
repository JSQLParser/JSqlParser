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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.piped.FromQuery;

import java.util.List;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class SelectVisitorAdapter<T> implements SelectVisitor<T> {
    private final ExpressionVisitor<T> expressionVisitor;
    private final PivotVisitor<T> pivotVisitor;
    private final SelectItemVisitor<T> selectItemVisitor;
    private final FromItemVisitor<T> fromItemVisitor;

    public SelectVisitorAdapter() {
        this.expressionVisitor = new ExpressionVisitorAdapter<>(this);
        this.pivotVisitor = new PivotVisitorAdapter<>(this.expressionVisitor);
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = new FromItemVisitorAdapter<>(this);
    }

    public SelectVisitorAdapter(ExpressionVisitor<T> expressionVisitor,
            PivotVisitor<T> pivotVisitor, SelectItemVisitor<T> selectItemVisitor,
            FromItemVisitor<T> fromItemVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = pivotVisitor;
        this.selectItemVisitor = selectItemVisitor;
        this.fromItemVisitor = fromItemVisitor;
    }

    public SelectVisitorAdapter(ExpressionVisitor<T> expressionVisitor,
            FromItemVisitor<T> fromItemVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = new PivotVisitorAdapter<>();
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = fromItemVisitor;
    }

    public SelectVisitorAdapter(ExpressionVisitor<T> expressionVisitor) {
        this.expressionVisitor = expressionVisitor;
        this.pivotVisitor = new PivotVisitorAdapter<>();
        this.selectItemVisitor = new SelectItemVisitorAdapter<>(this.expressionVisitor);
        this.fromItemVisitor = new FromItemVisitorAdapter<>();
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S context) {
        List<WithItem<?>> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept(this, context);
            }
        }

        select.getSelect().accept(this, context);

        if (select.getOrderByElements() != null) {
            for (OrderByElement orderByElement : select.getOrderByElements()) {
                orderByElement.getExpression().accept(expressionVisitor);
            }
        }

        Pivot pivot = select.getPivot();
        if (pivot != null) {
            pivot.accept(pivotVisitor, context);
        }
        UnPivot unpivot = select.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(pivotVisitor, context);
        }

        // if (select.getLimit() != null) {
        // //@todo: implement limit visitor
        // }
        if (select.getOffset() != null) {
            select.getOffset().getOffset().accept(expressionVisitor, null);
        }
        if (select.getFetch() != null && select.getFetch().getExpression() != null) {
            select.getFetch().getExpression().accept(expressionVisitor, null);
        }

        return null;
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public <S> T visit(PlainSelect plainSelect, S context) {
        List<WithItem<?>> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem<?> withItem : withItemsList) {
                withItem.accept(this, context);
            }
        }

        if (plainSelect.getDistinct() != null) {
            for (SelectItem<?> selectItem : plainSelect.getDistinct().getOnSelectItems()) {
                selectItem.accept(selectItemVisitor, context);
            }
        }

        if (plainSelect.getTop() != null) {
            plainSelect.getTop().getExpression().accept(expressionVisitor, context);
        }

        for (SelectItem<?> selectItem : plainSelect.getSelectItems()) {
            selectItem.accept(selectItemVisitor, context);
        }

        if (plainSelect.getIntoTables() != null) {
            for (Table table : plainSelect.getIntoTables()) {
                table.accept(fromItemVisitor, context);
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(fromItemVisitor, context);
        }

        // if (plainSelect.getLateralViews() != null) {
        // //@todo: implement this
        // }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getFromItem().accept(fromItemVisitor, context);
                for (Expression expression : join.getOnExpressions()) {
                    expression.accept(expressionVisitor, context);
                }
                for (Column column : join.getUsingColumns()) {
                    column.accept(expressionVisitor, context);
                }
            }
        }

        // if (plainSelect.getKsqlWindow() != null) {
        // //@todo: implement
        // }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(expressionVisitor, context);
        }

        // if (plainSelect.getOracleHierarchical() != null) {
        // //@todo: implement
        // }
        //
        // if (plainSelect.getPreferringClause() != null) {
        // //@todo: implement
        // }

        if (plainSelect.getGroupBy() != null) {
            GroupByElement groupBy = plainSelect.getGroupBy();
            for (Expression expression : groupBy.getGroupByExpressionList()) {
                expression.accept(expressionVisitor, context);
            }
            if (!groupBy.getGroupingSets().isEmpty()) {
                for (ExpressionList<?> expressionList : groupBy.getGroupingSets()) {
                    expressionList.accept(expressionVisitor, context);
                }
            }
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(expressionVisitor, context);
        }
        if (plainSelect.getQualify() != null) {
            plainSelect.getQualify().accept(expressionVisitor, context);
        }
        // if (plainSelect.getWindowDefinitions() != null) {
        // //@todo: implement
        // }

        Pivot pivot = plainSelect.getPivot();
        if (pivot != null) {
            pivot.accept(pivotVisitor, context);
        }
        UnPivot unpivot = plainSelect.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(pivotVisitor, context);
        }

        if (plainSelect.getOrderByElements() != null) {
            for (OrderByElement orderByElement : plainSelect.getOrderByElements()) {
                orderByElement.getExpression().accept(expressionVisitor);
            }
        }

        // if (plainSelect.getLimitBy() != null) {
        // //@todo: implement
        // }
        // if (plainSelect.getLimit() != null) {
        // //@todo: implement
        // }
        if (plainSelect.getOffset() != null) {
            plainSelect.getOffset().getOffset().accept(expressionVisitor, null);
        }
        if (plainSelect.getFetch() != null && plainSelect.getFetch().getExpression() != null) {
            plainSelect.getFetch().getExpression().accept(expressionVisitor, null);
        }
        // if (plainSelect.getForMode() != null) {
        // //@todo: implement
        // }
        if (plainSelect.getIntoTempTable() != null) {
            for (Table t : plainSelect.getIntoTables()) {
                t.accept(fromItemVisitor, context);
            }
        }
        return null;
    }

    @Override
    public <S> T visit(FromQuery fromQuery, S context) {
        return null;
    }

    @Override
    public <S> T visit(SetOperationList setOpList, S context) {
        for (Select select : setOpList.getSelects()) {
            select.accept(this, context);
        }
        return null;
    }

    @Override
    public <S> T visit(WithItem<?> withItem, S context) {
        return withItem.getSelect().accept(this, context);
    }

    @Override
    public <S> T visit(Values aThis, S context) {
        return null;
    }

    @Override
    public <S> T visit(LateralSubSelect lateralSubSelect, S context) {
        return lateralSubSelect.getSelect().accept(this, context);
    }

    @Override
    public <S> T visit(TableStatement tableStatement, S context) {
        return null;
    }
}
