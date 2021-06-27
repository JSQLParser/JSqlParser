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
import net.sf.jsqlparser.statement.select.PlainSelect;

public final class SetStatement implements Statement {

    private String effectParameter;
    private final List<NameExpr> values = new ArrayList<>();

    public SetStatement() {
        // empty constructor
    }

    public SetStatement(String name, List<Expression> value) {
        add(name, value, true);
    }

    public void add(String name, List<Expression> value, boolean useEqual) {
        values.add(new NameExpr(name, value, useEqual));
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

    public SetStatement withUseEqual(int idx, boolean useEqual) {
        this.setUseEqual(idx, useEqual);
        return this;
    }

    public SetStatement setUseEqual(int idx, boolean useEqual) {
        values.get(idx).useEqual = useEqual;
        return this;
    }

    public SetStatement withUseEqual(boolean useEqual) {
      this.setUseEqual(useEqual);
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

    public List<Expression> getExpressions(int idx) {
        return values.get(idx).expressions;
    }

    public List<Expression> getExpressions() {
        return getExpressions(0);
    }

    public void setExpressions(int idx, List<Expression> expressions) {
        values.get(idx).expressions = expressions;
    }

    public void setExpressions(List<Expression> expressions) {
        setExpressions(0, expressions);
    }

    private String toString(NameExpr ne) {
        return ne.name + (ne.useEqual ? " = " : " ") +
                PlainSelect.getStringList(ne.expressions, true, false);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("SET ");
        if (effectParameter != null) {
            b.append(effectParameter).append(" ");
        }
        boolean addComma = false;
        for (NameExpr ne : values) {
            if (addComma) {
                b.append(", ");
            } else {
                addComma = true;
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
        private List<Expression> expressions;
        private boolean useEqual;

        public NameExpr(String name, List<Expression> expressions, boolean useEqual) {
            this.name = name;
            this.expressions = expressions;
            this.useEqual = useEqual;
        }
    }

    public String getEffectParameter() {
        return effectParameter;
    }

    public void setEffectParameter(String effectParameter) {
        this.effectParameter = effectParameter;
    }
    public SetStatement withEffectParameter(String effectParameter) {
        this.effectParameter = effectParameter;
        return this;
    }
}
