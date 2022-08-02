/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.ArrayList;
import java.util.List;

public class RowValueConstructor extends ASTNodeAccessImpl implements Expression {

    private boolean hasRow;

    private List<Expression> expressionList = new ArrayList<>();

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
    }

    public boolean isHasRow() {
        return hasRow;
    }

    public void setHasRow(boolean hasRow) {
        this.hasRow = hasRow;
    }

    public void addExpression(Expression expr) {
        this.expressionList.add(expr);
    }

    public List<Expression> getExpressionList() {
        return expressionList;
    }

    public void setExpressionList(List<Expression> expressionList) {
        this.expressionList = expressionList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (hasRow) {
            sb.append("ROW ");
        }
        sb.append("( ");
        if (expressionList.size() > 0) {
            sb.append(expressionList.get(0));
        }
        for (int i = 1; i < expressionList.size(); i++) {
            sb.append(", ");
            sb.append(expressionList.get(i));
        }
        sb.append(" )");
        return sb.toString();
    }
}
