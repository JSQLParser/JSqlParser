package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

import java.util.ArrayList;
import java.util.List;

public class JsonTable extends ASTNodeAccessImpl implements Expression {

    private Expression expression;
    private boolean isFormatJson = false;

    private String pathExpression;

    private JsonOnErrorType onErrorType;
    private JsonTableType type;
    private JsonOnEmptyType onEmptyType;

    private List<JsonTableColumn> jsonColumns = new ArrayList<>();


    public StringBuilder append(StringBuilder builder) {
        builder.append("JSON_TABLE(");
        builder.append(expression.toString());

        if (isFormatJson) {
            builder.append(" FORMAT JSON");
        }

        if (pathExpression != null) {
            builder.append(", ");
            builder.append(pathExpression);
        }

        if (onErrorType != null) {
            builder.append(" ");
            builder.append(onErrorType);
            builder.append(" ON ERROR");
        }

        if (type != null) {
            builder.append(" TYPE(");
            builder.append(type);
            builder.append(")");
        }

        if (onEmptyType != null) {
            builder.append(" ");
            builder.append(onEmptyType);
            builder.append(" ON EMPTY");
        }

        builder.append(" COLUMNS(");

        for (JsonTableColumn column : jsonColumns) {
            column.append(builder);
        }

        builder.append("))");
        return builder;
    }

    @Override
    public <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context) {
        return expressionVisitor.visit(this, context);
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Expression getExpression() {
        return expression;
    }

    public JsonTable withExpression(Expression expression) {
        setExpression(expression);
        return this;
    }

    public void setPathExpression(String pathExpression) {
        this.pathExpression = pathExpression;
    }

    public String getPathExpression() {
        return pathExpression;
    }

    public JsonTable withPathExpression(String pathExpression) {
        setPathExpression(pathExpression);
        return this;
    }

    public void setFormatJson(boolean usingJson) {
        this.isFormatJson = true;
    }

    public boolean isFormatJson() {
        return isFormatJson;
    }

    public JsonTable withFormatJson(boolean isFormatJson) {
        setFormatJson(isFormatJson);
        return this;
    }

    public void setOnErrorType(JsonOnErrorType onErrorType) {
        this.onErrorType = onErrorType;
    }

    public JsonOnErrorType getOnErrorType() {
        return onErrorType;
    }

    public JsonTable withOnErrorType(JsonOnErrorType onErrorType) {
        setOnErrorType(onErrorType);
        return this;
    }

    public void setType(JsonTableType type) {
        this.type = type;
    }

    public JsonTableType getType() {
        return type;
    }

    public JsonTable withType(JsonTableType type) {
        setType(type);
        return this;
    }

    public void setOnEmptyType(JsonOnEmptyType onEmptyType) {
        this.onEmptyType = onEmptyType;
    }

    public JsonOnEmptyType getOnEmptyType() {
        return onEmptyType;
    }

    public JsonTable withOnEmptyType(JsonOnEmptyType onEmptyType) {
        setOnEmptyType(onEmptyType);
        return this;
    }

    public void addColumn(JsonTableColumn column) {
        this.jsonColumns.add(column);
    }

    public List<JsonTableColumn> getColumns() {
        return jsonColumns;
    }

}
