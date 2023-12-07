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

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.operators.relational.SupportsOldOracleJoinSyntax;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.function.CreateFunction;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.procedure.CreateProcedure;
import net.sf.jsqlparser.statement.create.schema.CreateSchema;
import net.sf.jsqlparser.statement.create.sequence.CreateSequence;
import net.sf.jsqlparser.statement.create.synonym.CreateSynonym;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.grant.Grant;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.refreshView.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.First;
import net.sf.jsqlparser.statement.select.KSQLWindow;
import net.sf.jsqlparser.statement.select.Limit;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.OptimizeFor;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.Skip;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Top;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.show.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

public enum Feature {

    // SQL KEYWORD FEATURES
    /**
     * "SELECT"
     */
    select,
    /**
     * "GROUP BY"
     */
    selectGroupBy,
    /**
     * "GROUPING SETS"
     */
    selectGroupByGroupingSets,
    /**
     * "HAVING"
     */
    selectHaving,
    /**
     * "INTO table(, table)*"
     */
    selectInto,

    /**
     * @see Limit
     */
    limit,
    /**
     * "LIMIT NULL"
     *
     * @see Limit#isLimitNull()
     */
    limitNull,
    /**
     * "LIMIT ALL"
     *
     * @see Limit#isLimitAll()
     */
    limitAll,
    /**
     * "LIMIT offset, limit"
     *
     * @see Limit#getOffset()
     */
    limitOffset,
    /**
     * "OFFSET offset"
     * 
     * @see Offset
     */
    offset,
    /**
     * "OFFSET offset param" where param is ROW | ROWS
     *
     * @see Offset#getOffsetParam()
     */
    offsetParam,

    /**
     * @see Fetch
     */
    fetch,
    /**
     * "FETCH FIRST row_count (ROW | ROWS) ONLY"
     * 
     * @see Fetch#isFetchParamFirst()
     */
    fetchFirst,
    /**
     * "FETCH NEXT row_count (ROW | ROWS) ONLY" if not {@link #fetchFirst}
     *
     * @see Fetch#isFetchParamFirst()
     */
    fetchNext,

    /**
     * "JOIN"
     */
    join,
    /**
     * join tables by ", OUTER" placing the join specification in WHERE-clause
     */
    joinOuterSimple,
    /**
     * join tables by "," placing the join specification in WHERE-clause
     */
    joinSimple,
    /**
     * "RIGHT" join
     */
    joinRight,
    /**
     * "NATURAL" join
     */
    joinNatural,
    /**
     * "FULL" join
     */
    joinFull,
    /**
     * "LEFT" join
     */
    joinLeft,
    /**
     * "CROSS" join
     */
    joinCross,
    /**
     * "OUTER" join
     */
    joinOuter,
    /**
     * "SEMI" join
     */
    joinSemi,
    /**
     * "INNER" join
     */
    joinInner,
    /**
     * "STRAIGHT_JOIN" join
     */
    joinStraight,
    /**
     * "APPLY" join
     */
    joinApply,

    joinWindow, joinUsingColumns,

    /**
     * "SKIP variable" | "SKIP ?" | "SKIP rowCount"
     *
     * @see Skip
     */
    skip,
    /**
     * "FIRST" \?|[0-9]+|variable or "LIMIT" \?|[0-9]+|variable
     *
     * @see First
     */
    first,
    /**
     * "TOP" ? "PERCENT"
     *
     * @see Top
     */
    top,
    /**
     * "OPTIMIZE FOR rowCount ROWS"
     *
     * @see OptimizeFor
     */
    optimizeFor,

    /**
     * "UNIQUE" keyword
     */
    selectUnique,
    /**
     * "DISTINCT" keyword
     */
    distinct,
    /**
     * "DISTINCT ON (col1, ...)"
     */
    distinctOn,

    /**
     * "ORDER BY"
     */
    orderBy,
    /**
     * "ORDER BY expression [ NULLS { FIRST | LAST } ]"
     */
    orderByNullOrdering,

    /**
     * "FOR UPDATE"
     */
    selectForUpdate,
    /**
     * "FOR UPDATE OF table"
     */
    selectForUpdateOfTable,
    /**
     * "FOR UPDATE WAIT timeout"
     */
    selectForUpdateWait,
    /**
     * "FOR UPDATE NOWAIT"
     */
    selectForUpdateNoWait,
    /**
     * "FOR UPDATE SKIP LOCKED"
     */
    selectForUpdateSkipLocked,


    /**
     * SQL "INSERT" statement is allowed
     */
    insert,
    /**
     * "INSERT .. SELECT"
     */
    insertFromSelect,
    /**
     * "LOW_PRIORITY | DELAYED | HIGH_PRIORITY | IGNORE"
     */
    insertModifierPriority,
    /**
     * "IGNORE"
     */
    insertModifierIgnore,
    /**
     * "INSERT .. SET"
     */
    insertUseSet,
    /**
     * "ON DUPLICATE KEY UPDATE"
     */
    insertUseDuplicateKeyUpdate,
    /**
     * "RETURNING *"
     */
    insertReturningAll,
    /**
     * "RETURNING expr(, expr)*"
     *
     * @see net.sf.jsqlparser.expression.operators.relational.ExpressionList
     */
    insertReturningExpressionList,

    /**
     * "VALUES"
     */
    insertValues,
    /**
     * @see net.sf.jsqlparser.statement.select.Values
     */
    values,

    /**
     * SQL "UPDATE" statement is allowed
     *
     * @see Update
     */
    update,
    /**
     * "UPDATE table1 SET ... FROM table2
     */
    updateFrom,
    /**
     * "UPDATE table1, table2 ..."
     */
    updateJoins,
    /**
     * UPDATE table SET (col, ...) = (SELECT col, ... )"
     */
    updateUseSelect, updateOrderBy, updateLimit,
    /**
     * "RETURNING expr(, expr)*"
     *
     * @see net.sf.jsqlparser.statement.select.SelectItem
     */
    updateReturning,
    /**
     * SQL "DELETE" statement is allowed
     *
     * @see Delete
     */
    delete,
    /**
     * "DELETE FROM table1, table1 ..."
     */
    deleteJoin,
    /**
     * "DELETE table1, table1 FROM table ..."
     */
    deleteTables,
    /**
     * "LIMIT row_count"
     */
    deleteLimit,
    /**
     * "ORDER BY ..."
     */
    deleteOrderBy,
    /**
     * "RETURNING expr(, expr)*"
     *
     * @see net.sf.jsqlparser.statement.select.SelectItem
     */
    deleteReturningExpressionList,

    /**
     * SQL "UPSERT" statement is allowed
     *
     * @see Upsert
     * @see <a href=
     *      "https://wiki.postgresql.org/wiki/UPSERT">https://wiki.postgresql.org/wiki/UPSERT</a>
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
    alterTable,
    /**
     * SQL "ALTER SEQUENCE" statement is allowed
     *
     * @see AlterSequence
     */
    alterSequence,
    /**
     * SQL "ALTER VIEW" statement is allowed
     *
     * @see AlterView
     */
    alterView,

    /**
     * SQL "REFRESH MATERIALIZED VIEW" statement is allowed
     *
     * @see RefreshMaterializedViewStatement
     */
    refreshMaterializedView, refreshMaterializedConcurrentlyView, refreshMaterializedWithDataView, refreshMaterializedWithNoDataView, 
    
    /**
     * SQL "REPLACE VIEW" statement is allowed
     *
     * @see AlterView
     */
    alterViewReplace,
    /**
     * SQL "ALTER INDEX" statement is allowed
     */
    alterIndex,


    /**
     * SQL "ANALYZE" statement is allowed
     *
     * @see Analyze
     */
    analyze,

    /**
     * SQL "TRUNCATE" statement is allowed
     *
     * @see Truncate
     */
    truncate,
    /**
     * SQL "CALL|EXEC|EXECUTE" stored procedure is allowed
     *
     * @see Execute
     */
    execute, executeExec, executeCall, executeExecute,

    /**
     * SQL "EXECUTE" statement is allowed
     */
    executeStatement,
    /**
     * SQL "EXECUTE IMMEDIATE" statement is allowed
     */
    executeStatementImmediate,

    executeUsing,
    /**
     * SQL "REPLACE" statement is allowed
     *
     */
    @Deprecated
    replace,
    /**
     * SQL "DROP" statement is allowed
     *
     * @see Drop
     */
    drop, dropTable, dropIndex, dropView, dropSchema, dropSequence, dropTableIfExists, dropIndexIfExists, dropViewIfExists, dropSchemaIfExists, dropSequenceIfExists,

    /**
     * SQL "CREATE SCHEMA" statement is allowed
     *
     * @see CreateSchema
     */
    createSchema,
    /**
     * SQL "CREATE VIEW" statement is allowed
     *
     * @see CreateView
     */
    createView,
    /**
     * "CREATE FORCE VIEW"
     */
    createViewForce,
    /**
     * "CREATE TEMPORARAY VIEW"
     */
    createViewTemporary,
    /**
     * "CREATE OR REPLACE VIEW"
     */
    createOrReplaceView,
    /**
     * SQL "CREATE MATERIALIZED VIEW" statement is allowed
     */
    createViewMaterialized,
    /**
     * SQL "CREATE TABLE" statement is allowed
     *
     * @see CreateTable
     */
    createTable,
    /**
     * "CREATE GLOBAL UNLOGGED"
     */
    createTableUnlogged,
    /**
     * i.e. "CREATE GLOBAL TEMPORARY TABLE", "CREATE SHARDED TABLE"
     */
    createTableCreateOptionStrings,
    /**
     * i.e. "ENGINE = InnoDB AUTO_INCREMENT = 8761 DEFAULT CHARSET = utf8"
     */
    createTableTableOptionStrings,
    /**
     * "CREATE TABLE IF NOT EXISTS table"
     */
    createTableIfNotExists,
    /**
     * " ROW MOVEMENT"
     */
    createTableRowMovement,
    /**
     * "CREATE TABLE (colspec) SELECT ...
     */
    createTableFromSelect,
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
     * SQL "CREATE SYNONYM" statement is allowed
     *
     * @see CreateSynonym
     */
    createSynonym,
    /**
     * SQL "CREATE TRIGGER" statement is allowed
     */
    createTrigger,
    /**
     * SQL "COMMIT" statement is allowed
     *
     * @see Commit
     */
    commit,
    /**
     * SQL "COMMENT ON" statement is allowed
     *
     * @see Comment
     */
    comment,
    /**
     * "COMMENT ON table"
     */
    commentOnTable,
    /**
     * "COMMENT ON column"
     */
    commentOnColumn,
    /**
     * "COMMENT ON view"
     */
    commentOnView,

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
     * @see ShowTablesStatement
     */
    showTables,
    /**
     * @see ShowColumnsStatement
     */
    showColumns,
    /**
     * @see ShowIndexStatement
     */
    showIndex,
    /**
     * @see UseStatement
     */
    use,
    /**
     * @see Grant
     */
    grant,
    /**
     * @see Function
     */
    function,
    /**
     * @see CreateFunction
     */
    createFunction,
    /**
     * @see CreateProcedure
     */
    createProcedure,
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
     * @see ResetStatement
     */
    reset,
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

    setOperation, setOperationUnion, setOperationIntersect, setOperationExcept, setOperationMinus,

    /**
     * "WITH name query"
     */
    withItem, withItemRecursive,

    lateralSubSelect,
    /**
     * @see net.sf.jsqlparser.statement.select.Values
     */
    valuesList,
    /**
     * @see TableFunction
     */
    tableFunction,

    // JDBC
    /**
     * @see JdbcParameter
     */
    jdbcParameter,
    /**
     * @see JdbcNamedParameter
     */
    jdbcNamedParameter,

    // EXPRESSIONS
    /**
     * "LIKE"
     */
    exprLike,
    /**
     * "SIMILAR TO"
     */
    exprSimilarTo,

    // VENDOR SPECIFIC SYNTAX FEATURES

    /**
     * @see KSQLWindow
     */
    kSqlWindow,

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
    oracleHierarchicalExpression, oracleOrderBySiblings,

    // MYSQL

    mySqlHintStraightJoin, mysqlSqlCacheFlag, mysqlCalcFoundRows,

    // SQLSERVER

    /**
     * "FOR XML PATH(...)"
     */
    selectForXmlPath,

    /**
     * allows square brackets for names, disabled by default
     */
    allowSquareBracketQuotation(false),

    /**
     * allow parsing of RDBMS specific syntax by switching off SQL Standard Compliant Syntax
     */
    allowPostgresSpecificSyntax(false),

    // PERFORMANCE

    /**
     * allows complex expression parameters or named parameters for functions will be switched off,
     * when deep nesting of functions is detected
     */
    allowComplexParsing(true),

    /**
     * allows passing through Unsupported Statements as a plain List of Tokens needs to be switched
     * off, when VALIDATING statements or parsing blocks
     */
    allowUnsupportedStatements(false),

    timeOut(8000),

    /**
     * allows Backslash '\' as Escape Character
     */
    allowBackslashEscapeCharacter(false),;

    private final Object value;
    private final boolean configurable;

    /**
     * a feature which can't configured within the parser
     */
    Feature() {
        this.value = null;
        this.configurable = false;
    }

    /**
     * a feature which can be configured by {@link FeatureConfiguration}
     *
     * @param value The Value Object of the Parameter.
     */
    Feature(Object value) {
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
