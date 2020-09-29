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
 * @see <a href=
 *      "https://mariadb.com/kb/en/sql-statements-structure/">https://mariadb.com/kb/en/sql-statements-structure/</a>
 */
public enum MariaDbVersion implements Version {
    V10_5_4("10.5.4",
            EnumSet.of(// supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // expressions
                    Feature.exprLike,
                    // https://mariadb.com/kb/en/select/
                    Feature.select,
                    Feature.selectGroupBy, Feature.function,
                    Feature.selectHaving,
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

                    // https://mariadb.com/kb/en/common-table-expressions/
                    // https://mariadb.com/kb/en/with/
                    // https://mariadb.com/kb/en/non-recursive-common-table-expressions-overview/
                    // https://mariadb.com/kb/en/recursive-common-table-expressions-overview/
                    Feature.withItem, Feature.withItemRecursive,

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
                    Feature.deleteJoin, Feature.deleteTables,
                    Feature.deleteLimit, Feature.deleteOrderBy,

                    // https://mariadb.com/kb/en/truncate-table/
                    Feature.truncate,

                    // https://mariadb.com/kb/en/call/
                    Feature.execute, Feature.executeCall,

                    // https://mariadb.com/kb/en/drop/
                    Feature.drop,
                    // https://mariadb.com/kb/en/drop-index/
                    Feature.dropIndex,
                    // https://mariadb.com/kb/en/drop-table/
                    Feature.dropTable,
                    // https://mariadb.com/kb/en/drop-database/
                    // SCHEMA = DATABASE
                    Feature.dropSchema,
                    // https://mariadb.com/kb/en/drop-view/
                    Feature.dropView,
                    // https://mariadb.com/kb/en/drop-sequence/
                    Feature.dropSequence, Feature.dropTableIfExists, Feature.dropIndexIfExists,
                    Feature.dropViewIfExists, Feature.dropSchemaIfExists, Feature.dropSequenceIfExists,

                    // https://mariadb.com/kb/en/replace/
                    Feature.replace,

                    // https://mariadb.com/kb/en/alter/
                    Feature.alterTable,
                    // https://mariadb.com/kb/en/alter-sequence/
                    Feature.alterSequence,
                    // https://mariadb.com/kb/en/alter-view/
                    Feature.alterView,
                    // https://mariadb.com/kb/en/create-view/
                    Feature.createView,
                    Feature.createOrReplaceView,

                    // https://mariadb.com/kb/en/create-table/
                    Feature.createTable, Feature.createTableCreateOptionStrings, Feature.createTableTableOptionStrings,
                    Feature.createTableFromSelect, Feature.createTableIfNotExists,
                    // https://mariadb.com/kb/en/create-index/
                    Feature.createIndex,
                    // https://mariadb.com/kb/en/create-sequence/
                    Feature.createSequence,
                    // https://mariadb.com/kb/en/create-database/
                    Feature.createSchema,
                    // https://mariadb.com/kb/en/create-trigger/
                    Feature.createTrigger,

                    // https://mariadb.com/kb/en/describe/
                    Feature.describe,
                    // https://mariadb.com/kb/en/explain/
                    Feature.explain,
                    // https://mariadb.com/kb/en/show/
                    Feature.show,
                    // https://mariadb.com/kb/en/show-tables/
                    Feature.showTables,
                    // https://mariadb.com/kb/en/show-columns/
                    Feature.showColumns,
                    // https://mariadb.com/kb/en/use/
                    Feature.use,
                    // https://mariadb.com/kb/en/grant/
                    Feature.grant,
                    // https://mariadb.com/kb/en/commit/
                    Feature.commit,
                    // https://mariadb.com/kb/en/optimizer-hints/
                    Feature.mySqlHintStraightJoin,
                    Feature.mysqlCalcFoundRows, Feature.mysqlSqlNoCache)),

    ORACLE_MODE("oracle_mode", V10_5_4.copy().add(Feature.selectUnique).getFeatures());

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #copy() to copy from previous version
     */
    private MariaDbVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #copy() to copy from previous version
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
