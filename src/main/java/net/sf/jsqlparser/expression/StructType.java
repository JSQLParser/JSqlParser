package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;
import java.util.Map;

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
    private String keyword;
    private List<Map.Entry<String, ColDataType>> parameters;
    private List<SelectItem<?>> arguments;

    public StructType(String keyword, List<Map.Entry<String, ColDataType>> parameters,
            List<SelectItem<?>> arguments) {
        this.keyword = keyword;
        this.parameters = parameters;
        this.arguments = arguments;
    }

    public StructType(List<Map.Entry<String, ColDataType>> parameters,
            List<SelectItem<?>> arguments) {
        this.keyword = "STRUCT";
        this.parameters = parameters;
        this.arguments = arguments;
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

    public StringBuilder appendTo(StringBuilder builder) {
        if (keyword != null) {
            builder.append(keyword);
        }

        if (parameters != null && !parameters.isEmpty()) {
            builder.append("<");
            int i = 0;

            for (Map.Entry<String, ColDataType> e : parameters) {
                if (0 < i++) {
                    builder.append(",");
                }
                // optional name
                if (e.getKey() != null && !e.getKey().isEmpty()) {
                    builder.append(e.getKey()).append(" ");
                }

                // mandatory type
                builder.append(e.getValue());
            }

            builder.append(">");
        }

        if (arguments != null && !arguments.isEmpty()) {
            builder.append("(");
            int i = 0;

            for (SelectItem<?> e : arguments) {
                if (0 < i++) {
                    builder.append(",");
                }
                e.appendTo(builder);
            }

            builder.append(")");
        }

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public void accept(ExpressionVisitor expressionVisitor) {
        expressionVisitor.visit(this);
    }
}
