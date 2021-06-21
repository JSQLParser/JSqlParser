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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class Upsert implements Statement {

    private Table table;
    private List<Column> columns;
    private ItemsList itemsList;
    private boolean useValues = true;
    private Select select;
    private boolean useSelectBrackets = true;
    private boolean useDuplicate = false;
    private List<Column> duplicateUpdateColumns;
    private List<Expression> duplicateUpdateExpressionList;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this); 
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
    
    public void setItemsList(ItemsList list) {
        itemsList = list;
    }
    
    public ItemsList getItemsList() {
        return itemsList;
    }
    
    public void setUseValues(boolean useValues) {
        this.useValues = useValues;
    }
    
    public boolean isUseValues() {
        return useValues;
    }
    
    public void setSelect(Select select) {
        this.select = select;
    }
    
    public Select getSelect() {
        return select;
    }
    
    public void setUseSelectBrackets(boolean useSelectBrackets) {
        this.useSelectBrackets = useSelectBrackets;
    }
    
    public boolean isUseSelectBrackets() {
        return useSelectBrackets;
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
    @SuppressWarnings({"PMD.CyclomaticComplexity"})
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("UPSERT INTO ");
        sb.append(table).append(" ");
        if (columns != null) {
            sb.append(PlainSelect.getStringList(columns, true, true)).append(" ");
        }
        if (useValues) {
            sb.append("VALUES ");
        }
        
        if (itemsList != null) {
            sb.append(itemsList);
        } else {
            if (useSelectBrackets) {
                sb.append("(");
            }
            if (select != null) {
                sb.append(select);
            }
            if (useSelectBrackets) {
                sb.append(")");
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

    public Upsert withUseValues(boolean useValues) {
        this.setUseValues(useValues);
        return this;
    }

    public Upsert withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public Upsert withUseSelectBrackets(boolean useSelectBrackets) {
        this.setUseSelectBrackets(useSelectBrackets);
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

    public Upsert withDuplicateUpdateExpressionList(List<Expression> duplicateUpdateExpressionList) {
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

    public Upsert withItemsList(ItemsList itemsList) {
        this.setItemsList(itemsList);
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
        List<Column> collection = Optional.ofNullable(getDuplicateUpdateColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, duplicateUpdateColumns);
        return this.withDuplicateUpdateColumns(collection);
    }

    public Upsert addDuplicateUpdateColumns(Collection<? extends Column> duplicateUpdateColumns) {
        List<Column> collection = Optional.ofNullable(getDuplicateUpdateColumns()).orElseGet(ArrayList::new);
        collection.addAll(duplicateUpdateColumns);
        return this.withDuplicateUpdateColumns(collection);
    }

    public Upsert addDuplicateUpdateExpressionList(Expression... duplicateUpdateExpressionList) {
        List<Expression> collection = Optional.ofNullable(getDuplicateUpdateExpressionList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, duplicateUpdateExpressionList);
        return this.withDuplicateUpdateExpressionList(collection);
    }

    public Upsert addDuplicateUpdateExpressionList(Collection<? extends Expression> duplicateUpdateExpressionList) {
        List<Expression> collection = Optional.ofNullable(getDuplicateUpdateExpressionList()).orElseGet(ArrayList::new);
        collection.addAll(duplicateUpdateExpressionList);
        return this.withDuplicateUpdateExpressionList(collection);
    }

    public <E extends ItemsList> E getItemsList(Class<E> type) {
        return type.cast(getItemsList());
    }
}
