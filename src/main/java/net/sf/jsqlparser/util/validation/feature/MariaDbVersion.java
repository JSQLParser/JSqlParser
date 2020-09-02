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
 * @see https://mariadb.com/kb/en/sql-statements-structure/
 */
public enum MariaDbVersion implements Version {
    V10_5_4("10.5.4",
            EnumSet.of(// supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // common features
                    // https://mariadb.com/kb/en/select/
                    Feature.select,
                    Feature.selectGroupBy, Feature.selectHaving,
                    Feature.limit, Feature.limitOffset, Feature.offset, Feature.offsetParam,
                    Feature.orderBy,
                    Feature.selectForUpdate, Feature.selectForUpdateWait, Feature.selectForUpdateNoWait,

                    // https://mariadb.com/kb/en/join-syntax/
                    Feature.join, Feature.joinSimple, Feature.joinRight, Feature.joinNatural, Feature.joinLeft,
                    Feature.joinCross, Feature.joinOuter, Feature.joinInner, Feature.joinStraight,
                    Feature.joinUsingColumns,

                    // https://mariadb.com/kb/en/select/#distinct
                    Feature.distinct,

                    Feature.setOperation,
                    // https://mariadb.com/kb/en/union/
                    Feature.setOperationUnion,
                    // https://mariadb.com/kb/en/intersect/
                    Feature.setOperationIntersect,
                    // https://mariadb.com/kb/en/except/
                    Feature.setOperationExcept,

                    // https://mariadb.com/kb/en/insert/
                    Feature.insert, Feature.insertValues,
                    Feature.insertFromSelect, Feature.insertModifierPriority, Feature.insertModifierIgnore,
                    Feature.insertUseSet, Feature.insertUseDuplicateKeyUpdate, Feature.insertReturningExpressionList,

                    // https://mariadb.com/kb/en/update/
                    Feature.update,
                    Feature.updateJoins,
                    Feature.updateOrderBy, Feature.updateLimit,

                    // https://mariadb.com/kb/en/delete/
                    Feature.delete,
                    Feature.deleteTables,
                    Feature.deleteLimit, Feature.deleteOrderBy,

                    // https://mariadb.com/kb/en/truncate-table/
                    Feature.truncate,

                    // https://mariadb.com/kb/en/drop/
                    Feature.drop,

                    // https://mariadb.com/kb/en/replace/
                    Feature.replace,

                    // https://mariadb.com/kb/en/alter/
                    Feature.alter,
                    // https://mariadb.com/kb/en/alter-sequence/
                    Feature.alterSequence,
                    // https://mariadb.com/kb/en/alter-view/
                    Feature.alterView,
                    // https://mariadb.com/kb/en/create-view/
                    Feature.createView,
                    // https://mariadb.com/kb/en/create-table/
                    Feature.createTable,
                    // https://mariadb.com/kb/en/create-index/
                    Feature.createIndex,
                    // https://mariadb.com/kb/en/create-sequence/
                    Feature.createSequence,
                    // https://mariadb.com/kb/en/create-database/
                    Feature.createSchema,
                    // https://mariadb.com/kb/en/describe/
                    Feature.describe,
                    // https://mariadb.com/kb/en/explain/
                    Feature.explain, Feature.show,
                    // https://mariadb.com/kb/en/show-columns/
                    Feature.showColumns,
                    // https://mariadb.com/kb/en/use/
                    Feature.use,
                    // https://mariadb.com/kb/en/grant/
                    Feature.grant,
                    // https://mariadb.com/kb/en/optimizer-hints/
                    Feature.mySqlHintStraightJoin,
                    Feature.mysqlCalcFoundRows, Feature.mysqlSqlNoCache)),

    ORACLE_MODE("oracle_mode", EnumSet.of(Feature.selectUnique));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private MariaDbVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private MariaDbVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.MARIADB.getName() + " " + getVersionString();
    }

}
