/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.ParenthesedSelectBody;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import java.util.LinkedList;
import java.util.List;

/**
 * Add aliases to every column and expression selected by a select - statement. Existing aliases are
 * recognized and preserved. This class standard uses a prefix of A and a counter to generate new
 * aliases (e.g. A1, A5, ...). This behaviour can be altered.
 *
 * @author tw
 */
public class AddAliasesVisitor implements SelectVisitor, SelectItemVisitor {

    private static final String NOT_SUPPORTED_YET = "Not supported yet.";
    private List<String> aliases = new LinkedList<String>();
    private boolean firstRun = true;
    private int counter = 0;
    private String prefix = "A";

    @Override
    public void visit(ParenthesedSelectBody parenthesedSelectBody) {
        parenthesedSelectBody.getSelectBody().accept(this);
    }

    @Override
    public void visit(PlainSelect plainSelect) {
        firstRun = true;
        counter = 0;
        aliases.clear();
        for (SelectItem item : plainSelect.getSelectItems()) {
            item.accept(this);
        }
        firstRun = false;
        for (SelectItem item : plainSelect.getSelectItems()) {
            item.accept(this);
        }
    }

    @Override
    public void visit(SetOperationList setOpList) {
        for (SelectBody select : setOpList.getSelects()) {
            select.accept(this);
        }
    }

    @Override
    public void visit(AllTableColumns allTableColumns) {

    }

    @Override
    public void visit(SelectExpressionItem selectExpressionItem) {
        if (firstRun) {
            if (selectExpressionItem.getAlias() != null) {
                aliases.add(selectExpressionItem.getAlias().getName().toUpperCase());
            }
        } else {
            if (selectExpressionItem.getAlias() == null) {

                while (true) {
                    String alias = getNextAlias().toUpperCase();
                    if (!aliases.contains(alias)) {
                        aliases.add(alias);
                        selectExpressionItem.setAlias(new Alias(alias));
                        break;
                    }
                }
            }
        }
    }

    protected String getNextAlias() {
        counter++;
        return prefix + counter;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public void visit(WithItem withItem) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visit(AllColumns allColumns) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_YET); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void visit(ValuesStatement aThis) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
