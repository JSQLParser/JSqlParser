/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.OrderByVisitor;
import net.sf.jsqlparser.statement.update.Update;

public class UpdateValidator extends AbstractValidator<Update> implements OrderByVisitor {

    @Override
    public void validate(Update update) {
        //        buffer.append("UPDATE ").append(update.getTable());
        //        if (update.getStartJoins() != null) {
        //            for (Join join : update.getStartJoins()) {
        //                if (join.isSimple()) {
        //                    buffer.append(", ").append(join);
        //                } else {
        //                    buffer.append(" ").append(join);
        //                }
        //            }
        //        }
        //        buffer.append(" SET ");
        //
        //        if (!update.isUseSelect()) {
        //            for (int i = 0; i < update.getColumns().size(); i++) {
        //                Column column = update.getColumns().get(i);
        //                column.accept(expressionVisitor);
        //
        //                buffer.append(" = ");
        //
        //                Expression expression = update.getExpressions().get(i);
        //                expression.accept(expressionVisitor);
        //                if (i < update.getColumns().size() - 1) {
        //                    buffer.append(", ");
        //                }
        //            }
        //        } else {
        //            if (update.isUseColumnsBrackets()) {
        //                buffer.append("(");
        //            }
        //            for (int i = 0; i < update.getColumns().size(); i++) {
        //                if (i != 0) {
        //                    buffer.append(", ");
        //                }
        //                Column column = update.getColumns().get(i);
        //                column.accept(expressionVisitor);
        //            }
        //            if (update.isUseColumnsBrackets()) {
        //                buffer.append(")");
        //            }
        //            buffer.append(" = ");
        //            buffer.append("(");
        //            Select select = update.getSelect();
        //            select.getSelectBody().accept(selectVisitor);
        //            buffer.append(")");
        //        }
        //
        //        if (update.getFromItem() != null) {
        //            buffer.append(" FROM ").append(update.getFromItem());
        //            if (update.getJoins() != null) {
        //                for (Join join : update.getJoins()) {
        //                    if (join.isSimple()) {
        //                        buffer.append(", ").append(join);
        //                    } else {
        //                        buffer.append(" ").append(join);
        //                    }
        //                }
        //            }
        //        }
        //
        //        if (update.getWhere() != null) {
        //            buffer.append(" WHERE ");
        //            update.getWhere().accept(expressionVisitor);
        //        }
        //        if (update.getOrderByElements() != null) {
        //            new OrderByValidator().validate(update.getOrderByElements());
        //        }
        //        if (update.getLimit() != null) {
        //            new LimitValidator(buffer).deParse(update.getLimit());
        //        }
        //
        //        if (update.isReturningAllColumns()) {
        //            buffer.append(" RETURNING *");
        //        } else if (update.getReturningExpressionList() != null) {
        //            buffer.append(" RETURNING ");
        //            for (Iterator<SelectExpressionItem> iter = update.getReturningExpressionList().iterator(); iter
        //                    .hasNext();) {
        //                buffer.append(iter.next().toString());
        //                if (iter.hasNext()) {
        //                    buffer.append(", ");
        //                }
        //            }
        //        }
    }


    @Override
    public void visit(OrderByElement orderBy) {
        //        orderBy.getExpression().accept(expressionVisitor);
        //        if (!orderBy.isAsc()) {
        //            buffer.append(" DESC");
        //        } else if (orderBy.isAscDescPresent()) {
        //            buffer.append(" ASC");
        //        }
        //        if (orderBy.getNullOrdering() != null) {
        //            buffer.append(' ');
        //            buffer.append(orderBy.getNullOrdering() == OrderByElement.NullOrdering.NULLS_FIRST ? "NULLS FIRST"
        //                    : "NULLS LAST");
        //        }
    }
}
