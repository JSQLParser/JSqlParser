/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.schema.Table;

/**
 * A MySQL/MariaDB {@code CHECK TABLE}, {@code CHECKSUM TABLE}, {@code OPTIMIZE TABLE} or
 * {@code REPAIR TABLE} statement. {@code ANALYZE TABLE} remains
 * {@link net.sf.jsqlparser.statement.analyze.Analyze}.
 */
public class TableMaintenanceStatement extends ASTNodeAccessImpl implements Statement {

    public enum Operation {
        CHECK, CHECKSUM, OPTIMIZE, REPAIR;

        /** Whether this operation accepts the option after its table list. */
        public boolean allows(Option option) {
            switch (this) {
                case CHECK:
                    return option != Option.USE_FRM;
                case CHECKSUM:
                    return option == Option.QUICK || option == Option.EXTENDED;
                case REPAIR:
                    return option == Option.QUICK || option == Option.EXTENDED
                            || option == Option.USE_FRM;
                default:
                    return false;
            }
        }

        /** Whether this operation accepts {@code NO_WRITE_TO_BINLOG} or {@code LOCAL}. */
        public boolean allowsModifier() {
            return this == OPTIMIZE || this == REPAIR;
        }
    }

    public enum Modifier {
        NO_WRITE_TO_BINLOG, LOCAL
    }

    public enum Option {
        FOR_UPGRADE, QUICK, FAST, MEDIUM, EXTENDED, CHANGED, USE_FRM;

        /** Returns the SQL spelling, for example {@code FOR UPGRADE}. */
        public String getKeyword() {
            return this == FOR_UPGRADE ? "FOR UPGRADE" : name();
        }

        /** Returns the single-word option spelled by {@code word}, or null. */
        public static Option fromKeyword(String word) {
            for (Option option : values()) {
                if (option != FOR_UPGRADE && option.name().equalsIgnoreCase(word)) {
                    return option;
                }
            }
            return null;
        }
    }

    private Operation operation;
    private Modifier modifier;
    private boolean tablesKeyword;
    private final List<Table> tables = new ArrayList<>();
    private final List<Option> options = new ArrayList<>();

    public TableMaintenanceStatement() {
        // empty constructor
    }

    public TableMaintenanceStatement(Operation operation) {
        this.operation = operation;
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Modifier getModifier() {
        return modifier;
    }

    public void setModifier(Modifier modifier) {
        this.modifier = modifier;
    }

    /** Whether the statement is spelled with {@code TABLES} instead of {@code TABLE}. */
    public boolean isTablesKeyword() {
        return tablesKeyword;
    }

    public void setTablesKeyword(boolean tablesKeyword) {
        this.tablesKeyword = tablesKeyword;
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public void setTables(Collection<? extends Table> tables) {
        this.tables.clear();
        if (tables != null) {
            this.tables.addAll(tables);
        }
    }

    /** Returns the options in source order; repeated options are retained as written. */
    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setOptions(Collection<Option> options) {
        this.options.clear();
        if (options != null) {
            this.options.addAll(options);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder().append(operation);
        if (modifier != null) {
            builder.append(' ').append(modifier);
        }
        builder.append(tablesKeyword ? " TABLES " : " TABLE ")
                .append(tables.stream().map(Table::toString).collect(Collectors.joining(", ")));
        for (Option option : options) {
            builder.append(' ').append(option.getKeyword());
        }
        return builder.toString();
    }

    public TableMaintenanceStatement withOperation(Operation operation) {
        setOperation(operation);
        return this;
    }

    public TableMaintenanceStatement withModifier(Modifier modifier) {
        setModifier(modifier);
        return this;
    }

    public TableMaintenanceStatement withTablesKeyword(boolean tablesKeyword) {
        setTablesKeyword(tablesKeyword);
        return this;
    }

    public TableMaintenanceStatement withTables(Collection<? extends Table> tables) {
        setTables(tables);
        return this;
    }

    public TableMaintenanceStatement addTables(Table... tables) {
        Collections.addAll(this.tables, tables);
        return this;
    }

    public TableMaintenanceStatement withOptions(Collection<Option> options) {
        setOptions(options);
        return this;
    }

    public TableMaintenanceStatement addOptions(Option... options) {
        Collections.addAll(this.options, options);
        return this;
    }
}
