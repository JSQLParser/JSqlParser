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
 */
public enum SqlServerVersion implements Version {
    V2019("2019",
            EnumSet.of(
                    // supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // https://docs.microsoft.com/en-us/sql/t-sql/queries/select-transact-sql?view=sql-server-ver15
                    Feature.select,
                    Feature.selectInto,
                    Feature.withItem,
                    Feature.selectGroupBy, Feature.function,
                    Feature.selectHaving, Feature.orderBy,
                    Feature.distinct,
                    Feature.withItem, Feature.withItemRecursive,
                    // https://docs.microsoft.com/en-us/sql/t-sql/queries/top-transact-sql?view=sql-server-ver15
                    Feature.top,
                    // https://docs.microsoft.com/en-us/sql/t-sql/queries/select-order-by-clause-transact-sql?view=sql-server-ver15
                    Feature.offset, Feature.offsetParam, Feature.fetch, Feature.fetchFirst, Feature.fetchNext,

                    // https://docs.microsoft.com/en-us/sql/t-sql/language-elements/set-operators-except-and-intersect-transact-sql?view=sql-server-ver15
                    // https://docs.microsoft.com/en-us/sql/t-sql/language-elements/set-operators-union-transact-sql?view=sql-server-ver15
                    Feature.setOperation, Feature.setOperationUnion, Feature.setOperationIntersect,
                    Feature.setOperationExcept,

                    // https://docs.microsoft.com/en-us/sql/t-sql/queries/from-transact-sql?view=sql-server-ver15#syntax
                    Feature.join,
                    Feature.joinSimple,
                    Feature.joinRight,
                    Feature.joinFull,
                    Feature.joinLeft,
                    Feature.joinCross,
                    Feature.joinOuter,
                    Feature.joinInner,
                    Feature.joinApply,

                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/insert-transact-sql?view=sql-server-ver15
                    Feature.insert,
                    Feature.insertValues,
                    Feature.insertFromSelect,
                    // https://docs.microsoft.com/en-us/sql/t-sql/queries/update-transact-sql?view=sql-server-ver15
                    Feature.update,
                    Feature.updateFrom,

                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/delete-transact-sql?view=sql-server-ver15
                    Feature.delete,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/truncate-table-transact-sql?view=sql-server-ver15
                    Feature.truncate,

                    Feature.drop,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/drop-table-transact-sql?view=sql-server-ver15
                    Feature.dropTable,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/drop-index-transact-sql?view=sql-server-ver15
                    Feature.dropIndex,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/drop-view-transact-sql?view=sql-server-ver15
                    Feature.dropView,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/drop-schema-transact-sql?view=sql-server-ver15
                    Feature.dropSchema,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/drop-sequence-transact-sql?view=sql-server-ver15
                    Feature.dropSequence,
                    Feature.dropTableIfExists, Feature.dropIndexIfExists, Feature.dropViewIfExists,
                    Feature.dropSchemaIfExists, Feature.dropSequenceIfExists,

                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/alter-table-transact-sql?view=sql-server-ver15
                    Feature.alter,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/alter-sequence-transact-sql?view=sql-server-ver15
                    Feature.alterSequence,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/alter-view-transact-sql?view=sql-server-ver15
                    Feature.alterView,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/alter-index-transact-sql?view=sql-server-ver15
                    Feature.alterIndex,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/create-index-transact-sql?view=sql-server-ver15
                    Feature.createIndex,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/create-function-transact-sql?view=sql-server-ver15
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/create-procedure-transact-sql?view=sql-server-ver15
                    Feature.functionalStatement, Feature.createProcedure, Feature.createFunction, Feature.block,
                    Feature.declare,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/create-schema-transact-sql?view=sql-server-ver15
                    Feature.createSchema,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/create-view-transact-sql?view=sql-server-ver15
                    Feature.createView,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/create-trigger-transact-sql?view=sql-server-ver15
                    Feature.createTrigger,
                    // https://docs.microsoft.com/en-us/sql/t-sql/statements/merge-transact-sql?view=sql-server-ver15
                    Feature.merge,
                    // special sql-server features
                    // https://docs.microsoft.com/en-us/sql/relational-databases/xml/for-xml-sql-server?view=sql-server-ver15
                    Feature.selectForXmlPath,
                    Feature.use, Feature.allowSquareBracketQuotation, //
                    Feature.pivot, Feature.unpivot, Feature.pivotXml,
                    Feature.selectGroupByGroupingSets));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #copy() to copy from previous version
     */
    private SqlServerVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #copy() to copy from previous version
     */
    private SqlServerVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.SQLSERVER.getName() + " " + getVersionString();
    }

}
