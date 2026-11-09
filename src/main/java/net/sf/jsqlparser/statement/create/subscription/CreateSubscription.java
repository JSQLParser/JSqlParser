/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class CreateSubscription extends ASTNodeAccessImpl implements Statement {
    private String name;
    private StringValue connection;
    private List<String> publications = new ArrayList<>();
    private List<SubscriptionOption> options = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StringValue getConnection() {
        return connection;
    }

    public void setConnection(StringValue connection) {
        this.connection = connection;
    }

    public List<String> getPublications() {
        return publications;
    }

    public void setPublications(List<String> publications) {
        this.publications = publications;
    }

    public List<SubscriptionOption> getOptions() {
        return options;
    }

    public void setOptions(List<SubscriptionOption> options) {
        this.options = options;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressions) {
        sql.append("CREATE SUBSCRIPTION ").append(name).append(" CONNECTION ");
        expressions.accept(connection);
        sql.append(" PUBLICATION ").append(String.join(", ", publications));
        if (!options.isEmpty()) {
            sql.append(" WITH (");
            for (int i = 0; i < options.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                options.get(i).appendTo(sql, expressions);
            }
            sql.append(')');
        }
    }

    @Override
    public String toString() {
        StringBuilder sql = new StringBuilder();
        appendTo(sql, sql::append);
        return sql.toString();
    }
}
