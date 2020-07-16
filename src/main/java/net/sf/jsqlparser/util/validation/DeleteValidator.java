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

import net.sf.jsqlparser.statement.delete.Delete;

public class DeleteValidator extends AbstractValidator<Delete> {


    @Override
    public void validate(Delete delete) {
        //        buffer.append("DELETE");
        //        if (delete.getTables() != null && !delete.getTables().isEmpty()) {
        //            buffer.append(
        //                    delete.getTables().stream().map(Table::getFullyQualifiedName).collect(joining(", ", " ", "")));
        //        }
        //        buffer.append(" FROM ").append(delete.getTable().toString());
        //
        //        if (delete.getJoins() != null) {
        //            for (Join join : delete.getJoins()) {
        //                if (join.isSimple()) {
        //                    buffer.append(", ").append(join);
        //                } else {
        //                    buffer.append(" ").append(join);
        //                }
        //            }
        //        }
        //
        //        if (delete.getWhere() != null) {
        //            buffer.append(" WHERE ");
        //            delete.getWhere().accept(expressionVisitor);
        //        }
        //
        //        if (delete.getOrderByElements() != null) {
        //            new OrderByValidator().validate(delete.getOrderByElements());
        //        }
        //        if (delete.getLimit() != null) {
        //            new LimitValidator(buffer).deParse(delete.getLimit());
        //        }

    }

}
