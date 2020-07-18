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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.sf.jsqlparser.parser.feature.Feature;

/**
 * <p>
 * The DatabaseType is named like the identifier used within the
 * jdbc-connection-url, this may change in future, therefore use
 * {@link #get(String)} to retrieve the {@link DatabaseType}.
 * </p>
 */
public enum DatabaseType implements FeatureSetValidation {

    oracle, mysql, sqlserver, mariadb, postgresql(PostgresqlVersion.values()), h2, hsqldb;

    private Version[] versions;

    /**
     * @param versions - ordered ascending - the last version is the latest.
     */
    private DatabaseType(Version... versions) {
        this.versions = versions;
    }

    /**
     * @param jdbcIdentifier - the database-identifier-part of jdbc-url
     * @return the {@link DatabaseType}
     * @throws IllegalArgumentException - if the specified jdbcIdentifier cannot be mapped to a {@link DatabaseType}
     * @throws NullPointerException if {@code jdbcIdentifier} is null
     */
    public static DatabaseType get(String jdbcIdentifier) {
        return DatabaseType.valueOf(jdbcIdentifier.toLowerCase());
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

    public interface Version extends FeatureSetValidation {
        public String getVersionString();
    }

    public enum PostgresqlVersion implements Version {
        V11("11", EnumSet.of(Feature.select, Feature.insert, Feature.update, Feature.delete, Feature.upsert)), //
        V12("12", V11.getFeatures());

        private Set<Feature> features;
        private String versionString;

        private PostgresqlVersion(String versionString, Set<Feature> featuresSupported) {
            this(versionString, featuresSupported, Collections.emptySet());
        }

        private PostgresqlVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
            this.versionString = versionString;
            this.features = new HashSet<>(featuresSupported);
            this.features.removeAll(unsupported);
        }

        @Override
        public String getVersionString() {
            return versionString;
        }

        @Override
        public Set<Feature> getFeatures() {
            return features;
        }

    }


}
