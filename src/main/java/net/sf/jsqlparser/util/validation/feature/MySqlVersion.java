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
 *      "https://dev.mysql.com/doc/refman/8.0/en/">https://dev.mysql.com/doc/refman/8.0/en/</a>
 */
public enum MySqlVersion implements Version {
    V8_0("8.0",
            EnumSet.of(// supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter, // expressions
                    Feature.exprLike, // https://dev.mysql.com/doc/refman/8.0/en/select.html
                    Feature.select, // https://dev.mysql.com/doc/refman/8.0/en/select-into.html
                    Feature.selectInto,
                    Feature.selectGroupBy, Feature.selectHaving,
                    Feature.limit, Feature.limitOffset, Feature.offset, Feature.offsetParam,
                    Feature.orderBy,
                    Feature.selectForUpdate,
                    Feature.selectForUpdateOfTable,
                    Feature.selectForUpdateNoWait,
                    Feature.selectForUpdateSkipLocked,
                    Feature.selectForShare,
                    Feature.distinct,

                    Feature.setOperation, // https://dev.mysql.com/doc/refman/8.0/en/union.html
                    Feature.setOperationUnion,

                    // https://dev.mysql.com/doc/refman/8.0/en/with.html#common-table-expressions
                    Feature.withItem, Feature.withItemRecursive,

                    // https://dev.mysql.com/doc/refman/8.0/en/sql-function-reference.html
                    Feature.function,

                    // https://dev.mysql.com/doc/refman/8.0/en/join.html
                    Feature.join, Feature.joinSimple, Feature.joinLeft, Feature.joinRight,
                    Feature.joinOuter,
                    Feature.joinNatural, Feature.joinInner, Feature.joinCross, Feature.joinStraight,
                    Feature.joinUsingColumns,

                    // https://dev.mysql.com/doc/refman/8.0/en/insert.html
                    Feature.insert,
                    Feature.insertValues,
                    Feature.values,
                    Feature.tableStatement,
                    Feature.insertFromSelect, Feature.insertUseSet, Feature.insertModifierPriority,
                    Feature.insertModifierIgnore, Feature.insertUseDuplicateKeyUpdate,

                    // https://dev.mysql.com/doc/refman/8.0/en/update.html
                    Feature.update, Feature.updateJoins, Feature.updateOrderBy, Feature.updateLimit,

                    // https://dev.mysql.com/doc/refman/8.0/en/replace.html
                    Feature.upsert,

                    // https://dev.mysql.com/doc/refman/8.0/en/delete.html
                    Feature.delete, Feature.deleteJoin, Feature.deleteTables, Feature.deleteLimit,
                    Feature.deleteOrderBy,

                    // https://dev.mysql.com/doc/refman/8.0/en/truncate-table.html
                    Feature.truncate,

                    // https://dev.mysql.com/doc/refman/8.0/en/table-maintenance-statements.html
                    Feature.analyze, Feature.tableMaintenance,

                    // https://dev.mysql.com/doc/refman/8.0/en/flush.html
                    // https://dev.mysql.com/doc/refman/8.0/en/kill.html
                    // https://dev.mysql.com/doc/refman/8.0/en/reset-persist.html
                    Feature.flush, Feature.kill, Feature.resetPersist,

                    // https://dev.mysql.com/doc/refman/8.0/en/handler.html
                    Feature.handler,

                    // https://dev.mysql.com/doc/refman/8.0/en/lock-tables.html
                    Feature.lock, Feature.unlockTables,

                    // https://dev.mysql.com/doc/refman/8.0/en/lock-instance-for-backup.html
                    Feature.backupLock,

                    // https://dev.mysql.com/doc/refman/8.0/en/resource-groups.html
                    Feature.createResourceGroup, Feature.alterResourceGroup,
                    Feature.setResourceGroup,

                    // https://dev.mysql.com/doc/refman/8.0/en/account-management-statements.html
                    Feature.createRole, Feature.createUser, Feature.alterUser, Feature.renameUser,
                    Feature.setRole,
                    Feature.setDefaultRole, Feature.setPassword,

                    // https://dev.mysql.com/doc/refman/8.0/en/call.html
                    Feature.execute, Feature.executeCall,

                    // https://dev.mysql.com/doc/refman/8.4/en/sql-prepared-statements.html
                    Feature.prepare, Feature.executeExecute, Feature.executeUsing,

                    // https://dev.mysql.com/doc/refman/8.0/en/deallocate-prepare.html
                    Feature.deallocate,

                    // https://dev.mysql.com/doc/refman/8.4/en/do.html
                    Feature.doStatement,

                    Feature.drop, // https://dev.mysql.com/doc/refman/8.0/en/drop-table.html
                    Feature.dropTable, // https://dev.mysql.com/doc/refman/8.0/en/drop-index.html
                    Feature.dropIndex, // https://dev.mysql.com/doc/refman/8.0/en/drop-view.html
                    Feature.dropView, // https://dev.mysql.com/doc/refman/8.0/en/drop-database.html
                    Feature.dropSchema,
                    Feature.dropTableIfExists, Feature.dropViewIfExists,
                    Feature.dropSchemaIfExists, Feature.dropSequenceIfExists,

                    // https://dev.mysql.com/doc/refman/8.0/en/alter-table.html
                    Feature.alterTable, // https://dev.mysql.com/doc/refman/8.0/en/alter-view.html
                    Feature.alterView,

                    // https://dev.mysql.com/doc/refman/8.0/en/create-tablespace.html
                    Feature.createTablespace, // https://dev.mysql.com/doc/refman/8.0/en/alter-tablespace.html
                    Feature.alterTablespace,

                    // https://dev.mysql.com/doc/refman/8.0/en/create-spatial-reference-system.html
                    Feature.createSpatialReferenceSystem,

                    // https://dev.mysql.com/doc/refman/8.0/en/alter-instance.html
                    Feature.alterInstance,

                    // https://dev.mysql.com/doc/refman/8.0/en/create-database.html
                    // https://dev.mysql.com/doc/refman/8.0/en/alter-database.html
                    Feature.createDatabase, Feature.alterDatabase,

                    // https://dev.mysql.com/doc/refman/8.0/en/alter-function.html
                    // https://dev.mysql.com/doc/refman/8.0/en/alter-procedure.html
                    Feature.alterFunction, Feature.alterProcedure,

                    // https://dev.mysql.com/doc/refman/8.0/en/create-database.html
                    Feature.createSchema, // https://dev.mysql.com/doc/refman/8.0/en/create-view.html
                    Feature.createView,
                    Feature.createViewWithComment,
                    Feature.createOrReplaceView, // https://dev.mysql.com/doc/refman/8.0/en/create-table.html
                    Feature.createTable, Feature.createTableCreateOptionStrings,
                    Feature.createTableTableOptionStrings,
                    Feature.createTableFromSelect, Feature.createTableIfNotExists, // https://dev.mysql.com/doc/refman/8.0/en/create-index.html
                    Feature.createIndex, // https://dev.mysql.com/doc/refman/8.0/en/create-trigger.html
                    Feature.createTrigger,

                    // https://dev.mysql.com/doc/refman/8.0/en/create-event.html
                    // https://dev.mysql.com/doc/refman/8.0/en/alter-event.html
                    Feature.createEvent, Feature.alterEvent,

                    // https://dev.mysql.com/doc/refman/8.0/en/describe.html
                    Feature.describe,
                    Feature.desc, // https://dev.mysql.com/doc/refman/8.0/en/explain.html
                    Feature.explain, // https://dev.mysql.com/doc/refman/8.0/en/show.html
                    Feature.show, // https://dev.mysql.com/doc/refman/8.0/en/show-tables.html
                    Feature.showTables, // https://dev.mysql.com/doc/refman/8.0/en/show-columns.html
                    Feature.showColumns, // https://dev.mysql.com/doc/refman/8.0/en/show-index.html
                    Feature.showIndex, // https://dev.mysql.com/doc/refman/8.0/en/show-table-status.html
                    Feature.showTableStatus, // https://dev.mysql.com/doc/refman/8.0/en/show-create-table.html
                    Feature.showCreate, // https://dev.mysql.com/doc/refman/8.0/en/grant.html
                    Feature.grant, // https://dev.mysql.com/doc/refman/8.0/en/revoke.html
                    Feature.revoke, // https://dev.mysql.com/doc/refman/8.0/en/use.html
                    Feature.use, // https://dev.mysql.com/doc/refman/8.0/en/commit.html
                    Feature.commit, Feature.rollback, Feature.startTransaction,
                    Feature.savepoint, Feature.releaseSavepoint,
                    Feature.transactionChain, Feature.transactionConsistentSnapshot,
                    Feature.transactionRelease, // https://dev.mysql.com/doc/refman/8.4/en/set-transaction.html
                    Feature.setTransaction, Feature.setTransactionGlobal,
                    Feature.setTransactionPersist, // https://dev.mysql.com/doc/refman/8.4/en/xa-statements.html
                    Feature.xa, //
                    Feature.mySqlHintStraightJoin,
                    Feature.mysqlSqlCacheFlag,
                    Feature.mysqlSelectModifiers, Feature.mysqlCalcFoundRows));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #copy() to copy from previous version
     */
    MySqlVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #copy() to copy from previous version
     */
    MySqlVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
