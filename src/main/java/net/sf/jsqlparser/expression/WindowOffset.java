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

import java.io.Serializable;

public class WindowOffset implements Serializable {

    private Expression expression;
    private Type type;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (expression != null) {
            buffer.append(' ').append(expression);
            if (type != null) {
                buffer.append(' ');
                buffer.append(type);
            }
        } else {
            if (type != null) {
                switch (type) {
                    case PRECEDING:
                        buffer.append(" UNBOUNDED PRECEDING");
                        break;
                    case FOLLOWING:
                        buffer.append(" UNBOUNDED FOLLOWING");
                        break;
                    case CURRENT:
                        buffer.append(" CURRENT ROW");
                        break;
                    default:
                        break;
                }
            }
        }
        return buffer.toString();
    }

    public WindowOffset withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public WindowOffset withType(Type type) {
        this.setType(type);
        return this;
    }

    public <E extends Expression> E getExpression(Class<E> type) {
        return type.cast(getExpression());
    }

    public enum Type {
        PRECEDING, FOLLOWING, CURRENT, EXPR;

        public static Type from(String type) {
            return Enum.valueOf(Type.class, type.toUpperCase());
        }
    }

}
