/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2023 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.MultiPartName;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.SelectItem;

/**
 * RETURNING clause according to <a href=
 * "https://docs.oracle.com/en/database/oracle/oracle-database/19/sqlrf/DELETE.html#GUID-156845A5-B626-412B-9F95-8869B988ABD7"
 * /> Part of UPDATE, INSERT, DELETE statements
 */

public class ReturningClause extends ArrayList<SelectItem<?>> {
    /**
     * List of output targets like Table or UserVariable
     */
    private final List<Object> dataItems;
    private final List<ReturningOutputAlias> outputAliases;
    private Keyword keyword;

    public ReturningClause(Keyword keyword, List<SelectItem<?>> selectItems,
            List<Object> dataItems) {
        this(keyword, selectItems, null, dataItems);
    }

    public ReturningClause(Keyword keyword, List<SelectItem<?>> selectItems,
            List<ReturningOutputAlias> outputAliases, List<Object> dataItems) {
        this.keyword = keyword;
        this.addAll(selectItems);
        this.outputAliases = outputAliases;
        this.dataItems = dataItems;
        normalizeReturningReferences();
    }

    public ReturningClause(String keyword, List<SelectItem<?>> selectItems,
            List<Object> dataItems) {
        this(Keyword.from(keyword), selectItems, dataItems);
    }

    public ReturningClause(String keyword, List<SelectItem<?>> selectItems,
            List<ReturningOutputAlias> outputAliases, List<Object> dataItems) {
        this(Keyword.from(keyword), selectItems, outputAliases, dataItems);
    }

    public ReturningClause(Keyword keyword, List<SelectItem<?>> selectItems) {
        this(keyword, selectItems, null, null);
    }

    public ReturningClause(String keyword, List<SelectItem<?>> selectItems) {
        this(Keyword.from(keyword), selectItems, null, null);
    }

    public Keyword getKeyword() {
        return keyword;
    }

    public ReturningClause setKeyword(Keyword keyword) {
        this.keyword = keyword;
        return this;
    }

    public List<?> getDataItems() {
        return dataItems;
    }

    public List<ReturningOutputAlias> getOutputAliases() {
        return outputAliases;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" ").append(keyword).append(" ");
        if (outputAliases != null && !outputAliases.isEmpty()) {
            builder.append("WITH (");
            for (int i = 0; i < outputAliases.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(outputAliases.get(i));
            }
            builder.append(") ");
        }
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(get(i));
        }

        if (dataItems != null && !dataItems.isEmpty()) {
            builder.append(" INTO ");
            for (int i = 0; i < dataItems.size(); i++) {
                if (i > 0) {
                    builder.append(" ,");
                }
                builder.append(dataItems.get(i));
            }
        }
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    private void normalizeReturningReferences() {
        Map<QualifierKey, ReturningReferenceType> qualifierMap = buildQualifierMap();
        if (qualifierMap.isEmpty()) {
            return;
        }

        ReturningReferenceNormalizer normalizer = new ReturningReferenceNormalizer(qualifierMap);
        forEach(selectItem -> {
            if (selectItem != null && selectItem.getExpression() != null) {
                selectItem.getExpression().accept(normalizer, null);
            }
        });
    }

    private Map<QualifierKey, ReturningReferenceType> buildQualifierMap() {
        LinkedHashMap<QualifierKey, ReturningReferenceType> qualifierMap = new LinkedHashMap<>();

        if (outputAliases == null || outputAliases.isEmpty()) {
            qualifierMap.put(QualifierKey.from("OLD"), ReturningReferenceType.OLD);
            qualifierMap.put(QualifierKey.from("NEW"), ReturningReferenceType.NEW);
            return qualifierMap;
        }

        for (ReturningOutputAlias outputAlias : outputAliases) {
            if (outputAlias == null || outputAlias.getAlias() == null
                    || outputAlias.getReferenceType() == null) {
                continue;
            }
            qualifierMap.put(QualifierKey.from(outputAlias.getAlias()),
                    outputAlias.getReferenceType());
        }
        return qualifierMap;
    }

    private static class ReturningReferenceNormalizer extends ExpressionVisitorAdapter<Void> {
        private final Map<QualifierKey, ReturningReferenceType> qualifierMap;

        ReturningReferenceNormalizer(Map<QualifierKey, ReturningReferenceType> qualifierMap) {
            this.qualifierMap = qualifierMap;
        }

        @Override
        public <S> Void visit(Column column, S context) {
            Table table = column.getTable();
            String qualifier = extractSimpleQualifier(table);
            if (qualifier == null) {
                return null;
            }
            ReturningReferenceType referenceType = qualifierMap.get(QualifierKey.from(qualifier));
            if (referenceType != null) {
                column.withReturningReference(referenceType, qualifier);
                column.setTable(null);
            }
            return null;
        }

        @Override
        public <S> Void visit(AllTableColumns allTableColumns, S context) {
            Table table = allTableColumns.getTable();
            String qualifier = extractSimpleQualifier(table);
            if (qualifier == null) {
                return null;
            }
            ReturningReferenceType referenceType = qualifierMap.get(QualifierKey.from(qualifier));
            if (referenceType != null) {
                allTableColumns.withReturningReference(referenceType, qualifier);
                allTableColumns.setTable(null);
            }
            return null;
        }

        private String extractSimpleQualifier(Table table) {
            if (table == null || table.getSchemaName() != null || table.getDatabaseName() != null) {
                return null;
            }
            String qualifier = table.getName();
            if (qualifier == null || qualifier.contains("@")) {
                return null;
            }
            return qualifier;
        }
    }

    private static class QualifierKey {
        private final boolean quoted;
        private final String normalizedIdentifier;

        private QualifierKey(boolean quoted, String normalizedIdentifier) {
            this.quoted = quoted;
            this.normalizedIdentifier = normalizedIdentifier;
        }

        static QualifierKey from(String identifier) {
            boolean quoted = MultiPartName.isQuoted(identifier);
            String unquoted = MultiPartName.unquote(identifier);
            if (!quoted && unquoted != null) {
                unquoted = unquoted.toUpperCase(Locale.ROOT);
            }
            return new QualifierKey(quoted, unquoted);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof QualifierKey)) {
                return false;
            }
            QualifierKey that = (QualifierKey) o;
            return quoted == that.quoted
                    && Objects.equals(normalizedIdentifier, that.normalizedIdentifier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(quoted, normalizedIdentifier);
        }
    }

    public enum Keyword {
        RETURN, RETURNING;

        public static Keyword from(String keyword) {
            return Enum.valueOf(Keyword.class, keyword.toUpperCase());
        }
    }
}
