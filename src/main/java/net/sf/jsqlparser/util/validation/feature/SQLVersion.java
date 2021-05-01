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
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/SQL#Interoperability_and_standardization">https://en.wikipedia.org/wiki/SQL#Interoperability_and_standardization</a>
 */
public enum SQLVersion implements Version {
    SQL1986("SQL-86", EnumSet.of(
            // supported if used with jdbc
            Feature.jdbcParameter,
            Feature.jdbcNamedParameter,
            // common features
            Feature.select,
            Feature.selectGroupBy, Feature.function,
            Feature.insert,
            Feature.insertValues,
            Feature.update,
            Feature.delete,
            Feature.truncate,
            Feature.drop,
            Feature.alterTable)), //
    SQL1989("SQL-89", SQL1986.copy().getFeatures()), //
    SQL1992("SQL-92", SQL1989.copy().getFeatures()), //
    SQL1999("SQL:1999", SQL1992.copy().add(Feature.exprSimilarTo).getFeatures()), //
    SQL2003("SQL:2003", SQL1999.copy().getFeatures()), //
    SQL2006("SQL:2006", SQL2003.copy().getFeatures()), //
    SQL2008("SQL:2008", SQL2006.copy().getFeatures()), //
    SQL2011("SQL:2011", SQL2008.copy().getFeatures()), //
    SQL2016("SQL:2016", SQL2011.copy().getFeatures()), //
    SQL2019("SQL:2019", SQL2016.copy().getFeatures());

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #copy() to copy from previous version
     */
    SQLVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #copy() to copy from previous version
     */
    SQLVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
