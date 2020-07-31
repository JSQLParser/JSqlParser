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
 * Please add Features supported and place a link to public documentation
 * 
 * @author gitmotte
 */
public enum OracleVersion implements Version {
    V19C("19c",
            EnumSet.of(
                    // supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // common features
                    Feature.select,
                    Feature.insert,
                    Feature.update,
                    Feature.delete,
                    Feature.truncate,
                    Feature.drop,
                    Feature.alter,

                    // https://www.oracletutorial.com/oracle-basics/oracle-select-distinct/
                    Feature.distinct, Feature.selectUnique,

                    // https://www.oracletutorial.com/oracle-basics/oracle-merge/
                    Feature.merge,

                    // special oracle features
                    Feature.oracleOldJoinSyntax,
                    Feature.oraclePriorPosition,
                    Feature.oracleHint,
                    Feature.oracleHierarchicalExpression,
                    Feature.oracleOrderBySiblings));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private OracleVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private OracleVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.ORACLE.getName() + " " + getVersionString();
    }

}
