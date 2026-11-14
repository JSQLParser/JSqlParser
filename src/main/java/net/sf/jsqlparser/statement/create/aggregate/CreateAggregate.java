/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.aggregate;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.statement.RoutineReference;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/**
 * PostgreSQL {@code CREATE [OR REPLACE] AGGREGATE name (arguments) (SFUNC = fn, STYPE = type,
 * ...)}, and the form from before PostgreSQL 8.2 that names the input type in the definition,
 * {@code CREATE AGGREGATE name (BASETYPE = type, SFUNC = fn, STYPE = type, ...)}. PostgreSQL checks
 * the names and values of the definition when it runs the statement, so they are not restricted.
 */
public class CreateAggregate extends ASTNodeAccessImpl implements Statement {
    private boolean orReplace;
    private RoutineReference aggregate;
    private List<DefinitionElement> definition = new ArrayList<>();

    public boolean isOrReplace() {
        return orReplace;
    }

    public void setOrReplace(boolean orReplace) {
        this.orReplace = orReplace;
    }

    /**
     * The name and the argument signature, read as in DROP AGGREGATE and COMMENT ON AGGREGATE. The
     * old form has a name only: its arguments, {@code *} flag and ORDER BY arguments are unset.
     *
     * @return the aggregate
     */
    public RoutineReference getAggregate() {
        return aggregate;
    }

    public void setAggregate(RoutineReference aggregate) {
        this.aggregate = aggregate;
    }

    /**
     * Whether this is the old form, which has no argument signature before the definition.
     *
     * @return {@code true} for {@code CREATE AGGREGATE name (BASETYPE = type, ...)}
     */
    public boolean isOldStyle() {
        return aggregate != null && aggregate.getArguments() == null;
    }

    /**
     * The definition elements in source order, such as {@code SFUNC = fn} or {@code HYPOTHETICAL}.
     *
     * @return the elements
     */
    public List<DefinitionElement> getDefinition() {
        return definition;
    }

    public void setDefinition(List<DefinitionElement> definition) {
        this.definition = definition;
    }

    /**
     * Finds a definition element by its name, ignoring case.
     *
     * @param name the name, such as {@code SFUNC}
     * @return the first element of that name, or {@code null}
     */
    public DefinitionElement getDefinitionElement(String name) {
        return DefinitionElement.find(definition, name);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append(orReplace ? "CREATE OR REPLACE AGGREGATE " : "CREATE AGGREGATE ")
                .append(aggregate).append(' ').append(DefinitionElement.toString(definition));
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
