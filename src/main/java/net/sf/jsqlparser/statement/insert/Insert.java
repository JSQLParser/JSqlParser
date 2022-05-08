/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.insert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.OracleHint;
import net.sf.jsqlparser.expression.RowConstructor;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class Insert implements Statement {

    private Table table;
    private OracleHint oracleHint = null;
    private List<Column> columns;
    private Select select;
    private boolean useDuplicate = false;
    private List<Column> duplicateUpdateColumns;
    private List<Expression> duplicateUpdateExpressionList;
    private InsertModifierPriority modifierPriority = null;
    private boolean modifierIgnore = false;

    private boolean returningAllColumns = false;

    private List<SelectExpressionItem> returningExpressionList = null;
    
    private boolean useSet = false;
    private List<Column> setColumns;
    private List<Expression> setExpressionList;
    private List<WithItem> withItemsList;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table name) {
        table = name;
    }
    
    public OracleHint getOracleHint() {
        return oracleHint;
    }

    public void setOracleHint(OracleHint oracleHint) {
        this.oracleHint = oracleHint;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> list) {
        columns = list;
    }

    /**
     * Get the values (as VALUES (...) or SELECT)
     *
     * @return the values of the insert
     */
    @Deprecated
    public ItemsList getItemsList() {
        if (select!=null) {
            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof SetOperationList) {
                SetOperationList setOperationList = (SetOperationList) selectBody;
                List<SelectBody> selects = setOperationList.getSelects();

                if (selects.size() == 1) {
                    SelectBody selectBody1 = selects.get(0);
                    if (selectBody1 instanceof ValuesStatement) {
                        ValuesStatement valuesStatement = (ValuesStatement) selectBody1;
                        if (valuesStatement.getExpressions() instanceof ExpressionList) {
                            ExpressionList expressionList = (ExpressionList) valuesStatement.getExpressions();

                            if (expressionList.getExpressions().size() == 1 && expressionList.getExpressions().get(0) instanceof RowConstructor) {
                                RowConstructor rowConstructor = (RowConstructor) expressionList.getExpressions().get(0);
                                return rowConstructor.getExprList();
                            } else {
                                return expressionList;
                            }
                        } else {
                            return valuesStatement.getExpressions();
                        }
                    }
                }
            }
        }
        return null;
    }


    @Deprecated
    public boolean isUseValues() {
        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof ValuesStatement) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isReturningAllColumns() {
        return returningAllColumns;
    }

    public void setReturningAllColumns(boolean returningAllColumns) {
        this.returningAllColumns = returningAllColumns;
    }

    public List<SelectExpressionItem> getReturningExpressionList() {
        return returningExpressionList;
    }

    public void setReturningExpressionList(List<SelectExpressionItem> returningExpressionList) {
        this.returningExpressionList = returningExpressionList;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    @Deprecated
    public boolean isUseSelectBrackets() {
        return false;
    }

    public boolean isUseDuplicate() {
        return useDuplicate;
    }

    public void setUseDuplicate(boolean useDuplicate) {
        this.useDuplicate = useDuplicate;
    }

    public List<Column> getDuplicateUpdateColumns() {
        return duplicateUpdateColumns;
    }

    public void setDuplicateUpdateColumns(List<Column> duplicateUpdateColumns) {
        this.duplicateUpdateColumns = duplicateUpdateColumns;
    }

    public List<Expression> getDuplicateUpdateExpressionList() {
        return duplicateUpdateExpressionList;
    }

    public void setDuplicateUpdateExpressionList(List<Expression> duplicateUpdateExpressionList) {
        this.duplicateUpdateExpressionList = duplicateUpdateExpressionList;
    }

    public InsertModifierPriority getModifierPriority() {
        return modifierPriority;
    }

    public void setModifierPriority(InsertModifierPriority modifierPriority) {
        this.modifierPriority = modifierPriority;
    }

    public boolean isModifierIgnore() {
        return modifierIgnore;
    }

    public void setModifierIgnore(boolean modifierIgnore) {
        this.modifierIgnore = modifierIgnore;
    }

    public void setUseSet(boolean useSet) {
        this.useSet = useSet;
    }

    public boolean isUseSet() {
        return useSet;
    }

    public void setSetColumns(List<Column> setColumns) {
        this.setColumns = setColumns;
    }

    public List<Column> getSetColumns() {
        return setColumns;
    }

    public void setSetExpressionList(List<Expression> setExpressionList) {
        this.setExpressionList = setExpressionList;
    }

    public List<Expression> getSetExpressionList() {
        return setExpressionList;
    }

    public List<WithItem> getWithItemsList() {
        return withItemsList;
    }

    public void setWithItemsList(List<WithItem> withItemsList) {
        this.withItemsList = withItemsList;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
    public String toString() {
        StringBuilder sql = new StringBuilder();
        if (withItemsList != null && !withItemsList.isEmpty()) {
            sql.append("WITH ");
            for (Iterator<WithItem> iter = withItemsList.iterator(); iter.hasNext();) {
                WithItem withItem = iter.next();
                sql.append(withItem);
                if (iter.hasNext()) {
                    sql.append(",");
                }
                sql.append(" ");
            }
        }
        sql.append("INSERT ");
        if (modifierPriority != null) {
            sql.append(modifierPriority.name()).append(" ");
        }
        if (modifierIgnore) {
            sql.append("IGNORE ");
        }
        sql.append("INTO ");
        sql.append(table).append(" ");
        if (columns != null) {
            sql.append(PlainSelect.getStringList(columns, true, true)).append(" ");
        }

        if (select != null) {
            sql.append(select);
        }

        if (useSet) {
            sql.append("SET ");
            for (int i = 0; i < getSetColumns().size(); i++) {
                if (i != 0) {
                    sql.append(", ");
                }
                sql.append(setColumns.get(i)).append(" = ");
                sql.append(setExpressionList.get(i));
            }
        }

        if (useDuplicate) {
            sql.append(" ON DUPLICATE KEY UPDATE ");
            for (int i = 0; i < getDuplicateUpdateColumns().size(); i++) {
                if (i != 0) {
                    sql.append(", ");
                }
                sql.append(duplicateUpdateColumns.get(i)).append(" = ");
                sql.append(duplicateUpdateExpressionList.get(i));
            }
        }

        if (isReturningAllColumns()) {
            sql.append(" RETURNING *");
        } else if (getReturningExpressionList() != null) {
            sql.append(" RETURNING ").append(PlainSelect.
                    getStringList(getReturningExpressionList(), true, false));
        }

        return sql.toString();
    }
    
    public Insert withWithItemsList(List<WithItem> withList) {
        this.withItemsList = withList;
        return this;
    }
    
    public Insert withSelect(Select select) {
        this.setSelect(select);
        return this;
    }

    public Insert withUseDuplicate(boolean useDuplicate) {
        this.setUseDuplicate(useDuplicate);
        return this;
    }

    public Insert withDuplicateUpdateColumns(List<Column> duplicateUpdateColumns) {
        this.setDuplicateUpdateColumns(duplicateUpdateColumns);
        return this;
    }

    public Insert withDuplicateUpdateExpressionList(List<Expression> duplicateUpdateExpressionList) {
        this.setDuplicateUpdateExpressionList(duplicateUpdateExpressionList);
        return this;
    }

    public Insert withModifierPriority(InsertModifierPriority modifierPriority) {
        this.setModifierPriority(modifierPriority);
        return this;
    }

    public Insert withModifierIgnore(boolean modifierIgnore) {
        this.setModifierIgnore(modifierIgnore);
        return this;
    }

    public Insert withReturningAllColumns(boolean returningAllColumns) {
        this.setReturningAllColumns(returningAllColumns);
        return this;
    }

    public Insert withReturningExpressionList(List<SelectExpressionItem> returningExpressionList) {
        this.setReturningExpressionList(returningExpressionList);
        return this;
    }

    public Insert withUseSet(boolean useSet) {
        this.setUseSet(useSet);
        return this;
    }

    public Insert withUseSetColumns(List<Column> setColumns) {
        this.setSetColumns(setColumns);
        return this;
    }

    public Insert withSetExpressionList(List<Expression> setExpressionList) {
        this.setSetExpressionList(setExpressionList);
        return this;
    }

    public Insert withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public Insert withColumns(List<Column> columns) {
        this.setColumns(columns);
        return this;
    }

    public Insert withSetColumns(List<Column> columns) {
        this.setSetColumns(columns);
        return this;
    }

    public Insert addColumns(Column... columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public Insert addColumns(Collection<? extends Column> columns) {
        List<Column> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public Insert addDuplicateUpdateColumns(Column... duplicateUpdateColumns) {
        List<Column> collection = Optional.ofNullable(getDuplicateUpdateColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, duplicateUpdateColumns);
        return this.withDuplicateUpdateColumns(collection);
    }

    public Insert addDuplicateUpdateColumns(Collection<? extends Column> duplicateUpdateColumns) {
        List<Column> collection = Optional.ofNullable(getDuplicateUpdateColumns()).orElseGet(ArrayList::new);
        collection.addAll(duplicateUpdateColumns);
        return this.withDuplicateUpdateColumns(collection);
    }

    public Insert addDuplicateUpdateExpressionList(Expression... duplicateUpdateExpressionList) {
        List<Expression> collection = Optional.ofNullable(getDuplicateUpdateExpressionList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, duplicateUpdateExpressionList);
        return this.withDuplicateUpdateExpressionList(collection);
    }

    public Insert addDuplicateUpdateExpressionList(Collection<? extends Expression> duplicateUpdateExpressionList) {
        List<Expression> collection = Optional.ofNullable(getDuplicateUpdateExpressionList()).orElseGet(ArrayList::new);
        collection.addAll(duplicateUpdateExpressionList);
        return this.withDuplicateUpdateExpressionList(collection);
    }

    public Insert addReturningExpressionList(SelectExpressionItem... returningExpressionList) {
        List<SelectExpressionItem> collection = Optional.ofNullable(getReturningExpressionList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, returningExpressionList);
        return this.withReturningExpressionList(collection);
    }

    public Insert addReturningExpressionList(Collection<? extends SelectExpressionItem> returningExpressionList) {
        List<SelectExpressionItem> collection = Optional.ofNullable(getReturningExpressionList()).orElseGet(ArrayList::new);
        collection.addAll(returningExpressionList);
        return this.withReturningExpressionList(collection);
    }

    public Insert addSetColumns(Column... setColumns) {
        List<Column> collection = Optional.ofNullable(getSetColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, setColumns);
        return this.withSetColumns(collection);
    }

    public Insert addSetColumns(Collection<? extends Column> setColumns) {
        List<Column> collection = Optional.ofNullable(getSetColumns()).orElseGet(ArrayList::new);
        collection.addAll(setColumns);
        return this.withSetColumns(collection);
    }

    public Insert addSetExpressionList(Expression... setExpressionList) {
        List<Expression> collection = Optional.ofNullable(getSetExpressionList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, setExpressionList);
        return this.withSetExpressionList(collection);
    }

    public Insert addSetExpressionList(Collection<? extends Expression> setExpressionList) {
        List<Expression> collection = Optional.ofNullable(getSetExpressionList()).orElseGet(ArrayList::new);
        collection.addAll(setExpressionList);
        return this.withSetExpressionList(collection);
    }

    public <E extends ItemsList> E getItemsList(Class<E> type) {
        return type.cast(getItemsList());
    }
}
