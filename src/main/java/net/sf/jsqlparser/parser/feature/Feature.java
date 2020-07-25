/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser.feature;

import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.statement.values.ValuesStatement;

public enum Feature {

    // SQL KEYWORD FEATURES
    select,
    selectGroupBy,
    selectGroupByGroupingSets,
    selectHaving,
    selectInto,

    /**
     * @see Limit
     */
    limit,
    /**
     * @see Limit#isLimitNull()
     */
    limitNull,
    /**
     * @see Limit#isLimitAll()
     */
    limitAll,
    /**
     * @see Limit#getOffset()
     */
    limitOffset,
    /**
     * @see Offset
     */
    offset,
    /**
     * @see Offset#getOffsetParam()
     */
    offsetParam,

    /**
     * @see Fetch
     */
    fetch,
    /**
     * "FETCH FIRST row_count (ROW | ROWS) ONLY"
     * @see Fetch#isFetchParamFirst()
     */
    fetchFirst,
    /**
     * "FETCH NEXT row_count (ROW | ROWS) ONLY"
     * if not {@link #fetchFirst}
     *
     * @see Fetch#isFetchParamFirst()
     */
    fetchNext,

    join,
    joinOuterSimple,
    joinSimple,
    joinRight,
    joinNatural,
    joinFull,
    joinLeft,
    joinCross,
    joinOuter,
    joinSemi,
    joinInner,
    joinStaight,
    joinApply,
    joinWindow,
    joinUsingColumns,

    skip,
    first,
    top,
    optimizeFor,

    selectUnique,
    distinct,
    distinctOn,

    orderBy,
    orderByNullOrdering,

    /**
     * @see KSQLWindow
     */
    window,
    selectForXmlPath,

    selectForUpdate,
    selectForUpdateOfTable,
    selectForUpdateWait,
    selectForUpdateNoWait,


    /**
     * SQL "INSERT" statement is allowed
     */
    insert,
    insertFromSelect,
    insertModifierPriority,
    insertModifierIgnore,
    insertUseSet,
    insertUseDuplicateKeyUpdate,
    insertReturning,
    insertReturningExpressionList,

    /**
     * @see ValuesStatement
     */
    insertValues,
    /**
     * SQL "UPDATE" statement is allowed
     *
     * @see Update
     */
    update,
    updateFrom,
    updateJoins,
    updateUseSelect,
    updateOrderBy,
    updateLimit,
    updateReturning,
    /**
     * SQL "DELETE" statement is allowed
     *
     * @see Delete
     */
    delete,
    deleteJoin,
    deleteTables,
    deleteLimit,
    deleteOrderBy,
    /**
     * SQL "UPSERT" statement is allowed
     *
     * @see Upsert
     */
    upsert,
    /**
     * SQL "MERGE" statement is allowed
     *
     * @see Merge
     */
    merge,
    /**
     * SQL "ALTER" statement is allowed
     *
     * @see Alter
     */
    alter,
    /**
     * SQL "ALTER SEQUENCE" statement is allowed
     *
     * @see AlterSequence
     */
    alterSequence,
    /**
     * SQL "TRUNCATE" statement is allowed
     *
     * @see Truncate
     */
    truncate,
    /**
     * SQL "EXECUTE" statement is allowed
     *
     * @see Execute
     */
    execute,
    /**
     * SQL "REPLACE" statement is allowed
     *
     * @see Replace
     */
    replace,
    /**
     * SQL "DROP" statement is allowed
     *
     * @see Drop
     */
    drop,
    /**
     * SQL "ALTER VIEW" statement is allowed
     *
     * @see AlterView
     */
    alterView,
    /**
     * SQL "CREATE VIEW" statement is allowed
     *
     * @see CreateView
     */
    createView,
    /**
     * SQL "CREATE TABLE" statement is allowed
     *
     * @see CreateTable
     */
    createTable,
    /**
     * SQL "CREATE INDEX" statement is allowed
     *
     * @see CreateIndex
     */
    createIndex,
    /**
     * SQL "CREATE SEQUENCE" statement is allowed
     *
     * @see CreateSequence
     */
    createSequence,
    /**
     * SQL "COMMIT" statement is allowed
     *
     * @see Commit
     */
    commit,
    /**
     * SQL "COMMENT" statement is allowed
     *
     * @see Comment
     */
    comment,
    /**
     * SQL "DESCRIBE" statement is allowed
     *
     * @see DescribeStatement
     */
    describe,
    /**
     * SQL "EXPLAIN" statement is allowed
     *
     * @see ExplainStatement
     */
    explain,
    /**
     * @see ShowStatement
     */
    show,
    /**
     * @see ShowColumnsStatement
     */
    showColumns,
    /**
     * @see UseStatement
     */
    use,
    /**
     * @see Grant
     */
    grant,
    /**
     * SQL "CREATE SCHEMA" statement is allowed
     *
     * @see CreateSchema
     */
    createSchema,
    /**
     * @see CreateFunction
     */
    function,
    /**
     * @see CreateProcedure
     */
    procedure,
    /**
     * @see CreateFunctionalStatement
     */
    functionalStatement,
    /**
     * SQL block starting with "BEGIN" and ends with "END" statement is allowed
     *
     * @see Block
     */
    block,
    /**
     * @see DeclareStatement
     */
    declare,
    /**
     * @see SetStatement
     */
    set,

    /**
     * @see Pivot
     */
    pivot,
    /**
     * @see UnPivot
     */
    unpivot,
    /**
     * @see PivotXml
     */
    pivotXml,

    // EXPRESSIONS
    /**
     * @see JdbcParameter
     */
    jdbcParameter,
    /**
     * @see JdbcNamedParameter
     */
    jdbcNamedParameter,

    // SYNTAX FEATURES

    // ORACLE

    /**
     * allows old oracle join syntax (+)
     *
     * @see SupportsOldOracleJoinSyntax
     */
    oracleOldJoinSyntax,
    /**
     * allows oracle prior position
     *
     * @see SupportsOldOracleJoinSyntax
     */
    oraclePriorPosition,
    /**
     * @see OracleHint
     */
    oracleHint,
    /**
     * oracle SQL "CONNECT BY"
     *
     * @see OracleHierarchicalExpression
     */
    oracleHierarchicalExpression,
    oracleOrderBySiblings,

    // MYSQL

    mySqlHintStraightJoin,
    mysqlSqlNoCache,
    mysqlCalcFoundRows,

    // SQLSERVER

    /**
     * allows square brackets for names, disabled by default
     */
    allowSquareBracketQuotation(false),


    ;

    private Object value;
    private boolean configurable;

    /**
     * a feature which can't configured within the parser
     */
    private Feature() {
        this.value = null;
        this.configurable = false;
    }

    /**
     * a feature which can be configured by {@link FeatureConfiguration}
     *
     * @param value
     */
    private Feature(Object value) {
        this.value = value;
        this.configurable = true;
    }

    public Object getDefaultValue() {
        return value;
    }

    public boolean isConfigurable() {
        return configurable;
    }

}
