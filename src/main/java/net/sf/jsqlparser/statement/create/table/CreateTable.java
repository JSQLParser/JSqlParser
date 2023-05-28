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
import net.sf.jsqlparser.expression.SpannerInterleaveIn;
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

    private List<String> columns;

    private List<Index> indexes;

    private Select select;

    private Table likeTable;

    private boolean selectParenthesis;

    private boolean ifNotExists = false;

    private boolean orReplace = false;

    private RowMovement rowMovement;

    private SpannerInterleaveIn interleaveIn = null;

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
     * @return a list of {@link ColumnDefinition}s of this table.
     */
    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public void setColumnDefinitions(List<ColumnDefinition> list) {
        columnDefinitions = list;
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    /**
     * @return a list of options (as simple strings) of this table definition, as ("TYPE", "=", "MYISAM")
     */
    public List<String> getTableOptionsStrings() {
        return tableOptionsStrings;
    }

    public void setTableOptionsStrings(List<String> tableOptionsStrings) {
        this.tableOptionsStrings = tableOptionsStrings;
    }

    public List<String> getCreateOptionsStrings() {
        return createOptionsStrings;
    }

    public void setCreateOptionsStrings(List<String> createOptionsStrings) {
        this.createOptionsStrings = createOptionsStrings;
    }

    /**
     * @return a list of {@link Index}es (for example "PRIMARY KEY") of this table.<br>
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

    public Table getLikeTable() {
        return likeTable;
    }

    public void setLikeTable(Table likeTable, boolean parenthesis) {
        this.likeTable = likeTable;
        this.selectParenthesis = parenthesis;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
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
    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity" })
    public String toString() {
        String sql;
        String createOps = PlainSelect.getStringList(createOptionsStrings, false, false);
        sql = "CREATE " + (unlogged ? "UNLOGGED " : "") + (!"".equals(createOps) ? createOps + " " : "") + (orReplace ? "OR REPLACE " : "") + "TABLE " + (ifNotExists ? "IF NOT EXISTS " : "") + table;
        if (columns != null && !columns.isEmpty()) {
            sql += " ";
            sql += PlainSelect.getStringList(columns, true, true);
        }
        if (columnDefinitions != null && !columnDefinitions.isEmpty()) {
            sql += " (";
            sql += PlainSelect.getStringList(columnDefinitions, true, false);
            if (indexes != null && !indexes.isEmpty()) {
                sql += ", ";
                sql += PlainSelect.getStringList(indexes);
            }
            sql += ")";
        }
        String options = PlainSelect.getStringList(tableOptionsStrings, false, false);
        if (options != null && options.length() > 0) {
            sql += " " + options;
        }
        if (rowMovement != null) {
            sql += " " + rowMovement.getMode().toString() + " ROW MOVEMENT";
        }
        if (select != null) {
            sql += " AS " + (selectParenthesis ? "(" : "") + select.toString() + (selectParenthesis ? ")" : "");
        }
        if (likeTable != null) {
            sql += " LIKE " + (selectParenthesis ? "(" : "") + likeTable.toString() + (selectParenthesis ? ")" : "");
        }
        if (interleaveIn != null) {
            sql += ", " + interleaveIn;
        }
        return sql;
    }

    public CreateTable withTable(Table table) {
        this.setTable(table);
        return this;
    }

    public CreateTable withUnlogged(boolean unlogged) {
        this.setUnlogged(unlogged);
        return this;
    }

    public CreateTable withCreateOptionsStrings(List<String> createOptionsStrings) {
        this.setCreateOptionsStrings(createOptionsStrings);
        return this;
    }

    public CreateTable withSelectParenthesis(boolean selectParenthesis) {
        this.setSelectParenthesis(selectParenthesis);
        return this;
    }

    public CreateTable withIfNotExists(boolean ifNotExists) {
        this.setIfNotExists(ifNotExists);
        return this;
    }

    public CreateTable withRowMovement(RowMovement rowMovement) {
        this.setRowMovement(rowMovement);
        return this;
    }

    public CreateTable withTableOptionsStrings(List<String> tableOptionsStrings) {
        this.setTableOptionsStrings(tableOptionsStrings);
        return this;
    }

    public CreateTable withColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.setColumnDefinitions(columnDefinitions);
        return this;
    }

    public CreateTable withColumns(List<String> columns) {
        this.setColumns(columns);
        return this;
    }

    public CreateTable withIndexes(List<Index> indexes) {
        this.setIndexes(indexes);
        return this;
    }

    public CreateTable addCreateOptionsStrings(String... createOptionsStrings) {
        List<String> collection = Optional.ofNullable(getCreateOptionsStrings()).orElseGet(ArrayList::new);
        Collections.addAll(collection, createOptionsStrings);
        return this.withCreateOptionsStrings(collection);
    }

    public CreateTable addCreateOptionsStrings(Collection<String> createOptionsStrings) {
        List<String> collection = Optional.ofNullable(getCreateOptionsStrings()).orElseGet(ArrayList::new);
        collection.addAll(createOptionsStrings);
        return this.withCreateOptionsStrings(collection);
    }

    public CreateTable addColumnDefinitions(ColumnDefinition... columnDefinitions) {
        List<ColumnDefinition> collection = Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columnDefinitions);
        return this.withColumnDefinitions(collection);
    }

    public CreateTable addColumnDefinitions(Collection<? extends ColumnDefinition> columnDefinitions) {
        List<ColumnDefinition> collection = Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        collection.addAll(columnDefinitions);
        return this.withColumnDefinitions(collection);
    }

    public CreateTable addColumns(String... columns) {
        List<String> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, columns);
        return this.withColumns(collection);
    }

    public CreateTable addColumns(Collection<String> columns) {
        List<String> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(columns);
        return this.withColumns(collection);
    }

    public CreateTable addIndexes(Index... indexes) {
        List<Index> collection = Optional.ofNullable(getIndexes()).orElseGet(ArrayList::new);
        Collections.addAll(collection, indexes);
        return this.withIndexes(collection);
    }

    public CreateTable addIndexes(Collection<? extends Index> indexes) {
        List<Index> collection = Optional.ofNullable(getIndexes()).orElseGet(ArrayList::new);
        collection.addAll(indexes);
        return this.withIndexes(collection);
    }

    public SpannerInterleaveIn getSpannerInterleaveIn() {
        return interleaveIn;
    }

    public void setSpannerInterleaveIn(SpannerInterleaveIn spannerInterleaveIn) {
        this.interleaveIn = spannerInterleaveIn;
    }

    public CreateTable withSpannerInterleaveIn(SpannerInterleaveIn spannerInterleaveIn) {
        this.interleaveIn = spannerInterleaveIn;
        return this;
    }
}
