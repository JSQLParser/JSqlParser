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

import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;

import java.util.Iterator;

public class InsertDeParser extends AbstractDeParser<Insert> {

    private ExpressionVisitor<StringBuilder> expressionVisitor;
    private SelectVisitor<StringBuilder> selectVisitor;

    public InsertDeParser() {
        super(new StringBuilder());
    }

    public InsertDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            SelectVisitor<StringBuilder> selectVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength",
            "PMD.NPathComplexity"})
    public void deParse(Insert insert) {
        if (insert.getWithItemsList() != null && !insert.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = insert.getWithItemsList().iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                withItem.accept(this.selectVisitor, null);
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
        if (insert.getOracleHint() != null) {
            buffer.append(insert.getOracleHint()).append(" ");
        }
        if (insert.isModifierIgnore()) {
            buffer.append("IGNORE ");
        }
        buffer.append("INTO ");

        buffer.append(insert.getTable().toString());

        if (insert.isOnlyDefaultValues()) {
            buffer.append(" DEFAULT VALUES");
        }

        if (insert.getColumns() != null) {
            buffer.append(" (");
            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext();) {
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
            select.accept(selectVisitor, null);
        }

        if (insert.getSetUpdateSets() != null) {
            buffer.append(" SET ");
            deparseUpdateSets(insert.getSetUpdateSets(), buffer, expressionVisitor);
        }

        if (insert.getDuplicateUpdateSets() != null) {
            buffer.append(" ON DUPLICATE KEY UPDATE ");
            deparseUpdateSets(insert.getDuplicateUpdateSets(), buffer, expressionVisitor);
        }

        // @todo: Accept some Visitors for the involved Expressions
        if (insert.getConflictAction() != null) {
            buffer.append(" ON CONFLICT");

            if (insert.getConflictTarget() != null) {
                insert.getConflictTarget().appendTo(buffer);
            }
            insert.getConflictAction().appendTo(buffer);
        }

        if (insert.getReturningClause() != null) {
            insert.getReturningClause().appendTo(buffer);
        }
    }

    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }

    public SelectVisitor<StringBuilder> getSelectVisitor() {
        return selectVisitor;
    }

    public void setSelectVisitor(SelectVisitor<StringBuilder> visitor) {
        selectVisitor = visitor;
    }
}
