package net.sf.jsqlparser.expression.json;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public class JsonReturnClause extends ASTNodeAccessImpl {

    private JsonReturnType type;

    private Long varcharSize;

    public JsonReturnClause() {}

    public JsonReturnClause(JsonReturnType type) {
        this.type = type;
    }

    public JsonReturnType getType() {
        return type;
    }

    public void setType(JsonReturnType type) {
        this.type = type;
    }

    public JsonReturnClause withType(JsonReturnType type) {
        setType(type);
        return this;
    }

    public Long getVarcharSize() {
        return varcharSize;
    }

    public void setVarcharSize(Long varcharSize) {
        this.varcharSize = varcharSize;
    }

    public JsonReturnClause withVarcharSize(Long varcharSize) {
        setVarcharSize(varcharSize);
        return this;
    }

    public StringBuilder append(StringBuilder builder) {
        builder.append(" ");
        builder.append(type.getValue());
        switch (type) {
            case VARCHAR2:
            case VARCHAR2_BYTE:
            case VARCHAR2_CHAR:
                if (varcharSize != null) {
                    builder.append("(");
                    builder.append(varcharSize);
                    switch (type) {
                        case VARCHAR2_BYTE:
                            builder.append(" BYTE");
                            break;
                        case VARCHAR2_CHAR:
                            builder.append(" CHAR");
                            break;
                    }
                    builder.append(")");
                }
                break;
            default:
                // Nothing to do
                break;
        }
        return builder;
    }

}
