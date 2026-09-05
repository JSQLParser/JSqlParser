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
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Index implements TableElement, Serializable {

    public enum Kind {
        PRIMARY_KEY, UNIQUE, INDEX, FULLTEXT, SPATIAL, FOREIGN_KEY, CHECK, EXCLUDE, OTHER
    }

    private final List<String> name = new ArrayList<>();
    private String type;
    private String using;
    private List<ColumnParams> columns;
    private List<String> idxSpec;
    private String commentText;
    private String indexKeyword;
    private Kind kind = Kind.OTHER;

    public List<String> getColumnsNames() {
        return columns.stream()
                .map(ColumnParams::toString)
                .collect(toList());
    }

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

    public void setType(String string) {
        type = string;
        if (kind == Kind.OTHER && string != null) {
            String normalized = string.toUpperCase(java.util.Locale.ROOT);
            if (normalized.startsWith("PRIMARY")) {
                kind = Kind.PRIMARY_KEY;
            } else if (normalized.startsWith("UNIQUE")) {
                kind = Kind.UNIQUE;
            } else if (normalized.startsWith("FULLTEXT")) {
                kind = Kind.FULLTEXT;
            } else if (normalized.startsWith("SPATIAL")) {
                kind = Kind.SPATIAL;
            } else if (normalized.startsWith("FOREIGN")) {
                kind = Kind.FOREIGN_KEY;
            } else if (normalized.startsWith("CHECK")) {
                kind = Kind.CHECK;
            } else if (normalized.startsWith("EXCLUDE")) {
                kind = Kind.EXCLUDE;
            } else if (normalized.contains("INDEX") || normalized.contains("KEY")) {
                kind = Kind.INDEX;
            }
        }
    }

    public Kind getKind() {
        return kind;
    }

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
        String idxSpecText = PlainSelect.getStringList(idxSpec, false, false);
        String keyword = indexKeyword != null
                && (type == null || !type.toUpperCase(java.util.Locale.ROOT)
                        .endsWith(indexKeyword.toUpperCase(java.util.Locale.ROOT)))
                                ? " " + indexKeyword
                                : "";
        String head =
                (type != null ? type : "") +
                        keyword +
                        (!name.isEmpty() ? " " + getName() : "") +
                        (using != null ? " USING " + using : "");

        String tail = (columns != null && !columns.isEmpty()
                ? PlainSelect.getStringList(columns, true, true)
                : "")
                + (!idxSpecText.isEmpty() ? " " + idxSpecText : "");

        return tail.isEmpty() ? head : head + " " + tail;
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
        private String collation;
        private String operatorClass;
        private List<Option> operatorClassParameters;
        private SortOrder sortOrder;
        private NullOrdering nullOrdering;

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
            StringBuilder builder = new StringBuilder(
                    expression != null ? "(" + expression + ")" : columnName);
            appendParams(builder);
            appendCollation(builder);
            appendOperatorClass(builder);
            appendSortOrder(builder);
            appendNullOrdering(builder);
            return builder.toString();
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

        private void appendOperatorClass(StringBuilder builder) {
            if (operatorClass != null && !hasParam(operatorClass)) {
                builder.append(" ").append(operatorClass);
                if (operatorClassParameters != null && !operatorClassParameters.isEmpty()) {
                    builder.append(" ")
                            .append(PlainSelect.getStringList(
                                    operatorClassParameters, true, true));
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

    /** A named PostgreSQL index option with an optional value. */
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
            return value == null ? name : name + (useEquals ? " = " : " ") + value;
        }
    }
}
