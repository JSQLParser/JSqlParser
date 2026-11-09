/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.extension;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class CreateExtension extends ASTNodeAccessImpl implements Statement {
    private String name;
    private boolean ifNotExists;
    private boolean useWith;
    private List<Option> options = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public boolean isUseWith() {
        return useWith;
    }

    public void setUseWith(boolean useWith) {
        this.useWith = useWith;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public enum OptionKind {
        SCHEMA, VERSION, CASCADE
    }
    public static class Option implements java.io.Serializable {
        private final OptionKind kind;
        private final String schema;
        private final Expression version;

        public Option(OptionKind kind, String schema, Expression version) {
            this.kind = kind;
            this.schema = schema;
            this.version = version;
        }

        public OptionKind getKind() {
            return kind;
        }

        public String getSchema() {
            return schema;
        }

        public Expression getVersion() {
            return version;
        }

        @Override
        public String toString() {
            return kind + (kind == OptionKind.SCHEMA ? " " + schema
                    : kind == OptionKind.VERSION ? " " + version : "");
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder("CREATE EXTENSION ")
                .append(ifNotExists ? "IF NOT EXISTS " : "").append(name);
        if (useWith) {
            sql.append(" WITH");
        }
        options.forEach(option -> sql.append(' ').append(option));
        return sql.toString();
    }
}
