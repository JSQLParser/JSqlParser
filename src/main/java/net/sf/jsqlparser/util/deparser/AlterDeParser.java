/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.statement.alter.AlterExpression;
import net.sf.jsqlparser.statement.alter.AlterExpressionPartition;
import net.sf.jsqlparser.statement.alter.AlterExpressionPrimaryKey;
import net.sf.jsqlparser.statement.alter.RelationAlterAction;
import net.sf.jsqlparser.statement.alter.AlterOperation;
import net.sf.jsqlparser.statement.select.PlainSelect;
import java.util.Iterator;

public class AlterDeParser extends AbstractDeParser<Alter> {
    private final ExpressionVisitor<StringBuilder> expressionVisitor;

    public AlterDeParser(StringBuilder buffer) {
        this(buffer, new StatementDeParser(buffer).getExpressionDeParser());
    }

    public AlterDeParser(StringBuilder buffer, ExpressionVisitor<StringBuilder> expressionVisitor) {
        super(buffer);
        this.expressionVisitor = expressionVisitor;
    }

    @Override
    public void deParse(Alter alter) {
        builder.append("ALTER TABLE ");
        if (alter.isUseOnly()) {
            builder.append("ONLY ");
        }
        if (alter.isUseTableIfExists()) {
            builder.append("IF EXISTS ");
        }
        builder.append(alter.getTable().getFullyQualifiedName()).append(' ');
        for (Iterator<AlterExpression> iterator = alter.getAlterExpressions().iterator(); iterator
                .hasNext();) {
            deParseAction(iterator.next());
            if (iterator.hasNext()) {
                builder.append(", ");
            }
        }
    }

    private void deParseAction(AlterExpression action) {
        if (action instanceof RelationAlterAction) {
            ((RelationAlterAction) action).appendTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
            return;
        }
        if (action instanceof AlterExpressionPartition) {
            ((AlterExpressionPartition) action).appendTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
            return;
        }
        if (action instanceof AlterExpressionPrimaryKey) {
            ((AlterExpressionPrimaryKey) action).appendTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
            return;
        }
        if (action.getOperation() == AlterOperation.ADD && action.getIndex() != null
                && action.getConstraintType() == null) {
            builder.append(action.getOperation()).append(' ');
            new TableElementDeParser(builder, expressionVisitor).deParse(action.getIndex());
            if (action.getConstraints() != null && !action.getConstraints().isEmpty()) {
                builder.append(' ')
                        .append(PlainSelect.getStringList(action.getConstraints(), false, false));
            }
            if (action.getUseEqual()) {
                builder.append('=');
            }
            deParseTail(action);
            return;
        }
        if (action.getOperation() == net.sf.jsqlparser.statement.alter.AlterOperation.ALTER
                && action.getColumnSetDefaultList() != null
                && !action.getColumnSetDefaultList().isEmpty()) {
            action.appendColumnActionTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
            return;
        }
        if (action.getColDataTypeList() != null) {
            action.appendColumnDefinitionsTo(builder,
                    expression -> expression.accept(expressionVisitor, null));
        } else {
            builder.append(action);
        }
    }

    private void deParseTail(AlterExpression action) {
        if (action.getParameters() != null && !action.getParameters().isEmpty()) {
            builder.append(' ')
                    .append(PlainSelect.getStringList(action.getParameters(), false, false));
        }
        if (action.getIndex() != null && action.getIndex().getCommentText() != null) {
            builder.append(" COMMENT ").append(action.getIndex().getCommentText());
        }
    }

}
