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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.upsert.Upsert;

public class UpsertDeParser extends AbstractDeParser<Upsert> implements ItemsListVisitor {

    private ExpressionVisitor expressionVisitor;
    private SelectVisitor selectVisitor;

    public UpsertDeParser(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
    }

    @Override
    public void deParse(Upsert upsert) {
        buffer.append("UPSERT INTO ");

        buffer.append(upsert.getTable().getFullyQualifiedName());
        if (upsert.getColumns() != null) {
            appendColumns(upsert);
        }

        if (upsert.getItemsList() != null) {
            upsert.getItemsList().accept(this);
        }

        if (upsert.getSelect() != null) {
            appendSelect(upsert);
        }

        if (upsert.isUseDuplicate()) {
            appendDuplicate(upsert);
        }

    }

    private void appendColumns(Upsert upsert) {
        buffer.append(" (");
        for (Iterator<Column> iter = upsert.getColumns().iterator(); iter.hasNext();) {
            Column column = iter.next();
            buffer.append(column.getColumnName());
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }

    private void appendSelect(Upsert upsert) {
        buffer.append(" ");
        if (upsert.isUseSelectBrackets()) {
            buffer.append("(");
        }
        if (upsert.getSelect().getWithItemsList() != null) {
            buffer.append("WITH ");
            for (WithItem with : upsert.getSelect().getWithItemsList()) {
                with.accept(selectVisitor);
            }
            buffer.append(" ");
        }
        upsert.getSelect().getSelectBody().accept(selectVisitor);
        if (upsert.isUseSelectBrackets()) {
            buffer.append(")");
        }
    }

    private void appendDuplicate(Upsert upsert) {
        buffer.append(" ON DUPLICATE KEY UPDATE ");
        for (int i = 0; i < upsert.getDuplicateUpdateColumns().size(); i++) {
            Column column = upsert.getDuplicateUpdateColumns().get(i);
            buffer.append(column.getFullyQualifiedName()).append(" = ");

            Expression expression = upsert.getDuplicateUpdateExpressionList().get(i);
            expression.accept(expressionVisitor);
            if (i < upsert.getDuplicateUpdateColumns().size() - 1) {
                buffer.append(", ");
            }
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        buffer.append(" VALUES (");
        for (Iterator<Expression> iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
            Expression expression = iter.next();
            expression.accept(expressionVisitor);
            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }
        buffer.append(")");
    }

// not used by top-level upsert
    @Override
    public void visit(NamedExpressionList namedExpressionList) {
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        buffer.append(" VALUES ");
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            buffer.append("(");
            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
                Expression expression = iter.next();
                expression.accept(expressionVisitor);
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
            if (it.hasNext()) {
                buffer.append(", ");
            }
        }
    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(selectVisitor);
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public SelectVisitor getSelectVisitor() {
        return selectVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }

    public void setSelectVisitor(SelectVisitor visitor) {
        selectVisitor = visitor;
    }

}
