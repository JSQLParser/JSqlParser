/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.export.ExportIntoItem;
import net.sf.jsqlparser.statement.imprt.ImportFromItem;

import java.io.Serializable;
import java.util.List;

public class ScriptSourceDestination implements ImportFromItem, ExportIntoItem, Serializable {
    private Table script;
    private ConnectionDefinition connectionDefinition;
    private List<String> properties;
    private List<StringValue> values;
    private ErrorClause errorClause;

    public Table getScript() {
        return script;
    }

    public void setScript(Table script) {
        this.script = script;
    }

    public ConnectionDefinition getConnectionDefinition() {
        return connectionDefinition;
    }

    public void setConnectionDefinition(ConnectionDefinition connectionDefinition) {
        this.connectionDefinition = connectionDefinition;
    }

    public List<String> getProperties() {
        return properties;
    }

    public void setProperties(List<String> properties) {
        this.properties = properties;
    }

    public List<StringValue> getValues() {
        return values;
    }

    public void setValues(List<StringValue> values) {
        this.values = values;
    }

    @Override
    public ErrorClause getErrorClause() {
        return errorClause;
    }

    @Override
    public void setErrorClause(ErrorClause errorClause) {
        this.errorClause = errorClause;
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();

        sql.append("SCRIPT ");
        sql.append(script);

        if (connectionDefinition != null) {
            sql.append(" ");
            sql.append(connectionDefinition);
        }

        if (properties != null && values != null) {
            sql.append(" WITH");

            int max = Math.min(properties.size(), values.size());
            for (int i = 0; i < max; i++) {
                sql.append(" ");
                sql.append(properties.get(i));
                sql.append(" = ");
                sql.append(values.get(i));
            }
        }

        if (errorClause != null) {
            sql.append(errorClause);
        }

        return sql.toString();
    }
}
