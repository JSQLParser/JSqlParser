package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum MySqlVersion implements Version {
    V8_0_21("8.0.x",
            EnumSet.of(
                    Feature.mySqlHintStraightJoin,
                    Feature.mysqlSqlNoCache,
                    Feature.mysqlCalcFoundRows));

    private Set<Feature> features;
    private String versionString;

    private MySqlVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private MySqlVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.MYSQL.name() + " " + name();
    }

}