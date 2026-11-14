/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.operator;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ExclusionOperator;

/**
 * PostgreSQL {@code CREATE OPERATOR [schema.]op (FUNCTION = fn, LEFTARG = type, ...)}. The operator
 * keeps its exact spelling. PostgreSQL checks the names and values of the definition when it runs
 * the statement, so they are not restricted.
 */
public class CreateOperator extends ASTNodeAccessImpl implements Statement {
    private ExclusionOperator operator;
    private List<DefinitionElement> definition = new ArrayList<>();

    /**
     * The operator symbol and its schema, read as in DROP OPERATOR and COMMENT ON OPERATOR.
     *
     * @return the operator
     */
    public ExclusionOperator getOperator() {
        return operator;
    }

    public void setOperator(ExclusionOperator operator) {
        this.operator = operator;
    }

    /**
     * The definition elements in source order, such as {@code FUNCTION = fn} or {@code HASHES}.
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
     * @param name the name, such as {@code COMMUTATOR}
     * @return the first element of that name, or {@code null}
     */
    public DefinitionElement getDefinitionElement(String name) {
        return DefinitionElement.find(definition, name);
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        return builder.append("CREATE OPERATOR ").append(operator.getQualifiedName()).append(' ')
                .append(DefinitionElement.toString(definition));
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
