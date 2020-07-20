package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum OracleVersion implements Version {
    _19c("19c",
            EnumSet.of(
                    Feature.oracleOldJoinSyntax,
                    Feature.oraclePriorPosition,
                    Feature.oracleHint,
                    Feature.oracleHierarchicalExpression,
                    Feature.oracleOrderBySiblings));

    private Set<Feature> features;
    private String versionString;

    private OracleVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private OracleVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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

    @Override
    public String getName() {
        return DatabaseType.oracle.name() + " " + name();
    }

}