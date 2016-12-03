/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;

import java.util.ArrayList;
import java.util.List;

/**
 * Find all used tables within an select statement.
 */
public class TablesNamesFinder extends JSQLBaseVisitor {

    private List<String> tables;
    /**
     * There are special names, that are not table names but are parsed as tables. These names are
     * collected here and are not included in the tables - names anymore.
     */
    private List<String> otherItemNames;

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param statement
     * @return
     */
    public List<String> getTableList(Statement statement) {
        init();
        statement.accept(this);
        return tables;
    }

    /**
     * Main entry for this Tool class. A list of found tables is returned.
     *
     * @param expr
     * @return
     */
    public List<String> getTableList(Expression expr) {
        init();
        expr.accept(this);
        return tables;
    }

    @Override
    public void visit(WithItem withItem) {
        otherItemNames.add(withItem.getName().toLowerCase());
        super.visit(withItem);
    }

    @Override
    public void visit(Table tableName) {
        String tableWholeName = tableName.getFullyQualifiedName();
        if (!otherItemNames.contains(tableWholeName.toLowerCase()) && !tables.contains(tableWholeName)) {
            tables.add(tableWholeName);
        }
        super.visit(tableName);
    }

    /**
     * Initializes table names collector.
     */
    protected void init() {
        otherItemNames = new ArrayList<String>();
        tables = new ArrayList<String>();
    }

    @Override
    public void visit(Delete delete) {
        tables.add(delete.getTable().getName());
        super.visit(delete);
    }

    @Override
    public void visit(Update update) {
        for (Table table : update.getTables()) {
            tables.add(table.getName());
        }
        super.visit(update);
    }

    @Override
    public void visit(Insert insert) {
        tables.add(insert.getTable().getName());
        super.visit(insert);
    }

    @Override
    public void visit(Replace replace) {
        tables.add(replace.getTable().getName());
        super.visit(replace);
    }

    @Override
    public void visit(CreateTable create) {
        tables.add(create.getTable().getFullyQualifiedName());
        super.visit(create);
    }

    @Override
    public void visit(Merge merge) {
        tables.add(merge.getTable().getName());
        super.visit(merge);
    }

}
