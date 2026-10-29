/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.publication;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

public class CreatePublication extends ASTNodeAccessImpl implements Statement {
    private String name;
    private boolean allTables;
    private List<PublicationTarget> targets = new ArrayList<>();
    private List<PublicationOption> options = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAllTables() {
        return allTables;
    }

    public void setAllTables(boolean allTables) {
        this.allTables = allTables;
    }

    public List<PublicationTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<PublicationTarget> targets) {
        this.targets = targets;
    }

    public List<PublicationOption> getOptions() {
        return options;
    }

    public void setOptions(List<PublicationOption> options) {
        this.options = options;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }

    public void appendTo(StringBuilder sql, Consumer<Expression> expressions) {
        if (allTables && !targets.isEmpty()) {
            throw new IllegalArgumentException("FOR ALL TABLES cannot have explicit targets");
        }
        sql.append("CREATE PUBLICATION ").append(name);
        if (allTables) {
            sql.append(" FOR ALL TABLES");
        } else if (!targets.isEmpty()) {
            sql.append(" FOR ");
            for (int i = 0; i < targets.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                targets.get(i).appendTo(sql, expressions);
            }
        }
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
