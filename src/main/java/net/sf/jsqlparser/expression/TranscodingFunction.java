/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.Locale;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

import java.util.Objects;
import java.util.function.Consumer;

public class TranscodingFunction extends ASTNodeAccessImpl implements Expression {
    private String keyword = "CONVERT";

    public enum Syntax {
        USING, TYPE_FIRST, TYPE_LAST
    }

    private Syntax syntax = Syntax.USING;
    private ColDataType colDataType;
    private Expression expression;
    private String transcodingName;

    public TranscodingFunction(String keyword, Expression expression, String transcodingName) {
        this.keyword = Objects.requireNonNullElse(keyword, "CONVERT").toUpperCase(Locale.ROOT);
        this.expression = expression;
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction(Expression expression, String transcodingName) {
        this.expression = expression;
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction(String keyword, ColDataType colDataType, Expression expression,
            String transcodingName) {
        this.keyword = Objects.requireNonNullElse(keyword, "CONVERT").toUpperCase(Locale.ROOT);
        this.colDataType = colDataType;
        this.expression = expression;
        this.transcodingName = transcodingName;
        this.syntax = Syntax.TYPE_FIRST;
    }

    public TranscodingFunction(ColDataType colDataType, Expression expression,
            String transcodingName) {
        this.colDataType = colDataType;
        this.expression = expression;
        this.transcodingName = transcodingName;
        this.syntax = Syntax.TYPE_FIRST;
    }

    public TranscodingFunction() {
        this(null, null);
    }

    public String getKeyword() {
        return keyword;
    }

    public TranscodingFunction setKeyword(String keyword) {
        this.keyword = Objects.requireNonNullElse(keyword, "CONVERT").toUpperCase(Locale.ROOT);
        return this;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public TranscodingFunction withExpression(Expression expression) {
        this.setExpression(expression);
        return this;
    }

    public String getTranscodingName() {
        return transcodingName;
    }

    public void setTranscodingName(String transcodingName) {
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction withTranscodingName(String transcodingName) {
        this.setTranscodingName(transcodingName);
        return this;

    }

    public ColDataType getColDataType() {
        return colDataType;
    }

    public TranscodingFunction setColDataType(ColDataType colDataType) {
        this.colDataType = colDataType;
        return this;
    }

    public boolean isTranscodeStyle() {
        return syntax == Syntax.USING;
    }

    public TranscodingFunction setTranscodeStyle(boolean transcodeStyle) {
        syntax = transcodeStyle ? Syntax.USING : Syntax.TYPE_FIRST;
        return this;
    }

    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public Syntax getSyntax() {
        return syntax;
    }

    public TranscodingFunction setSyntax(Syntax syntax) {
        this.syntax = Objects.requireNonNull(syntax);
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, builder::append);
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressionRenderer) {
        builder.append(keyword).append("( ");
        if (syntax == Syntax.TYPE_FIRST) {
            builder.append(colDataType).append(", ");
        }
        expressionRenderer.accept(expression);
        if (syntax == Syntax.USING) {
            builder.append(" USING ").append(transcodingName);
        } else if (syntax == Syntax.TYPE_LAST) {
            builder.append(", ").append(colDataType);
        } else if (transcodingName != null && !transcodingName.isEmpty()) {
            builder.append(", ").append(transcodingName);
        }
        return builder.append(" )");
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
