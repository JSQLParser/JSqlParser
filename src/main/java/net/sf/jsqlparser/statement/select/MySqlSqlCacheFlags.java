/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

/**
 * @author tw
 */
public enum MySqlSqlCacheFlags {
    SQL_CACHE, SQL_NO_CACHE;

    public static MySqlSqlCacheFlags from(String flag) {
        return Enum.valueOf(MySqlSqlCacheFlags.class, flag.toUpperCase());
    }
}
