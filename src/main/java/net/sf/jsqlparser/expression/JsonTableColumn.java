package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class JsonTableColumn extends ASTNodeAccessImpl {

    private String name;
    private JsonTableColumnType type;

    private JsonReturnClause returnClause;

    private boolean isFormatJson = false;
    private String jsonPath;

    // Can be true, false or NULL
    private Boolean allowScalars;
    private JsonQueryWrapperType queryWrapperType;
    private JsonOnErrorType onErrorType;
    private JsonOnEmptyType onEmptyType;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public JsonTableColumn withName(String columnName) {
        setName(columnName);
        return this;
    }

    public void setJsonPath(String jsonPath) {
        this.jsonPath = jsonPath;
    }

    public String getJsonPath() {
        return jsonPath;
    }

    public JsonTableColumn withJsonPath(String jsonPath) {
        setJsonPath(jsonPath);
        return this;
    }

    public void setType(JsonTableColumnType type) {
        this.type = type;
    }

    public JsonTableColumn withType(JsonTableColumnType type) {
        setType(type);
        return this;
    }

    public JsonTableColumnType getType() {
        return type;
    }

    public void setFormatJson(boolean usingJson) {
        if (usingJson && type != JsonTableColumnType.JSON_QUERY) {
            throw new IllegalArgumentException(
                    "FORMAT JSON can only be used on JSON_QUERY-Columns");
        }
        this.isFormatJson = true;
    }

    public boolean isFormatJson() {
        return isFormatJson;
    }

    public JsonTableColumn withFormatJson(boolean isFormatJson) {
        setFormatJson(isFormatJson);
        return this;
    }

    public void setOnEmptyType(JsonOnEmptyType onEmptyType) {
        if (onEmptyType != null) {
            switch (type) {
                case JSON_EXISTS:
                    switch (onEmptyType) {
                        case TRUE:
                        case FALSE:
                        case ERROR:
                            break;
                        default:
                            throw new IllegalArgumentException("OnEmpty type " + onEmptyType
                                    + " is not allowed in JsonTableColumn of type " + type);
                    }
                    break;
                case JSON_QUERY:
                case ORDINALITY:
                    throw new IllegalArgumentException("OnEmpty type " + onEmptyType
                            + " is not allowed in JsonTableColumn of type " + type);
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

    public JsonTableColumn withOnEmptyType(JsonOnEmptyType onEmptyType) {
        setOnEmptyType(onEmptyType);
        return this;
    }

    public void setOnErrorType(JsonOnErrorType onErrorType) {
        if (onErrorType != null) {
            switch (type) {
                case JSON_EXISTS:
                    switch (onErrorType) {
                        case TRUE:
                        case FALSE:
                        case ERROR:
                            break;
                        default:
                            throw new IllegalArgumentException("OnError type " + onErrorType
                                    + " is not allowed in JsonTableColumn of type " + type);
                    }
                    break;
                case JSON_QUERY:
                    switch (onErrorType) {
                        case ERROR:
                        case NULL:
                        case EMPTY:
                        case EMPTY_ARRAY:
                        case EMPTY_OBJECT:
                            break;
                        default:
                            throw new IllegalArgumentException("OnError type " + onErrorType
                                    + " is not allowed in JsonTableColumn of type " + type);
                    }
                    break;
                case ORDINALITY:
                    throw new IllegalArgumentException("OnError type " + onErrorType
                            + " is not allowed in JsonTableColumn of type " + type);
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

    public JsonTableColumn withOnErrorType(JsonOnErrorType onErrorType) {
        setOnErrorType(onErrorType);
        return this;
    }

    public void setQueryWrapperType(JsonQueryWrapperType queryWrapperType) {
        if (type != JsonTableColumnType.JSON_QUERY) {
            throw new IllegalArgumentException(
                    "QueryWrapperType is only allowed on columns with type JSON_QUERY");
        }
        this.queryWrapperType = queryWrapperType;
    }

    public JsonQueryWrapperType getQueryWrapperType() {
        return queryWrapperType;
    }

    public JsonTableColumn withQueryWrapperType(JsonQueryWrapperType queryWrapperType) {
        setQueryWrapperType(queryWrapperType);
        return this;
    }

    public Boolean getAllowScalars() {
        return allowScalars;
    }

    public void setAllowScalars(Boolean allowScalars) {
        if (allowScalars != null && type != JsonTableColumnType.JSON_QUERY) {
            throw new IllegalArgumentException(
                    "AllowScalars is only allowed on columns with type JSON_QUERY");
        }
        this.allowScalars = allowScalars;
    }

    public JsonTableColumn withAllowScalars(Boolean allowScalars) {
        setAllowScalars(allowScalars);
        return this;
    }

    public JsonReturnClause getReturnClause() {
        return returnClause;
    }

    public void setReturnClause(JsonReturnClause returnClause) {
        this.returnClause = returnClause;
    }

    public JsonTableColumn withReturnClause(JsonReturnClause returnClause) {
        setReturnClause(returnClause);
        return this;
    }

    public StringBuilder append(StringBuilder builder) {

        builder.append(name);

        switch (type) {
            case ORDINALITY:
                builder.append(" FOR ORDINALITY");
                break;
            case JSON_EXISTS:
                appendJsonExists(builder);
                break;
            case JSON_QUERY:
                appendJsonQuery(builder);
                break;
            default:
                throw new IllegalStateException("Type " + type + " is unknown");
        }

        return builder;
    }

    private void appendJsonQuery(StringBuilder builder) {
        if (returnClause != null) {
            returnClause.append(builder);
        }
        if (isFormatJson) {
            builder.append(" FORMAT JSON");
        }
        if (allowScalars != null) {
            if (allowScalars) {
                builder.append(" ALLOW");
            } else {
                builder.append(" DISALLOW");
            }
            builder.append(" SCALARS");
        }
        if (queryWrapperType != null) {
            builder.append(" ");
            builder.append(queryWrapperType.getValue());
        }
        if (jsonPath != null) {
            builder.append(" PATH '");
            builder.append(jsonPath);
            builder.append("'");
        }
        if (onErrorType != null) {
            builder.append(" ");
            builder.append(onErrorType.getValue());
            builder.append(" ON ERROR");
        }
    }

    private void appendJsonExists(StringBuilder builder) {
        // TODO: Append return type
        builder.append(" EXISTS");
        if (jsonPath != null) {
            builder.append(" PATH '");
            builder.append(jsonPath);
            builder.append("'");
        }
        if (onErrorType != null) {
            builder.append(" ");
            builder.append(onErrorType);
            builder.append(" ON ERROR");
        }
        if (onEmptyType != null) {
            builder.append(" ");
            builder.append(onEmptyType);
            builder.append(" ON EMPTY");
        }
    }

}
