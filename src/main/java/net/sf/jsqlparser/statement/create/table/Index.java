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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** An index declaration, independent of table and column constraints. */
public class Index implements TableElement, Serializable, KeyColumnSource, IndexOptionSource {
    public enum Kind {
        UNIQUE, INDEX, FULLTEXT, SPATIAL, OTHER
    }

    private final List<String> name = new ArrayList<>();
    private String type;
    private Kind kind = Kind.OTHER;
    private List<KeyElement> columns;
    private String indexKeyword;
    private IndexOptions indexOptions = new IndexOptions();
    private List<String> tailParameters = new ArrayList<>();

    /** Opaque accepted suffixes rendered after the structured index options. */
    public List<String> getTailParameters() {
        return tailParameters;
    }

    public void setTailParameters(List<String> values) {
        tailParameters = values;
    }

    public Index withTailParameters(List<String> values) {
        setTailParameters(values);
        return this;
    }

    public String getName() {
        return name.isEmpty() ? null : String.join(".", name);
    }

    public void setName(String value) {
        name.clear();
        if (value != null) {
            name.add(value);
        }
    }

    public void setName(List<String> value) {
        name.clear();
        name.addAll(value);
    }

    public List<String> getNameParts() {
        return Collections.unmodifiableList(name);
    }

    public String getType() {
        return type;
    }

    /** Retains SQL spelling and refreshes the index-only classification. */
    public void setType(String value) {
        type = value;
        if (value == null) {
            kind = Kind.OTHER;
            return;
        }
        String normalized = value.trim().toUpperCase(java.util.Locale.ROOT);
        switch (normalized.split("\\s+", 2)[0]) {
            case "UNIQUE":
                kind = Kind.UNIQUE;
                break;
            case "FULLTEXT":
                kind = Kind.FULLTEXT;
                break;
            case "SPATIAL":
                kind = Kind.SPATIAL;
                break;
            case "PRIMARY":
            case "FOREIGN":
            case "CHECK":
            case "EXCLUDE":
            case "DEFAULT":
            case "NOT":
                kind = Kind.OTHER;
                break;
            default:
                kind = normalized.equals("INDEX") || normalized.equals("KEY")
                        || normalized.endsWith(" INDEX") || normalized.endsWith(" KEY")
                                ? Kind.INDEX
                                : Kind.OTHER;
        }
    }

    public Kind getKind() {
        return kind;
    }

    /** Changes classification metadata without rewriting SQL. */
    public void setKind(Kind value) {
        kind = value;
    }

    public Index withKind(Kind value) {
        setKind(value);
        return this;
    }

    public String getIndexKeyword() {
        return indexKeyword;
    }

    public void setIndexKeyword(String value) {
        indexKeyword = value;
    }

    public Index withIndexKeyword(String value) {
        setIndexKeyword(value);
        return this;
    }

    @Override
    public List<KeyElement> getColumns() {
        return columns;
    }

    @Override
    public void setColumns(List<KeyElement> value) {
        columns = value;
    }

    public Index withColumns(List<KeyElement> value) {
        setColumns(value);
        return this;
    }

    public Index withColumnsNames(List<String> value) {
        setColumnsNames(value);
        return this;
    }

    public Index addColumns(KeyElement... values) {
        return addColumns(java.util.Arrays.asList(values));
    }

    public Index addColumns(Collection<? extends KeyElement> values) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.addAll(values);
        return this;
    }

    @Override
    public IndexOptions getIndexOptions() {
        return indexOptions;
    }

    public void setIndexOptions(IndexOptions options) {
        indexOptions = Objects.requireNonNull(options, "indexOptions");
    }

    public Index withUsing(String value) {
        setUsing(value);
        return this;
    }

    public Index withIndexSpec(List<String> value) {
        setIndexSpec(value);
        return this;
    }

    public Index withClustering(IndexOptions.Clustering value) {
        setClustering(value);
        return this;
    }

    public Index withType(String value) {
        setType(value);
        return this;
    }

    public Index withName(String value) {
        setName(value);
        return this;
    }

    public Index withName(List<String> value) {
        setName(value);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        appendDeclarationTo(sql);
        appendColumnsAndSpecTo(sql, expressionPrinter);
        appendIndexOptionsTo(sql, expressionPrinter);
        if (tailParameters != null && !tailParameters.isEmpty()) {
            sql.append(' ').append(PlainSelect.getStringList(tailParameters, false, false));
        }
    }

    private void appendDeclarationTo(StringBuilder sql) {
        String keyword = indexKeyword != null && (type == null
                || !type.toUpperCase(java.util.Locale.ROOT)
                        .endsWith(indexKeyword.toUpperCase(java.util.Locale.ROOT)))
                                ? " " + indexKeyword
                                : "";
        sql.append(type != null ? type : "").append(keyword);
        if (!name.isEmpty()) {
            sql.append(' ').append(getName());
        }
        if (getUsing() != null) {
            sql.append(" USING ").append(getUsing());
        }
        sql.append(nullsDistinctClause()).append(clusteringClause());
    }

    private void appendColumnsAndSpecTo(StringBuilder sql,
            Consumer<Expression> expressionPrinter) {
        String spec = PlainSelect.getStringList(getIndexSpec(), false, false);
        boolean hasColumns = columns != null && !columns.isEmpty();
        if (hasColumns) {
            sql.append(' ');
            appendColumnsTo(sql, expressionPrinter);
        }
        if (!spec.isEmpty()) {
            sql.append(hasColumns ? " " : "  ").append(spec);
        }
    }
}
