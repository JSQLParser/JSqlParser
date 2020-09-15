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
 * @see https://dev.mysql.com/doc/refman/8.0/en/
 */
public enum MySqlVersion implements Version {
    V8_0("8.0",
            EnumSet.of(
                    // supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // expressions
                    Feature.exprLike,
                    // https://dev.mysql.com/doc/refman/8.0/en/select.html
                    Feature.select,
                    Feature.selectGroupBy, Feature.selectHaving,
                    Feature.limit, Feature.limitOffset, Feature.offset, Feature.offsetParam, Feature.orderBy,
                    Feature.selectForUpdate, Feature.selectForUpdateOfTable, Feature.selectForUpdateNoWait,
                    Feature.distinct,

                    Feature.setOperation,
                    // https://dev.mysql.com/doc/refman/8.0/en/union.html
                    Feature.setOperationUnion,

                    // https://dev.mysql.com/doc/refman/8.0/en/with.html#common-table-expressions
                    Feature.withItem, Feature.withItemRecursive,

                    // https://dev.mysql.com/doc/refman/8.0/en/sql-function-reference.html
                    Feature.function,

                    // https://dev.mysql.com/doc/refman/8.0/en/join.html
                    Feature.join, Feature.joinSimple, Feature.joinLeft, Feature.joinRight, Feature.joinOuter,
                    Feature.joinNatural, Feature.joinInner, Feature.joinCross, Feature.joinStraight,
                    Feature.joinUsingColumns,

                    // https://dev.mysql.com/doc/refman/8.0/en/insert.html
                    Feature.insert,
                    Feature.insertValues,
                    Feature.insertFromSelect, Feature.insertUseSet, Feature.insertModifierPriority,
                    Feature.insertModifierIgnore, Feature.insertUseDuplicateKeyUpdate,

                    // https://dev.mysql.com/doc/refman/8.0/en/update.html
                    Feature.update, Feature.updateJoins, Feature.updateOrderBy, Feature.updateLimit,

                    // https://dev.mysql.com/doc/refman/8.0/en/replace.html
                    Feature.replace,

                    // https://dev.mysql.com/doc/refman/8.0/en/delete.html
                    Feature.delete, Feature.deleteJoin, Feature.deleteTables, Feature.deleteLimit,
                    Feature.deleteOrderBy,

                    // https://dev.mysql.com/doc/refman/8.0/en/truncate-table.html
                    Feature.truncate,

                    Feature.drop,
                    // https://dev.mysql.com/doc/refman/8.0/en/drop-table.html
                    Feature.dropTable,
                    // https://dev.mysql.com/doc/refman/8.0/en/drop-index.html
                    Feature.dropIndex,
                    // https://dev.mysql.com/doc/refman/8.0/en/drop-view.html
                    Feature.dropView,
                    // https://dev.mysql.com/doc/refman/8.0/en/drop-database.html
                    Feature.dropSchema,
                    Feature.dropTableIfExists, Feature.dropViewIfExists,
                    Feature.dropSchemaIfExists, Feature.dropSequenceIfExists,

                    // https://dev.mysql.com/doc/refman/8.0/en/alter-table.html
                    Feature.alter,
                    // https://dev.mysql.com/doc/refman/8.0/en/alter-view.html
                    Feature.alterView,

                    // https://dev.mysql.com/doc/refman/8.0/en/create-database.html
                    Feature.createSchema,
                    // https://dev.mysql.com/doc/refman/8.0/en/create-view.html
                    Feature.createView,
                    // https://dev.mysql.com/doc/refman/8.0/en/create-table.html
                    Feature.createTable, Feature.createTableCreateOptionStrings, Feature.createTableTableOptionStrings,
                    Feature.createTableFromSelect, Feature.createTableIfNotExists,
                    // https://dev.mysql.com/doc/refman/8.0/en/create-index.html
                    Feature.createIndex,
                    // https://dev.mysql.com/doc/refman/8.0/en/create-trigger.html
                    Feature.createTrigger,

                    // https://dev.mysql.com/doc/refman/8.0/en/execute.html
                    Feature.execute,
                    // https://dev.mysql.com/doc/refman/8.0/en/describe.html
                    Feature.describe,
                    // https://dev.mysql.com/doc/refman/8.0/en/explain.html
                    Feature.explain,
                    // https://dev.mysql.com/doc/refman/8.0/en/show-tables.html
                    Feature.show,
                    // https://dev.mysql.com/doc/refman/8.0/en/show-columns.html
                    Feature.showColumns,
                    // https://dev.mysql.com/doc/refman/8.0/en/grant.html
                    Feature.grant,
                    // https://dev.mysql.com/doc/refman/8.0/en/use.html
                    Feature.use,
                    //
                    Feature.mySqlHintStraightJoin,
                    Feature.mysqlSqlNoCache,
                    Feature.mysqlCalcFoundRows));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #copy() to copy from previous version
     */
    private MySqlVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #copy() to copy from previous version
     */
    private MySqlVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.MYSQL.getName() + " " + name();
    }

}
