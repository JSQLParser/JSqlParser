/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.server;

import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.foreign.ForeignDataStatement;
import net.sf.jsqlparser.expression.StringValue;

public class CreateServer extends ForeignDataStatement {
    private String name;
    private boolean ifNotExists;
    private StringValue type;
    private Expression version;
    private String foreignDataWrapper;

    public String getName() {
        return name;
    }

    public void setName(String value) {
        name = value;
    }

    public boolean isIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(boolean value) {
        ifNotExists = value;
    }

    public StringValue getType() {
        return type;
    }

    public void setType(StringValue value) {
        type = value;
    }

    public Expression getVersion() {
        return version;
    }

    public void setVersion(Expression value) {
        version = value;
    }

    public String getForeignDataWrapper() {
        return foreignDataWrapper;
    }

    public void setForeignDataWrapper(String value) {
        foreignDataWrapper = value;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    @Override
    public StringBuilder appendTo(StringBuilder sql, Consumer<Expression> printer) {
        sql.append("CREATE SERVER ");
        if (ifNotExists) {
            sql.append("IF NOT EXISTS ");
        }
        sql.append(name);
        if (type != null) {
            sql.append(" TYPE ");
            printer.accept(type);
        }
        if (version != null) {
            sql.append(" VERSION ");
            printer.accept(version);
        }
        sql.append(" FOREIGN DATA WRAPPER ").append(foreignDataWrapper);
        appendOptionsTo(sql, printer);
        return sql;
    }

    @Override
    public void visitExpressions(Consumer<Expression> visitor) {
        super.visitExpressions(visitor);
        visit(type, visitor);
        visit(version, visitor);
    }
}
