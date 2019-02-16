/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;

public final class SetStatement implements Statement {

    private final List<NameExpr> values = new ArrayList<>();

    public SetStatement(String name, Expression expression) {
        add(name, expression, true);
    }

    public void add(String name, Expression expression, boolean useEqual) {
        values.add(new NameExpr(name, expression, useEqual));
    }

    public void remove(int idx) {
        values.remove(idx);
    }

    public int getCount() {
        return values.size();
    }

    public boolean isUseEqual(int idx) {
        return values.get(idx).useEqual;
    }

    public boolean isUseEqual() {
        return isUseEqual(0);
    }

    public SetStatement setUseEqual(int idx, boolean useEqual) {
        values.get(idx).useEqual = useEqual;
        return this;
    }

    public SetStatement setUseEqual(boolean useEqual) {
        return setUseEqual(0, useEqual);
    }

    public String getName() {
        return getName(0);
    }

    public String getName(int idx) {
        return values.get(idx).name;
    }

    public void setName(String name) {
        setName(0, name);
    }

    public void setName(int idx, String name) {
        values.get(idx).name = name;
    }

    public Expression getExpression(int idx) {
        return values.get(idx).expression;
    }

    public Expression getExpression() {
        return getExpression(0);
    }

    public void setExpression(int idx, Expression expression) {
        values.get(idx).expression = expression;
    }

    public void setExpression(Expression expression) {
        setExpression(0, expression);
    }

    private String toString(NameExpr ne) {
        return ne.name + (ne.useEqual ? " = " : " ") + ne.expression.toString();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("SET ");

        for (NameExpr ne : values) {
            if (b.length() != 4) {
                b.append(", ");
            }
            b.append(toString(ne));
        }

        return b.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    static class NameExpr {

        private String name;
        private Expression expression;
        private boolean useEqual;

        public NameExpr(String name, Expression expr, boolean useEqual) {
            this.name = name;
            this.expression = expr;
            this.useEqual = useEqual;
        }
    }
}
