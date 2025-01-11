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
import net.sf.jsqlparser.schema.Partition;
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
            builder.append("WITH ");
            for (Iterator<WithItem<?>> iter = insert.getWithItemsList().iterator(); iter
                    .hasNext();) {
                WithItem<?> withItem = iter.next();
                withItem.accept(this.selectVisitor, null);
                if (iter.hasNext()) {
                    builder.append(",");
                }
                builder.append(" ");
            }
        }

        builder.append("INSERT ");
        if (insert.getModifierPriority() != null) {
            builder.append(insert.getModifierPriority()).append(" ");
        }
        if (insert.getOracleHint() != null) {
            builder.append(insert.getOracleHint()).append(" ");
        }
        if (insert.isModifierIgnore()) {
            builder.append("IGNORE ");
        }
        if (insert.isOverwrite()) {
            builder.append("OVERWRITE ");
        } else {
            builder.append("INTO ");
        }
        if (insert.isTableKeyword()) {
            builder.append("TABLE ");
        }

        builder.append(insert.getTable().toString());

        if (insert.isOnlyDefaultValues()) {
            builder.append(" DEFAULT VALUES");
        }

        if (insert.getColumns() != null) {
            builder.append(" (");
            for (Iterator<Column> iter = insert.getColumns().iterator(); iter.hasNext();) {
                Column column = iter.next();
                builder.append(column.getColumnName());
                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        }

        if (insert.getPartitions() != null) {
            builder.append(" PARTITION (");
            Partition.appendPartitionsTo(builder, insert.getPartitions());
            builder.append(")");
        }

        if (insert.getOutputClause() != null) {
            builder.append(insert.getOutputClause().toString());
        }

        if (insert.getSelect() != null) {
            builder.append(" ");
            Select select = insert.getSelect();
            select.accept(selectVisitor, null);
        }

        if (insert.getSetUpdateSets() != null) {
            builder.append(" SET ");
            deparseUpdateSets(insert.getSetUpdateSets(), builder, expressionVisitor);
        }

        if (insert.getDuplicateUpdateSets() != null) {
            builder.append(" ON DUPLICATE KEY UPDATE ");
            deparseUpdateSets(insert.getDuplicateUpdateSets(), builder, expressionVisitor);
        }

        // @todo: Accept some Visitors for the involved Expressions
        if (insert.getConflictAction() != null) {
            builder.append(" ON CONFLICT");

            if (insert.getConflictTarget() != null) {
                insert.getConflictTarget().appendTo(builder);
            }
            insert.getConflictAction().appendTo(builder);
        }

        if (insert.getReturningClause() != null) {
            insert.getReturningClause().appendTo(builder);
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
