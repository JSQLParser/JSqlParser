/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

public class CreateTable implements Statement {

    private Table table;
    private boolean unlogged = false;
    private List<String> createOptionsStrings;
    private List<String> tableOptionsStrings;
    private List<ColumnDefinition> columnDefinitions;
    private List<Index> indexes;
    private Select select;
    private boolean selectParenthesis;
    private boolean ifNotExists = false;
    private RowMovement rowMovement;

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public boolean isUnlogged() {
        return unlogged;
    }

    public void setUnlogged(boolean unlogged) {
        this.unlogged = unlogged;
    }

    /**
     * A list of {@link ColumnDefinition}s of this table.
     */
    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(List<ColumnDefinition> list) {
        columnDefinitions = list;
    }

    /**
     * A list of options (as simple strings) of this table definition, as ("TYPE", "=", "MYISAM")
     */
    public List<?> getTableOptionsStrings() {
        return tableOptionsStrings;
    }

    public void setTableOptionsStrings(List<String> list) {
        tableOptionsStrings = list;
    }

    public List<String> getCreateOptionsStrings() {
        return createOptionsStrings;
    }

    public void setCreateOptionsStrings(List<String> createOptionsStrings) {
        this.createOptionsStrings = createOptionsStrings;
    }

    /**
     * A list of {@link Index}es (for example "PRIMARY KEY") of this table.<br>
     * Indexes created with column definitions (as in mycol INT PRIMARY KEY) are not inserted into
     * this list.
     */
    public List<Index> getIndexes() {
        return indexes;
    }

    public void setIndexes(List<Index> list) {
        indexes = list;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select, boolean parenthesis) {
        this.select = select;
        this.selectParenthesis = parenthesis;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public boolean isSelectParenthesis() {
        return selectParenthesis;
    }

    public void setSelectParenthesis(boolean selectParenthesis) {
        this.selectParenthesis = selectParenthesis;
    }

    public RowMovement getRowMovement() {
        return rowMovement;
    }

    public void setRowMovement(RowMovement rowMovement) {
        this.rowMovement = rowMovement;
    }

    @Override
    public String toString() {
        String sql;
        String createOps = PlainSelect.getStringList(createOptionsStrings, false, false);

        sql = "CREATE " + (unlogged ? "UNLOGGED " : "")
                + (!"".equals(createOps) ? createOps + " " : "")
                + "TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table;

        if (columnDefinitions != null && !columnDefinitions.isEmpty()) {
            sql += " (";

            sql += PlainSelect.getStringList(columnDefinitions, true, false);
            if (indexes != null && !indexes.isEmpty()) {
                sql += ", ";
                sql += PlainSelect.getStringList(indexes);
            }
            sql += ")";
            String options = PlainSelect.getStringList(tableOptionsStrings, false, false);
            if (options != null && options.length() > 0) {
                sql += " " + options;
            }
        }

        if (rowMovement != null) {
            sql += " " + rowMovement.getMode().toString() + " ROW MOVEMENT";
        }
        if (select != null) {
            sql += " AS " + (selectParenthesis ? "(" : "") + select.toString() + (selectParenthesis ? ")" : "");
        }
        return sql;
    }

    public CreateTable table(Table table) {
        this.setTable(table);
        return this;
    }

    public CreateTable unlogged(boolean unlogged) {
        this.setUnlogged(unlogged);
        return this;
    }

    public CreateTable createOptionsStrings(List<String> createOptionsStrings) {
        this.setCreateOptionsStrings(createOptionsStrings);
        return this;
    }

    public CreateTable selectParenthesis(boolean selectParenthesis) {
        this.setSelectParenthesis(selectParenthesis);
        return this;
    }

    public CreateTable ifNotExists(boolean ifNotExists) {
        this.setIfNotExists(ifNotExists);
        return this;
    }

    public CreateTable rowMovement(RowMovement rowMovement) {
        this.setRowMovement(rowMovement);
        return this;
    }

    public CreateTable tableOptionsStrings(List<String> tableOptionsStrings) {
        this.setTableOptionsStrings(tableOptionsStrings);
        return this;
    }

    public CreateTable columnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.setColumnDefinitions(columnDefinitions);
        return this;
    }

    public CreateTable indexes(List<Index> indexes) {
        this.setIndexes(indexes);
        return this;
    }

    public CreateTable addCreateOptionsStrings(String... createOptionsStrings) {
        List<String> collection = Optional.ofNullable(getCreateOptionsStrings()).orElseGet(ArrayList::new);
        Collections.addAll(collection, createOptionsStrings);
        return this.createOptionsStrings(collection);
    }

    public CreateTable addCreateOptionsStrings(Collection<String> createOptionsStrings) {
        List<String> collection = Optional.ofNullable(getCreateOptionsStrings()).orElseGet(ArrayList::new);
        collection.addAll(createOptionsStrings);
        return this.createOptionsStrings(collection);
    }

    public CreateTable addColumnDefinitions(ColumnDefinition... columnDefinitions) {
        List<ColumnDefinition> collection = Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columnDefinitions);
        return this.columnDefinitions(collection);
    }

    public CreateTable addColumnDefinitions(Collection<? extends ColumnDefinition> columnDefinitions) {
        List<ColumnDefinition> collection = Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        collection.addAll(columnDefinitions);
        return this.columnDefinitions(collection);
    }

    public CreateTable addIndexes(Index... indexes) {
        List<Index> collection = Optional.ofNullable(getIndexes()).orElseGet(ArrayList::new);
        Collections.addAll(collection, indexes);
        return this.indexes(collection);
    }

    public CreateTable addIndexes(Collection<? extends Index> indexes) {
        List<Index> collection = Optional.ofNullable(getIndexes()).orElseGet(ArrayList::new);
        collection.addAll(indexes);
        return this.indexes(collection);
    }
}
