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
import net.sf.jsqlparser.expression.JdbcNamedParameter;
import net.sf.jsqlparser.expression.JdbcParameter;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Column;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

public class FullTextSearch extends ASTNodeAccessImpl implements Expression {

    private ExpressionList<Column> _matchColumns;
    private Expression _againstValue;
    private String _searchModifier;

    public FullTextSearch() {

    }

    public void setMatchColumns(ExpressionList<Column> columns) {
        this._matchColumns = columns;
    }

    public ExpressionList<Column> getMatchColumns() {
        return this._matchColumns;
    }

    public void setAgainstValue(StringValue val) {
        this._againstValue = val;
    }

    public void setAgainstValue(JdbcNamedParameter val) {
        this._againstValue = val;
    }

    public void setAgainstValue(JdbcParameter val) {
        this._againstValue = val;
    }

    public Expression getAgainstValue() {
        return this._againstValue;
    }

    public void setSearchModifier(String val) {
        this._searchModifier = val;
    }

    public String getSearchModifier() {
        return this._searchModifier;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S arguments) {
        return expressionVisitor.visit(this, arguments);
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

        return "MATCH (" + columnsListCommaSeperated + ") AGAINST (" + this._againstValue +
                (this._searchModifier != null ? " " + this._searchModifier : "") + ")";
    }

    public FullTextSearch withMatchColumns(ExpressionList<Column> matchColumns) {
        this.setMatchColumns(matchColumns);
        return this;
    }

    public FullTextSearch withAgainstValue(StringValue againstValue) {
        this.setAgainstValue(againstValue);
        return this;
    }

    public FullTextSearch withSearchModifier(String searchModifier) {
        this.setSearchModifier(searchModifier);
        return this;
    }

    public FullTextSearch addMatchColumns(Column... matchColumns) {
        return this.addMatchColumns(Arrays.asList(matchColumns));
    }

    public FullTextSearch addMatchColumns(Collection<? extends Column> matchColumns) {
        ExpressionList<Column> collection =
                Optional.ofNullable(getMatchColumns()).orElseGet(ExpressionList::new);
        collection.addAll(matchColumns);
        return this.withMatchColumns(collection);
    }
}
