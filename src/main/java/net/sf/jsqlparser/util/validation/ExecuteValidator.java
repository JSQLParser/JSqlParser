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

import net.sf.jsqlparser.statement.execute.Execute;

public class ExecuteValidator extends AbstractValidator<Execute> {


    @Override
    public void validate(Execute execute) {
        //        buffer.append(execute.getExecType().name()).append(" ").append(execute.getName());
        //        if (execute.isParenthesis()) {
        //            buffer.append(" (");
        //        } else if (execute.getExprList() != null) {
        //            buffer.append(" ");
        //        }
        //        if (execute.getExprList() != null) {
        //            List<Expression> expressions = execute.getExprList().getExpressions();
        //            for (int i = 0; i < expressions.size(); i++) {
        //                if (i > 0) {
        //                    buffer.append(", ");
        //                }
        //                expressions.get(i).accept(expressionVisitor);
        //            }
        //        }
        //        if (execute.isParenthesis()) {
        //            buffer.append(")");
        //        }
    }

}
