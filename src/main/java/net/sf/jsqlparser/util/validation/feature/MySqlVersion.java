package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

/**
 * 
 * @author gitmotte
 * @see https://dev.mysql.com/doc/refman/8.0/en/
 */
public enum MySqlVersion implements Version {
    V8_0("8.0",
            EnumSet.of(
                    Feature.select,
                    Feature.limit,
                    Feature.offset, Feature.limitOffset, Feature.offset,
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