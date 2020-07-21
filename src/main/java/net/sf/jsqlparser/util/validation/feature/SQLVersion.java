package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
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
    SQL1986("SQL-86", EnumSet.of(Feature.select)), //
    SQL1989("SQL-89", EnumSet.of(Feature.select)), //
    SQL1992("SQL-92", EnumSet.of(Feature.select)), //
    SQL1999("SQL:1999", EnumSet.of(Feature.select)), //
    SQL2003("SQL:2003", EnumSet.of(Feature.select)), //
    SQL2006("SQL:2006", EnumSet.of(Feature.select)), //
    SQL2008("SQL:2008", EnumSet.of(Feature.select)), //
    SQL2011("SQL:2011", EnumSet.of(Feature.select)), //
    SQL2016("SQL:2016", EnumSet.of(Feature.select)), //
    SQL2019("SQL:2019", EnumSet.of(Feature.select));

    private Set<Feature> features;
    private String versionString;

    private SQLVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private SQLVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.SQLSERVER.name() + " " + name();
    }

}
