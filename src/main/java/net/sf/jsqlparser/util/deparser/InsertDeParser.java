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

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.expression.operators.relational.NamedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;
import java.util.Iterator;
import java.util.List;

public class InsertDeParser extends AbstractDeParser<Insert> implements ItemsListVisitor {

    private ExpressionVisitor expressionVisitor;

    private SelectVisitor selectVisitor;

    public InsertDeParser() {
        super(new StringBuilder());
    }

    public InsertDeParser(ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
    }

    @Override
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength", "PMD.NPathComplexity" })
    public void deParse(Insert insert) {
        if (insert.getWithItemsList() != null && !insert.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = insert.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                withItem.accept(this.selectVisitor);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        buffer.append("INSERT ");
        if (insert.getModifierPriority() != null) {
            buffer.append(insert.getModifierPriority()).append(" ");
        }
        if (insert.isModifierIgnore()) {
            buffer.append("IGNORE ");
        }
        buffer.append("INTO ");
        buffer.append(insert.getTable().toString());
        if (insert.getColumns() != null) {
            buffer.append(" (");
            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext(); ) {
                Column column = iter.next();
                buffer.append(column.getColumnName());
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(")");
        }
        if (insert.getOutputClause() != null) {
            buffer.append(insert.getOutputClause().toString());
        }
        if (insert.getSelect() != null) {
            buffer.append(" ");
            Select select = insert.getSelect();
            select.accept(selectVisitor);
        }
        if (insert.isUseSet()) {
            buffer.append(" SET ");
            for (int i = 0; i < insert.getSetColumns().size(); i++) {
                Column column = insert.getSetColumns().get(i);
                column.accept(expressionVisitor);
                buffer.append(" = ");
                Expression expression = insert.getSetExpressionList().get(i);
                expression.accept(expressionVisitor);
                if (i < insert.getSetColumns().size() - 1) {
                    buffer.append(", ");
                }
            }
        }
        if (insert.isUseDuplicate()) {
            buffer.append(" ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < insert.getDuplicateUpdateColumns().size(); i++) {
                Column column = insert.getDuplicateUpdateColumns().get(i);
                buffer.append(column.getFullyQualifiedName()).append(" = ");
                Expression expression = insert.getDuplicateUpdateExpressionList().get(i);
                expression.accept(expressionVisitor);
                if (i < insert.getDuplicateUpdateColumns().size() - 1) {
                    buffer.append(", ");
                }
            }
        }
        // @todo: Accept some Visitors for the involved Expressions
        if (insert.getConflictAction() != null) {
            buffer.append(" ON CONFLICT");
            if (insert.getConflictTarget() != null) {
                insert.getConflictTarget().appendTo(buffer);
            }
            insert.getConflictAction().appendTo(buffer);
        }
        if (insert.getReturningExpressionList() != null) {
            buffer.append(" RETURNING ").append(PlainSelect.getStringList(insert.getReturningExpressionList(), true, false));
        }
    }

    @Override
    public void visit(ExpressionList expressionList) {
        new ExpressionListDeParser(expressionVisitor, buffer, expressionList.isUsingBrackets(), true).deParse(expressionList.getExpressions());
    }

    @Override
    public void visit(NamedExpressionList NamedExpressionList) {
        // not used in a top-level insert statement
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        List<ExpressionList> expressionLists = multiExprList.getExpressionLists();
        int n = expressionLists.size() - 1;
        int i = 0;
        for (ExpressionList expressionList : expressionLists) {
            new ExpressionListDeParser(expressionVisitor, buffer, expressionList.isUsingBrackets(), true).deParse(expressionList.getExpressions());
            if (i < n) {
                buffer.append(", ");
            }
            i++;
        }
    }

    @Override
    public void visit(ParenthesedSelect selectBody) {
        selectBody.accept(selectVisitor);
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
