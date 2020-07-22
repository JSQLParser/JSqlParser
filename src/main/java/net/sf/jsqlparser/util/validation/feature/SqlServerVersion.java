package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum SqlServerVersion implements Version {
    V2019("2019",
            EnumSet.of(Feature.select, Feature.top, Feature.use, Feature.allowSquareBracketQuotation, //
                    Feature.pivot, Feature.unpivot, Feature.pivotXml));

    private Set<Feature> features;
    private String versionString;

    private SqlServerVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private SqlServerVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.SQLSERVER.getName() + " " + getVersionString();
    }

}