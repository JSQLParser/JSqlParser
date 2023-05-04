/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.upsert;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.Values;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class Upsert implements Statement {

    private Table table;
    private List<Column> columns;
    private ExpressionList expressions;
    private Select select;
    private boolean useDuplicate = false;
    private List<Column> duplicateUpdateColumns;
    private List<Expression> duplicateUpdateExpressionList;

    private UpsertType upsertType = UpsertType.UPSERT;

    private boolean isUsingInto;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public UpsertType getUpsertType() {
        return upsertType;
    }

    public void setUpsertType(UpsertType upsertType) {
        this.upsertType = upsertType;
    }

    public Upsert withUpsertType(UpsertType upsertType) {
        setUpsertType(upsertType);
        return this;
    }

    public boolean isUsingInto() {
        return isUsingInto;
    }

    public void setUsingInto(boolean useInto) {
        this.isUsingInto = useInto;
    }

    public Upsert withUsingInto(boolean useInto) {
        setUsingInto(useInto);
        return this;
    }

    public void setTable(Table name) {
        table = name;
    }

    public Table getTable() {
        return table;
    }

    public void setColumns(List<Column> list) {
        columns = list;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setExpressions(ExpressionList list) {
        expressions = list;
    }

    public ExpressionList getExpressions() {
        return expressions;
    }

    public List<Expression> getSetExpressions() {
        List<Expression> expressions = null;
        if (this.expressions instanceof ExpressionList) {
            ExpressionList expressionList = (ExpressionList) this.expressions;
            expressions = expressionList.getExpressions();
        }
        return expressions;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public Select getSelect() {
        return select;
    }

    public Values getValues() {
        return select.getValues();
    }

    public PlainSelect getPlainSelect() {
        return select.getPlainSelect();
    }

    public SetOperationList getSetOperationList() {
        return select.getSetOperationList();
    }


    public void setUseDuplicate(boolean useDuplicate) {
        this.useDuplicate = useDuplicate;
    }

    public boolean isUseDuplicate() {
        return useDuplicate;
    }

    public void setDuplicateUpdateColumns(List<Column> duplicateUpdateColumns) {
        this.duplicateUpdateColumns = duplicateUpdateColumns;
    }

    public List<Column> getDuplicateUpdateColumns() {
        return duplicateUpdateColumns;
    }

    public void setDuplicateUpdateExpressionList(List<Expression> duplicateUpdateExpressionList) {
        this.duplicateUpdateExpressionList = duplicateUpdateExpressionList;
    }

    public List<Expression> getDuplicateUpdateExpressionList() {
        return duplicateUpdateExpressionList;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (upsertType) {
            case REPLACE:
            case REPLACE_SET:
                sb.append("REPLACE ");
                break;
            case INSERT_OR_ABORT:
                sb.append("INSERT OR ABORT ");
                break;
            case INSERT_OR_FAIL:
                sb.append("INSERT OR FAIL ");
                break;
            case INSERT_OR_IGNORE:
                sb.append("INSERT OR IGNORE ");
                break;
            case INSERT_OR_REPLACE:
                sb.append("INSERT OR REPLACE ");
                break;
            case INSERT_OR_ROLLBACK:
                sb.append("INSERT OR ROLLBACK ");
                break;
            case UPSERT:
            default:
                sb.append("UPSERT ");
        }

        if (isUsingInto) {
            sb.append("INTO ");
        }
        sb.append(table).append(" ");

        if (upsertType == UpsertType.REPLACE_SET) {
            sb.append("SET ");
            // each element from expressions match up with a column from columns.
            List<Expression> expressions = getSetExpressions();
            for (int i = 0, s = columns.size(); i < s; i++) {
                sb.append(columns.get(i)).append("=").append(expressions.get(i));
                sb.append(i < s - 1
                        ? ", "
                        : "");
            }
        } else {
            if (columns != null) {
                sb.append(PlainSelect.getStringList(columns, true, true)).append(" ");
            }
            if (select != null) {
                sb.append(select);
            }
        }

        if (useDuplicate) {
            sb.append(" ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < getDuplicateUpdateColumns().size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(duplicateUpdateColumns.get(i)).append(" = ");
                sb.append(duplicateUpdateExpressionList.get(i));
            }
        }

        return sb.toString();
    }

    public Upsert withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public Upsert withUseDuplicate(boolean useDuplicate) {
        this.setUseDuplicate(useDuplicate);
        return this;
    }

    public Upsert withDuplicateUpdateColumns(List<Column> duplicateUpdateColumns) {
        this.setDuplicateUpdateColumns(duplicateUpdateColumns);
        return this;
    }

    public Upsert withDuplicateUpdateExpressionList(
            List<Expression> duplicateUpdateExpressionList) {
        this.setDuplicateUpdateExpressionList(duplicateUpdateExpressionList);
        return this;
    }

    public Upsert withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Upsert withColumns(List<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public Upsert withExpressions(ExpressionList expressions) {
        this.setExpressions(expressions);
        return this;
    }

    public Upsert addColumns(Column... columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public Upsert addColumns(Collection<? extends Column> columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public Upsert addDuplicateUpdateColumns(Column... duplicateUpdateColumns) {
        List<Column> collection =
                Optional.ofNullable(getDuplicateUpdateColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, duplicateUpdateColumns);
        return this.withDuplicateUpdateColumns(collection);
    }

    public Upsert addDuplicateUpdateColumns(Collection<? extends Column> duplicateUpdateColumns) {
        List<Column> collection =
                Optional.ofNullable(getDuplicateUpdateColumns()).orElseGet(ArrayList::new);
        collection.addAll(duplicateUpdateColumns);
        return this.withDuplicateUpdateColumns(collection);
    }

    public Upsert addDuplicateUpdateExpressionList(Expression... duplicateUpdateExpressionList) {
        List<Expression> collection =
                Optional.ofNullable(getDuplicateUpdateExpressionList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, duplicateUpdateExpressionList);
        return this.withDuplicateUpdateExpressionList(collection);
    }

    public Upsert addDuplicateUpdateExpressionList(
            Collection<? extends Expression> duplicateUpdateExpressionList) {
        List<Expression> collection =
                Optional.ofNullable(getDuplicateUpdateExpressionList()).orElseGet(ArrayList::new);
        collection.addAll(duplicateUpdateExpressionList);
        return this.withDuplicateUpdateExpressionList(collection);
    }
}
