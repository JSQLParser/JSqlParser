package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.test.upsert.Upsert;

import java.util.Iterator;

/**
 * 文件描述：
 * 作者： bamboo
 * 时间： 2017/1/3
 */
public class UpsertDeParser implements ItemsListVisitor {

    private StringBuilder builder;
    private ExpressionVisitor expressionVisitor;
    private SelectVisitor selectVisitor;

    public UpsertDeParser() {
    }


    public UpsertDeParser(StringBuilder builder, ExpressionVisitor expressionVisitor, SelectVisitor selectVisitor) {
        this.builder = builder;
        this.expressionVisitor = expressionVisitor;
        this.selectVisitor = selectVisitor;
    }

    public StringBuilder getBuilder() {
        return builder;
    }

    public void setBuilder(StringBuilder builder) {
        this.builder = builder;
    }


    public void deParser(Upsert upsert) {
        builder.append("UPSERT INTO ");
        builder.append(upsert.getTable().getFullyQualifiedName());
        if (upsert.getColumns() != null) {
            builder.append("(").append(PlainSelect.getStringList(upsert.getColumns(), true, true));
        }
        if (upsert.getItemsList() != null) {
            upsert.getItemsList().accept(this);
        }

        if (upsert.getSelect() != null) {
            builder.append(" ");
            if (upsert.isUseSelectBrackets()) {
                builder.append("(");
            }
            if (upsert.getSelect().getWithItemsList() != null) {
                builder.append("WITH ");
                for (WithItem with : upsert.getSelect().getWithItemsList()) {
                    with.accept(selectVisitor);
                }
                builder.append(" ");
            }
            upsert.getSelect().getSelectBody().accept(selectVisitor);
            if (upsert.isUseSelectBrackets()) {
                builder.append(")");
            }
        }

        if (upsert.isUseDuplicate()) {
            builder.append(" ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < upsert.getDuplicateUpdateColumns().size(); i++) {
                Column column = upsert.getDuplicateUpdateColumns().get(i);
                builder.append(column.getFullyQualifiedName()).append(" = ");

                Expression expression = upsert.getDuplicateUpdateExpressionList().get(i);
                expression.accept(expressionVisitor);
                if (i < upsert.getDuplicateUpdateColumns().size() - 1) {
                    builder.append(", ");
                }
            }
        }

    }

    @Override
    public void visit(SubSelect subSelect) {
        subSelect.getSelectBody().accept(selectVisitor);
    }

    @Override
    public void visit(ExpressionList expressionList) {
        builder.append(" VALUES ");
        builder.append(PlainSelect.getStringList(expressionList.getExpressions(), true, true));
    }

    @Override
    public void visit(MultiExpressionList multiExprList) {
        builder.append(" VALUES ");
        for (Iterator<ExpressionList> it = multiExprList.getExprList().iterator(); it.hasNext();) {
            builder.append("(");
            for (Iterator<Expression> iter = it.next().getExpressions().iterator(); iter.hasNext();) {
                Expression expression = iter.next();
                expression.accept(expressionVisitor);
                if (iter.hasNext()) {
                    builder.append(", ");
                }
            }
            builder.append(")");
            if (it.hasNext()) {
                builder.append(", ");
            }
        }
    }
}
