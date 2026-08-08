/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;

/**
 * Represents an {@code XMLTABLE} table function (SQL/XML standard, supported by PostgreSQL and
 * Oracle), which turns an XML document into a relational row set, e.g.:
 *
 * <pre>
 * XMLTABLE('//ROWS/ROW'
 *          PASSING data
 *          COLUMNS id int PATH '@id',
 *                  ordinality FOR ORDINALITY,
 *                  country_id text PATH 'COUNTRY_ID',
 *                  size float PATH 'SIZE' DEFAULT 0)
 * </pre>
 *
 * It is the XML counterpart of {@link JsonTableFunction} and mirrors its structure, without the
 * JSON-specific wrapper/error/plan clauses.
 */
public class XmlTableFunction extends Function {

    private Expression rowPathExpression;
    private final List<XmlTablePassingClause> passingClauses = new ArrayList<>();
    private final List<XmlTableColumnDefinition> columnDefinitions = new ArrayList<>();

    public static class XmlTablePassingClause extends ASTNodeAccessImpl implements Serializable {
        private Expression valueExpression;
        private String name;

        public Expression getValueExpression() {
            return valueExpression;
        }

        public XmlTablePassingClause setValueExpression(Expression valueExpression) {
            this.valueExpression = valueExpression;
            return this;
        }

        public String getName() {
            return name;
        }

        public XmlTablePassingClause setName(String name) {
            this.name = name;
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            if (valueExpression != null) {
                expressions.add(valueExpression);
            }
        }

        @Override
        public String toString() {
            return valueExpression + (name != null ? " AS " + name : "");
        }
    }

    public static class XmlTableColumnDefinition extends ASTNodeAccessImpl implements Serializable {
        private String columnName;
        private boolean forOrdinality;
        private ColDataType dataType;
        private Expression pathExpression;
        private Expression defaultExpression;

        public String getColumnName() {
            return columnName;
        }

        public XmlTableColumnDefinition setColumnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public boolean isForOrdinality() {
            return forOrdinality;
        }

        public XmlTableColumnDefinition setForOrdinality(boolean forOrdinality) {
            this.forOrdinality = forOrdinality;
            return this;
        }

        public ColDataType getDataType() {
            return dataType;
        }

        public XmlTableColumnDefinition setDataType(ColDataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Expression getPathExpression() {
            return pathExpression;
        }

        public XmlTableColumnDefinition setPathExpression(Expression pathExpression) {
            this.pathExpression = pathExpression;
            return this;
        }

        public Expression getDefaultExpression() {
            return defaultExpression;
        }

        public XmlTableColumnDefinition setDefaultExpression(Expression defaultExpression) {
            this.defaultExpression = defaultExpression;
            return this;
        }

        public void collectExpressions(List<Expression> expressions) {
            if (pathExpression != null) {
                expressions.add(pathExpression);
            }
            if (defaultExpression != null) {
                expressions.add(defaultExpression);
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(columnName);
            if (forOrdinality) {
                builder.append(" FOR ORDINALITY");
                return builder.toString();
            }
            if (dataType != null) {
                builder.append(" ").append(dataType);
            }
            if (pathExpression != null) {
                builder.append(" PATH ").append(pathExpression);
            }
            if (defaultExpression != null) {
                builder.append(" DEFAULT ").append(defaultExpression);
            }
            return builder.toString();
        }
    }

    public Expression getRowPathExpression() {
        return rowPathExpression;
    }

    public XmlTableFunction setRowPathExpression(Expression rowPathExpression) {
        this.rowPathExpression = rowPathExpression;
        return this;
    }

    public List<XmlTablePassingClause> getPassingClauses() {
        return passingClauses;
    }

    public XmlTableFunction addPassingClause(XmlTablePassingClause passingClause) {
        passingClauses.add(passingClause);
        return this;
    }

    public List<XmlTableColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
    }

    public XmlTableFunction addColumnDefinition(XmlTableColumnDefinition columnDefinition) {
        columnDefinitions.add(columnDefinition);
        return this;
    }

    public List<Expression> getAllExpressions() {
        List<Expression> expressions = new ArrayList<>();
        if (rowPathExpression != null) {
            expressions.add(rowPathExpression);
        }
        for (XmlTablePassingClause passingClause : passingClauses) {
            passingClause.collectExpressions(expressions);
        }
        for (XmlTableColumnDefinition columnDefinition : columnDefinitions) {
            columnDefinition.collectExpressions(expressions);
        }
        return expressions;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("XMLTABLE(");
        builder.append(rowPathExpression);
        if (!passingClauses.isEmpty()) {
            builder.append(" PASSING ");
            boolean first = true;
            for (XmlTablePassingClause passingClause : passingClauses) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(passingClause);
                first = false;
            }
        }
        if (!columnDefinitions.isEmpty()) {
            builder.append(" COLUMNS ");
            boolean first = true;
            for (XmlTableColumnDefinition columnDefinition : columnDefinitions) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(columnDefinition);
                first = false;
            }
        }
        builder.append(")");
        return builder.toString();
    }
}
