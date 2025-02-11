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

import net.sf.jsqlparser.expression.StringValue;

public class CSVColumn {
    private Long startIndex;
    private Long endIndex;
    private StringValue format;
    private String delimit;

    public CSVColumn(Long startIndex, Long endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public CSVColumn(Long index) {
        this(index, null);
    }

    public Long getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(Long startIndex) {
        this.startIndex = startIndex;
    }

    public Long getIndex() {
        return getStartIndex();
    }

    public void setIndex(Long index) {
        setStartIndex(index);
    }

    public Long getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(Long endIndex) {
        this.endIndex = endIndex;
    }

    public StringValue getFormat() {
        return format;
    }

    public void setFormat(StringValue format) {
        this.format = format;
    }

    public String getDelimit() {
        return delimit;
    }

    public void setDelimit(String delimit) {
        this.delimit = delimit;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append(startIndex);
        if (endIndex != null) {
            sql.append(" .. ");
            sql.append(endIndex);
        } else if (format != null || delimit != null) {
            if (format != null) {
                sql.append(" FORMAT = ");
                sql.append(format);
            }

            if (delimit != null) {
                sql.append(" DELIMIT = ");
                sql.append(delimit);
            }
        }

        return sql.toString();
    }
}
