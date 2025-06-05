/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;

public class FBVColumn {
    private boolean precedesComma;
    private String key;
    private Expression value;
    private String stringValue;

    private FBVColumn(String key, Expression value) {
        this.key = key;
        this.value = value;
        this.stringValue = null;
    }

    public FBVColumn(String key, String value) {
        this.key = key;
        this.value = null;
        this.stringValue = value;
    }

    public FBVColumn(String key, StringValue value) {
        this(key, (Expression) value);
    }

    public FBVColumn(String key, LongValue value) {
        this(key, (Expression) value);
    }

    public boolean precedesComma() {
        return precedesComma;
    }

    public void setPrecedesComma(boolean precedesComma) {
        this.precedesComma = precedesComma;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Expression getValue() {
        return value;
    }

    public void setValue(String value) {
        this.stringValue = value;
        this.value = null;
    }

    private void setValue(Expression value) {
        this.value = value;
        this.stringValue = null;
    }

    public void setValue(StringValue value) {
        setValue((Expression) value);
    }

    public void setValue(LongValue value) {
        setValue((Expression) value);
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        if (precedesComma) {
            sql.append(", ");
        }

        sql.append(key);
        sql.append(" = ");
        if (stringValue != null) {
            sql.append(stringValue);
        } else {
            sql.append(value);
        }

        return sql.toString();
    }
}
