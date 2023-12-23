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
    private Expression expr;

    private final List<Map.Entry<String, String>> idents = new ArrayList<>();

    public JsonExpression() {

    }

    public JsonExpression(Expression expr) {
        this.expr = expr;
    }

    public JsonExpression(Expression expr, List<Map.Entry<String, String>> idents) {
        this.expr = expr;
        this.idents.addAll(idents);
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    public Expression getExpression() {
        return expr;
    }

    public void setExpression(Expression expr) {
        this.expr = expr;
    }

    public void addIdent(String ident, String operator) {
        idents.add(new AbstractMap.SimpleEntry<>(ident, operator));
    }

    public void addAllIdents(Collection<Map.Entry<String, String>> idents) {
        this.idents.addAll(idents);
    }

    public List<Map.Entry<String, String>> getIdentList() {
        return idents;
    }

    public Map.Entry<String, String> getIdent(int index) {
        return idents.get(index);
    }

    @Deprecated
    public List<String> getIdents() {
        ArrayList<String> l = new ArrayList<>();
        for (Map.Entry<String, String> ident : idents) {
            l.add(ident.getKey());
        }

        return l;
    }

    @Deprecated
    public List<String> getOperators() {
        ArrayList<String> l = new ArrayList<>();
        for (Map.Entry<String, String> ident : idents) {
            l.add(ident.getValue());
        }
        return l;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(expr.toString());
        for (Map.Entry<String, String> ident : idents) {
            b.append(ident.getValue()).append(ident.getKey());
        }
        return b.toString();
    }

    public JsonExpression withExpression(Expression expr) {
        this.setExpression(expr);
        return this;
    }
}
