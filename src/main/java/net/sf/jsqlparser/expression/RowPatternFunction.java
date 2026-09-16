/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.function.Consumer;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

/** An explicit RUNNING or FINAL evaluation mode on a row-pattern function. */
public class RowPatternFunction extends ASTNodeAccessImpl implements Expression {
    public enum EvaluationMode {
        RUNNING, FINAL
    }

    private EvaluationMode evaluationMode;
    private Function function;

    public RowPatternFunction(EvaluationMode evaluationMode, Function function) {
        this.evaluationMode = evaluationMode;
        this.function = function;
    }

    public EvaluationMode getEvaluationMode() {
        return evaluationMode;
    }

    public RowPatternFunction setEvaluationMode(EvaluationMode mode) {
        evaluationMode = mode;
        return this;
    }

    public Function getFunction() {
        return function;
    }

    public RowPatternFunction setFunction(Function function) {
        this.function = function;
        return this;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
        builder.append(evaluationMode).append(' ');
        expressions.accept(function);
        return builder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, expression -> builder.append(expression)).toString();
    }
}
