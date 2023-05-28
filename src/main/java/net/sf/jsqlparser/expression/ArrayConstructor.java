/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2021 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.PlainSelect;
import java.util.List;

public class ArrayConstructor extends ASTNodeAccessImpl implements Expression {

    private List<Expression> expressions;

    private boolean arrayKeyword;

    public List<Expression> getExpressions() {
        return expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        this.expressions = expressions;
    }

    public boolean isArrayKeyword() {
        return arrayKeyword;
    }

    public void setArrayKeyword(boolean arrayKeyword) {
        this.arrayKeyword = arrayKeyword;
    }

    public ArrayConstructor(List<Expression> expressions, boolean arrayKeyword) {
        this.expressions = expressions;
        this.arrayKeyword = arrayKeyword;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (arrayKeyword) {
            sb.append("ARRAY");
        }
        sb.append("[");
        sb.append(PlainSelect.getStringList(expressions, true, false));
        sb.append("]");
        return sb.toString();
    }
}
