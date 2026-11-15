/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.textsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.sf.jsqlparser.statement.DefinitionArgument;
import net.sf.jsqlparser.statement.DefinitionElement;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;

/** PostgreSQL CREATE TEXT SEARCH PARSER. Functions may appear in any order. */
public class CreateTextSearchParser extends ASTNodeAccessImpl implements Statement {
    private String name;
    private List<DefinitionElement> functions = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreateTextSearchParser withName(String name) {
        setName(name);
        return this;
    }

    public List<DefinitionElement> getFunctions() {
        return functions;
    }

    public void setFunctions(List<DefinitionElement> functions) {
        this.functions = functions;
    }

    public CreateTextSearchParser withFunctions(List<DefinitionElement> functions) {
        setFunctions(functions);
        return this;
    }

    /**
     * Returns the last matching function; the list retains all typed definitions in source order.
     */
    public DefinitionArgument getFunction(String name) {
        DefinitionElement function = find(functions, name);
        return function == null ? null : function.getArgument();
    }

    public DefinitionArgument getStartFunction() {
        return getFunction("START");
    }

    public DefinitionArgument getGetTokenFunction() {
        return getFunction("GETTOKEN");
    }

    public DefinitionArgument getEndFunction() {
        return getFunction("END");
    }

    public DefinitionArgument getLexTypesFunction() {
        return getFunction("LEXTYPES");
    }

    public DefinitionArgument getHeadlineFunction() {
        return getFunction("HEADLINE");
    }

    private static DefinitionElement find(List<DefinitionElement> elements, String name) {
        String requested = name.startsWith("\"")
                ? name.substring(1, name.length() - 1).replace("\"\"", "\"")
                : name.toLowerCase(Locale.ROOT);
        for (int i = elements.size() - 1; i >= 0; i--) {
            DefinitionElement element = elements.get(i);
            String key = element.getName();
            if (key.startsWith("\"")) {
                key = key.substring(1, key.length() - 1).replace("\"\"", "\"");
            } else {
                key = key.toLowerCase(Locale.ROOT);
            }
            if (requested.equals(key)) {
                return element;
            }
        }
        return null;
    }

    public StringBuilder appendTo(StringBuilder sql) {
        return sql.append("CREATE TEXT SEARCH PARSER ").append(name).append(' ')
                .append(DefinitionElement.toString(functions));
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
