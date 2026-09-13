/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2024 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/*
 * STRUCT<T>
 *
 * Type Declaration Meaning STRUCT<INT64> Simple struct with a single unnamed 64-bit integer field.
 * STRUCT<x STRING(10)> Simple struct with a single parameterized string field named x. STRUCT<x
 * STRUCT<y INT64, z INT64>> A struct with a nested struct named x inside it. The struct x has two
 * fields, y and z, both of which are 64-bit integers. STRUCT<inner_array ARRAY<INT64>> A struct
 * containing an array named inner_array that holds 64-bit integer elements.
 *
 * STRUCT( expr1 [AS field_name] [, ... ])
 *
 * Syntax Output Type STRUCT(1,2,3) STRUCT<int64,int64,int64> STRUCT() STRUCT<> STRUCT('abc')
 * STRUCT<string> STRUCT(1, t.str_col) STRUCT<int64, str_col string> STRUCT(1 AS a, 'abc' AS b)
 * STRUCT<a int64, b string> STRUCT(str_col AS abc) STRUCT<abc string>
 *
 *
 * Struct Literals
 *
 * Example Output Type (1, 2, 3) STRUCT<INT64, INT64, INT64> (1, 'abc') STRUCT<INT64, STRING>
 * STRUCT(1 AS foo, 'abc' AS bar) STRUCT<foo INT64, bar STRING> STRUCT<INT64, STRING>(1, 'abc')
 * STRUCT<INT64, STRING> STRUCT(1) STRUCT<INT64> STRUCT<INT64>(1) STRUCT<INT64>
 *
 */
public class StructType extends ASTNodeAccessImpl implements Expression {
    private Dialect dialect = Dialect.BIG_QUERY;;
    private String keyword;
    private List<Map.Entry<String, ColDataType>> parameters;
    private List<SelectItem<?>> arguments;

    public StructType(Dialect dialect, String keyword,
            List<Map.Entry<String, ColDataType>> parameters,
            List<SelectItem<?>> arguments) {
        this.dialect = dialect;
        this.keyword = keyword;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public StructType(Dialect dialect, List<Map.Entry<String, ColDataType>> parameters,
            List<SelectItem<?>> arguments) {
        this.dialect = dialect;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public StructType(Dialect dialect, List<SelectItem<?>> arguments) {
        this.dialect = dialect;
        this.arguments = arguments;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public StructType setDialect(Dialect dialect) {
        this.dialect = dialect;
        return this;
    }

    public String getKeyword() {
        return keyword;
    }

    public StructType setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public List<Map.Entry<String, ColDataType>> getParameters() {
        return parameters;
    }

    public StructType setParameters(List<Map.Entry<String, ColDataType>> parameters) {
        this.parameters = parameters;
        return this;
    }

    public List<SelectItem<?>> getArguments() {
        return arguments;
    }

    public StructType setArguments(List<SelectItem<?>> arguments) {
        this.arguments = arguments;
        return this;
    }

    public StructType add(Expression expression, String aliasName) {
        if (arguments == null) {
            arguments = new ArrayList<>();
        }
        arguments.add(new SelectItem<>(expression, aliasName));

        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        return appendTo(builder, expression -> builder.append(expression));
    }

    /** Renders current argument expressions through the caller's visitor. */
    public StringBuilder appendTo(StringBuilder builder, Consumer<Expression> expressions) {
        if (dialect == Dialect.DUCKDB) {
            appendArguments(builder, expressions);
            appendTypeParameters(builder, true);
        } else {
            if (keyword != null) {
                builder.append(keyword);
            }
            appendTypeParameters(builder, false);
            appendArguments(builder, expressions);
        }
        return builder;
    }

    private void appendTypeParameters(StringBuilder builder, boolean cast) {
        if (parameters == null || parameters.isEmpty()) {
            return;
        }
        builder.append(cast ? "::STRUCT( " : "<");
        int i = 0;
        for (Map.Entry<String, ColDataType> parameter : parameters) {
            if (i++ > 0) {
                builder.append(',');
            }
            if (cast || parameter.getKey() != null && !parameter.getKey().isEmpty()) {
                builder.append(parameter.getKey()).append(' ');
            }
            builder.append(parameter.getValue());
        }
        builder.append(cast ? ')' : '>');
    }

    private void appendArguments(StringBuilder builder, Consumer<Expression> expressions) {
        if (arguments == null || arguments.isEmpty()) {
            return;
        }
        boolean structLiteral = dialect == Dialect.DUCKDB;
        builder.append(structLiteral ? "{ " : "(");
        int i = 0;
        for (SelectItem<?> argument : arguments) {
            if (i++ > 0) {
                builder.append(',');
            }
            if (structLiteral) {
                builder.append(argument.getAlias().getName()).append(':');
            }
            expressions.accept(argument.getExpression());
            if (!structLiteral && argument.getAlias() != null) {
                builder.append(argument.getAlias());
            }
        }
        builder.append(structLiteral ? " }" : ")");
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public enum Dialect {
        BIG_QUERY, DUCKDB
    }
}
