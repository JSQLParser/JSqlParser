/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

/** PRIMARY KEY or UNIQUE declaration with explicitly supplied supporting-index options. */
public class KeyConstraint extends NamedConstraint implements KeyColumnSource, IndexOptionSource {
    private List<KeyElement> columns;
    private String indexName;
    private String indexKeyword;
    private IndexOptions indexOptions = new IndexOptions();

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String value) {
        indexName = value;
    }

    public KeyConstraint withIndexName(String value) {
        setIndexName(value);
        return this;
    }

    public String getIndexKeyword() {
        return indexKeyword;
    }

    public void setIndexKeyword(String value) {
        indexKeyword = value;
    }

    public KeyConstraint withIndexKeyword(String value) {
        setIndexKeyword(value);
        return this;
    }

    @Override
    public KeyConstraint withKind(ConstraintKind value) {
        setKind(value);
        return this;
    }

    @Override
    public KeyConstraint withUseConstraintKeyword(boolean value) {
        setUseConstraintKeyword(value);
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

    public KeyConstraint withColumns(List<KeyElement> value) {
        setColumns(value);
        return this;
    }

    public KeyConstraint withColumnsNames(List<String> value) {
        setColumnsNames(value);
        return this;
    }

    public KeyConstraint addColumns(KeyElement... values) {
        return addColumns(java.util.Arrays.asList(values));
    }

    public KeyConstraint addColumns(Collection<? extends KeyElement> values) {
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

    public KeyConstraint withUsing(String value) {
        setUsing(value);
        return this;
    }

    public KeyConstraint withIndexSpec(List<String> value) {
        setIndexSpec(value);
        return this;
    }

    public KeyConstraint withClustering(IndexOptions.Clustering value) {
        setClustering(value);
        return this;
    }

    public KeyConstraint withType(String value) {
        setType(value);
        return this;
    }

    public KeyConstraint withName(String value) {
        setName(value);
        return this;
    }

    public KeyConstraint withName(List<String> value) {
        setName(value);
        return this;
    }

    @Override
    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        String spec = PlainSelect.getStringList(getIndexSpec(), false, false);
        String keyword = indexKeyword != null && (getType() == null
                || !getType().toUpperCase(java.util.Locale.ROOT)
                        .endsWith(indexKeyword.toUpperCase(java.util.Locale.ROOT)))
                                ? " " + indexKeyword
                                : "";
        appendConstraintPrefixTo(sql);
        sql.append(getType()).append(nullsDistinctClause()).append(keyword)
                .append(clusteringClause());
        if (indexName != null) {
            sql.append(' ').append(indexName);
        }
        if (getUsing() != null) {
            sql.append(" USING ").append(getUsing());
        }
        if (columns != null) {
            sql.append(' ');
            appendColumnsTo(sql, expressionPrinter);
        }
        if (!spec.isEmpty()) {
            sql.append(' ').append(spec);
        }
        appendIndexOptionsTo(sql, expressionPrinter);
        appendConstraintSuffixTo(sql);
        appendConstraintAttributesTo(sql);
    }
}
