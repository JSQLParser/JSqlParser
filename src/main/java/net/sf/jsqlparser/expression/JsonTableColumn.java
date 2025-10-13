package net.sf.jsqlparser.expression;

public class JsonTableColumn {

    private String columnName;
    private JsonTableColumnType type;

    private String jsonPath;

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public void setType(JsonTableColumnType type) {
        this.type = type;
    }

    public JsonTableColumnType getType() {
        return type;
    }

    public StringBuilder append(StringBuilder builder) {

        builder.append(columnName);

        switch (type) {
            case ORDINALITY:
                builder.append(" FOR ORDINALITY");

                break;
        }

        return builder;
    }

}
