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

import net.sf.jsqlparser.expression.MySQLIndexHint;
import net.sf.jsqlparser.expression.SQLServerHints;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.Fetch;
import net.sf.jsqlparser.statement.select.FromItemVisitor;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.LateralSubSelect;
import net.sf.jsqlparser.statement.select.Offset;
import net.sf.jsqlparser.statement.select.ParenthesisFromItem;
import net.sf.jsqlparser.statement.select.Pivot;
import net.sf.jsqlparser.statement.select.PivotVisitor;
import net.sf.jsqlparser.statement.select.PivotXml;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.TableFunction;
import net.sf.jsqlparser.statement.select.UnPivot;
import net.sf.jsqlparser.statement.select.ValuesList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class SelectValidator extends AbstractValidator<SelectItem>
implements SelectVisitor, SelectItemVisitor, FromItemVisitor, PivotVisitor {

    @Override
    public void visit(PlainSelect plainSelect) {

        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.select);
            validateFeature(c, plainSelect.getMySqlHintStraightJoin(), Feature.mySqlHintStraightJoin);
            validateFeature(c, plainSelect.getOracleHint() != null, Feature.oracleHint);
            validateFeature(c, plainSelect.getSkip() != null, Feature.skip);
            validateFeature(c, plainSelect.getFirst() != null, Feature.first);

            if (plainSelect.getDistinct() != null) {
                if (plainSelect.getDistinct().isUseUnique()) {
                    validateFeature(c, Feature.selectUnique);
                } else {
                    validateFeature(c, Feature.distinct);
                }
                validateFeature(c, plainSelect.getDistinct().getOnSelectItems() != null, Feature.distinctOn);
            }

            validateFeature(c, plainSelect.getTop() != null, Feature.top);
            validateFeature(c, plainSelect.getMySqlSqlNoCache(), Feature.mysqlSqlNoCache);
            validateFeature(c, plainSelect.getMySqlSqlCalcFoundRows(), Feature.mysqlCalcFoundRows);
            validateFeature(c, plainSelect.getIntoTables() != null, Feature.selectInto);
            validateFeature(c, plainSelect.getKsqlWindow() != null, Feature.window);
            validateFeature(c, plainSelect.getOrderByElements() != null && plainSelect.isOracleSiblings(),
                    Feature.oracleOrderBySiblings);

            if (plainSelect.isForUpdate()) {
                validateFeature(c, Feature.selectForUpdate);
                validateFeature(c, plainSelect.getForUpdateTable() != null, Feature.selectForUpdateOfTable);
                validateFeature(c, plainSelect.getWait() != null, Feature.selectForUpdateWait);
                validateFeature(c, plainSelect.isNoWait(), Feature.selectForUpdateNoWait);
            }

            validateFeature(c, plainSelect.getForXmlPath() != null, Feature.selectForXmlPath);
            validateFeature(c, plainSelect.getOptimizeFor() != null, Feature.optimizeFor);
        } // end for

        validateOptionalFromItem(plainSelect.getFromItem());

        if (plainSelect.getIntoTables() != null) {
            plainSelect.getIntoTables().forEach(this::visit);
        }

        validateOptionalJoins(plainSelect.getJoins());

        validateOptionalExpression(plainSelect.getWhere());
        validateOptionalExpression(plainSelect.getOracleHierarchical());

        if (plainSelect.getGroupBy() != null) {
            plainSelect.getGroupBy().accept(getValidator(GroupByValidator.class));
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

    }

    @Override
    public void visit(AllTableColumns allTableColumns) {
        // nothing to validate - allTableColumns.getTable() will be validated with from
        // clause
    }

    @Override
    public void visit(AllColumns allColumns) {
        // nothing to validate
    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        selectExpressionItem.getExpression().accept(getValidator(ExpressionValidator.class));
    }

    @Override
    public void visit(SubSelect subSelect) {
        if (subSelect.getWithItemsList() != null) {
            subSelect.getWithItemsList().forEach(withItem -> withItem.accept(this));
        }
        subSelect.getSelectBody().accept(this);
        validateOptional(subSelect.getPivot(), p -> p.accept(this));
    }

    @Override
    public void visit(Table table) {
        validateName(NamedObject.table, table.getFullyQualifiedName());
        Pivot pivot = table.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        UnPivot unpivot = table.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this);
        }
        MySQLIndexHint indexHint = table.getIndexHint();
        if (indexHint != null && indexHint.getIndexNames() != null) {
            indexHint.getIndexNames().forEach(i -> validateName(NamedObject.index, i));
        }
        SQLServerHints sqlServerHints = table.getSqlServerHints();
        if (sqlServerHints != null) {
            validateName(NamedObject.index, sqlServerHints.getIndexName());
        }
    }

    @Override
    public void visit(Pivot pivot) {
        validateFeature(Feature.pivot);
        validateOptionalExpressions(pivot.getForColumns());
    }

    @Override
    public void visit(UnPivot unpivot) {
        validateFeature(Feature.unpivot);

        validateOptionalExpressions(unpivot.getUnPivotForClause());
        validateOptionalExpression(unpivot.getUnPivotClause());
    }

    @Override
    public void visit(PivotXml pivot) {
        validateFeature(Feature.pivotXml);
        validateOptionalExpressions(pivot.getForColumns());
        if (pivot.getFunctionItems() != null) {
            ExpressionValidator v = getValidator(ExpressionValidator.class);
            pivot.getFunctionItems().forEach(f -> f.getFunction().accept(v));
        }
        if (pivot.getInSelect() != null) {
            pivot.getInSelect().accept(this);
        }
    }

    public void validateOffset(Offset offset) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.offset);
            validateFeature(c, offset.getOffsetParam() != null, Feature.offsetParam);
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

    @Override
    public void visit(SubJoin subjoin) {
        validateOptionalFromItem(subjoin.getLeft());
        validateOptionalJoins(subjoin.getJoinList());
        validateOptional(subjoin.getPivot(), e -> e.accept(this));
    }

    public void validateOptionalJoins(List<Join> joins) {
        if (joins != null) {
            for (Join join : joins) {
                validateOptionalJoin(join);
            }
        }
    }

    public void validateOptionalJoin(Join join) {
        if (join != null) {
            //        if (join.isSimple() && join.isOuter()) {
            //            errors.append(", OUTER ");
            //        } else if (join.isSimple()) {
            //            errors.append(", ");
            //        } else {
            //
            //            if (join.isRight()) {
            //                errors.append(" RIGHT");
            //            } else if (join.isNatural()) {
            //                errors.append(" NATURAL");
            //            } else if (join.isFull()) {
            //                errors.append(" FULL");
            //            } else if (join.isLeft()) {
            //                errors.append(" LEFT");
            //            } else if (join.isCross()) {
            //                errors.append(" CROSS");
            //            }
            //
            //            if (join.isOuter()) {
            //                errors.append(" OUTER");
            //            } else if (join.isInner()) {
            //                errors.append(" INNER");
            //            } else if (join.isSemi()) {
            //                errors.append(" SEMI");
            //            }
            //
            //            if (join.isStraight()) {
            //                errors.append(" STRAIGHT_JOIN ");
            //            } else if (join.isApply()) {
            //                errors.append(" APPLY ");
            //            } else {
            //                errors.append(" JOIN ");
            //            }
            //
            //        }
            //
            //        FromItem fromItem = join.getRightItem();
            //        fromItem.accept(this);
            //        if (join.isWindowJoin()) {
            //            errors.append(" WITHIN ");
            //            errors.append(join.getJoinWindow().toString());
            //        }
            //        if (join.getOnExpression() != null) {
            //            errors.append(" ON ");
            //            join.getOnExpression().accept(expressionVisitor);
            //        }
            //        if (join.getUsingColumns() != null) {
            //            errors.append(" USING (");
            //            for (Iterator<Column> iterator = join.getUsingColumns().iterator(); iterator.hasNext();) {
            //                Column column = iterator.next();
            //                errors.append(column.toString());
            //                if (iterator.hasNext()) {
            //                    errors.append(", ");
            //                }
            //            }
            //            errors.append(")");
            //        }
        }

    }

    @Override
    public void visit(SetOperationList list) {
        //        for (int i = 0; i < list.getSelects().size(); i++) {
        //            if (i != 0) {
        //                errors.append(' ').append(list.getOperations().get(i - 1)).append(' ');
        //            }
        //            boolean brackets = list.getBrackets() == null || list.getBrackets().get(i);
        //            if (brackets) {
        //                errors.append("(");
        //            }
        //            list.getSelects().get(i).accept(this);
        //            if (brackets) {
        //                errors.append(")");
        //            }
        //        }
        //        if (list.getOrderByElements() != null) {
        //            new OrderByDeParser(expressionVisitor, errors).deParse(list.getOrderByElements());
        //        }
        //
        //        if (list.getLimit() != null) {
        //            new LimitDeparser(errors).deParse(list.getLimit());
        //        }
        //        if (list.getOffset() != null) {
        //            deparseOffset(list.getOffset());
        //        }
        //        if (list.getFetch() != null) {
        //            deparseFetch(list.getFetch());
        //        }
    }

    @Override
    public void visit(WithItem withItem) {
        //        if (withItem.isRecursive()) {
        //            errors.append("RECURSIVE ");
        //        }
        //        errors.append(withItem.getName());
        //        if (withItem.getWithItemList() != null) {
        //            errors.append(" ").append(PlainSelect.
        //                    getStringList(withItem.getWithItemList(), true, true));
        //        }
        //        errors.append(" AS (");
        //        withItem.getSelectBody().accept(this);
        //        errors.append(")");
    }

    @Override
    public void visit(LateralSubSelect lateralSubSelect) {
        //        errors.append(lateralSubSelect.toString());
    }

    @Override
    public void visit(ValuesList valuesList) {
        //        errors.append(valuesList.toString());
    }



    @Override
    public void visit(TableFunction tableFunction) {
        //        errors.append(tableFunction.toString());
    }

    @Override
    public void visit(ParenthesisFromItem parenthesis) {
        //        errors.append("(");
        //        parenthesis.getFromItem().accept(this);
        //        errors.append(")");
        //        if (parenthesis.getAlias() != null) {
        //            errors.append(parenthesis.getAlias().toString());
        //        }
    }

    @Override
    public void visit(ValuesStatement values) {
        //        new ValuesStatementDeParser(expressionVisitor, errors).deParse(values);
    }

    @Override
    public void validate(SelectItem statement) {
        statement.accept(this);
    }



}
