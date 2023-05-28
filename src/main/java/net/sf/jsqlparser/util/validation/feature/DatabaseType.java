/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.feature;

import java.util.EnumSet;
import java.util.Set;
import net.sf.jsqlparser.parser.feature.Feature;

/**
 * <p>
 * The DatabaseType is named like the identifier used within the
 * jdbc-connection-url (upper case), this may change in future, therefore use
 * {@link #get(String)} to retrieve the {@link DatabaseType}.
 * </p>
 */
public enum DatabaseType implements FeatureSetValidation, Version {

    ANSI_SQL("ANSI SQL", SQLVersion.values()),
    // DBMS
    ORACLE(OracleVersion.values()),
    MYSQL(MySqlVersion.values()),
    SQLSERVER(SqlServerVersion.values()),
    MARIADB(MariaDbVersion.values()),
    POSTGRESQL(PostgresqlVersion.values()),
    H2(H2Version.values());

    public static final DatabaseType[] DATABASES = new DatabaseType[] { ORACLE, MYSQL, SQLSERVER, MARIADB, POSTGRESQL, H2 };

    private String name;

    private Version[] versions;

    /**
     * @param versions - ordered ascending - the last version is the latest.
     */
    DatabaseType(Version... versions) {
        this.versions = versions;
    }

    /**
     * @param versions - ordered ascending - the last version is the latest.
     */
    DatabaseType(String name, Version... versions) {
        this.name = name;
        this.versions = versions;
    }

    /**
     * @param jdbcIdentifier - the database-identifier-part of jdbc-url
     * @return the {@link DatabaseType}
     * @throws IllegalArgumentException - if the specified jdbcIdentifier cannot be mapped to a {@link DatabaseType}
     * @throws NullPointerException if {@code jdbcIdentifier} is null
     */
    public static DatabaseType get(String jdbcIdentifier) {
        return DatabaseType.valueOf(jdbcIdentifier.toUpperCase());
    }

    /**
     */
    @Override
    public String getName() {
        return name == null ? name() : name;
    }

    /**
     * @return the features supported by the latest version.
     */
    @Override
    public Set<Feature> getFeatures() {
        if (versions.length > 0) {
            return versions[versions.length - 1].getFeatures();
        } else {
            return EnumSet.noneOf(Feature.class);
        }
    }

    /**
     * @return the latest version-string.
     */
    @Override
    public String getVersionString() {
        if (versions.length > 0) {
            return versions[versions.length - 1].getVersionString();
        } else {
            return null;
        }
    }
}
