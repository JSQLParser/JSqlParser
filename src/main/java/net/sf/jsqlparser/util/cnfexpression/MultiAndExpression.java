/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.cnfexpression;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;

public final class MultiAndExpression extends MultipleExpression {

    public MultiAndExpression(List<Expression> childlist) {
        super(childlist);
    }

    @Override
    public String getStringExpression() {
        return "AND";
    }

}
