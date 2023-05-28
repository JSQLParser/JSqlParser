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
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;

public class UpdateDeParser extends AbstractDeParser<Update> implements OrderByVisitor {

    private ExpressionVisitor expressionVisitor = new ExpressionVisitorAdapter();

    public UpdateDeParser() {
        super(new StringBuilder());
    }

    public UpdateDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength" })
    public void deParse(Update update) {
        if (update.getWithItemsList() != null && !update.getWithItemsList().isEmpty()) {
            buffer.append("WITH ");
            for (Iterator<WithItem> iter = update.getWithItemsList().iterator(); iter.hasNext(); ) {
                WithItem withItem = iter.next();
                buffer.append(withItem);
                if (iter.hasNext()) {
                    buffer.append(",");
                }
                buffer.append(" ");
            }
        }
        buffer.append("UPDATE ");
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
        int j = 0;
        for (UpdateSet updateSet : update.getUpdateSets()) {
            if (j > 0) {
                buffer.append(", ");
            }
            if (updateSet.isUsingBracketsForColumns()) {
                buffer.append("(");
            }
            for (int i = 0; i < updateSet.getColumns().size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                updateSet.getColumns().get(i).accept(expressionVisitor);
            }
            if (updateSet.isUsingBracketsForColumns()) {
                buffer.append(")");
            }
            buffer.append(" = ");
            if (updateSet.isUsingBracketsForValues()) {
                buffer.append("(");
            }
            for (int i = 0; i < updateSet.getExpressions().size(); i++) {
                if (i > 0) {
                    buffer.append(", ");
                }
                updateSet.getExpressions().get(i).accept(expressionVisitor);
            }
            if (updateSet.isUsingBracketsForValues()) {
                buffer.append(")");
            }
            j++;
        }
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
        if (update.getWhere() != null) {
            buffer.append(" WHERE ");
            update.getWhere().accept(expressionVisitor);
        }
        if (update.getOrderByElements() != null) {
            new OrderByDeParser(expressionVisitor, buffer).deParse(update.getOrderByElements());
        }
        if (update.getLimit() != null) {
            new LimitDeparser(buffer).deParse(update.getLimit());
        }
        if (update.getReturningExpressionList() != null) {
            buffer.append(" RETURNING ").append(PlainSelect.getStringList(update.getReturningExpressionList(), true, false));
        }
    }

    public ExpressionVisitor getExpressionVisitor() {
        return expressionVisitor;
    }

    public void setExpressionVisitor(ExpressionVisitor visitor) {
        expressionVisitor = visitor;
    }

    @Override
    public void visit(OrderByElement orderBy) {
        orderBy.getExpression().accept(expressionVisitor);
        if (!orderBy.isAsc()) {
            buffer.append(" DESC");
        } else if (orderBy.isAscDescPresent()) {
            buffer.append(" ASC");
        }
        if (orderBy.getNullOrdering() != null) {
            buffer.append(' ');
            buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST" : "NULLS LAST");
        }
    }
}
