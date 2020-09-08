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
 * @see https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/index.html
 */
public enum OracleVersion implements Version {
    V19C("19c",
            EnumSet.of(
                    // supported if used with jdbc
                    Feature.jdbcParameter,
                    Feature.jdbcNamedParameter,
                    // common features
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    Feature.select,
                    // https://www.oracletutorial.com/oracle-basics/oracle-group-by/
                    Feature.selectGroupBy, Feature.function,
                    // https://www.oracletutorial.com/oracle-basics/oracle-grouping-sets/
                    Feature.selectGroupByGroupingSets,
                    // https://www.oracletutorial.com/oracle-basics/oracle-having/
                    Feature.selectHaving,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    // see "join_clause"
                    Feature.join,
                    Feature.joinSimple,
                    Feature.joinLeft,
                    Feature.joinRight,
                    Feature.joinFull, Feature.joinCross,
                    Feature.joinNatural,
                    Feature.joinOuter,
                    Feature.joinInner,
                    Feature.joinApply,
                    Feature.joinUsingColumns,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    // see "row_limiting_clause"
                    Feature.offset, Feature.offsetParam, Feature.fetch, Feature.fetchFirst, Feature.fetchNext,

                    // https://www.oracletutorial.com/oracle-basics/oracle-select-distinct/
                    Feature.distinct, Feature.selectUnique,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    // see "order_by_clause"
                    Feature.orderBy,
                    Feature.orderByNullOrdering,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    // see "with_clause"
                    Feature.withItem,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    // see "pivot_clause"
                    // see "unpivot_clause"
                    // see "LATERAL"
                    Feature.lateralSubSelect,


                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/Set-Operators.html
                    Feature.setOperation, Feature.setOperationUnion, Feature.setOperationIntersect,
                    Feature.setOperationMinus,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/SELECT.html
                    // see "for_update_clause"
                    Feature.selectForUpdate,
                    Feature.selectForUpdateWait, Feature.selectForUpdateNoWait,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/INSERT.html
                    Feature.insert,
                    Feature.insertValues,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/INSERT.html
                    // see "single_table_insert"
                    Feature.insertFromSelect,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/INSERT.html
                    // see "returning_clause"
                    Feature.insertReturningExpressionList,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/UPDATE.html
                    Feature.update,
                    Feature.updateReturning,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DELETE.html
                    Feature.delete,

                    // https://www.oracletutorial.com/oracle-basics/oracle-truncate-table/
                    Feature.truncate,

                    Feature.drop,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DROP-TABLE.html
                    Feature.dropTable,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DROP-INDEX.html
                    Feature.dropIndex,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DROP-VIEW.html
                    Feature.dropView,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DROP-SEQUENCE.html
                    Feature.dropSequence,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/ALTER-TABLE.html
                    Feature.alter,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/ALTER-SEQUENCE.html
                    Feature.alterSequence,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/lnpls/EXECUTE-IMMEDIATE-statement.html
                    Feature.execute,

                    Feature.createView,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-TABLE.html
                    Feature.createTable,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-INDEX.html
                    Feature.createIndex,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-SEQUENCE.html
                    Feature.createSequence,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-TRIGGER.html
                    Feature.createTrigger,
                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/CREATE-SCHEMA.html
                    Feature.createSchema,

                    Feature.commit,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/COMMENT.html
                    Feature.comment, Feature.commentOnTable, Feature.commentOnColumn, Feature.commentOnView,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/rcmrf/DESCRIBE.html
                    Feature.describe,

                    // https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/GRANT.htm
                    Feature.grant,

                    // https://www.oracletutorial.com/oracle-basics/oracle-merge/
                    Feature.merge,

                    Feature.createFunction, Feature.createProcedure, Feature.functionalStatement, Feature.block,
                    Feature.declare,

                    // special oracle features
                    Feature.oracleOldJoinSyntax,
                    Feature.oraclePriorPosition,
                    Feature.oracleHint,
                    Feature.oracleHierarchicalExpression,
                    Feature.oracleOrderBySiblings));

    private Set<Feature> features;
    private String versionString;

    /**
     * @param versionString
     * @param featuresSupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private OracleVersion(String versionString, Set<Feature> featuresSupported) {
        this(versionString, featuresSupported, Collections.emptySet());
    }

    /**
     * @param versionString
     * @param featuresSupported
     * @param unsupported
     * @see #getFeaturesClone() to copy from previous version
     */
    private OracleVersion(String versionString, Set<Feature> featuresSupported, Set<Feature> unsupported) {
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
        return DatabaseType.ORACLE.getName() + " " + getVersionString();
    }

}
