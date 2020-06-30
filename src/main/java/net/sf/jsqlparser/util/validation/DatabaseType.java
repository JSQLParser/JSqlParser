package net.sf.jsqlparser.util.validation;

/**
 * <p>
 * The DatabaseType is named like the identifier used within the
 * jdbc-connection-url, this may change in future, therefore use
 * {@link #get(String)} to retrieve the {@link DatabaseType}.
 * </p>
 */
public enum DatabaseType {

    oracle, mysql, sqlserver;

    /**
     * @param jdbcIdentifier - the database-identifier-part of jdbc-url
     * @return the {@link DatabaseType}
     * @throws IllegalArgumentException - if the specified jdbcIdentifier cannot be mapped to a {@link DatabaseType}
     */
    public static DatabaseType get(String jdbcIdentifier) {
        return DatabaseType.valueOf(jdbcIdentifier);
    }

}
