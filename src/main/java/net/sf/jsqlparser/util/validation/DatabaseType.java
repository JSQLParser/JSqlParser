/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

/**
 * <p>
 * The DatabaseType is named like the identifier used within the
 * jdbc-connection-url, this may change in future, therefore use
 * {@link #get(String)} to retrieve the {@link DatabaseType}.
 * </p>
 */
public enum DatabaseType {

    oracle, mysql, sqlserver, mariadb, postgresql, h2, hsqldb;

    /**
     * @param jdbcIdentifier - the database-identifier-part of jdbc-url
     * @return the {@link DatabaseType}
     * @throws IllegalArgumentException - if the specified jdbcIdentifier cannot be mapped to a {@link DatabaseType}
     * @throws NullPointerException if {@code jdbcIdentifier} is null
     */
    public static DatabaseType get(String jdbcIdentifier) {
        return DatabaseType.valueOf(jdbcIdentifier.toLowerCase());
    }

}
