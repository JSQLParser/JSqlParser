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

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.OutputClause;
import net.sf.jsqlparser.statement.piped.FromQuery;

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
        this.fromItemVisitor = new FromItemVisitorAdapter<>(this, this.expressionVisitor);
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
    public <S> T visitOutputClause(OutputClause outputClause, S context) {
        if (outputClause != null) {
            if (outputClause.getSelectItemList() != null) {
                for (SelectItem<?> selectItem : outputClause.getSelectItemList()) {
                    selectItem.accept(selectItemVisitor, context);
                }
            }
            if (outputClause.getTableVariable() != null) {
                outputClause.getTableVariable().accept(expressionVisitor, context);
            }
            if (outputClause.getOutputTable() != null) {
                outputClause.getOutputTable().accept(fromItemVisitor, context);
            }
            // @todo: check why this is a list of strings
            // if (outputClause.getColumnList()!=null) {
            // for (Column column:outputClause.getColumnList())
            // }
        }
        return null;
    }

    public ExpressionVisitor<T> getExpressionVisitor() {
        return expressionVisitor;
    }

    public PivotVisitor<T> getPivotVisitor() {
        return pivotVisitor;
    }

    public SelectItemVisitor<T> getSelectItemVisitor() {
        return selectItemVisitor;
    }

    public FromItemVisitor<T> getFromItemVisitor() {
        return fromItemVisitor;
    }

    @Override
    public <S> T visit(ParenthesedSelect select, S context) {
        visitWithItems(select.withItemsList, context);

        select.getSelect().accept(this, context);

        expressionVisitor.visitOrderBy(select.getOrderByElements(), context);

        Pivot pivot = select.getPivot();
        if (pivot != null) {
            pivot.accept(pivotVisitor, context);
        }
        UnPivot unpivot = select.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(pivotVisitor, context);
        }

        expressionVisitor.visitLimit(select.getLimit(), context);

        if (select.getOffset() != null) {
            expressionVisitor.visitExpression(select.getOffset().getOffset(), null);
        }
        if (select.getFetch() != null) {
            expressionVisitor.visitExpression(select.getFetch().getExpression(), null);
        }

        return null;
    }

    @Override
    @SuppressWarnings({"PMD.ExcessiveMethodLength"})
    public <S> T visit(PlainSelect plainSelect, S context) {
        visitWithItems(plainSelect.withItemsList, context);

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

        fromItemVisitor.visitTables(plainSelect.getIntoTables(), context);
        fromItemVisitor.visitFromItem(plainSelect.getFromItem(), context);

        // if (plainSelect.getLateralViews() != null) {
        // //@todo: implement this
        // }

        fromItemVisitor.visitJoins(plainSelect.getJoins(), context);

        // if (plainSelect.getKsqlWindow() != null) {
        // //@todo: implement
        // }

        expressionVisitor.visitExpression(plainSelect.getWhere(), context);

        // if (plainSelect.getOracleHierarchical() != null) {
        // //@todo: implement
        // }
        //

        expressionVisitor.visitPreferringClause(plainSelect.getPreferringClause(), context);
        expressionVisitor.visit(plainSelect.getGroupBy(), context);
        expressionVisitor.visitExpression(plainSelect.getHaving(), context);
        expressionVisitor.visitExpression(plainSelect.getQualify(), context);

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

        expressionVisitor.visitOrderBy(plainSelect.getOrderByElements(), context);

        // if (plainSelect.getLimitBy() != null) {
        // //@todo: implement
        // }
        // if (plainSelect.getLimit() != null) {
        // //@todo: implement
        // }
        if (plainSelect.getOffset() != null) {
            expressionVisitor.visitExpression(plainSelect.getOffset().getOffset(), context);
        }
        if (plainSelect.getFetch() != null) {
            expressionVisitor.visitExpression(plainSelect.getFetch().getExpression(), context);
        }
        // if (plainSelect.getForMode() != null) {
        // //@todo: implement
        // }

        fromItemVisitor.visitFromItem(plainSelect.getIntoTempTable(), context);
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
