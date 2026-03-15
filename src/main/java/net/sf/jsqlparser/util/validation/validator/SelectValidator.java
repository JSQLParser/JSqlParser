/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import java.util.List;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.from.JsonTable;
import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.SQLServerHints;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.imprt.Import;
import net.sf.jsqlparser.statement.piped.FromQuery;
import net.sf.jsqlparser.statement.select.ExceptOp;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.ForMode;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.IntersectOp;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.MinusOp;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
import net.sf.jsqlparser.statement.select.ParenthesedSelect;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.TableStatement;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.UnionOp;
import net.sf.jsqlparser.statement.select.Values;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.ValidationUtil;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class SelectValidator extends AbstractValidator<SelectItem<?>>
        implements SelectVisitor<Void>, SelectItemVisitor<Void>, FromItemVisitor<Void>,
        PivotVisitor<Void> {

    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    @Override
    public <S> Void visit(PlainSelect plainSelect, S context) {
        if (isNotEmpty(plainSelect.getWithItemsList())) {
            plainSelect.getWithItemsList()
                    .forEach(withItem -> withItem.accept((SelectVisitor<Void>) this, context));
        }

        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.select);
            validateFeature(c, plainSelect.getMySqlHintStraightJoin(),
                    Feature.mySqlHintStraightJoin);
            validateOptionalFeature(c, plainSelect.getOracleHint(), Feature.oracleHint);
            validateOptionalFeature(c, plainSelect.getSkip(), Feature.skip);
            validateOptionalFeature(c, plainSelect.getFirst(), Feature.first);

            if (plainSelect.getDistinct() != null) {
                if (plainSelect.getDistinct().isUseUnique()) {
                    validateFeature(c, Feature.selectUnique);
                } else {
                    validateFeature(c, Feature.distinct);
                }
                validateOptionalFeature(c, plainSelect.getDistinct().getOnSelectItems(),
                        Feature.distinctOn);
            }

            validateOptionalFeature(c, plainSelect.getTop(), Feature.top);
            validateFeature(c, plainSelect.getMySqlSqlCacheFlag() != null,
                    Feature.mysqlSqlCacheFlag);
            validateFeature(c, plainSelect.getMySqlSqlCalcFoundRows(), Feature.mysqlCalcFoundRows);
            validateOptionalFeature(c, plainSelect.getIntoTables(), Feature.selectInto);
            validateOptionalFeature(c, plainSelect.getKsqlWindow(), Feature.kSqlWindow);
            validateFeature(c,
                    isNotEmpty(plainSelect.getOrderByElements()) && plainSelect.isOracleSiblings(),
                    Feature.oracleOrderBySiblings);

            if (plainSelect.getForMode() != null) {
                validateFeature(c, Feature.selectForUpdate);
                validateFeature(c, plainSelect.getForMode() == ForMode.KEY_SHARE,
                        Feature.selectForKeyShare);
                validateFeature(c, plainSelect.getForMode() == ForMode.NO_KEY_UPDATE,
                        Feature.selectForNoKeyUpdate);
                validateFeature(c, plainSelect.getForMode() == ForMode.SHARE,
                        Feature.selectForShare);

                validateOptionalFeature(c, plainSelect.getForUpdateTable(),
                        Feature.selectForUpdateOfTable);
                validateOptionalFeature(c, plainSelect.getWait(), Feature.selectForUpdateWait);
                validateFeature(c, plainSelect.isNoWait(), Feature.selectForUpdateNoWait);
                validateFeature(c, plainSelect.isSkipLocked(), Feature.selectForUpdateSkipLocked);
            }

            validateOptionalFeature(c, plainSelect.getForXmlPath(), Feature.selectForXmlPath);
            validateOptionalFeature(c, plainSelect.getOptimizeFor(), Feature.optimizeFor);
        } // end for

        validateOptionalFromItem(plainSelect.getFromItem());
        validateOptionalFromItems(plainSelect.getIntoTables());
        validateOptionalJoins(plainSelect.getJoins());

        // to correctly recognize aliased tables
        // @todo: fix this properly, I don't understand functional syntax
        // validateOptionalList(plainSelect.getSelectItems(), () -> this, SelectItem::accept,
        // context);

        validateOptionalExpression(plainSelect.getPreWhere());
        validateOptionalExpression(plainSelect.getWhere());
        validateOptionalExpression(plainSelect.getOracleHierarchical());

        if (plainSelect.getGroupBy() != null) {
            plainSelect.getGroupBy().accept(getValidator(GroupByValidator.class), context);
        }

        validateOptionalExpression(plainSelect.getHaving());
        validateOptionalOrderByElements(plainSelect.getOrderByElements());

        if (plainSelect.getLimit() != null) {
            getValidator(LimitValidator.class).validate(plainSelect.getLimit());
        }

        if (plainSelect.getOffset() != null) {
            validateOffset(plainSelect.getOffset());
        }

        if (plainSelect.getFetch() != null) {
            validateFetch(plainSelect.getFetch());
        }

        validateOptional(plainSelect.getPivot(), p -> p.accept(this, context));

        return null;
    }

    @Override
    public <S> Void visit(SelectItem<?> selectExpressionItem, S context) {
        selectExpressionItem.getExpression().accept(getValidator(ExpressionValidator.class),
                context);
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedSelect selectBody, S context) {
        if (isNotEmpty(selectBody.getWithItemsList())) {
            selectBody.getWithItemsList()
                    .forEach(withItem -> withItem.accept((SelectVisitor<Void>) this, context));
        }
        selectBody.getSelect().accept((SelectVisitor<Void>) this, context);
        validateOptional(selectBody.getPivot(), p -> p.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(Table table, S context) {
        validateNameWithAlias(NamedObject.table, table.getFullyQualifiedName(),
                ValidationUtil.getAlias(table.getAlias()));

        validateOptional(table.getPivot(), p -> p.accept(this, context));
        validateOptional(table.getUnPivot(), up -> up.accept(this, context));

        MySQLIndexHint indexHint = table.getIndexHint();
        if (indexHint != null && isNotEmpty(indexHint.getIndexNames())) {
            indexHint.getIndexNames().forEach(i -> validateName(NamedObject.index, i));
        }
        SQLServerHints sqlServerHints = table.getSqlServerHints();
        if (sqlServerHints != null) {
            validateName(NamedObject.index, sqlServerHints.getIndexName());
        }
        return null;
    }

    @Override
    public <S> Void visit(Pivot pivot, S context) {
        validateFeature(Feature.pivot);
        validateOptionalExpressions(pivot.getForColumns());
        return null;
    }

    @Override
    public <S> Void visit(UnPivot unpivot, S context) {
        validateFeature(Feature.unpivot);

        validateOptionalExpressions(unpivot.getUnPivotForClause());
        validateOptionalExpressions(unpivot.getUnPivotClause());
        return null;
    }

    @Override
    public <S> Void visit(PivotXml pivot, S context) {
        validateFeature(Feature.pivotXml);
        validateOptionalExpressions(pivot.getForColumns());
        if (isNotEmpty(pivot.getFunctionItems())) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            pivot.getFunctionItems().forEach(f -> f.getExpression().accept(v, context));
        }
        if (pivot.getInSelect() != null) {
            pivot.getInSelect().accept((SelectVisitor<Void>) this, context);
        }
        return null;
    }

    public void validateOffset(Offset offset) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.offset);
            validateOptionalFeature(c, offset.getOffsetParam(), Feature.offsetParam);
        }
    }

    public void validateFetch(Fetch fetch) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.fetch);
            validateFeature(c, fetch.isFetchParamFirst(), Feature.fetchFirst);
            validateFeature(c, !fetch.isFetchParamFirst(), Feature.fetchNext);
        }

        validateOptionalExpression(fetch.getFetchJdbcParameter());
    }

    public void validateOptionalJoins(List<Join> joins) {
        if (joins != null) {
            for (Join join : joins) {
                validateOptionalJoin(join);
            }
        }
    }

    public void validateOptionalJoin(Join join) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.join);
            validateFeature(c, join.isSimple() && join.isOuter(), Feature.joinOuterSimple);
            validateFeature(c, join.isSimple(), Feature.joinSimple);
            validateFeature(c, join.isRight(), Feature.joinRight);
            validateFeature(c, join.isNatural(), Feature.joinNatural);
            validateFeature(c, join.isFull(), Feature.joinFull);
            validateFeature(c, join.isLeft(), Feature.joinLeft);
            validateFeature(c, join.isCross(), Feature.joinCross);
            validateFeature(c, join.isOuter(), Feature.joinOuter);
            validateFeature(c, join.isInner(), Feature.joinInner);
            validateFeature(c, join.isSemi(), Feature.joinSemi);
            validateFeature(c, join.isStraight(), Feature.joinStraight);
            validateFeature(c, join.isApply(), Feature.joinApply);
            validateFeature(c, join.isWindowJoin(), Feature.joinWindow);
            validateOptionalFeature(c, join.getUsingColumns(), Feature.joinUsingColumns);
        }

        validateOptionalFromItem(join.getFromItem());
        for (Expression onExpression : join.getOnExpressions()) {
            validateOptionalExpression(onExpression);
        }
        validateOptionalExpressions(join.getUsingColumns());
    }

    @Override
    public <S> Void visit(SetOperationList setOperation, S context) {
        if (isNotEmpty(setOperation.getWithItemsList())) {
            setOperation.getWithItemsList()
                    .forEach(withItem -> withItem.accept((SelectVisitor<Void>) this, context));
        }
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.setOperation);
            validateFeature(c,
                    setOperation.getOperations().stream().anyMatch(o -> o instanceof UnionOp),
                    Feature.setOperationUnion);
            validateFeature(c,
                    setOperation.getOperations().stream().anyMatch(o -> o instanceof IntersectOp),
                    Feature.setOperationIntersect);
            validateFeature(c,
                    setOperation.getOperations().stream().anyMatch(o -> o instanceof ExceptOp),
                    Feature.setOperationExcept);
            validateFeature(c,
                    setOperation.getOperations().stream().anyMatch(o -> o instanceof MinusOp),
                    Feature.setOperationMinus);
        }

        if (isNotEmpty(setOperation.getSelects())) {
            setOperation.getSelects().forEach(s -> s.accept((SelectVisitor<Void>) this, context));
        }

        validateOptionalOrderByElements(setOperation.getOrderByElements());

        if (setOperation.getLimit() != null) {
            getValidator(LimitValidator.class).validate(setOperation.getLimit());
        }

        if (setOperation.getOffset() != null) {
            validateOffset(setOperation.getOffset());
        }

        if (setOperation.getFetch() != null) {
            validateFetch(setOperation.getFetch());
        }
        return null;
    }

    @Override
    public <S> Void visit(WithItem<?> withItem, S context) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.withItem);
            validateFeature(c, withItem.isRecursive(), Feature.withItemRecursive);
        }
        if (isNotEmpty(withItem.getWithItemList())) {
            withItem.getWithItemList().forEach(wi -> wi.accept(this, context));
        }
        withItem.getSelect().accept((SelectVisitor<?>) this, context);
        return null;
    }

    @Override
    public <S> Void visit(LateralSubSelect lateralSubSelect, S context) {
        if (isNotEmpty(lateralSubSelect.getWithItemsList())) {
            lateralSubSelect.getWithItemsList()
                    .forEach(withItem -> withItem.accept((SelectVisitor<Void>) this, context));
        }

        validateFeature(Feature.lateralSubSelect);
        validateOptional(lateralSubSelect.getPivot(), p -> p.accept(this, context));
        validateOptional(lateralSubSelect.getUnPivot(), up -> up.accept(this, context));
        validateOptional(lateralSubSelect.getSelect(),
                e -> e.accept((SelectVisitor<Void>) this, context));
        return null;
    }

    @Override
    public <S> Void visit(TableStatement tableStatement, S context) {
        getValidator(TableStatementValidator.class).validate(tableStatement);
        return null;
    }

    @Override
    public <S> Void visit(TableFunction tableFunction, S context) {
        validateFeature(Feature.tableFunction);

        validateOptional(tableFunction.getPivot(), p -> p.accept(this, context));
        validateOptional(tableFunction.getUnPivot(), up -> up.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(ParenthesedFromItem parenthesis, S context) {
        validateOptional(parenthesis.getFromItem(), e -> e.accept(this, context));
        return null;
    }

    @Override
    public <S> Void visit(Values values, S context) {
        getValidator(ValuesStatementValidator.class).validate(values);
        return null;
    }

    @Override
    public <S> Void visit(Import imprt, S context) {
        // TODO: not yet implemented
        return null;
    }

    @Override
    public void validate(SelectItem<?> statement) {
        statement.accept(this, null);
    }

    public void visit(PlainSelect plainSelect) {
        visit(plainSelect, null);
    }

    public void visit(SelectItem<?> selectExpressionItem) {
        visit(selectExpressionItem, null);
    }

    public void visit(ParenthesedSelect selectBody) {
        visit(selectBody, null);
    }

    public void visit(Table table) {
        visit(table, null);
    }

    public void visit(Pivot pivot) {
        visit(pivot, null);
    }

    public void visit(UnPivot unpivot) {
        visit(unpivot, null);
    }

    public void visit(PivotXml pivot) {
        visit(pivot, null);
    }

    public void visit(SetOperationList setOperation) {
        visit(setOperation, null);
    }

    public void visit(WithItem<?> withItem) {
        visit(withItem, null);
    }

    public void visit(LateralSubSelect lateralSubSelect) {
        visit(lateralSubSelect, null);
    }

    public void visit(TableStatement tableStatement) {
        visit(tableStatement, null);
    }

    @Override
    public <S> Void visit(FromQuery fromQuery, S context) {
        return null;
    }

    @Override
    public <S> Void visit(JsonTable jsonTable, S context) {
        return null;
    }

    public void visit(TableFunction tableFunction) {
        visit(tableFunction, null);
    }

    public void visit(ParenthesedFromItem parenthesis) {
        visit(parenthesis, null);
    }

    public void visit(Values values) {
        visit(values, null);
    }

    public void visit(Import imprt) {
        visit(imprt, null);
    }



}
