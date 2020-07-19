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

import net.sf.jsqlparser.statement.select.GroupByElement;

public class GroupByValidator extends AbstractValidator<GroupByElement> {

    @Override
    public void validate(GroupByElement groupBy) {
        //        buffer.append("GROUP BY ");
        //        for (Iterator<Expression> iter = groupBy.getGroupByExpressions().iterator(); iter.hasNext();) {
        //            iter.next().accept(expressionVisitor);
        //            if (iter.hasNext()) {
        //                buffer.append(", ");
        //            }
        //        }
        //        if (!groupBy.getGroupingSets().isEmpty()) {
        //            buffer.append("GROUPING SETS (");
        //            boolean first = true;
        //            for (Object o : groupBy.getGroupingSets()) {
        //                if (first) {
        //                    first = false;
        //                } else {
        //                    buffer.append(", ");
        //                }
        //                if (o instanceof Expression) {
        //                    buffer.append(o.toString());
        //                } else if (o instanceof ExpressionList) {
        //                    ExpressionList list = (ExpressionList) o;
        //                    buffer.append(list.getExpressions() == null ? "()" : list.toString());
        //                }
        //            }
        //            buffer.append(")");
        //        }
    }


}
