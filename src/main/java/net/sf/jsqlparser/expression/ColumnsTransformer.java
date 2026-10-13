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
 * A ClickHouse transformer following a {@code COLUMNS(...)} matcher or a {@code *}, for example the
 * {@code APPLY(x -> round(x, 2))} in {@code SELECT COLUMNS('^m') APPLY(x -> round(x, 2))}.
 *
 * ClickHouse parses its transformers in a loop, so they may repeat and combine in any order.
 */
public class ColumnsTransformer extends ASTNodeAccessImpl {

    public enum ColumnsTransformerType {
        APPLY, EXCEPT, EXCLUDE, REPLACE, RENAME
    }

    private ColumnsTransformerType type;
    private Expression applyExpression;
    private ParenthesedExpressionList<Column> exceptColumns;
    private StringValue exceptPattern;
    private List<SelectItem<?>> replaceItems;
    private List<SelectItem<?>> renameItems;
    private boolean parenthesized = true;
    private boolean strict;

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
        if (exceptColumns != null) {
            this.exceptPattern = null;
        }
        return this;
    }

    /**
     * The re2 pattern of the ClickHouse {@code EXCEPT 'pattern'} form, mutually exclusive with
     * {@link #getExceptColumns()}.
     */
    public StringValue getExceptPattern() {
        return exceptPattern;
    }

    public ColumnsTransformer setExceptPattern(StringValue exceptPattern) {
        this.exceptPattern = exceptPattern;
        if (exceptPattern != null) {
            this.exceptColumns = null;
        }
        return this;
    }

    /**
     * The {@code STRICT} modifier of ClickHouse {@code EXCEPT}/{@code REPLACE} transformers: named
     * columns must exist, otherwise the database rejects the query. Meaningless for {@code APPLY}
     * and {@code EXCLUDE}.
     */
    public boolean isStrict() {
        return strict;
    }

    public ColumnsTransformer setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public List<SelectItem<?>> getReplaceItems() {
        return replaceItems;
    }

    public ColumnsTransformer setReplaceItems(List<SelectItem<?>> replaceItems) {
        this.replaceItems = replaceItems;
        return this;
    }

    /** DuckDB's {@code RENAME (column AS name, ...)}: each item is a column with its new name. */
    public List<SelectItem<?>> getRenameItems() {
        return renameItems;
    }

    public ColumnsTransformer setRenameItems(List<SelectItem<?>> renameItems) {
        this.renameItems = renameItems;
        return this;
    }

    /**
     * Whether the columns or items of an {@code EXCLUDE}, {@code REPLACE} or {@code RENAME} are
     * parenthesized; DuckDB also accepts a single one without parentheses, e.g.
     * {@code * EXCLUDE i}.
     */
    public boolean isParenthesized() {
        return parenthesized;
    }

    public ColumnsTransformer setParenthesized(boolean parenthesized) {
        this.parenthesized = parenthesized;
        return this;
    }

    void collectExpressions(List<Expression> expressions) {
        switch (type) {
            case APPLY:
                if (applyExpression != null) {
                    expressions.add(applyExpression);
                }
                break;
            case EXCEPT:
                if (exceptPattern != null) {
                    expressions.add(exceptPattern);
                }
                if (exceptColumns != null) {
                    expressions.addAll(exceptColumns);
                }
                break;
            case EXCLUDE:
                if (exceptColumns != null) {
                    expressions.addAll(exceptColumns);
                }
                break;
            case REPLACE:
                if (replaceItems != null) {
                    for (SelectItem<?> item : replaceItems) {
                        expressions.add(item.getExpression());
                    }
                }
                break;
            case RENAME:
                if (renameItems != null) {
                    for (SelectItem<?> item : renameItems) {
                        expressions.add(item.getExpression());
                    }
                }
                break;
            default:
                throw new IllegalStateException("Unhandled ColumnsTransformerType: " + type);
        }
    }

    public StringBuilder appendTo(StringBuilder builder) {
        switch (type) {
            case APPLY:
                builder.append("APPLY(").append(applyExpression).append(")");
                break;
            case EXCEPT:
                builder.append("EXCEPT").append(strict ? " STRICT" : "").append(" ")
                        .append(exceptPattern != null ? exceptPattern : exceptColumns);
                break;
            case EXCLUDE:
                builder.append("EXCLUDE ")
                        .append(parenthesized ? exceptColumns : exceptColumns.get(0));
                break;
            case REPLACE:
                builder.append("REPLACE").append(strict ? " STRICT" : "")
                        .append(parenthesized ? "(" : " ")
                        .append(Select.getStringList(replaceItems))
                        .append(parenthesized ? ")" : "");
                break;
            case RENAME:
                builder.append("RENAME ").append(parenthesized ? "(" : "")
                        .append(Select.getStringList(renameItems))
                        .append(parenthesized ? ")" : "");
                break;
            default:
                throw new IllegalStateException("Unhandled ColumnsTransformerType: " + type);
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
