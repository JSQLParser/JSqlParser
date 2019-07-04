/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

import java.util.Iterator;
import java.util.List;

public class FullTextSearch extends ASTNodeAccessImpl implements Expression {

    private List<Column> _matchColumns;
    private StringValue _againstValue;
    private String _searchModifier;

    public FullTextSearch() {

    }

    public void setMatchColumns(List<Column> columns) {
        this._matchColumns = columns;
    }

    public List<Column> getMatchColumns() {
        return this._matchColumns;
    }

    public void setAgainstValue(StringValue val) {
        this._againstValue = val;
    }

    public StringValue getAgainstValue() {
        return this._againstValue;
    }

    public void setSearchModifier(String val) {
        this._searchModifier = val;
    }

    public String getSearchModifier() {
        return this._searchModifier;
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }

    @Override
    public String toString() {
        // Build a list of matched columns
        String columnsListCommaSeperated = "";
        Iterator<Column> iterator = this._matchColumns.iterator();
        while (iterator.hasNext()) {
            Column col = iterator.next();
            columnsListCommaSeperated += col.getFullyQualifiedName();
            if (iterator.hasNext()) {
                columnsListCommaSeperated += ",";
            }
        }
        return "MATCH (" + columnsListCommaSeperated + ") AGAINST (" + this._againstValue + " " + this._searchModifier + ")";
    }
}
