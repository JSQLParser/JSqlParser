/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter.database;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.database.DatabaseOption;

/** MySQL ALTER DATABASE (or SCHEMA); a missing name selects the current database. */
public class AlterDatabase implements Statement {
    private String databaseName;
    private boolean useSchemaKeyword;
    private final List<DatabaseOption> options = new ArrayList<>();

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean isUseSchemaKeyword() {
        return useSchemaKeyword;
    }

    public void setUseSchemaKeyword(boolean useSchemaKeyword) {
        this.useSchemaKeyword = useSchemaKeyword;
    }

    public List<DatabaseOption> getOptions() {
        return options;
    }

    public Optional<DatabaseOption> getOption(DatabaseOption.Kind kind) {
        return options.stream().filter(option -> option.getKind() == kind).findFirst();
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(useSchemaKeyword ? "ALTER SCHEMA" : "ALTER DATABASE");
        if (databaseName != null) {
            builder.append(' ').append(databaseName);
        }
        options.forEach(option -> builder.append(' ').append(option));
        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> visitor, S context) {
        return visitor.visit(this, context);
    }
}
