package net.sf.jsqlparser.util.validation.feature;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.sf.jsqlparser.parser.feature.Feature;

public enum PostgresqlVersion implements Version {
    V10("10",
            EnumSet.of(
                    // https://www.postgresql.org/docs/current/sql-select.html
                    Feature.select,
                    Feature.selectGroupBy,
                    // https://www.postgresql.org/docs/current/queries-limit.html
                    Feature.limit,
                    Feature.limitAll, //
                    Feature.limitNull,
                    Feature.offset,
                    Feature.distinct,
                    Feature.distinctOn,
                    Feature.selectHaving,
                    Feature.window,
                    Feature.orderBy,
                    Feature.orderByNullOrdering,
                    Feature.selectForUpdate,
                    Feature.selectForUpdateOfTable,
                    Feature.selectForUpdateNoWait,

                    // https://www.postgresql.org/docs/current/sql-insert.html
                    Feature.insert,
                    // https://www.postgresql.org/docs/current/sql-update.html
                    Feature.update,
                    // https://www.postgresql.org/docs/current/sql-delete.html
                    Feature.delete,
                    // https://www.postgresql.org/docs/current/sql-truncate.html
                    Feature.truncate,
                    // https://www.postgresql.org/docs/current/sql-droptable.html
                    // https://www.postgresql.org/docs/current/sql-dropindex.html
                    Feature.drop,
                    // https://www.postgresql.org/docs/current/sql-altertable.html
                    Feature.alter)),
    V11("11", V10.getFeatures()),
    V12("12", V11.getFeatures());

    private Set<Feature> features;
    private String versionString;

    private PostgresqlVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    private PostgresqlVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.POSTGRESQL.getName() + " " + getVersionString();
    }

}