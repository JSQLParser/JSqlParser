/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** SQL/JSON type and key uniqueness predicate; the JSON value is not evaluated by the parser. */
public class IsJsonExpression extends ASTNodeAccessImpl implements Expression {
    public enum Type {
        VALUE, SCALAR, ARRAY, OBJECT
    }

    public enum UniqueKeys {
        WITH, WITHOUT
    }

    private Expression leftExpression;
    private boolean not;
    private Type type;
    private UniqueKeys uniqueKeys;
    private boolean useKeysKeyword = true;

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public UniqueKeys getUniqueKeys() {
        return uniqueKeys;
    }

    public void setUniqueKeys(UniqueKeys uniqueKeys) {
        this.uniqueKeys = uniqueKeys;
    }

    public boolean isUseKeysKeyword() {
        return useKeysKeyword;
    }

    public void setUseKeysKeyword(boolean useKeysKeyword) {
        this.useKeysKeyword = useKeysKeyword;
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        expressionRenderer.accept(leftExpression);
        builder.append(not ? " IS NOT JSON" : " IS JSON");
        if (type != null) {
            builder.append(' ').append(type);
        }
        if (uniqueKeys != null) {
            builder.append(' ').append(uniqueKeys).append(" UNIQUE");
            if (useKeysKeyword) {
                builder.append(" KEYS");
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
