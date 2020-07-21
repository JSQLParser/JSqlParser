package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum H2Version implements Version {
    V_1_4_200("1.4.200",
            EnumSet.of(
                    // http://h2database.com/html/commands.html#select
                    Feature.select,
                    // http://h2database.com/html/commands.html#insert
                    Feature.insert,
                    // http://h2database.com/html/commands.html#update
                    Feature.update,
                    // http://h2database.com/html/commands.html#delete
                    Feature.delete,
                    // http://h2database.com/html/commands.html#truncate_table
                    Feature.truncate,
                    // http://h2database.com/html/commands.html#drop_table
                    // http://h2database.com/html/commands.html#drop_index
                    // ...
                    Feature.drop,
                    // http://h2database.com/html/commands.html#alter_table_add
                    // http://h2database.com/html/commands.html#alter_table_add_constraint
                    // ...
                    Feature.alter));

    private Set<Feature> features;
    private String versionString;

    private H2Version(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private H2Version(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.H2.name() + " " + name();
    }

}