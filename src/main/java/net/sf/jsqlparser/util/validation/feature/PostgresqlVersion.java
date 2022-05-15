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
 *      "https://www.postgresql.org/docs/current">https://www.postgresql.org/docs/current</a>
 */
public enum PostgresqlVersion implements Version {
    V10("10",
            EnumSet.of(
                    // supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // expressions
                    Feature.exprLike,
                    Feature.exprSimilarTo,
                    // https://www.postgresql.org/docs/current/sql-select.html
                    Feature.select,
                    Feature.selectGroupBy, Feature.function, Feature.tableFunction, Feature.lateralSubSelect,
                    Feature.selectHaving,
                    // https://www.postgresql.org/docs/current/queries-table-expressions.html#QUERIES-GROUPING-SETS
                    Feature.selectGroupByGroupingSets,

                    // https://www.postgresql.org/docs/current/sql-select.html#join_type
                    Feature.join,
                    Feature.joinSimple,
                    Feature.joinRight,
                    Feature.joinFull,
                    Feature.joinLeft,
                    Feature.joinCross,
                    Feature.joinOuter,
                    Feature.joinInner,
                    Feature.joinUsingColumns,

                    // https://www.postgresql.org/docs/current/queries-with.html
                    Feature.withItem,
                    Feature.withItemRecursive,

                    // https://www.postgresql.org/docs/current/queries-limit.html
                    // https://www.postgresql.org/docs/current/sql-select.html#SQL-LIMIT
                    Feature.limit,
                    Feature.limitAll, //
                    Feature.limitNull,
                    Feature.offset,

                    // https://www.postgresql.org/docs/current/sql-select.html
                    Feature.fetch, //
                    Feature.fetchFirst, //
                    Feature.fetchNext,

                    // https://www.postgresql.org/docs/current/sql-select.html
                    Feature.distinct,
                    Feature.distinctOn,

                    // https://www.postgresql.org/docs/current/sql-select.html
                    Feature.orderBy,
                    Feature.orderByNullOrdering,

                    Feature.selectForUpdate,
                    Feature.selectForUpdateOfTable,
                    Feature.selectForUpdateNoWait,

                    // https://www.postgresql.org/docs/current/queries-union.html
                    Feature.setOperation,
                    Feature.setOperationUnion,
                    Feature.setOperationIntersect,
                    Feature.setOperationExcept,

                    // https://www.postgresql.org/docs/current/sql-comment.html
                    Feature.comment,
                    Feature.commentOnTable,
                    Feature.commentOnColumn,
                    Feature.commentOnView,

                    // https://www.postgresql.org/docs/current/sql-createsequence.html
                    Feature.createSequence,
                    // https://www.postgresql.org/docs/current/sql-altersequence.html
                    Feature.alterSequence,
                    // https://www.postgresql.org/docs/current/sql-createschema.html
                    Feature.createSchema,
                    // https://www.postgresql.org/docs/current/sql-createindex.html
                    Feature.createIndex,
                    // https://www.postgresql.org/docs/current/sql-createtable.html
                    Feature.createTable, Feature.createTableUnlogged, 
                    Feature.createTableCreateOptionStrings, Feature.createTableTableOptionStrings,
                    Feature.createTableFromSelect, Feature.createTableIfNotExists,
                    // https://www.postgresql.org/docs/current/sql-createview.html
                    Feature.createView, Feature.createViewTemporary, Feature.createOrReplaceView,
                    // https://www.postgresql.org/docs/current/sql-alterview.html
                    // Feature.alterView,

                    // https://www.postgresql.org/docs/current/sql-insert.html
                    Feature.insert,
                    Feature.insertValues,
                    Feature.values,
                    Feature.insertFromSelect,
                    Feature.insertReturningAll, Feature.insertReturningExpressionList,
                    // https://www.postgresql.org/docs/current/sql-update.html
                    Feature.update,
                    Feature.updateReturning,
                    // https://www.postgresql.org/docs/current/sql-delete.html
                    Feature.delete,
                    // https://www.postgresql.org/docs/current/sql-truncate.html
                    Feature.truncate,

                    // https://www.postgresql.org/docs/current/sql-call.html
                    Feature.execute, Feature.executeCall,

                    Feature.drop,
                    // https://www.postgresql.org/docs/current/sql-droptable.html
                    Feature.dropTable,
                    // https://www.postgresql.org/docs/current/sql-dropindex.html
                    Feature.dropIndex,
                    // https://www.postgresql.org/docs/current/sql-dropview.html
                    Feature.dropView,
                    // https://www.postgresql.org/docs/current/sql-dropschema.html
                    Feature.dropSchema,
                    // https://www.postgresql.org/docs/current/sql-dropsequence.html
                    Feature.dropSequence,
                    Feature.dropTableIfExists, Feature.dropIndexIfExists, Feature.dropViewIfExists,
                    Feature.dropSchemaIfExists, Feature.dropSequenceIfExists,

                    // https://www.postgresql.org/docs/current/sql-altertable.html
                    Feature.alterTable,
                    // https://www.postgresql.org/docs/current/using-explain.html
                    Feature.explain,
                    // https://www.postgresql.org/docs/current/sql-grant.html
                    Feature.grant,
                    // https://www.postgresql.org/docs/current/sql-set.html
                    Feature.set,
                    // https://www.postgresql.org/docs/current/sql-reset.html
                    Feature.reset,
                    // https://www.postgresql.org/docs/current/sql-commit.html
                    Feature.commit
                    )),
    V11("11", V10.copy().getFeatures()), V12("12", V11.copy().getFeatures());

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #copy() to copy from previous version
     */
    PostgresqlVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #copy() to copy from previous version
     */
    PostgresqlVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.POSTGRESQL.getName() + " " + getVersionString();
    }

}
