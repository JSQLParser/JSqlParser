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

import static java.util.stream.Collectors.toList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Index implements TableElement, Serializable {

    public enum Kind {
        PRIMARY_KEY, UNIQUE, INDEX, FULLTEXT, SPATIAL, FOREIGN_KEY, CHECK, EXCLUDE, DEFAULT, NOT_NULL, OTHER
    }

    public enum Clustering {
        CLUSTERED, NONCLUSTERED
    }

    private final List<String> name = new ArrayList<>();
    private String type;
    private String using;
    private List<ColumnParams> columns;
    private List<String> idxSpec;
    private String commentText;
    private String indexKeyword;
    private Kind kind = Kind.OTHER;
    private Clustering clustering;
    private Boolean nullsDistinct;
    private List<String> includeColumns;
    private List<Option> storageParameters;
    private String tableSpace;
    private ConstraintAttributes constraintAttributes;

    /** Returns the explicit SQL Server clustering option, or null when it was omitted. */
    public Clustering getClustering() {
        return clustering;
    }

    public void setClustering(Clustering clustering) {
        this.clustering = clustering;
    }

    public Index withClustering(Clustering clustering) {
        setClustering(clustering);
        return this;
    }

    public String clusteringClause() {
        return clustering == null ? "" : " " + clustering;
    }

    public Boolean getNullsDistinct() {
        return nullsDistinct;
    }

    public void setNullsDistinct(Boolean nullsDistinct) {
        this.nullsDistinct = nullsDistinct;
    }

    public List<String> getIncludeColumns() {
        return includeColumns;
    }

    public void setIncludeColumns(List<String> includeColumns) {
        this.includeColumns = includeColumns == null ? null : new ArrayList<>(includeColumns);
    }

    public List<Option> getStorageParameters() {
        return storageParameters;
    }

    public void setStorageParameters(List<Option> storageParameters) {
        this.storageParameters =
                storageParameters == null ? null : new ArrayList<>(storageParameters);
    }

    public String getTableSpace() {
        return tableSpace;
    }

    public void setTableSpace(String tableSpace) {
        this.tableSpace = tableSpace;
    }

    public ConstraintAttributes getConstraintAttributes() {
        return constraintAttributes;
    }

    public void setConstraintAttributes(ConstraintAttributes constraintAttributes) {
        this.constraintAttributes = constraintAttributes;
    }

    public String nullsDistinctClause() {
        return nullsDistinct == null ? ""
                : nullsDistinct ? " NULLS DISTINCT" : " NULLS NOT DISTINCT";
    }

    public void appendConstraintOptionsTo(StringBuilder sql) {
        appendConstraintOptionsTo(sql, sql::append);
    }

    public void appendConstraintOptionsTo(StringBuilder sql,
            Consumer<Expression> expressionPrinter) {
        if (includeColumns != null) {
            sql.append(" INCLUDE ").append(PlainSelect.getStringList(includeColumns, true, true));
        }
        if (storageParameters != null) {
            sql.append(" WITH ");
            Option.appendListTo(sql, storageParameters, expressionPrinter);
        }
        if (tableSpace != null) {
            sql.append(" USING INDEX TABLESPACE ").append(tableSpace);
        }
    }

    public void appendConstraintAttributesTo(StringBuilder sql) {
        if (constraintAttributes != null) {
            constraintAttributes.appendTo(sql);
        }
    }

    /**
     * Returns a mutable snapshot of the rendered key elements, including their options. An index
     * without columns returns an empty snapshot. Use {@link #getColumns()} for structured edits.
     */
    public List<String> getColumnsNames() {
        return columns == null ? new ArrayList<>()
                : columns.stream()
                        .map(ColumnParams::toString)
                        .collect(toList());
    }

    /**
     * Replaces all key elements with plain {@link ColumnParams} wrapping the supplied strings, or
     * clears them for null. The strings are not parsed and existing expressions and element options
     * are not retained. Use {@link #setColumns(List)} to preserve structured elements.
     */
    public void setColumnsNames(List<String> list) {
        if (list == null) {
            this.columns = Collections.emptyList();
        } else {
            this.columns = list.stream().map(ColumnParams::new).collect(toList());
        }
    }

    @Deprecated
    public List<ColumnParams> getColumnWithParams() {
        return getColumns();
    }

    @Deprecated
    public void setColumnNamesWithParams(List<ColumnParams> list) {
        setColumns(list);
    }

    public List<ColumnParams> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnParams> columns) {
        this.columns = columns;
    }

    public Index withColumns(List<ColumnParams> columns) {
        setColumns(columns);
        return this;
    }

    public Index addColumns(ColumnParams... functionDeclarationParts) {
        List<ColumnParams> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, functionDeclarationParts);
        return this.withColumns(collection);
    }

    public Index addColumns(Collection<? extends ColumnParams> functionDeclarationParts) {
        List<ColumnParams> collection = Optional.ofNullable(getColumns()).orElseGet(ArrayList::new);
        collection.addAll(functionDeclarationParts);
        return this.withColumns(collection);
    }

    public String getName() {
        return name.isEmpty() ? null : String.join(".", name);
    }

    public void setName(String name) {
        this.name.clear();
        if (name != null) {
            this.name.add(name);
        }
    }

    public void setName(List<String> name) {
        this.name.clear();
        this.name.addAll(name);
    }

    public List<String> getNameParts() {
        return Collections.unmodifiableList(name);
    }

    public String getType() {
        return type;
    }

    /**
     * Sets the rendered type and refreshes its classification, including when replacing an existing
     * type. Null and unrecognized types reset the classification to {@link Kind#OTHER}.
     */
    public void setType(String string) {
        type = string;
        kind = classifyType(string);
    }

    private static Kind classifyType(String type) {
        if (type == null) {
            return Kind.OTHER;
        }
        String normalized = type.trim().toUpperCase(java.util.Locale.ROOT);
        String keyword = normalized.split("\\s+", 2)[0];
        switch (keyword) {
            case "PRIMARY":
                return Kind.PRIMARY_KEY;
            case "UNIQUE":
                return Kind.UNIQUE;
            case "FULLTEXT":
                return Kind.FULLTEXT;
            case "SPATIAL":
                return Kind.SPATIAL;
            case "FOREIGN":
                return Kind.FOREIGN_KEY;
            case "NOT":
                return "NOT NULL".equals(normalized) ? Kind.NOT_NULL : Kind.OTHER;
            case "CHECK":
                return Kind.CHECK;
            case "EXCLUDE":
                return Kind.EXCLUDE;
            case "DEFAULT":
                return Kind.DEFAULT;
            default:
                return normalized.equals("INDEX") || normalized.equals("KEY")
                        || normalized.endsWith(" INDEX") || normalized.endsWith(" KEY")
                                ? Kind.INDEX
                                : Kind.OTHER;
        }
    }

    public Kind getKind() {
        return kind;
    }

    /**
     * Sets classification metadata without changing the rendered type. This also supports index
     * declarations whose keyword is stored separately. A subsequent {@link #setType(String)}
     * derives the classification from the new type again.
     */
    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public Index withColumnsNames(List<String> list) {
        setColumnsNames(list);
        return this;
    }

    public String getUsing() {
        return using;
    }

    /**
     * In postgresql, the index type (Btree, GIST, etc.) is indicated with a USING clause. Please
     * note that: Oracle - the type might be BITMAP, indicating a bitmap kind of index MySQL - the
     * type might be FULLTEXT or SPATIAL
     *
     * @param using
     */
    public void setUsing(String using) {
        this.using = using;
    }

    public List<String> getIndexSpec() {
        return idxSpec;
    }

    public void setIndexSpec(List<String> idxSpec) {
        this.idxSpec = idxSpec;
    }

    public Index withIndexSpec(List<String> idxSpec) {
        setIndexSpec(idxSpec);
        return this;
    }

    public void setIndexKeyword(String indexKeyword) {
        this.indexKeyword = indexKeyword;
    }

    public String getIndexKeyword() {
        return indexKeyword;
    }

    public Index withIndexKeyword(String indexKeyword) {
        this.setIndexKeyword(indexKeyword);
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }

    /** Renders index definitions through the supplied expression writer. */
    public void appendTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        String idxSpecText = PlainSelect.getStringList(idxSpec, false, false);
        String keyword = indexKeyword != null
                && (type == null || !type.toUpperCase(java.util.Locale.ROOT)
                        .endsWith(indexKeyword.toUpperCase(java.util.Locale.ROOT)))
                                ? " " + indexKeyword
                                : "";
        sql.append(type != null ? type : "").append(keyword);
        if (!name.isEmpty()) {
            sql.append(' ').append(getName());
        }
        if (using != null) {
            sql.append(" USING ").append(using);
        }
        sql.append(nullsDistinctClause()).append(clusteringClause());
        boolean hasColumns = columns != null && !columns.isEmpty();
        if (hasColumns) {
            sql.append(' ');
            appendColumnsTo(sql, expressionPrinter);
        }
        if (!idxSpecText.isEmpty()) {
            sql.append(hasColumns ? " " : "  ").append(idxSpecText);
        }
        appendConstraintOptionsTo(sql, expressionPrinter);
        appendConstraintAttributesTo(sql);
    }

    /** Appends a parenthesized list of keys, including expression keys and operator options. */
    protected void appendColumnsTo(StringBuilder sql, Consumer<Expression> expressionPrinter) {
        sql.append('(');
        for (int i = 0; i < columns.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            columns.get(i).appendTo(sql, expressionPrinter);
        }
        sql.append(')');
    }

    public Index withType(String type) {
        this.setType(type);
        return this;
    }

    public Index withKind(Kind kind) {
        setKind(kind);
        return this;
    }

    public Index withUsing(String using) {
        this.setUsing(using);
        return this;
    }

    public Index withName(List<String> name) {
        this.setName(name);
        return this;
    }

    public Index withName(String name) {
        this.setName(name);
        return this;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public static class ColumnParams implements Serializable {
        public enum SortOrder {
            ASC, DESC
        }

        public enum NullOrdering {
            FIRST, LAST
        }

        public final String columnName;
        public final List<String> params;
        private final Expression expression;
        private boolean expressionParenthesized = true;
        private String collation;
        private String operatorClass;
        private List<Option> operatorClassParameters;
        private SortOrder sortOrder;
        private NullOrdering nullOrdering;
        private String exclusionOperator;

        public String getExclusionOperator() {
            return exclusionOperator;
        }

        public void setExclusionOperator(String exclusionOperator) {
            this.exclusionOperator = exclusionOperator;
        }

        public ColumnParams(String columnName) {
            this.columnName = columnName;
            this.params = null;
            this.expression = null;
        }

        public ColumnParams(String columnName, List<String> params) {
            this.columnName = columnName;
            this.params = params;
            this.expression = null;
        }

        public ColumnParams(Expression expression) {
            this.columnName = null;
            this.params = null;
            this.expression = expression;
        }

        public ColumnParams(Expression expression, List<String> params) {
            this.columnName = null;
            this.params = params;
            this.expression = expression;
        }

        public String getColumnName() {
            return expression != null ? expression.toString() : columnName;
        }

        public List<String> getParams() {
            return params;
        }

        public Expression getExpression() {
            return expression;
        }

        public boolean isExpression() {
            return expression != null;
        }

        public boolean isExpressionParenthesized() {
            return expressionParenthesized;
        }

        public void setExpressionParenthesized(boolean expressionParenthesized) {
            this.expressionParenthesized = expressionParenthesized;
        }

        public ColumnParams withExpressionParenthesized(boolean expressionParenthesized) {
            setExpressionParenthesized(expressionParenthesized);
            return this;
        }

        public String getCollation() {
            return collation;
        }

        public void setCollation(String collation) {
            this.collation = collation;
        }

        public String getOperatorClass() {
            return operatorClass;
        }

        public void setOperatorClass(String operatorClass) {
            this.operatorClass = operatorClass;
        }

        public List<Option> getOperatorClassParameters() {
            return operatorClassParameters;
        }

        public void setOperatorClassParameters(List<Option> operatorClassParameters) {
            this.operatorClassParameters = operatorClassParameters;
        }

        public SortOrder getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(SortOrder sortOrder) {
            this.sortOrder = sortOrder;
        }

        public NullOrdering getNullOrdering() {
            return nullOrdering;
        }

        public void setNullOrdering(NullOrdering nullOrdering) {
            this.nullOrdering = nullOrdering;
        }

        public ColumnParams withCollation(String collation) {
            setCollation(collation);
            return this;
        }

        public ColumnParams withOperatorClass(String operatorClass) {
            setOperatorClass(operatorClass);
            return this;
        }

        public ColumnParams withOperatorClassParameters(List<Option> operatorClassParameters) {
            setOperatorClassParameters(operatorClassParameters);
            return this;
        }

        public ColumnParams withSortOrder(SortOrder sortOrder) {
            setSortOrder(sortOrder);
            return this;
        }

        public ColumnParams withNullOrdering(NullOrdering nullOrdering) {
            setNullOrdering(nullOrdering);
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            appendTo(builder, value -> builder.append(value));
            return builder.toString();
        }

        /** Renders expression keys through the caller's expression printer. */
        public void appendTo(StringBuilder builder, Consumer<Expression> expressionPrinter) {
            if (expression != null) {
                if (expressionParenthesized) {
                    builder.append('(');
                }
                expressionPrinter.accept(expression);
                if (expressionParenthesized) {
                    builder.append(')');
                }
            } else {
                builder.append(columnName);
            }
            appendParams(builder);
            appendCollation(builder);
            appendOperatorClass(builder, expressionPrinter);
            appendSortOrder(builder);
            appendNullOrdering(builder);
            if (exclusionOperator != null) {
                builder.append(" WITH ").append(exclusionOperator);
            }
        }

        private void appendParams(StringBuilder builder) {
            if (params != null) {
                builder.append(" ").append(String.join(" ", params));
            }
        }

        private void appendCollation(StringBuilder builder) {
            if (collation != null && !hasParam("COLLATE")) {
                builder.append(" COLLATE ").append(collation);
            }
        }

        private void appendOperatorClass(StringBuilder builder,
                Consumer<Expression> expressionPrinter) {
            if (operatorClass != null && !hasParam(operatorClass)) {
                builder.append(" ").append(operatorClass);
                if (operatorClassParameters != null && !operatorClassParameters.isEmpty()) {
                    builder.append(" ");
                    Option.appendListTo(builder, operatorClassParameters, expressionPrinter);
                }
            }
        }

        private void appendSortOrder(StringBuilder builder) {
            if (sortOrder != null && !hasParam(sortOrder.name())) {
                builder.append(" ").append(sortOrder);
            }
        }

        private void appendNullOrdering(StringBuilder builder) {
            if (nullOrdering != null && !hasParam("NULLS")) {
                builder.append(" NULLS ").append(nullOrdering);
            }
        }

        private boolean hasParam(String expected) {
            return params != null && params.stream().anyMatch(expected::equalsIgnoreCase);
        }
    }

    /** A named index option with an optional value. */
    public static class Option implements Serializable {
        private String name;
        private Expression value;
        private boolean useEquals;

        public Option() {}

        public Option(String name, Expression value, boolean useEquals) {
            this.name = name;
            this.value = value;
            this.useEquals = useEquals;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Expression getValue() {
            return value;
        }

        public void setValue(Expression value) {
            this.value = value;
        }

        public boolean isUseEquals() {
            return useEquals;
        }

        public void setUseEquals(boolean useEquals) {
            this.useEquals = useEquals;
        }

        public Option withName(String name) {
            setName(name);
            return this;
        }

        public Option withValue(Expression value) {
            setValue(value);
            return this;
        }

        public Option withUseEquals(boolean useEquals) {
            setUseEquals(useEquals);
            return this;
        }

        @Override
        public String toString() {
            if (value == null) {
                return name;
            }
            StringBuilder builder = new StringBuilder();
            return appendTo(builder, expression -> builder.append(expression)).toString();
        }

        public StringBuilder appendTo(StringBuilder builder,
                Consumer<Expression> expressionPrinter) {
            builder.append(name);
            if (value != null) {
                builder.append(useEquals ? " = " : " ");
                expressionPrinter.accept(value);
            }
            return builder;
        }

        public static StringBuilder appendListTo(StringBuilder builder, List<Option> options,
                Consumer<Expression> expressionPrinter) {
            builder.append('(');
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                options.get(i).appendTo(builder, expressionPrinter);
            }
            return builder.append(')');
        }
    }
}
