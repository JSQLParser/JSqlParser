/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class JsonExpression extends ASTNodeAccessImpl implements Expression {
    private final List<Map.Entry<Expression, String>> idents = new ArrayList<>();
    private Expression expr;

    public JsonExpression() {

    }

    public JsonExpression(Expression expr) {
        this.expr = expr;
    }

    public JsonExpression(Expression expr, List<Map.Entry<Expression, String>> idents) {
        this.expr = expr;
        this.idents.addAll(idents);
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public Expression getExpression() {
        return expr;
    }

    public void setExpression(Expression expr) {
        this.expr = expr;
    }

    public void addIdent(Expression ident, String operator) {
        idents.add(new AbstractMap.SimpleEntry<>(ident, operator));
    }

    public void addAllIdents(Collection<Map.Entry<Expression, String>> idents) {
        this.idents.addAll(idents);
    }

    public List<Map.Entry<Expression, String>> getIdentList() {
        return idents;
    }

    public Map.Entry<Expression, String> getIdent(int index) {
        return idents.get(index);
    }

    @Deprecated
    public List<Expression> getIdents() {
        ArrayList<Expression> l = new ArrayList<>();
        for (Map.Entry<Expression, String> ident : idents) {
            l.add(ident.getKey());
        }

        return l;
    }

    @Deprecated
    public List<String> getOperators() {
        ArrayList<String> l = new ArrayList<>();
        for (Map.Entry<Expression, String> ident : idents) {
            l.add(ident.getValue());
        }
        return l;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(expr.toString());
        for (Map.Entry<Expression, String> ident : idents) {
            b.append(ident.getValue()).append(ident.getKey());
        }
        return b.toString();
    }

    public JsonExpression withExpression(Expression expr) {
        this.setExpression(expr);
        return this;
    }
}
