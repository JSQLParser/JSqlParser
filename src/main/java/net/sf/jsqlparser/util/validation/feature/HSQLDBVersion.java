package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum HSQLDBVersion implements Version {
    V_2_5_0("2.5.0",
            EnumSet.of(
                    Feature.select,
                    Feature.insert,
                    Feature.update,
                    Feature.delete,
                    Feature.truncate,
                    Feature.drop,
                    Feature.alter));

    private Set<Feature> features;
    private String versionString;

    private HSQLDBVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private HSQLDBVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.h2.name() + " " + name();
    }

}