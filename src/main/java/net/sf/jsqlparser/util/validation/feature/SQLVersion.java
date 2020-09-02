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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import net.sf.jsqlparser.parser.feature.Feature;

/**
 * Enum containing the ANSI SQL Standard Versions - features are not guaranteed
 * to be complete, just add them if you are sure they are part of the standard
 * :)
 *
 * @author gitmotte
 * @see https://en.wikipedia.org/wiki/SQL#Interoperability_and_standardization
 */
public enum SQLVersion implements Version {
    SQL1986("SQL-86", EnumSet.of(
            // supported if used with jdbc
            Feature.jdbcParameter,
            Feature.jdbcNamedParameter,
            // common features
            Feature.select,
            Feature.selectGroupBy,
            Feature.insert,
            Feature.insertValues,
            Feature.update,
            Feature.delete,
            Feature.truncate,
            Feature.drop,
            Feature.alter)), //
    SQL1989("SQL-89", SQL1986.getFeaturesClone()), //
    SQL1992("SQL-92", SQL1989.getFeaturesClone()), //
    SQL1999("SQL:1999", SQL1992.getFeaturesClone()), //
    SQL2003("SQL:2003", SQL1999.getFeaturesClone()), //
    SQL2006("SQL:2006", SQL2003.getFeaturesClone()), //
    SQL2008("SQL:2008", SQL2006.getFeaturesClone()), //
    SQL2011("SQL:2011", SQL2008.getFeaturesClone()), //
    SQL2016("SQL:2016", SQL2011.getFeaturesClone()), //
    SQL2019("SQL:2019", SQL2016.getFeaturesClone());

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private SQLVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private SQLVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
        this.versionString = versionString;
        this.features = featuresSupported;
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

    @Override
    public String getName() {
        return DatabaseType.SQLSERVER.getName() + " " + getVersionString();
    }

}
