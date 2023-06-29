/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
import net.sf.jsqlparser.expression.operators.arithmetic.Division;
import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
import net.sf.jsqlparser.expression.operators.relational.Between;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.Matches;
import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Block;
import net.sf.jsqlparser.statement.Commit;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.statement.DescribeStatement;
import net.sf.jsqlparser.statement.ExplainStatement;
import net.sf.jsqlparser.statement.IfElseStatement;
import net.sf.jsqlparser.statement.PurgeObjectType;
import net.sf.jsqlparser.statement.PurgeStatement;
import net.sf.jsqlparser.statement.ResetStatement;
import net.sf.jsqlparser.statement.RollbackStatement;
import net.sf.jsqlparser.statement.SavepointStatement;
import net.sf.jsqlparser.statement.SetStatement;
import net.sf.jsqlparser.statement.ShowColumnsStatement;
import net.sf.jsqlparser.statement.ShowStatement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.UnsupportedStatement;
import net.sf.jsqlparser.statement.UseStatement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.alter.AlterSession;
import net.sf.jsqlparser.statement.alter.AlterSystemStatement;
import net.sf.jsqlparser.statement.alter.RenameTableStatement;
import net.sf.jsqlparser.statement.alter.sequence.AlterSequence;
import net.sf.jsqlparser.statement.analyze.Analyze;
import net.sf.jsqlparser.statement.comment.Comment;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
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
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Find all used tables within an select statement.
 *
 * <p>
 * Override extractTableName method to modify the extracted table names (e.g. without schema).
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.UncommentedEmptyMethodBody"})
public class TablesNamesFinder implements SelectVisitor, FromItemVisitor, ExpressionVisitor,
        SelectItemVisitor, StatementVisitor {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";
    private List<String> tables;
    private boolean allowColumnProcessing = false;

    private List<String> otherItemNames;

    public List<String> getTableList(Statement statement) {
        init(false);
        statement.accept(this);
        return tables;
    }

    @Override
    public void visit(Select select) {
        List<WithItem> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        select.accept((SelectVisitor) this);
    }

    @Override
    public void visit(TranscodingFunction transcodingFunction) {
        transcodingFunction.getExpression().accept(this);
    }

    @Override
    public void visit(TrimFunction trimFunction) {
        if (trimFunction.getExpression() != null) {
            trimFunction.getExpression().accept(this);
        }
        if (trimFunction.getFromExpression() != null) {
            trimFunction.getFromExpression().accept(this);
        }
    }

    @Override
    public void visit(RangeExpression rangeExpression) {
        rangeExpression.getStartExpression().accept(this);
        rangeExpression.getEndExpression().accept(this);
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     */
    public List<String> getTableList(Expression expr) {
        init(true);
        expr.accept(this);
        return tables;
    }

    @Override
    public void visit(WithItem withItem) {
        otherItemNames.add(withItem.getAlias().getName().toLowerCase());
        withItem.getSelect().accept((SelectVisitor) this);
    }

    @Override
    public void visit(ParenthesedSelect selectBody) {
        List<WithItem> withItemsList = selectBody.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        selectBody.getSelect().accept((SelectVisitor) this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        List<WithItem> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                item.accept(this);
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                join.getFromItem().accept(this);
            }
        }
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this);
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(this);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this);
        }
    }

    /**
     * Override to adapt the tableName generation (e.g. with / without schema).
     *
     * @param table
     * @return
     */
    protected String extractTableName(Table table) {
        return table.getFullyQualifiedName();
    }

    @Override
    public void visit(Table tableName) {
        String tableWholeName = extractTableName(tableName);
        if (!otherItemNames.contains(tableWholeName.toLowerCase())
                && !tables.contains(tableWholeName)) {
            tables.add(tableWholeName);
        }
    }

    @Override
    public void visit(Addition addition) {
        visitBinaryExpression(addition);
    }

    @Override
    public void visit(AndExpression andExpression) {
        visitBinaryExpression(andExpression);
    }

    @Override
    public void visit(Between between) {
        between.getLeftExpression().accept(this);
        between.getBetweenExpressionStart().accept(this);
        between.getBetweenExpressionEnd().accept(this);
    }

    @Override
    public void visit(OverlapsCondition overlapsCondition) {
        overlapsCondition.getLeft().accept(this);
        overlapsCondition.getRight().accept(this);
    }

    @Override
    public void visit(Column tableColumn) {
        if (allowColumnProcessing && tableColumn.getTable() != null
                && tableColumn.getTable().getName() != null) {
            visit(tableColumn.getTable());
        }
    }

    @Override
    public void visit(Division division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(IntegerDivision division) {
        visitBinaryExpression(division);
    }

    @Override
    public void visit(DoubleValue doubleValue) {

    }

    @Override
    public void visit(EqualsTo equalsTo) {
        visitBinaryExpression(equalsTo);
    }

    @Override
    public void visit(Function function) {
        ExpressionList exprList = function.getParameters();
        if (exprList != null) {
            visit(exprList);
        }
    }

    @Override
    public void visit(GreaterThan greaterThan) {
        visitBinaryExpression(greaterThan);
    }

    @Override
    public void visit(GreaterThanEquals greaterThanEquals) {
        visitBinaryExpression(greaterThanEquals);
    }

    @Override
    public void visit(InExpression inExpression) {
        inExpression.getLeftExpression().accept(this);
        inExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(FullTextSearch fullTextSearch) {

    }

    @Override
    public void visit(SignedExpression signedExpression) {
        signedExpression.getExpression().accept(this);
    }

    @Override
    public void visit(IsNullExpression isNullExpression) {

    }

    @Override
    public void visit(IsBooleanExpression isBooleanExpression) {

    }

    @Override
    public void visit(JdbcParameter jdbcParameter) {

    }

    @Override
    public void visit(LikeExpression likeExpression) {
        visitBinaryExpression(likeExpression);
    }

    @Override
    public void visit(ExistsExpression existsExpression) {
        existsExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(MemberOfExpression memberOfExpression) {
        memberOfExpression.getLeftExpression().accept(this);
        memberOfExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(LongValue longValue) {

    }

    @Override
    public void visit(MinorThan minorThan) {
        visitBinaryExpression(minorThan);
    }

    @Override
    public void visit(MinorThanEquals minorThanEquals) {
        visitBinaryExpression(minorThanEquals);
    }

    @Override
    public void visit(Multiplication multiplication) {
        visitBinaryExpression(multiplication);
    }

    @Override
    public void visit(NotEqualsTo notEqualsTo) {
        visitBinaryExpression(notEqualsTo);
    }

    @Override
    public void visit(NullValue nullValue) {

    }

    @Override
    public void visit(OrExpression orExpression) {
        visitBinaryExpression(orExpression);
    }

    @Override
    public void visit(XorExpression xorExpression) {
        visitBinaryExpression(xorExpression);
    }

    @Override
    public void visit(Parenthesis parenthesis) {
        parenthesis.getExpression().accept(this);
    }

    @Override
    public void visit(StringValue stringValue) {

    }

    @Override
    public void visit(Subtraction subtraction) {
        visitBinaryExpression(subtraction);
    }

    @Override
    public void visit(NotExpression notExpr) {
        notExpr.getExpression().accept(this);
    }

    @Override
    public void visit(BitwiseRightShift expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(BitwiseLeftShift expr) {
        visitBinaryExpression(expr);
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this);
        binaryExpression.getRightExpression().accept(this);
    }

    @Override
    public void visit(ExpressionList<?> expressionList) {
        for (Expression expression : expressionList.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(DateValue dateValue) {

    }

    @Override
    public void visit(TimestampValue timestampValue) {

    }

    @Override
    public void visit(TimeValue timeValue) {

    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.
     * CaseExpression)
     */
    @Override
    public void visit(CaseExpression caseExpression) {
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this);
        }
        if (caseExpression.getWhenClauses() != null) {
            for (WhenClause when : caseExpression.getWhenClauses()) {
                when.accept(this);
            }
        }
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
     */
    @Override
    public void visit(WhenClause whenClause) {
        if (whenClause.getWhenExpression() != null) {
            whenClause.getWhenExpression().accept(this);
        }
        if (whenClause.getThenExpression() != null) {
            whenClause.getThenExpression().accept(this);
        }
    }

    @Override
    public void visit(AnyComparisonExpression anyComparisonExpression) {
        anyComparisonExpression.getSelect().accept((ExpressionVisitor) this);
    }

    @Override
    public void visit(Concat concat) {
        visitBinaryExpression(concat);
    }

    @Override
    public void visit(Matches matches) {
        visitBinaryExpression(matches);
    }

    @Override
    public void visit(BitwiseAnd bitwiseAnd) {
        visitBinaryExpression(bitwiseAnd);
    }

    @Override
    public void visit(BitwiseOr bitwiseOr) {
        visitBinaryExpression(bitwiseOr);
    }

    @Override
    public void visit(BitwiseXor bitwiseXor) {
        visitBinaryExpression(bitwiseXor);
    }

    @Override
    public void visit(CastExpression cast) {
        cast.getLeftExpression().accept(this);
    }

    @Override
    public void visit(InterpretExpression interpretExpression) {
        interpretExpression.getLeftExpression().accept(this);
    }

    @Override
    public void visit(Modulo modulo) {
        visitBinaryExpression(modulo);
    }

    @Override
    public void visit(AnalyticExpression analytic) {

    }

    @Override
    public void visit(SetOperationList list) {
        List<WithItem> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor) this);
            }
        }
        for (Select selectBody : list.getSelects()) {
            selectBody.accept((SelectVisitor) this);
        }
    }

    @Override
    public void visit(ExtractExpression eexpr) {

    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        lateralSubSelect.getSelect().accept((SelectVisitor) this);
    }

    /**
     * Initializes table names collector. Important is the usage of Column instances to find table
     * names. This is only allowed for expression parsing, where a better place for tablenames could
     * not be there. For complete statements only from items are used to avoid some alias as
     * tablenames.
     *
     * @param allowColumnProcessing
     */
    protected void init(boolean allowColumnProcessing) {
        otherItemNames = new ArrayList<String>();
        tables = new ArrayList<String>();
        this.allowColumnProcessing = allowColumnProcessing;
    }

    @Override
    public void visit(IntervalExpression iexpr) {

    }

    @Override
    public void visit(JdbcNamedParameter jdbcNamedParameter) {

    }

    @Override
    public void visit(OracleHierarchicalExpression oexpr) {
        if (oexpr.getStartExpression() != null) {
            oexpr.getStartExpression().accept(this);
        }

        if (oexpr.getConnectExpression() != null) {
            oexpr.getConnectExpression().accept(this);
        }
    }

    @Override
    public void visit(RegExpMatchOperator rexpr) {
        visitBinaryExpression(rexpr);
    }

    @Override
    public void visit(JsonExpression jsonExpr) {

    }

    @Override
    public void visit(JsonOperator jsonExpr) {

    }

    @Override
    public void visit(AllColumns allColumns) {

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(AllValue allValue) {

    }

    @Override
    public void visit(IsDistinctExpression isDistinctExpression) {
        visitBinaryExpression(isDistinctExpression);
    }

    @Override
    public void visit(SelectItem item) {
        item.getExpression().accept(this);
    }

    @Override
    public void visit(UserVariable var) {

    }

    @Override
    public void visit(NumericBind bind) {


    }

    @Override
    public void visit(KeepExpression aexpr) {

    }

    @Override
    public void visit(MySQLGroupConcat groupConcat) {

    }

    @Override
    public void visit(Delete delete) {
        visit(delete.getTable());

        if (delete.getUsingList() != null) {
            for (Table using : delete.getUsingList()) {
                visit(using);
            }
        }

        if (delete.getJoins() != null) {
            for (Join join : delete.getJoins()) {
                join.getFromItem().accept(this);
            }
        }

        if (delete.getWhere() != null) {
            delete.getWhere().accept(this);
        }
    }

    @Override
    public void visit(Update update) {
        visit(update.getTable());
        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                join.getFromItem().accept(this);
            }
        }
        if (update.getExpressions() != null) {
            for (Expression expression : update.getExpressions()) {
                expression.accept(this);
            }
        }

        if (update.getFromItem() != null) {
            update.getFromItem().accept(this);
        }

        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                join.getFromItem().accept(this);
            }
        }

        if (update.getWhere() != null) {
            update.getWhere().accept(this);
        }
    }

    @Override
    public void visit(Insert insert) {
        visit(insert.getTable());
        if (insert.getSelect() != null) {
            visit(insert.getSelect());
        }
    }

    public void visit(Analyze analyze) {
        visit(analyze.getTable());
    }

    @Override
    public void visit(Drop drop) {
        visit(drop.getName());
    }

    @Override
    public void visit(Truncate truncate) {
        visit(truncate.getTable());
    }

    @Override
    public void visit(CreateIndex createIndex) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(CreateSchema aThis) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(CreateTable create) {
        visit(create.getTable());
        if (create.getSelect() != null) {
            create.getSelect().accept((SelectVisitor) this);
        }
    }

    @Override
    public void visit(CreateView createView) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Alter alter) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Statements stmts) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(Execute execute) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(SetStatement set) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(ResetStatement reset) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(ShowColumnsStatement set) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(ShowIndexStatement showIndex) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(RowConstructor<?> rowConstructor) {
        for (Expression expr : rowConstructor) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(RowGetExpression rowGetExpression) {
        rowGetExpression.getExpression().accept(this);
    }

    @Override
    public void visit(HexValue hexValue) {


    }

    @Override
    public void visit(Merge merge) {
        visit(merge.getTable());
        merge.getFromItem().accept((FromItemVisitor) this);
    }

    @Override
    public void visit(OracleHint hint) {

    }

    @Override
    public void visit(TableFunction valuesList) {

    }

    @Override
    public void visit(AlterView alterView) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public void visit(TimeKeyExpression timeKeyExpression) {

    }

    @Override
    public void visit(DateTimeLiteralExpression literal) {


    }

    @Override
    public void visit(Commit commit) {


    }

    @Override
    public void visit(Upsert upsert) {
        visit(upsert.getTable());
        if (upsert.getExpressions() != null) {
            upsert.getExpressions().accept(this);
        }
        if (upsert.getSelect() != null) {
            visit(upsert.getSelect());
        }
    }

    @Override
    public void visit(UseStatement use) {

    }

    @Override
    public void visit(ParenthesedFromItem parenthesis) {
        parenthesis.getFromItem().accept(this);
    }

    @Override
    public void visit(Block block) {
        if (block.getStatements() != null) {
            visit(block.getStatements());
        }
    }

    @Override
    public void visit(Comment comment) {
        if (comment.getTable() != null) {
            visit(comment.getTable());
        }
        if (comment.getColumn() != null) {
            Table table = comment.getColumn().getTable();
            if (table != null) {
                visit(table);
            }
        }
    }

    @Override
    public void visit(Values values) {
        values.getExpressions().accept(this);
    }

    @Override
    public void visit(DescribeStatement describe) {
        describe.getTable().accept(this);
    }

    @Override
    public void visit(ExplainStatement explain) {
        explain.getStatement().accept((StatementVisitor) this);
    }

    @Override
    public void visit(NextValExpression nextVal) {

    }

    @Override
    public void visit(CollateExpression col) {
        col.getLeftExpression().accept(this);
    }

    @Override
    public void visit(ShowStatement aThis) {

    }

    @Override
    public void visit(SimilarToExpression expr) {
        visitBinaryExpression(expr);
    }

    @Override
    public void visit(DeclareStatement aThis) {

    }

    @Override
    public void visit(Grant grant) {


    }

    @Override
    public void visit(ArrayExpression array) {
        array.getObjExpression().accept(this);
        if (array.getStartIndexExpression() != null) {
            array.getIndexExpression().accept(this);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this);
        }
    }

    @Override
    public void visit(ArrayConstructor array) {
        for (Expression expression : array.getExpressions()) {
            expression.accept(this);
        }
    }

    @Override
    public void visit(CreateSequence createSequence) {
        throw new UnsupportedOperationException(
                "Finding tables from CreateSequence is not supported");
    }

    @Override
    public void visit(AlterSequence alterSequence) {
        throw new UnsupportedOperationException(
                "Finding tables from AlterSequence is not supported");
    }

    @Override
    public void visit(CreateFunctionalStatement createFunctionalStatement) {
        throw new UnsupportedOperationException(
                "Finding tables from CreateFunctionalStatement is not supported");
    }

    @Override
    public void visit(ShowTablesStatement showTables) {
        throw new UnsupportedOperationException(
                "Finding tables from ShowTablesStatement is not supported");
    }

    @Override
    public void visit(VariableAssignment var) {
        var.getVariable().accept(this);
        var.getExpression().accept(this);
    }

    @Override
    public void visit(XMLSerializeExpr aThis) {

    }

    @Override
    public void visit(CreateSynonym createSynonym) {
        throwUnsupported(createSynonym);
    }

    private static <T> void throwUnsupported(T type) {
        throw new UnsupportedOperationException(String.format(
                "Finding tables from %s is not supported", type.getClass().getSimpleName()));
    }

    @Override
    public void visit(TimezoneExpression aThis) {
        aThis.getLeftExpression().accept(this);
    }

    @Override
    public void visit(SavepointStatement savepointStatement) {}

    @Override
    public void visit(RollbackStatement rollbackStatement) {

    }

    @Override
    public void visit(AlterSession alterSession) {

    }

    @Override
    public void visit(JsonAggregateFunction expression) {
        Expression expr = expression.getExpression();
        if (expr != null) {
            expr.accept(this);
        }

        expr = expression.getFilterExpression();
        if (expr != null) {
            expr.accept(this);
        }
    }

    @Override
    public void visit(JsonFunction expression) {
        for (JsonFunctionExpression expr : expression.getExpressions()) {
            expr.getExpression().accept(this);
        }
    }

    @Override
    public void visit(ConnectByRootOperator connectByRootOperator) {
        connectByRootOperator.getColumn().accept(this);
    }

    public void visit(IfElseStatement ifElseStatement) {
        ifElseStatement.getIfStatement().accept(this);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this);
        }
    }

    public void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter) {
        oracleNamedFunctionParameter.getExpression().accept(this);
    }

    @Override
    public void visit(RenameTableStatement renameTableStatement) {
        for (Map.Entry<Table, Table> e : renameTableStatement.getTableNames()) {
            e.getKey().accept(this);
            e.getValue().accept(this);
        }
    }

    @Override
    public void visit(PurgeStatement purgeStatement) {
        if (purgeStatement.getPurgeObjectType() == PurgeObjectType.TABLE) {
            ((Table) purgeStatement.getObject()).accept(this);
        }
    }

    @Override
    public void visit(AlterSystemStatement alterSystemStatement) {
        // no tables involved in this statement
    }

    @Override
    public void visit(UnsupportedStatement unsupportedStatement) {
        // no tables involved in this statement
    }

    @Override
    public void visit(GeometryDistance geometryDistance) {
        visitBinaryExpression(geometryDistance);
    }

}
