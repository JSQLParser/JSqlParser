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
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;

import java.util.Iterator;

public class UpdateDeParser extends AbstractDeParser<Update>
        implements OrderByVisitor<StringBuilder> {

    private ExpressionVisitor<StringBuilder> expressionVisitor = new ExpressionVisitorAdapter<>();

    public UpdateDeParser() {
        super(new StringBuilder());
    }

    public UpdateDeParser(ExpressionVisitor<StringBuilder> expressionVisitor,
            StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength"})
    public void deParse(Update update) {
        if (update.getWithItemsList() != null && !update.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem<?>> iter = update.getWithItemsList().iterator(); iter
                    .hasNext();) {
                WithItem<?> withItem = iter.next();
                buffer.append(withItem);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        buffer.append("UPDATE ");
        if (update.getOracleHint() != null) {
            buffer.append(update.getOracleHint()).append(" ");
        }
        if (update.getModifierPriority() != null) {
            buffer.append(update.getModifierPriority()).append(" ");
        }
        if (update.isModifierIgnore()) {
            buffer.append("IGNORE ");
        }
        buffer.append(update.getTable());
        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                if (join.isSimple()) {
                    buffer.append(", ").append(join);
                } else {
                    buffer.append(" ").append(join);
                }
            }
        }
        buffer.append(" SET ");

        deparseUpdateSetsClause(update);

        if (update.getOutputClause() != null) {
            update.getOutputClause().appendTo(buffer);
        }

        if (update.getFromItem() != null) {
            buffer.append(" FROM ").append(update.getFromItem());
            if (update.getJoins() != null) {
                for (Join join : update.getJoins()) {
                    if (join.isSimple()) {
                        buffer.append(", ").append(join);
                    } else {
                        buffer.append(" ").append(join);
                    }
                }
            }
        }

        deparseWhereClause(update);

        if (update.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(update.getOrderByElements());
        }
        if (update.getLimit() != null) {
            new LimitDeparser(expressionVisitor, buffer).deParse(update.getLimit());
        }

        if (update.getReturningClause() != null) {
            update.getReturningClause().appendTo(buffer);
        }
    }

    protected void deparseWhereClause(Update update) {
        if (update.getWhere() != null) {
            buffer.append(" WHERE ");
            update.getWhere().accept(expressionVisitor, null);
        }
    }

    protected void deparseUpdateSetsClause(Update update) {
        deparseUpdateSets(update.getUpdateSets(), buffer, expressionVisitor);
    }


    public ExpressionVisitor<StringBuilder> getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor<StringBuilder> visitor) {
        expressionVisitor = visitor;
    }

    @Override
    public <S> StringBuilder visit(OrderByElement orderBy, S context) {
        orderBy.getExpression().accept(expressionVisitor, context);
        if (!orderBy.isAsc()) {
            buffer.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            buffer.append(" ASC");
        }
        if (orderBy.getNullOrdering() != null) {
            buffer.append(' ');
            buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST
                    ? "NULLS FIRST"
                    : "NULLS LAST");
        }
        return buffer;
    }
}
