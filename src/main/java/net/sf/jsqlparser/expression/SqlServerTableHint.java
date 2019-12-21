/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Example:
 * <code>SELECT 1 FROM tableName1 AS t1 WITH (INDEX (idx1), NOLOCK) JOIN tableName2 AS t2 WITH (INDEX (idx2)) ON t1.id = t2.id</code>
 */
public class SqlServerTableHint {
    private SqlServerTableHintType hintType;
    private String indexName;

    public SqlServerTableHint(SqlServerTableHintType hintType, String indexName) {
        this.hintType = hintType;
        this.indexName = indexName;
    }

    public static SqlServerTableHint createIndexHint(SqlServerTableHintType hintType, String indexName) {
        return new SqlServerTableHint(hintType, indexName);
    }

    public static SqlServerTableHint createHint(String hintType) {
        return new SqlServerTableHint(SqlServerTableHintType.valueOf(hintType), null);
    }

    public static SqlServerTableHint createHint(SqlServerTableHintType hintType) {
        return new SqlServerTableHint(hintType, null);
    }

    public SqlServerTableHintType getHintType() {
        return hintType;
    }

    public void setHintType(SqlServerTableHintType hintType) {
        this.hintType = hintType;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    @Override
    public String toString() {
        return hintType +
                (indexName == null ? "" : "(" + indexName + ")");
    }

    public static String toString(Collection<SqlServerTableHint> hints) {
        if (hints == null) {
            return "";
        }
        return " WITH (" + hints.stream().map(SqlServerTableHint::toString).collect(Collectors.joining(", ")) + ")";
    }
}
