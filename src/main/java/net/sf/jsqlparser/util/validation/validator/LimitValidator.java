/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.statement.select.Limit;

public class LimitValidator extends AbstractValidator<Limit> {


    @Override
    public void validate(Limit limit) {
        //        buffer.append(" LIMIT ");
        //        if (limit.isLimitNull()) {
        //            buffer.append("NULL");
        //        } else {
        //            if (limit.isLimitAll()) {
        //                buffer.append("ALL");
        //            } else {
        //                if (null != limit.getOffset()) {
        //                    buffer.append(limit.getOffset()).append(", ");
        //                }
        //
        //                if (null != limit.getRowCount()) {
        //                    buffer.append(limit.getRowCount());
        //                }
        //            }
        //        }
    }
}
