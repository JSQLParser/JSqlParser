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

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.AllValue;
import net.sf.jsqlparser.expression.AnalyticExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.ArrayConstructor;
import net.sf.jsqlparser.expression.ArrayExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.CaseExpression;
import net.sf.jsqlparser.expression.CastExpression;
import net.sf.jsqlparser.expression.CollateExpression;
import net.sf.jsqlparser.expression.ConnectByRootOperator;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import net.sf.jsqlparser.expression.DateValue;
import net.sf.jsqlparser.expression.DoubleValue;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExtractExpression;
import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.expression.HexValue;
import net.sf.jsqlparser.expression.IntervalExpression;
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.JsonAggregateFunction;
import net.sf.jsqlparser.expression.JsonExpression;
import net.sf.jsqlparser.expression.JsonFunction;
import net.sf.jsqlparser.expression.JsonFunctionExpression;
import net.sf.jsqlparser.expression.KeepExpression;
import net.sf.jsqlparser.expression.LambdaExpression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.MySQLGroupConcat;
import net.sf.jsqlparser.expression.NextValExpression;
import net.sf.jsqlparser.expression.NotExpression;
import net.sf.jsqlparser.expression.NullValue;
import net.sf.jsqlparser.expression.NumericBind;
import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
import net.sf.jsqlparser.expression.OverlapsCondition;
import net.sf.jsqlparser.expression.RangeExpression;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.RowGetExpression;
import net.sf.jsqlparser.expression.SignedExpression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.StructType;
import net.sf.jsqlparser.expression.TimeKeyExpression;
import net.sf.jsqlparser.expression.TimeValue;
import net.sf.jsqlparser.expression.TimestampValue;
import net.sf.jsqlparser.expression.TimezoneExpression;
import net.sf.jsqlparser.expression.TranscodingFunction;
import net.sf.jsqlparser.expression.TrimFunction;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.expression.VariableAssignment;
import net.sf.jsqlparser.expression.WhenClause;
import net.sf.jsqlparser.expression.XMLSerializeExpr;
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
import net.sf.jsqlparser.expression.operators.relational.ContainedBy;
import net.sf.jsqlparser.expression.operators.relational.Contains;
import net.sf.jsqlparser.expression.operators.relational.DoubleAnd;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExcludesExpression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.IncludesExpression;
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
import net.sf.jsqlparser.expression.operators.relational.TSQLLeftJoin;
import net.sf.jsqlparser.expression.operators.relational.TSQLRightJoin;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
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
import net.sf.jsqlparser.statement.refresh.RefreshMaterializedViewStatement;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.OrderByElement;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.show.ShowIndexStatement;
import net.sf.jsqlparser.statement.show.ShowTablesStatement;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Find all used tables within an select statement.
 *
 * <p>
 * Override extractTableName method to modify the extracted table names (e.g. without schema).
 */
@SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.UncommentedEmptyMethodBody"})
public class TablesNamesFinder<Void>
        implements SelectVisitor<Void>, FromItemVisitor<Void>, ExpressionVisitor<Void>,
        SelectItemVisitor<Void>, StatementVisitor<Void> {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";
    private Set<String> tables;
    private boolean allowColumnProcessing = false;

    private List<String> otherItemNames;

    @Deprecated
    public List<String> getTableList(Statement statement) {
        return new ArrayList<String>(getTables(statement));
    }

    public Set<String> getTables(Statement statement) {
        init(false);
        statement.accept(this);

        // @todo: assess this carefully, maybe we want to remove more specifically
        // only Aliases on WithItems, Parenthesed Selects and Lateral Selects
        otherItemNames.forEach(tables::remove);

        return tables;
    }

    public Set<String> getTablesOrOtherSources(Statement statement) {
        init(false);
        statement.accept(this);

        HashSet<String> tablesOrOtherSources = new HashSet<>(tables);
        tablesOrOtherSources.addAll(otherItemNames);

        return tablesOrOtherSources;
    }

    public static Set<String> findTables(String sqlStr) throws JSQLParserException {
        TablesNamesFinder<?> tablesNamesFinder = new TablesNamesFinder<>();
        return tablesNamesFinder.getTables(CCJSqlParserUtil.parse(sqlStr));
    }

    public static Set<String> findTablesOrOtherSources(String sqlStr) throws JSQLParserException {
        TablesNamesFinder<?> tablesNamesFinder = new TablesNamesFinder<>();
        return tablesNamesFinder.getTablesOrOtherSources(CCJSqlParserUtil.parse(sqlStr));
    }

    @Override
    public <S> Void visit(Select select, S parameters) {
        List<WithItem> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, parameters);
            }
        }
        select.accept((SelectVisitor<?>) this, parameters);
        return null;
    }

    @Override
    public Void visit(Select select) {
        return visit(select, null);
    }

    @Override
    public <S> Void visit(TranscodingFunction transcodingFunction, S parameters) {
        transcodingFunction.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(TrimFunction trimFunction, S parameters) {
        if (trimFunction.getExpression() != null) {
            trimFunction.getExpression().accept(this, parameters);
        }
        if (trimFunction.getFromExpression() != null) {
            trimFunction.getFromExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(RangeExpression rangeExpression, S parameters) {
        rangeExpression.getStartExpression().accept(this, parameters);
        rangeExpression.getEndExpression().accept(this, parameters);
        return null;
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     */
    @Deprecated
    public List<String> getTableList(Expression expr) {
        return new ArrayList<String>(getTables(expr));
    }

    public Set<String> getTables(Expression expr) {
        init(true);
        expr.accept(this, null);
        return tables;
    }

    public static Set<String> findTablesInExpression(String exprStr) throws JSQLParserException {
        TablesNamesFinder<?> tablesNamesFinder = new TablesNamesFinder<>();
        return tablesNamesFinder.getTables(CCJSqlParserUtil.parseExpression(exprStr));
    }

    @Override
    public <S> Void visit(WithItem withItem, S parameters) {
        otherItemNames.add(withItem.getAlias().getName());
        withItem.getSelect().accept((SelectVisitor<?>) this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedSelect select, S parameters) {
        if (select.getAlias() != null) {
            otherItemNames.add(select.getAlias().getName());
        }
        List<WithItem> withItemsList = select.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, parameters);
            }
        }
        select.getSelect().accept((SelectVisitor<?>) this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(PlainSelect plainSelect, S parameters) {
        List<WithItem> withItemsList = plainSelect.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, parameters);
            }
        }
        if (plainSelect.getSelectItems() != null) {
            for (SelectItem<?> item : plainSelect.getSelectItems()) {
                item.accept(this, parameters);
            }
        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this, parameters);
        }

        visitJoins(plainSelect.getJoins(), parameters);
        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(this, parameters);
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(this, parameters);
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(this, parameters);
        }
        return null;
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
    public <S> Void visit(Table tableName, S parameters) {
        String tableWholeName = extractTableName(tableName);
        if (!otherItemNames.contains(tableWholeName)) {
            tables.add(tableWholeName);
        }
        return null;
    }

    @Override
    public <S> Void visit(Addition addition, S parameters) {
        visitBinaryExpression(addition);
        return null;
    }

    @Override
    public <S> Void visit(AndExpression andExpression, S parameters) {
        visitBinaryExpression(andExpression);
        return null;
    }

    @Override
    public <S> Void visit(Between between, S parameters) {
        between.getLeftExpression().accept(this, parameters);
        between.getBetweenExpressionStart().accept(this, parameters);
        between.getBetweenExpressionEnd().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(OverlapsCondition overlapsCondition, S parameters) {
        overlapsCondition.getLeft().accept(this, parameters);
        overlapsCondition.getRight().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(Column tableColumn, S parameters) {
        if (allowColumnProcessing && tableColumn.getTable() != null
                && tableColumn.getTable().getName() != null) {
            visit(tableColumn.getTable(), parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(Division division, S parameters) {
        visitBinaryExpression(division);
        return null;
    }

    @Override
    public <S> Void visit(IntegerDivision division, S parameters) {
        visitBinaryExpression(division);
        return null;
    }

    @Override
    public <S> Void visit(DoubleValue doubleValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(EqualsTo equalsTo, S parameters) {
        visitBinaryExpression(equalsTo);
        return null;
    }

    @Override
    public <S> Void visit(Function function, S parameters) {
        ExpressionList<?> exprList = function.getParameters();
        if (exprList != null) {
            visit(exprList, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(GreaterThan greaterThan, S parameters) {
        visitBinaryExpression(greaterThan);
        return null;
    }

    @Override
    public <S> Void visit(GreaterThanEquals greaterThanEquals, S parameters) {
        visitBinaryExpression(greaterThanEquals);
        return null;
    }

    @Override
    public <S> Void visit(InExpression inExpression, S parameters) {
        inExpression.getLeftExpression().accept(this, parameters);
        inExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(IncludesExpression includesExpression, S parameters) {
        includesExpression.getLeftExpression().accept(this, parameters);
        includesExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(ExcludesExpression excludesExpression, S parameters) {
        excludesExpression.getLeftExpression().accept(this, parameters);
        excludesExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(FullTextSearch fullTextSearch, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(SignedExpression signedExpression, S parameters) {
        signedExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(IsNullExpression isNullExpression, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(IsBooleanExpression isBooleanExpression, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(JdbcParameter jdbcParameter, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(LikeExpression likeExpression, S parameters) {
        visitBinaryExpression(likeExpression);
        return null;
    }

    @Override
    public <S> Void visit(ExistsExpression existsExpression, S parameters) {
        existsExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(MemberOfExpression memberOfExpression, S parameters) {
        memberOfExpression.getLeftExpression().accept(this, parameters);
        memberOfExpression.getRightExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(LongValue longValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(MinorThan minorThan, S parameters) {
        visitBinaryExpression(minorThan);
        return null;
    }

    @Override
    public <S> Void visit(MinorThanEquals minorThanEquals, S parameters) {
        visitBinaryExpression(minorThanEquals);
        return null;
    }

    @Override
    public <S> Void visit(Multiplication multiplication, S parameters) {
        visitBinaryExpression(multiplication);
        return null;
    }

    @Override
    public <S> Void visit(NotEqualsTo notEqualsTo, S parameters) {
        visitBinaryExpression(notEqualsTo);
        return null;
    }

    @Override
    public <S> Void visit(DoubleAnd doubleAnd, S parameters) {
        visitBinaryExpression(doubleAnd);
        return null;
    }

    @Override
    public <S> Void visit(Contains contains, S parameters) {
        visitBinaryExpression(contains);
        return null;
    }

    @Override
    public <S> Void visit(ContainedBy containedBy, S parameters) {
        visitBinaryExpression(containedBy);
        return null;
    }

    @Override
    public <S> Void visit(NullValue nullValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(OrExpression orExpression, S parameters) {
        visitBinaryExpression(orExpression);
        return null;
    }

    @Override
    public <S> Void visit(XorExpression xorExpression, S parameters) {
        visitBinaryExpression(xorExpression);
        return null;
    }

    @Override
    public <S> Void visit(StringValue stringValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(Subtraction subtraction, S parameters) {
        visitBinaryExpression(subtraction);
        return null;
    }

    @Override
    public <S> Void visit(NotExpression notExpr, S parameters) {
        notExpr.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseRightShift expr, S parameters) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseLeftShift expr, S parameters) {
        visitBinaryExpression(expr);
        return null;
    }

    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        binaryExpression.getLeftExpression().accept(this, null);
        binaryExpression.getRightExpression().accept(this, null);
    }

    @Override
    public <S> Void visit(ExpressionList<?> expressionList, S parameters) {
        for (Expression expression : expressionList) {
            expression.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(DateValue dateValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(TimestampValue timestampValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(TimeValue timeValue, S parameters) {

        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.
     * CaseExpression)
     */
    @Override
    public <S> Void visit(CaseExpression caseExpression, S parameters) {
        if (caseExpression.getSwitchExpression() != null) {
            caseExpression.getSwitchExpression().accept(this, parameters);
        }
        if (caseExpression.getWhenClauses() != null) {
            for (WhenClause when : caseExpression.getWhenClauses()) {
                when.accept(this, parameters);
            }
        }
        if (caseExpression.getElseExpression() != null) {
            caseExpression.getElseExpression().accept(this, parameters);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * net.sf.jsqlparser.expression.ExpressionVisitor#visit(net.sf.jsqlparser.expression.WhenClause)
     */
    @Override
    public <S> Void visit(WhenClause whenClause, S parameters) {
        if (whenClause.getWhenExpression() != null) {
            whenClause.getWhenExpression().accept(this, parameters);
        }
        if (whenClause.getThenExpression() != null) {
            whenClause.getThenExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(AnyComparisonExpression anyComparisonExpression, S parameters) {
        anyComparisonExpression.getSelect().accept((ExpressionVisitor<?>) this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(Concat concat, S parameters) {
        visitBinaryExpression(concat);
        return null;
    }

    @Override
    public <S> Void visit(Matches matches, S parameters) {
        visitBinaryExpression(matches);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseAnd bitwiseAnd, S parameters) {
        visitBinaryExpression(bitwiseAnd);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseOr bitwiseOr, S parameters) {
        visitBinaryExpression(bitwiseOr);
        return null;
    }

    @Override
    public <S> Void visit(BitwiseXor bitwiseXor, S parameters) {
        visitBinaryExpression(bitwiseXor);
        return null;
    }

    @Override
    public <S> Void visit(CastExpression cast, S parameters) {
        cast.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(Modulo modulo, S parameters) {
        visitBinaryExpression(modulo);
        return null;
    }

    @Override
    public <S> Void visit(AnalyticExpression analytic, S parameters) {
        if (analytic.getExpression() != null) {
            analytic.getExpression().accept(this, parameters);
        }
        if (analytic.getDefaultValue() != null) {
            analytic.getDefaultValue().accept(this, parameters);
        }
        if (analytic.getOffset() != null) {
            analytic.getOffset().accept(this, parameters);
        }
        if (analytic.getKeep() != null) {
            analytic.getKeep().accept(this, parameters);
        }
        if (analytic.getFuncOrderBy() != null) {
            for (OrderByElement element : analytic.getOrderByElements()) {
                element.getExpression().accept(this, parameters);
            }
        }

        if (analytic.getWindowElement() != null) {
            analytic.getWindowElement().getRange().getStart().getExpression().accept(this,
                    parameters);
            analytic.getWindowElement().getRange().getEnd().getExpression().accept(this,
                    parameters);
            analytic.getWindowElement().getOffset().getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(SetOperationList list, S parameters) {
        List<WithItem> withItemsList = list.getWithItemsList();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            for (WithItem withItem : withItemsList) {
                withItem.accept((SelectVisitor<?>) this, parameters);
            }
        }
        for (Select selectBody : list.getSelects()) {
            selectBody.accept((SelectVisitor<?>) this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(ExtractExpression eexpr, S parameters) {
        if (eexpr.getExpression() != null) {
            eexpr.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(LateralSubSelect lateralSubSelect, S parameters) {
        if (lateralSubSelect.getAlias() != null) {
            otherItemNames.add(lateralSubSelect.getAlias().getName());
        }
        lateralSubSelect.getSelect().accept((SelectVisitor<?>) this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(TableStatement tableStatement, S parameters) {
        tableStatement.getTable().accept(this, null);
        return null;
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
        tables = new HashSet<>();
        this.allowColumnProcessing = allowColumnProcessing;
    }

    @Override
    public <S> Void visit(IntervalExpression iexpr, S parameters) {
        if (iexpr.getExpression() != null) {
            iexpr.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(JdbcNamedParameter jdbcNamedParameter, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(OracleHierarchicalExpression oexpr, S parameters) {
        if (oexpr.getStartExpression() != null) {
            oexpr.getStartExpression().accept(this, parameters);
        }

        if (oexpr.getConnectExpression() != null) {
            oexpr.getConnectExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(RegExpMatchOperator rexpr, S parameters) {
        visitBinaryExpression(rexpr);
        return null;
    }

    @Override
    public <S> Void visit(JsonExpression jsonExpr, S parameters) {
        if (jsonExpr.getExpression() != null) {
            jsonExpr.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(JsonOperator jsonExpr, S parameters) {
        visitBinaryExpression(jsonExpr);
        return null;
    }

    @Override
    public <S> Void visit(AllColumns allColumns, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(AllTableColumns allTableColumns, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(AllValue allValue, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(IsDistinctExpression isDistinctExpression, S parameters) {
        visitBinaryExpression(isDistinctExpression);
        return null;
    }

    @Override
    public <S> Void visit(SelectItem<?> item, S parameters) {
        item.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(UserVariable var, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(NumericBind bind, S parameters) {


        return null;
    }

    @Override
    public <S> Void visit(KeepExpression aexpr, S parameters) {

        return null;
    }

    @Override
    public <S> Void visit(MySQLGroupConcat groupConcat, S parameters) {

        return null;
    }

    @Override
    public Void visit(Delete delete) {
        visit(delete.getTable(), null);

        if (delete.getUsingList() != null) {
            for (Table using : delete.getUsingList()) {
                visit(using, null);
            }
        }

        visitJoins(delete.getJoins(), null);

        if (delete.getWhere() != null) {
            delete.getWhere().accept(this, null);
        }
        return null;
    }

    @Override
    public Void visit(Update update) {
        visit(update.getTable(), null);
        if (update.getWithItemsList() != null) {
            for (WithItem withItem : update.getWithItemsList()) {
                withItem.accept((SelectVisitor<?>) this, null);
            }
        }

        if (update.getStartJoins() != null) {
            for (Join join : update.getStartJoins()) {
                join.getRightItem().accept(this, null);
            }
        }
        if (update.getExpressions() != null) {
            for (Expression expression : update.getExpressions()) {
                expression.accept(this, null);
            }
        }

        if (update.getFromItem() != null) {
            update.getFromItem().accept(this, null);
        }

        if (update.getJoins() != null) {
            for (Join join : update.getJoins()) {
                join.getRightItem().accept(this, null);
                for (Expression expression : join.getOnExpressions()) {
                    expression.accept(this, null);
                }
            }
        }

        if (update.getWhere() != null) {
            update.getWhere().accept(this, null);
        }
        return null;
    }

    @Override
    public Void visit(Insert insert) {
        visit(insert.getTable(), null);
        if (insert.getWithItemsList() != null) {
            for (WithItem withItem : insert.getWithItemsList()) {
                withItem.accept((SelectVisitor<?>) this, null);
            }
        }
        if (insert.getSelect() != null) {
            visit(insert.getSelect());
        }
        return null;
    }

    public Void visit(Analyze analyze) {
        visit(analyze.getTable(), null);
        return null;
    }

    @Override
    public Void visit(Drop drop) {
        visit(drop.getName(), null);
        return null;
    }

    @Override
    public Void visit(Truncate truncate) {
        visit(truncate.getTable(), null);
        return null;
    }

    @Override
    public Void visit(CreateIndex createIndex) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(CreateSchema aThis) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(CreateTable create) {
        visit(create.getTable(), null);
        if (create.getSelect() != null) {
            create.getSelect().accept((SelectVisitor<?>) this, null);
        }
        return null;
    }

    @Override
    public Void visit(CreateView createView) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(Alter alter) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(Statements stmts) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(Execute execute) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(SetStatement set) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(ResetStatement reset) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(ShowColumnsStatement set) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(ShowIndexStatement showIndex) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public <S> Void visit(RowConstructor<?> rowConstructor, S parameters) {
        for (Expression expr : rowConstructor) {
            expr.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(RowGetExpression rowGetExpression, S parameters) {
        rowGetExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(HexValue hexValue, S parameters) {
        return null;
    }

    @Override
    public Void visit(Merge merge) {
        visit(merge.getTable(), null);
        if (merge.getWithItemsList() != null) {
            for (WithItem withItem : merge.getWithItemsList()) {
                withItem.accept((SelectVisitor<?>) this, null);
            }
        }

        if (merge.getFromItem() != null) {
            merge.getFromItem().accept(this, null);
        }
        return null;
    }


    @Override
    public <S> Void visit(OracleHint hint, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(TableFunction tableFunction, S parameters) {
        visit(tableFunction.getFunction(), null);
        return null;
    }

    @Override
    public Void visit(AlterView alterView) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET);
    }

    @Override
    public Void visit(RefreshMaterializedViewStatement materializedView) {
        visit(materializedView.getView(), null);
        return null;
    }

    @Override
    public <S> Void visit(TimeKeyExpression timeKeyExpression, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(DateTimeLiteralExpression literal, S parameters) {
        return null;
    }

    @Override
    public Void visit(Commit commit) {
        return null;
    }

    @Override
    public Void visit(Upsert upsert) {
        visit(upsert.getTable(), null);
        if (upsert.getExpressions() != null) {
            upsert.getExpressions().accept(this, null);
        }
        if (upsert.getSelect() != null) {
            visit(upsert.getSelect());
        }
        return null;
    }

    @Override
    public Void visit(UseStatement use) {
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedFromItem parenthesis, S parameters) {
        if (parenthesis.getAlias() != null) {
            otherItemNames.add(parenthesis.getAlias().getName());
        }
        parenthesis.getFromItem().accept(this, parameters);
        // support join keyword in fromItem
        visitJoins(parenthesis.getJoins(), parameters);
        return null;
    }

    /**
     * visit join block
     *
     * @param joins join sql block
     */
    private <S> void visitJoins(List<Join> joins, S parameters) {
        if (joins == null) {
            return;
        }
        for (Join join : joins) {
            join.getFromItem().accept(this, parameters);
            join.getRightItem().accept(this, parameters);
            for (Expression expression : join.getOnExpressions()) {
                expression.accept(this, parameters);
            }
        }
    }

    @Override
    public Void visit(Block block) {
        if (block.getStatements() != null) {
            visit(block.getStatements());
        }
        return null;
    }

    @Override
    public Void visit(Comment comment) {
        if (comment.getTable() != null) {
            visit(comment.getTable(), null);
        }
        if (comment.getColumn() != null) {
            Table table = comment.getColumn().getTable();
            if (table != null) {
                visit(table, null);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(Values values, S parameters) {
        values.getExpressions().accept(this, parameters);
        return null;
    }

    @Override
    public Void visit(DescribeStatement describe) {
        describe.getTable().accept(this, null);
        return null;
    }

    @Override
    public Void visit(ExplainStatement explain) {
        if (explain.getStatement() != null) {
            explain.getStatement().accept((StatementVisitor<?>) this);
        }
        return null;
    }

    @Override
    public <S> Void visit(NextValExpression nextVal, S parameters) {
        return null;
    }

    @Override
    public <S> Void visit(CollateExpression col, S parameters) {
        col.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public Void visit(ShowStatement showStatement) {
        return null;
    }

    @Override
    public <S> Void visit(SimilarToExpression expr, S parameters) {
        visitBinaryExpression(expr);
        return null;
    }

    @Override
    public Void visit(DeclareStatement aThis) {
        return null;
    }

    @Override
    public Void visit(Grant grant) {
        return null;
    }

    @Override
    public <S> Void visit(ArrayExpression array, S parameters) {
        array.getObjExpression().accept(this, parameters);
        if (array.getStartIndexExpression() != null) {
            array.getIndexExpression().accept(this, parameters);
        }
        if (array.getStartIndexExpression() != null) {
            array.getStartIndexExpression().accept(this, parameters);
        }
        if (array.getStopIndexExpression() != null) {
            array.getStopIndexExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(ArrayConstructor array, S parameters) {
        for (Expression expression : array.getExpressions()) {
            expression.accept(this, parameters);
        }
        return null;
    }

    @Override
    public Void visit(CreateSequence createSequence) {
        throw new UnsupportedOperationException(
                "Finding tables from CreateSequence is not supported");
    }

    @Override
    public Void visit(AlterSequence alterSequence) {
        throw new UnsupportedOperationException(
                "Finding tables from AlterSequence is not supported");
    }

    @Override
    public Void visit(CreateFunctionalStatement createFunctionalStatement) {
        throw new UnsupportedOperationException(
                "Finding tables from CreateFunctionalStatement is not supported");
    }

    @Override
    public Void visit(ShowTablesStatement showTables) {
        throw new UnsupportedOperationException(
                "Finding tables from ShowTablesStatement is not supported");
    }

    @Override
    public <S> Void visit(TSQLLeftJoin tsqlLeftJoin, S parameters) {
        visitBinaryExpression(tsqlLeftJoin);
        return null;
    }

    @Override
    public <S> Void visit(TSQLRightJoin tsqlRightJoin, S parameters) {
        visitBinaryExpression(tsqlRightJoin);
        return null;
    }

    @Override
    public <S> Void visit(StructType structType, S parameters) {
        if (structType.getArguments() != null) {
            for (SelectItem<?> selectItem : structType.getArguments()) {
                selectItem.getExpression().accept(this, parameters);
            }
        }
        return null;
    }

    @Override
    public <S> Void visit(LambdaExpression lambdaExpression, S parameters) {
        lambdaExpression.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(VariableAssignment var, S parameters) {
        var.getVariable().accept(this, parameters);
        var.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public <S> Void visit(XMLSerializeExpr aThis, S parameters) {

        return null;
    }

    @Override
    public Void visit(CreateSynonym createSynonym) {
        throwUnsupported(createSynonym);
        return null;
    }

    private static <T> void throwUnsupported(T type) {
        throw new UnsupportedOperationException(String.format(
                "Finding tables from %s is not supported", type.getClass().getSimpleName()));
    }

    @Override
    public <S> Void visit(TimezoneExpression aThis, S parameters) {
        aThis.getLeftExpression().accept(this, parameters);
        return null;
    }

    @Override
    public Void visit(SavepointStatement savepointStatement) {
        return null;
    }

    @Override
    public Void visit(RollbackStatement rollbackStatement) {

        return null;
    }

    @Override
    public Void visit(AlterSession alterSession) {

        return null;
    }

    @Override
    public <S> Void visit(JsonAggregateFunction expression, S parameters) {
        Expression expr = expression.getExpression();
        if (expr != null) {
            expr.accept(this, parameters);
        }

        expr = expression.getFilterExpression();
        if (expr != null) {
            expr.accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(JsonFunction expression, S parameters) {
        for (JsonFunctionExpression expr : expression.getExpressions()) {
            expr.getExpression().accept(this, parameters);
        }
        return null;
    }

    @Override
    public <S> Void visit(ConnectByRootOperator connectByRootOperator, S parameters) {
        connectByRootOperator.getColumn().accept(this, parameters);
        return null;
    }

    @Override
    public Void visit(IfElseStatement ifElseStatement) {
        ifElseStatement.getIfStatement().accept(this);
        if (ifElseStatement.getElseStatement() != null) {
            ifElseStatement.getElseStatement().accept(this);
        }
        return null;
    }

    @Override
    public <S> Void visit(OracleNamedFunctionParameter oracleNamedFunctionParameter, S parameters) {
        oracleNamedFunctionParameter.getExpression().accept(this, parameters);
        return null;
    }

    @Override
    public Void visit(RenameTableStatement renameTableStatement) {
        for (Map.Entry<Table, Table> e : renameTableStatement.getTableNames()) {
            e.getKey().accept(this, null);
            e.getValue().accept(this, null);
        }
        return null;
    }

    @Override
    public Void visit(PurgeStatement purgeStatement) {
        if (purgeStatement.getPurgeObjectType() == PurgeObjectType.TABLE) {
            ((Table) purgeStatement.getObject()).accept(this, null);
        }
        return null;
    }

    @Override
    public Void visit(AlterSystemStatement alterSystemStatement) {
        // no tables involved in this statement
        return null;
    }

    @Override
    public Void visit(UnsupportedStatement unsupportedStatement) {
        // no tables involved in this statement
        return null;
    }

    @Override
    public <S> Void visit(GeometryDistance geometryDistance, S parameters) {
        visitBinaryExpression(geometryDistance);
        return null;
    }

}
