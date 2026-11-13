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

/**
 * The Unicode normalization test {@code expression IS [NOT] [NFC | NFD | NFKC | NFKD] NORMALIZED}.
 */
public class IsNormalizedExpression extends ASTNodeAccessImpl implements Expression {
    /** The Unicode normalization forms. */
    public enum NormalForm {
        NFC, NFD, NFKC, NFKD;

        /** Whether the word names a normalization form, in any case. */
        public static boolean isNormalForm(String word) {
            for (NormalForm form : values()) {
                if (form.name().equalsIgnoreCase(word)) {
                    return true;
                }
            }
            return false;
        }
    }

    private Expression leftExpression;
    private boolean not;
    private NormalForm normalForm;

    public IsNormalizedExpression() {
        // empty constructor
    }

    public IsNormalizedExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public IsNormalizedExpression withLeftExpression(Expression leftExpression) {
        this.setLeftExpression(leftExpression);
        return this;
    }

    public <E extends Expression> E getLeftExpression(Class<E> type) {
        return type.cast(getLeftExpression());
    }

    public boolean isNot() {
        return not;
    }

    public void setNot(boolean not) {
        this.not = not;
    }

    public IsNormalizedExpression withNot(boolean not) {
        this.setNot(not);
        return this;
    }

    /** The normalization form as written, or null when it is omitted (the server tests NFC). */
    public NormalForm getNormalForm() {
        return normalForm;
    }

    public void setNormalForm(NormalForm normalForm) {
        this.normalForm = normalForm;
    }

    public IsNormalizedExpression withNormalForm(NormalForm normalForm) {
        this.setNormalForm(normalForm);
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    /** Appends the test, rendering the operand through the renderer. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        expressionRenderer.accept(leftExpression);
        builder.append(not ? " IS NOT " : " IS ");
        if (normalForm != null) {
            builder.append(normalForm).append(' ');
        }
        return builder.append("NORMALIZED");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }
}
