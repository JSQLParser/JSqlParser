/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.builder;

import static net.sf.jsqlparser.test.TestUtils.*;
import java.util.List;
import org.junit.Test;
import net.sf.jsqlparser.expression.AnyType;
import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperatorType;
import net.sf.jsqlparser.schema.Sequence.ParameterType;
import net.sf.jsqlparser.statement.ExplainStatement.OptionType;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.util.ReflectionTestUtils;

/**
 * Testing of setters, getters, with-/add-methods by calling them with random
 * parameter-values
 * <ul>
 * <li>testing, whether return-value is the specific type (not the parent)
 * <li>testing, whether calling the methods do not throw any exceptions
 * </ul>
 *
 * @author gitmotte
 */
public class ReflectionModelTest {

    private static final List<Object> MODEL_OBJECTS = asList(new net.sf.jsqlparser.expression.Alias("a"),
            new net.sf.jsqlparser.expression.Alias.AliasColumn("a", new ColDataType("varchar")),
            new net.sf.jsqlparser.expression.AllComparisonExpression(new SubSelect()),
            new net.sf.jsqlparser.expression.AnalyticExpression(),
            new net.sf.jsqlparser.expression.AnyComparisonExpression(AnyType.ANY, new SubSelect()),
            new net.sf.jsqlparser.expression.ArrayExpression(),
            new net.sf.jsqlparser.expression.CaseExpression(), new net.sf.jsqlparser.expression.CastExpression(),
            new net.sf.jsqlparser.expression.CollateExpression(),
            new net.sf.jsqlparser.expression.DateTimeLiteralExpression(),
            new net.sf.jsqlparser.expression.DateValue(), new net.sf.jsqlparser.expression.DoubleValue(),
            new net.sf.jsqlparser.expression.ExtractExpression(), new net.sf.jsqlparser.expression.Function(),
            new net.sf.jsqlparser.expression.HexValue(), new net.sf.jsqlparser.expression.IntervalExpression(),
            new net.sf.jsqlparser.expression.JdbcNamedParameter(), new net.sf.jsqlparser.expression.JdbcParameter(),
            new net.sf.jsqlparser.expression.JsonExpression(), new net.sf.jsqlparser.expression.KeepExpression(),
            new net.sf.jsqlparser.expression.LongValue(),
            new net.sf.jsqlparser.expression.MySQLGroupConcat(),
            new net.sf.jsqlparser.expression.MySQLIndexHint("action", "indexQualifier",
                    asList("idx_name", "idx_name_col")),
            new net.sf.jsqlparser.expression.NextValExpression(asList("sequence" ), "NEXT VALUE"),
            new net.sf.jsqlparser.expression.NotExpression(),
            new net.sf.jsqlparser.expression.NullValue(), new net.sf.jsqlparser.expression.NumericBind(),
            new net.sf.jsqlparser.expression.OracleHierarchicalExpression(),
            new net.sf.jsqlparser.expression.OracleHint(), new net.sf.jsqlparser.expression.OrderByClause(),
            new net.sf.jsqlparser.expression.Parenthesis(), new net.sf.jsqlparser.expression.PartitionByClause(),
            new net.sf.jsqlparser.expression.RowConstructor(), new net.sf.jsqlparser.expression.SQLServerHints(),
            new net.sf.jsqlparser.expression.SignedExpression(), new net.sf.jsqlparser.expression.StringValue(),
            new net.sf.jsqlparser.expression.TimeKeyExpression(), new net.sf.jsqlparser.expression.TimeValue(),
            new net.sf.jsqlparser.expression.TimestampValue(), new net.sf.jsqlparser.expression.UserVariable(),
            new net.sf.jsqlparser.expression.ValueListExpression(), new net.sf.jsqlparser.expression.WhenClause(),
            new net.sf.jsqlparser.expression.WindowElement(),
            new net.sf.jsqlparser.expression.WindowOffset(),
            new net.sf.jsqlparser.expression.WindowRange(),
            new net.sf.jsqlparser.expression.operators.arithmetic.Addition(),
            new net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd(),
            new net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift(),
            new net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr(),
            new net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift(),
            new net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor(),
            new net.sf.jsqlparser.expression.operators.arithmetic.Concat(),
            new net.sf.jsqlparser.expression.operators.arithmetic.Division(),
            new net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision(),
            new net.sf.jsqlparser.expression.operators.arithmetic.Modulo(),
            new net.sf.jsqlparser.expression.operators.arithmetic.Multiplication(),
            new net.sf.jsqlparser.expression.operators.arithmetic.Subtraction(),
            new net.sf.jsqlparser.expression.operators.conditional.AndExpression(),
            new net.sf.jsqlparser.expression.operators.conditional.OrExpression(),
            new net.sf.jsqlparser.expression.operators.relational.Between(),
            new net.sf.jsqlparser.expression.operators.relational.EqualsTo(),
            new net.sf.jsqlparser.expression.operators.relational.ExistsExpression(),
            new net.sf.jsqlparser.expression.operators.relational.ExpressionList(),
            new net.sf.jsqlparser.expression.operators.relational.FullTextSearch(),
            new net.sf.jsqlparser.expression.operators.relational.GreaterThan(),
            new net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals(),
            new net.sf.jsqlparser.expression.operators.relational.InExpression(),
            new net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression(),
            new net.sf.jsqlparser.expression.operators.relational.IsNullExpression(),
            new net.sf.jsqlparser.expression.operators.relational.JsonOperator("@>"),
            new net.sf.jsqlparser.expression.operators.relational.LikeExpression(),
            new net.sf.jsqlparser.expression.operators.relational.Matches(),
            new net.sf.jsqlparser.expression.operators.relational.MinorThan(),
            new net.sf.jsqlparser.expression.operators.relational.MinorThanEquals(),
            new net.sf.jsqlparser.expression.operators.relational.MultiExpressionList(),
            new net.sf.jsqlparser.expression.operators.relational.NamedExpressionList(),
            new net.sf.jsqlparser.expression.operators.relational.NotEqualsTo(),
            new net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator(
                    RegExpMatchOperatorType.MATCH_CASEINSENSITIVE),
            new net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator(
                    RegExpMatchOperatorType.NOT_MATCH_CASESENSITIVE),
            new net.sf.jsqlparser.expression.operators.relational.SimilarToExpression(),
            new net.sf.jsqlparser.schema.Column(),
            new net.sf.jsqlparser.schema.Database("db"),
            new net.sf.jsqlparser.schema.Sequence(),
            new net.sf.jsqlparser.schema.Sequence.Parameter(ParameterType.KEEP),
            new net.sf.jsqlparser.schema.Server("srv"),
            new net.sf.jsqlparser.schema.Table(), new net.sf.jsqlparser.statement.Block(),
            new net.sf.jsqlparser.statement.Commit(),
            new net.sf.jsqlparser.statement.DeclareStatement(),
            new net.sf.jsqlparser.statement.DescribeStatement(),
            new net.sf.jsqlparser.statement.ExplainStatement(),
            new net.sf.jsqlparser.statement.ExplainStatement.Option(OptionType.COSTS),
            new net.sf.jsqlparser.statement.SetStatement("name", null),
            new net.sf.jsqlparser.statement.ShowColumnsStatement(),
            new net.sf.jsqlparser.statement.ShowStatement(), new net.sf.jsqlparser.statement.Statements(),
            new net.sf.jsqlparser.statement.UseStatement(),
            new net.sf.jsqlparser.statement.alter.Alter(),
            new net.sf.jsqlparser.statement.alter.AlterExpression(),
            new net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDataType(false),
            new net.sf.jsqlparser.statement.alter.AlterExpression.ColumnDropNotNull("name"),
            new net.sf.jsqlparser.statement.merge.MergeInsert(),
            new net.sf.jsqlparser.statement.alter.DeferrableConstraint(),
            new net.sf.jsqlparser.statement.alter.EnableConstraint(),
            new net.sf.jsqlparser.statement.alter.ValidateConstraint(),
            new net.sf.jsqlparser.statement.alter.sequence.AlterSequence(),
            new net.sf.jsqlparser.statement.comment.Comment(),
            new net.sf.jsqlparser.statement.create.function.CreateFunction(),
            new net.sf.jsqlparser.statement.create.index.CreateIndex(),
            new net.sf.jsqlparser.statement.create.procedure.CreateProcedure(),
            new net.sf.jsqlparser.statement.create.schema.CreateSchema(),
            new net.sf.jsqlparser.statement.create.sequence.CreateSequence(),
            new net.sf.jsqlparser.statement.create.table.CheckConstraint(),
            new net.sf.jsqlparser.statement.create.table.ColDataType(),
            new net.sf.jsqlparser.statement.create.table.ColumnDefinition(),
            new net.sf.jsqlparser.statement.create.table.CreateTable(),
            new net.sf.jsqlparser.statement.create.table.ExcludeConstraint(),
            new net.sf.jsqlparser.statement.create.table.ForeignKeyIndex(),
            new net.sf.jsqlparser.statement.create.table.Index(),
            new net.sf.jsqlparser.statement.create.table.Index.ColumnParams("p"),
            new net.sf.jsqlparser.statement.create.table.NamedConstraint(),
            new net.sf.jsqlparser.statement.create.table.RowMovement(),
            new net.sf.jsqlparser.statement.create.view.AlterView(),
            new net.sf.jsqlparser.statement.create.view.CreateView(),
            new net.sf.jsqlparser.statement.delete.Delete(),
            new net.sf.jsqlparser.statement.drop.Drop(),
            new net.sf.jsqlparser.statement.execute.Execute(), new net.sf.jsqlparser.statement.grant.Grant(),
            new net.sf.jsqlparser.statement.insert.Insert(), new net.sf.jsqlparser.statement.merge.Merge(),
            new net.sf.jsqlparser.statement.merge.MergeUpdate(), new net.sf.jsqlparser.statement.replace.Replace(),
            new net.sf.jsqlparser.statement.select.AllColumns(),
            new net.sf.jsqlparser.statement.select.AllTableColumns(),
            new net.sf.jsqlparser.statement.select.Distinct(), new net.sf.jsqlparser.statement.select.ExceptOp(),
            new net.sf.jsqlparser.statement.select.ExpressionListItem(),
            new net.sf.jsqlparser.statement.select.Fetch(), new net.sf.jsqlparser.statement.select.First(),
            new net.sf.jsqlparser.statement.select.FunctionItem(),
            new net.sf.jsqlparser.statement.select.GroupByElement(),
            new net.sf.jsqlparser.statement.select.IntersectOp(),
            new net.sf.jsqlparser.statement.select.Join(), new net.sf.jsqlparser.statement.select.KSQLJoinWindow(),
            new net.sf.jsqlparser.statement.select.KSQLWindow(),
            new net.sf.jsqlparser.statement.select.LateralSubSelect(),
            new net.sf.jsqlparser.statement.select.Limit(),
            new net.sf.jsqlparser.statement.select.MinusOp(),
            new net.sf.jsqlparser.statement.select.Offset(), new net.sf.jsqlparser.statement.select.OptimizeFor(2L),
            new net.sf.jsqlparser.statement.select.OrderByElement(),
            new net.sf.jsqlparser.statement.select.ParenthesisFromItem(),
            new net.sf.jsqlparser.statement.select.Pivot(),
            new net.sf.jsqlparser.statement.select.PivotXml(), new net.sf.jsqlparser.statement.select.PlainSelect(),
            new net.sf.jsqlparser.statement.select.Select(),
            new net.sf.jsqlparser.statement.select.SelectExpressionItem(),
            new net.sf.jsqlparser.statement.select.SetOperationList(),
            new net.sf.jsqlparser.statement.select.Skip(),
            new net.sf.jsqlparser.statement.select.SubJoin(),
            new net.sf.jsqlparser.statement.select.SubSelect(),
            new net.sf.jsqlparser.statement.select.TableFunction(), new net.sf.jsqlparser.statement.select.Top(),
            new net.sf.jsqlparser.statement.select.UnPivot(), new net.sf.jsqlparser.statement.select.UnionOp(),
            new net.sf.jsqlparser.statement.select.ValuesList(), new net.sf.jsqlparser.statement.select.Wait(),
            new net.sf.jsqlparser.statement.select.WithItem(),
            new net.sf.jsqlparser.statement.truncate.Truncate(),
            new net.sf.jsqlparser.statement.update.Update(), new net.sf.jsqlparser.statement.upsert.Upsert(),
            new net.sf.jsqlparser.statement.values.ValuesStatement(),
            new net.sf.jsqlparser.statement.DeclareStatement.TypeDefExpr(new ColDataType("varchar"), null));

    @Test
    public void testModels() {
        ReflectionTestUtils.testGetterSetterChaining(MODEL_OBJECTS, m -> !"setASTNode".equals(m.getName()));
    }

}
