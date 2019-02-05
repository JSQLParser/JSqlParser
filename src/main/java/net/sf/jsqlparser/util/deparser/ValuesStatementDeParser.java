/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2017 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public class ValuesStatementDeParser {

    protected StringBuilder buffer;
    private final ExpressionVisitor expressionVisitor;

    public ValuesStatementDeParser(ExpressionVisitor expressionVisitor, StringBuilder buffer) {
        this.buffer = buffer;
        this.expressionVisitor = expressionVisitor;
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }

    public void deParse(ValuesStatement values) {
        boolean first = true;
        buffer.append("VALUES (");
        for (Expression expr : values.getExpressions()) {
            if (first) {
                first = false;
            } else {
                buffer.append(", ");
            }
            expr.accept(expressionVisitor);
        }
        buffer.append(")");
    }
}
