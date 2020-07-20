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

        }

        if (plainSelect.getFromItem() != null) {
            plainSelect.getFromItem().accept(this);
        }

        if (plainSelect.getIntoTables() != null) {
            plainSelect.getIntoTables().forEach(this::visit);
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                deparseJoin(join);
            }
        }

        if (plainSelect.getWhere() != null) {
            plainSelect.getWhere().accept(getValidator(ExpressionValidator.class));
        }

        if (plainSelect.getOracleHierarchical() != null) {
            plainSelect.getOracleHierarchical().accept(getValidator(ExpressionValidator.class));
        }

        if (plainSelect.getGroupBy() != null) {
            plainSelect.getGroupBy().accept(getValidator(GroupByValidator.class));
        }

        if (plainSelect.getHaving() != null) {
            plainSelect.getHaving().accept(getValidator(ExpressionValidator.class));
        }

        if (plainSelect.getOrderByElements() != null) {
            OrderByValidator v = getValidator(OrderByValidator.class);
            plainSelect.getOrderByElements().forEach(o -> o.accept(v));
        }

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
        Pivot pivot = subSelect.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
    }

    @Override
    public void visit(Table tableName) {
        validateName(NamedObject.table, tableName.getFullyQualifiedName());
        Pivot pivot = tableName.getPivot();
        if (pivot != null) {
            pivot.accept(this);
        }
        UnPivot unpivot = tableName.getUnPivot();
        if (unpivot != null) {
            unpivot.accept(this);
        }
        MySQLIndexHint indexHint = tableName.getIndexHint();
        if (indexHint != null && indexHint.getIndexNames() != null) {
            indexHint.getIndexNames().forEach(i -> validateName(NamedObject.index, i));
        }
        SQLServerHints sqlServerHints = tableName.getSqlServerHints();
        if (sqlServerHints != null) {
            validateName(NamedObject.index, sqlServerHints.getIndexName());
        }
    }

    @Override
    public void visit(Pivot pivot) {
        //        List<Column> forColumns = pivot.getForColumns();
        //        errors.append(" PIVOT (")
        //        .append(PlainSelect.getStringList(pivot.getFunctionItems()))
        //        .append(" FOR ")
        //        .append(PlainSelect.
        //                getStringList(forColumns, true, forColumns != null && forColumns.size() > 1)).
        //        append(" IN ")
        //        .append(PlainSelect.getStringList(pivot.getInItems(), true, true))
        //        .append(")");
        //        if (pivot.getAlias() != null) {
        //            errors.append(pivot.getAlias().toString());
        //        }
    }

    @Override
    public void visit(UnPivot unpivot) {
        //        boolean showOptions = unpivot.getIncludeNullsSpecified();
        //        boolean includeNulls = unpivot.getIncludeNulls();
        //        List<Column> unpivotForClause = unpivot.getUnPivotForClause();
        //        errors.append(" UNPIVOT")
        //        .append(showOptions && includeNulls ? " INCLUDE NULLS" : "")
        //        .append(showOptions && !includeNulls ? " EXCULDE NULLS" : "")
        //        .append(" (")
        //        .append(unpivot.getUnPivotClause())
        //        .append(" FOR ").append(PlainSelect.getStringList(unpivotForClause, true, unpivotForClause != null && unpivotForClause.size() > 1))
        //        .append(" IN ").append(PlainSelect.getStringList(unpivot.getUnPivotInClause(), true, true))
        //        .append(")");
    }

    @Override
    public void visit(PivotXml pivot) {
        //        List<Column> forColumns = pivot.getForColumns();
        //        errors.append(" PIVOT XML (")
        //        .append(PlainSelect.getStringList(pivot.getFunctionItems()))
        //        .append(" FOR ")
        //        .append(PlainSelect.
        //                getStringList(forColumns, true, forColumns != null && forColumns.size() > 1)).
        //        append(" IN (");
        //        if (pivot.isInAny()) {
        //            errors.append("ANY");
        //        } else if (pivot.getInSelect() != null) {
        //            errors.append(pivot.getInSelect());
        //        } else {
        //            errors.append(PlainSelect.getStringList(pivot.getInItems()));
        //        }
        //        errors.append("))");
    }

    public void validateOffset(Offset offset) {
        //        // OFFSET offset
        //        // or OFFSET offset (ROW | ROWS)
        //        if (offset.getOffsetJdbcParameter() != null) {
        //            errors.append(" OFFSET ").append(offset.getOffsetJdbcParameter());
        //        } else {
        //            errors.append(" OFFSET ");
        //            errors.append(offset.getOffset());
        //        }
        //        if (offset.getOffsetParam() != null) {
        //            errors.append(" ").append(offset.getOffsetParam());
        //        }

    }

    public void validateFetch(Fetch fetch) {
        //        // FETCH (FIRST | NEXT) row_count (ROW | ROWS) ONLY
        //        errors.append(" FETCH ");
        //        if (fetch.isFetchParamFirst()) {
        //            errors.append("FIRST ");
        //        } else {
        //            errors.append("NEXT ");
        //        }
        //        if (fetch.getFetchJdbcParameter() != null) {
        //            errors.append(fetch.getFetchJdbcParameter().toString());
        //        } else {
        //            errors.append(fetch.getRowCount());
        //        }
        //        errors.append(" ").append(fetch.getFetchParam()).append(" ONLY");

    }

    //    public ExpressionVisitor getExpressionVisitor() {
    //        return expressionVisitor;
    //    }
    //
    //    public void setExpressionVisitor(ExpressionVisitor visitor) {
    //        expressionVisitor = visitor;
    //    }

    @Override
    public void visit(SubJoin subjoin) {
        //        errors.append("(");
        //        subjoin.getLeft().accept(this);
        //        for (Join join : subjoin.getJoinList()) {
        //            deparseJoin(join);
        //        }
        //        errors.append(")");
        //
        //        if (subjoin.getPivot() != null) {
        //            subjoin.getPivot().accept(this);
        //        }
    }

    public void deparseJoin(Join join) {
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
        // TODO Auto-generated method stub

    }

}
