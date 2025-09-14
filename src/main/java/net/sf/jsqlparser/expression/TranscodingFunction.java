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

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

import java.util.Objects;

public class TranscodingFunction extends ASTNodeAccessImpl implements Expression {
    private String keyword = "CONVERT";
    private boolean isTranscodeStyle = true;
    private ColDataType colDataType;
    private Expression expression;
    private String transcodingName;

    public TranscodingFunction(String keyword, Expression expression, String transcodingName) {
        this.keyword = Objects.requireNonNullElse(keyword, "CONVERT").toUpperCase();
        this.expression = expression;
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction(Expression expression, String transcodingName) {
        this.expression = expression;
        this.transcodingName = transcodingName;
    }

    public TranscodingFunction(String keyword, ColDataType colDataType, Expression expression,
            String transcodingName) {
        this.keyword = Objects.requireNonNullElse(keyword, "CONVERT").toUpperCase();
        this.colDataType = colDataType;
        this.expression = expression;
        this.transcodingName = transcodingName;
        this.isTranscodeStyle = false;
    }

    public TranscodingFunction(ColDataType colDataType, Expression expression,
            String transcodingName) {
        this.colDataType = colDataType;
        this.expression = expression;
        this.transcodingName = transcodingName;
        this.isTranscodeStyle = false;
    }

    public TranscodingFunction() {
        this(null, null);
    }

    public String getKeyword() {
        return keyword;
    }

    public TranscodingFunction setKeyword(String keyword) {
        this.keyword = Objects.requireNonNullElse(keyword, "CONVERT").toUpperCase();
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
        return isTranscodeStyle;
    }

    public TranscodingFunction setTranscodeStyle(boolean transcodeStyle) {
        isTranscodeStyle = transcodeStyle;
        return this;
    }

    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        if (isTranscodeStyle) {
            return builder
                    .append(keyword)
                    .append("( ")
                    .append(expression)
                    .append(" USING ")
                    .append(transcodingName)
                    .append(" )");
        } else {
            return builder
                    .append(keyword)
                    .append("( ")
                    .append(colDataType)
                    .append(", ")
                    .append(expression)
                    .append(transcodingName != null && !transcodingName.isEmpty()
                            ? ", " + transcodingName
                            : "")
                    .append(" )");
        }
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
