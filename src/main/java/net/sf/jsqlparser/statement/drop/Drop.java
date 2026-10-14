/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.drop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.CascadeBehavior;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.index.AlgorithmOrLockOption;
import net.sf.jsqlparser.statement.create.user.UserAccount;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class Drop implements Statement {

    public enum ObjectType {
        DATABASE, EVENT, FUNCTION, INDEX, PROCEDURE, SCHEMA, SEQUENCE, SERVER, TABLE, TABLESPACE, TRIGGER, VIEW, FOREIGN_TABLE, AGGREGATE, DOMAIN, EVENT_TRIGGER, GROUP, OWNED, ROLE, ROUTINE, RULE, TYPE, USER, OTHER;

        /** Targets are {@link RoutineReference}s, each with an optional argument signature. */
        public boolean hasRoutineTargets() {
            return this == FUNCTION || this == PROCEDURE || this == ROUTINE || this == AGGREGATE;
        }

        /** Targets are role or MySQL account names, including the roles of DROP OWNED BY. */
        public boolean hasAccountTargets() {
            return this == USER || this == ROLE || this == GROUP || this == OWNED;
        }

        /** The target can name the table that owns it with ON. */
        public boolean hasOwnerTable() {
            return this == INDEX || this == TRIGGER || this == RULE;
        }
    }

    private String type;
    private ObjectType objectType = ObjectType.OTHER;
    private final List<Table> names = new ArrayList<>();
    private final List<RoutineReference> routines = new ArrayList<>();
    private final List<UserAccount> accounts = new ArrayList<>();
    private List<String> parameters;
    private Table table;
    private int tablePosition;
    private final List<AlgorithmOrLockOption> algorithmOrLockOptions = new ArrayList<>();
    private Integer algorithmOrLockPosition;
    private CascadeBehavior behavior;
    private Map<String, List<String>> typeToParameters = new HashMap<>();
    private boolean ifExists = false;
    private boolean materialized = false;
    private boolean concurrently;
    private boolean undo;
    private boolean force;
    private String engine;
    private boolean engineUseEquals;

    /** PostgreSQL DROP INDEX CONCURRENTLY, preceding IF EXISTS and the index name. */
    public boolean isConcurrently() {
        return concurrently;
    }

    public void setConcurrently(boolean concurrently) {
        this.concurrently = concurrently;
    }

    public Drop withConcurrently(boolean concurrently) {
        setConcurrently(concurrently);
        return this;
    }


    private boolean isUsingTemporary;

    public static String formatFuncParams(List<String> params) {
        if (params == null) {
            return "";
        }
        return params.isEmpty() ? "()" : PlainSelect.getStringList(params, true, true);
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }

    public Table getName() {
        List<Table> current = getNames();
        return current.isEmpty() ? null : current.get(0);
    }

    public void setName(Table name) {
        useNameTargets();
        names.clear();
        if (name != null) {
            names.add(name);
        }
    }

    /**
     * Returns the dropped object names. Routine and account targets return a read-only snapshot of
     * their names; edit {@link #getRoutines()} or {@link #getAccounts()} to change them.
     */
    public List<Table> getNames() {
        if (!routines.isEmpty()) {
            return Collections.unmodifiableList(routines.stream()
                    .map(routine -> routine.getName() == null ? new Table()
                            : qualifiedTable(routine.getName()))
                    .collect(Collectors.toList()));
        }
        if (!accounts.isEmpty()) {
            return Collections.unmodifiableList(accounts.stream()
                    .map(account -> new Table(account.toString(), false))
                    .collect(Collectors.toList()));
        }
        return Collections.unmodifiableList(names);
    }

    public void setNames(Collection<? extends Table> names) {
        useNameTargets();
        this.names.clear();
        if (names != null) {
            this.names.addAll(names);
        }
    }

    /**
     * Routine targets of DROP FUNCTION, PROCEDURE, ROUTINE and AGGREGATE. A routine without an
     * argument list has null arguments; an explicit {@code ()} has an empty list.
     */
    public List<RoutineReference> getRoutines() {
        return Collections.unmodifiableList(routines);
    }

    /** Replaces the targets with routines, discarding names, accounts and legacy parameters. */
    public void setRoutines(Collection<? extends RoutineReference> routines) {
        clearTargets();
        if (routines != null) {
            this.routines.addAll(routines);
        }
    }

    public Drop withRoutines(Collection<? extends RoutineReference> routines) {
        setRoutines(routines);
        return this;
    }

    /** Appends routine targets. Name or account targets are discarded first. */
    public Drop addRoutines(RoutineReference... routines) {
        if (routines.length > 0 && this.routines.isEmpty()) {
            clearTargets();
        }
        Collections.addAll(this.routines, routines);
        return this;
    }

    /** Role or MySQL account targets of DROP USER, ROLE, GROUP and the roles of DROP OWNED BY. */
    public List<UserAccount> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    /** Replaces the targets with accounts, discarding names, routines and legacy parameters. */
    public void setAccounts(Collection<? extends UserAccount> accounts) {
        clearTargets();
        if (accounts != null) {
            this.accounts.addAll(accounts);
        }
    }

    public Drop withAccounts(Collection<? extends UserAccount> accounts) {
        setAccounts(accounts);
        return this;
    }

    /** Appends account targets. Name or routine targets are discarded first. */
    public Drop addAccounts(UserAccount... accounts) {
        if (accounts.length > 0 && this.accounts.isEmpty()) {
            clearTargets();
        }
        Collections.addAll(this.accounts, accounts);
        return this;
    }

    private void clearTargets() {
        names.clear();
        routines.clear();
        accounts.clear();
        typeToParameters = new HashMap<>();
    }

    /**
     * Converts routine or account targets to the legacy representation before a legacy edit: the
     * names, and the last routine's argument declarations as the single legacy signature.
     */
    private void useNameTargets() {
        if (routines.isEmpty() && accounts.isEmpty()) {
            return;
        }
        List<Table> legacyNames = getNames();
        Map<String, List<String>> legacyParameters = new HashMap<>();
        getTypeToParameters().forEach((key, value) -> legacyParameters.put(key,
                value == null ? null : new ArrayList<>(value)));
        routines.clear();
        accounts.clear();
        names.addAll(legacyNames);
        typeToParameters = legacyParameters;
    }

    /** Splits a routine name at the dots outside quoted identifiers. */
    private static Table qualifiedTable(String name) {
        List<String> parts = new ArrayList<>();
        int start = 0;
        char closingQuote = 0;
        for (int i = 0; i < name.length(); i++) {
            char ch = name.charAt(i);
            if (closingQuote != 0) {
                if (ch == closingQuote) {
                    closingQuote = 0;
                }
            } else if (ch == '"' || ch == '`') {
                closingQuote = ch;
            } else if (ch == '[') {
                closingQuote = ']';
            } else if (ch == '.') {
                parts.add(name.substring(start, i));
                start = i + 1;
            }
        }
        parts.add(name.substring(start));
        return parts.size() == 1 ? new Table(parts.get(0), false) : new Table(parts);
    }

    /**
     * Returns the legacy tokens after the targets. With a structured ON table, ALGORITHM or LOCK
     * clauses or CASCADE/RESTRICT behavior, this is a snapshot that includes them in source order;
     * edit {@link #setTable}, {@link #getAlgorithmOrLockOptions()} or {@link #setBehavior} instead.
     */
    public List<String> getParameters() {
        if (table == null && algorithmOrLockOptions.isEmpty() && behavior == null) {
            return parameters;
        }
        List<String> tokens = new ArrayList<>();
        appendParameters(tokens::add, table -> tokens.add(table.toString()));
        if (behavior != null) {
            tokens.add(behavior.name());
        }
        return tokens;
    }

    /**
     * The table named by ON in DROP INDEX, DROP TRIGGER or DROP RULE, or null when SQL does not
     * name one.
     */
    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        setTable(table, this.table == null ? 0 : tablePosition);
    }

    /** Places ON among the remaining parameter tokens, preserving their source order. */
    public void setTable(Table table, int parameterPosition) {
        int size = parameters == null ? 0 : parameters.size();
        if (parameterPosition < 0 || parameterPosition > size) {
            throw new IllegalArgumentException("Invalid ON table position: " + parameterPosition);
        }
        this.table = table;
        tablePosition = table == null ? 0 : parameterPosition;
    }

    public Drop withTable(Table table) {
        setTable(table);
        return this;
    }

    /** Visits table/view targets and explicit owner tables without resolving catalog objects. */
    public void visitTables(Consumer<Table> visitor) {
        if (objectType == ObjectType.TABLE || objectType == ObjectType.VIEW
                || objectType == ObjectType.FOREIGN_TABLE) {
            names.forEach(visitor);
        } else if (objectType.hasOwnerTable() && table != null) {
            visitor.accept(table);
        }
    }

    /**
     * Replaces all legacy parameters and clears the structured ON table, the ALGORITHM and LOCK
     * clauses and the CASCADE/RESTRICT behavior.
     */
    public void setParameters(List<String> list) {
        parameters = list;
        table = null;
        tablePosition = 0;
        algorithmOrLockOptions.clear();
        algorithmOrLockPosition = null;
        behavior = null;
    }

    /** The explicit CASCADE or RESTRICT clause, or null when it is omitted. */
    public CascadeBehavior getBehavior() {
        return behavior;
    }

    public void setBehavior(CascadeBehavior behavior) {
        this.behavior = behavior;
    }

    public Drop withBehavior(CascadeBehavior behavior) {
        setBehavior(behavior);
        return this;
    }

    /** MySQL DROP UNDO TABLESPACE. */
    public boolean isUndo() {
        return undo;
    }

    public void setUndo(boolean undo) {
        this.undo = undo;
    }

    public Drop withUndo(boolean undo) {
        setUndo(undo);
        return this;
    }

    /** PostgreSQL DROP DATABASE ... WITH (FORCE). */
    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public Drop withForce(boolean force) {
        setForce(force);
        return this;
    }

    /** The storage engine of MySQL DROP TABLESPACE ... ENGINE, or null when it is omitted. */
    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Drop withEngine(String engine) {
        setEngine(engine);
        return this;
    }

    /** Whether the ENGINE clause is written with an equals sign. */
    public boolean isEngineUseEquals() {
        return engineUseEquals;
    }

    public void setEngineUseEquals(boolean engineUseEquals) {
        this.engineUseEquals = engineUseEquals;
    }

    public Drop withEngineUseEquals(boolean engineUseEquals) {
        setEngineUseEquals(engineUseEquals);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String string) {
        type = string;
        try {
            objectType = ObjectType.valueOf(string.toUpperCase(Locale.ROOT).replace(' ', '_'));
        } catch (IllegalArgumentException | NullPointerException ignored) {
            objectType = ObjectType.OTHER;
        }
    }

    public ObjectType getObjectType() {
        return objectType;
    }

    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType == null ? ObjectType.OTHER : objectType;
        if (this.objectType != ObjectType.OTHER) {
            this.type = this.objectType.name().replace('_', ' ');
        }
    }

    public boolean isIfExists() {
        return ifExists;
    }

    public void setIfExists(boolean ifExists) {
        this.ifExists = ifExists;
    }

    public boolean isUsingTemporary() {
        return isUsingTemporary;
    }

    public void setUsingTemporary(boolean useTemporary) {
        this.isUsingTemporary = useTemporary;
    }

    public Drop withUsingTemporary(boolean useTemporary) {
        setUsingTemporary(useTemporary);
        return this;
    }

    public boolean isMaterialized() {
        return materialized;
    }

    public void setMaterialized(boolean materialized) {
        this.materialized = materialized;
    }

    /**
     * Returns the legacy routine parameters by object type. For routine targets, this is a
     * read-only snapshot of the last routine's argument declarations, where the legacy
     * single-signature form kept them; edit {@link #getRoutines()} instead.
     */
    public Map<String, List<String>> getTypeToParameters() {
        if (routines.isEmpty()) {
            return typeToParameters;
        }
        RoutineReference last = routines.get(routines.size() - 1);
        List<String> arguments = null;
        if (last.isAllArguments()) {
            arguments = Collections.singletonList("*");
        } else if (last.getArguments() != null) {
            arguments = Collections.unmodifiableList(last.getArguments().stream()
                    .map(Object::toString).collect(Collectors.toList()));
        }
        return Collections.singletonMap(
                objectType.hasRoutineTargets() ? objectType.name() : type, arguments);
    }

    public void setTypeToParameters(Map<String, List<String>> typeToParameters) {
        useNameTargets();
        this.typeToParameters = typeToParameters;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return appendTo(builder, builder::append).toString();
    }

    public StringBuilder appendTo(StringBuilder builder, Consumer<Table> tablePrinter) {
        builder.append("DROP ");
        if (isUsingTemporary) {
            builder.append("TEMPORARY ");
        }
        if (materialized) {
            builder.append("MATERIALIZED ");
        }
        if (undo) {
            builder.append("UNDO ");
        }
        builder.append(type);
        if (concurrently) {
            builder.append(" CONCURRENTLY");
        }
        if (objectType == ObjectType.OWNED) {
            builder.append(" BY");
        }
        builder.append(ifExists ? " IF EXISTS " : " ");
        appendTargets(builder, tablePrinter);
        if (force) {
            builder.append(" WITH (FORCE)");
        }
        if (engine != null) {
            builder.append(engineUseEquals ? " ENGINE = " : " ENGINE ").append(engine);
        }
        appendParameters(token -> builder.append(' ').append(token), table -> {
            builder.append(' ');
            tablePrinter.accept(table);
        });
        if (behavior != null) {
            builder.append(' ').append(behavior);
        }
        return builder;
    }

    private void appendTargets(StringBuilder builder, Consumer<Table> tablePrinter) {
        if (!routines.isEmpty()) {
            builder.append(routines.stream().map(Object::toString)
                    .collect(Collectors.joining(", ")));
        } else if (!accounts.isEmpty()) {
            builder.append(accounts.stream().map(Object::toString)
                    .collect(Collectors.joining(", ")));
        } else {
            appendNames(builder, tablePrinter);
            if ("FUNCTION".equals(type)) {
                builder.append(formatFuncParams(getParamsByType("FUNCTION")));
            }
        }
    }

    private void appendNames(StringBuilder builder, Consumer<Table> tablePrinter) {
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            if (objectType == ObjectType.TABLE || objectType == ObjectType.VIEW
                    || objectType == ObjectType.FOREIGN_TABLE) {
                tablePrinter.accept(names.get(i));
            } else {
                builder.append(names.get(i));
            }
        }
    }

    /** Emits the parameter tokens with ON and the ALGORITHM/LOCK clauses at their positions. */
    private void appendParameters(Consumer<String> tokens, Consumer<Table> onTable) {
        int size = parameters == null ? 0 : parameters.size();
        int optionPosition = algorithmOrLockPosition == null ? size
                : Math.min(algorithmOrLockPosition, size);
        for (int i = 0; i <= size; i++) {
            if (table != null && i == tablePosition) {
                tokens.accept("ON");
                onTable.accept(table);
            }
            if (i == optionPosition) {
                algorithmOrLockOptions.forEach(option -> option.getTokens().forEach(tokens));
            }
            if (i < size) {
                tokens.accept(parameters.get(i));
            }
        }
    }

    public List<String> getParamsByType(String type) {
        return getTypeToParameters().get(type);
    }

    public Drop withIfExists(boolean ifExists) {
        this.setIfExists(ifExists);
        return this;
    }

    public Drop withMaterialized(boolean materialized) {
        this.setMaterialized(materialized);
        return this;
    }

    public Drop withType(String type) {
        this.setType(type);
        return this;
    }

    public Drop withName(Table name) {
        this.setName(name);
        return this;
    }

    public Drop withNames(Collection<? extends Table> names) {
        setNames(names);
        return this;
    }

    public Drop addNames(Table... names) {
        useNameTargets();
        Collections.addAll(this.names, names);
        return this;
    }

    public Drop withObjectType(ObjectType objectType) {
        setObjectType(objectType);
        return this;
    }

    public Drop withParameters(List<String> parameters) {
        this.setParameters(parameters);
        return this;
    }

    public Drop addParameters(String... parameters) {
        return addParameters(java.util.Arrays.asList(parameters));
    }

    /** Appends trailing tokens without discarding a structured ON table or behavior. */
    public Drop addParameters(Collection<String> parameters) {
        if (this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        this.parameters.addAll(parameters);
        return this;
    }

    /**
     * Returns the mutable MySQL ALGORITHM and LOCK clauses of DROP INDEX in source order. They
     * follow the parameter tokens unless positioned with
     * {@link #setAlgorithmOrLockOptions(List, int)}.
     */
    public List<AlgorithmOrLockOption> getAlgorithmOrLockOptions() {
        return algorithmOrLockOptions;
    }

    /** Copies the list membership and places the clauses after all parameter tokens. */
    public void setAlgorithmOrLockOptions(List<AlgorithmOrLockOption> options) {
        setAlgorithmOrLockOptions(options, -1);
    }

    /**
     * Copies the list membership and places the clauses before the parameter token at
     * {@code parameterPosition}; -1 places them after all parameter tokens.
     */
    public void setAlgorithmOrLockOptions(List<AlgorithmOrLockOption> options,
            int parameterPosition) {
        int size = parameters == null ? 0 : parameters.size();
        if (parameterPosition < -1 || parameterPosition > size) {
            throw new IllegalArgumentException(
                    "Invalid ALGORITHM/LOCK position: " + parameterPosition);
        }
        algorithmOrLockOptions.clear();
        if (options != null) {
            algorithmOrLockOptions.addAll(options);
        }
        algorithmOrLockPosition = parameterPosition < 0 ? null : parameterPosition;
    }

    public Drop withAlgorithmOrLockOptions(List<AlgorithmOrLockOption> options) {
        setAlgorithmOrLockOptions(options);
        return this;
    }

    public Drop addAlgorithmOrLockOptions(AlgorithmOrLockOption... options) {
        Collections.addAll(algorithmOrLockOptions, options);
        return this;
    }
}
