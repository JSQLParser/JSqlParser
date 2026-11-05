/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * A {@code DO} statement: a PostgreSQL anonymous routine, whose language-specific body remains a
 * string literal, or MySQL's {@code DO expr [, expr] ...}, which evaluates expressions without
 * returning a result. MySQL reads the expressions as a select list, so they can carry aliases.
 *
 * @see <a href="https://www.postgresql.org/docs/18/sql-do.html">PostgreSQL DO</a>
 * @see <a href="https://dev.mysql.com/doc/refman/8.4/en/do.html">MySQL DO</a>
 */
public class DoStatement extends ASTNodeAccessImpl implements Statement {
    private StringValue code;
    private String language;
    private boolean languageBeforeCode;
    private List<SelectItem<?>> selectItems;

    public StringValue getCode() {
        return code;
    }

    public void setCode(StringValue code) {
        this.code = code;
    }

    public DoStatement withCode(StringValue code) {
        setCode(code);
        return this;
    }

    /** Returns the explicit language, or null when omitted. */
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public DoStatement withLanguage(String language) {
        setLanguage(language);
        return this;
    }

    public boolean isLanguageBeforeCode() {
        return languageBeforeCode;
    }

    public void setLanguageBeforeCode(boolean languageBeforeCode) {
        this.languageBeforeCode = languageBeforeCode;
    }

    /** The expressions of MySQL's form, or null for a PostgreSQL code block. */
    public List<SelectItem<?>> getSelectItems() {
        return selectItems;
    }

    public void setSelectItems(List<SelectItem<?>> selectItems) {
        this.selectItems = selectItems;
    }

    public DoStatement withSelectItems(List<SelectItem<?>> selectItems) {
        setSelectItems(selectItems);
        return this;
    }

    /** Visits the code block, or the expression of every select item. */
    public void visitExpressions(Consumer<Expression> visitor) {
        if (selectItems != null) {
            for (SelectItem<?> item : selectItems) {
                visitor.accept(item.getExpression());
            }
        } else if (code != null) {
            visitor.accept(code);
        }
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<StringValue> codePrinter) {
        return appendTo(builder, codePrinter, builder::append);
    }

    /** Renders the code block or the select items through the caller's writers. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<StringValue> codePrinter,
            Consumer<SelectItem<?>> itemPrinter) {
        builder.append("DO ");
        if (selectItems != null) {
            for (int i = 0; i < selectItems.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                itemPrinter.accept(selectItems.get(i));
            }
            return builder;
        }
        if (languageBeforeCode && language != null) {
            builder.append("LANGUAGE ").append(language).append(' ');
        }
        codePrinter.accept(code);
        if (!languageBeforeCode && language != null) {
            builder.append(" LANGUAGE ").append(language);
        }
        return builder;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, value -> builder.append(value)).toString();
    }
}
