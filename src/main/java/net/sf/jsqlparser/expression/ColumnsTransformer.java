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

import java.util.List;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * A ClickHouse transformer following a {@code COLUMNS(...)} matcher, for example the
 * {@code APPLY(x -> round(x, 2))} in {@code SELECT COLUMNS('^m') APPLY(x -> round(x, 2))}.
 *
 * ClickHouse parses its transformers in a loop, so they may repeat and combine in any order.
 */
public class ColumnsTransformer extends ASTNodeAccessImpl {

    public enum ColumnsTransformerType {
        APPLY, EXCEPT, REPLACE
    }

    private ColumnsTransformerType type;
    private Expression applyExpression;
    private ParenthesedExpressionList<Column> exceptColumns;
    private List<SelectItem<?>> replaceItems;

    public ColumnsTransformer(ColumnsTransformerType type) {
        this.type = type;
    }

    public ColumnsTransformerType getType() {
        return type;
    }

    public ColumnsTransformer setType(ColumnsTransformerType type) {
        this.type = type;
        return this;
    }

    public Expression getApplyExpression() {
        return applyExpression;
    }

    public ColumnsTransformer setApplyExpression(Expression applyExpression) {
        this.applyExpression = applyExpression;
        return this;
    }

    public ParenthesedExpressionList<Column> getExceptColumns() {
        return exceptColumns;
    }

    public ColumnsTransformer setExceptColumns(ParenthesedExpressionList<Column> exceptColumns) {
        this.exceptColumns = exceptColumns;
        return this;
    }

    public List<SelectItem<?>> getReplaceItems() {
        return replaceItems;
    }

    public ColumnsTransformer setReplaceItems(List<SelectItem<?>> replaceItems) {
        this.replaceItems = replaceItems;
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        switch (type) {
            case APPLY:
                builder.append("APPLY(").append(applyExpression).append(")");
                break;
            case EXCEPT:
                builder.append("EXCEPT ").append(exceptColumns);
                break;
            case REPLACE:
                builder.append("REPLACE(").append(Select.getStringList(replaceItems)).append(")");
                break;
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
