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

public class FileOption {
    private String key;
    private Expression value;

    private FileOption(String key, Expression value) {
        this.key = key;
        this.value = value;
    }

    public FileOption(String key) {
        this(key, (Expression) null);
    }

    public FileOption(String key, StringValue value) {
        this(key, (Expression) value);
    }

    public FileOption(String key, LongValue value) {
        this(key, (Expression) value);
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

    public void setValue(StringValue value) {
        this.value = value;
    }

    public void setValue(LongValue value) {
        this.value = value;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(key);
        if (value != null) {
            sql.append(" = ");
            sql.append(value);
        }

        return sql.toString();
    }
}
