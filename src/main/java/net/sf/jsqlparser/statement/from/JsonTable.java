package net.sf.jsqlparser.statement.from;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.json.JsonOnEmptyType;
import net.sf.jsqlparser.expression.json.JsonOnErrorType;
import net.sf.jsqlparser.statement.select.AbstractFromitem;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.FromItemVisitor;

import java.util.ArrayList;
import java.util.List;

public class JsonTable extends AbstractFromitem implements FromItem {

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
            builder.append(", '");
            builder.append(pathExpression);
            builder.append("'");
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

        super.appendTo(builder, getAlias(), getSampleClause(), getPivot(), getUnPivot());

        return builder;
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
        if (onErrorType != null) {
            switch (onErrorType) {
                case NULL:
                case ERROR:
                    break;
                default:
                    throw new IllegalArgumentException(
                            "OnError type " + onErrorType + " is not allowed in JSON_TABLE");
            }
        }

        this.onErrorType = onErrorType;
    }

    /**
     * Returns the ON ERROR clause or NULL if none is set
     *
     * @return JsonOnErrorType or NULL
     */
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
        if (onEmptyType != null) {
            switch (onEmptyType) {
                case NULL:
                case ERROR:
                    break;
                default:
                    throw new IllegalArgumentException(
                            "OnEmpty type " + onEmptyType + " is not allowed in JSON_TABLE");
            }
        }
        this.onEmptyType = onEmptyType;
    }

    /**
     * Returns the ON EMPTY clause or NULL if none is set
     *
     * @return JsonOnEmptyType or NULL
     */
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

    public JsonTable withColumn(JsonTableColumn column) {
        addColumn(column);
        return this;
    }

    public List<JsonTableColumn> getColumns() {
        return jsonColumns;
    }

    @Override
    public String toString() {
        return append(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(FromItemVisitor<T> fromItemVisitor, S context) {
        return fromItemVisitor.visit(this, context);
    }
}
