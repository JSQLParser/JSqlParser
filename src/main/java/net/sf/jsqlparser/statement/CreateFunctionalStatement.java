/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A base for the declaration of function like statements
 */
public abstract class CreateFunctionalStatement implements Statement {

    private String kind;

    private List<String> functionDeclarationParts;

    protected CreateFunctionalStatement(String kind) {
        this.kind = kind;
    }

    protected CreateFunctionalStatement(String kind, List<String> functionDeclarationParts) {
        this.kind = kind;
        this.functionDeclarationParts = functionDeclarationParts;
    }

    public void setFunctionDeclarationParts(List<String> functionDeclarationParts) {
        this.functionDeclarationParts = functionDeclarationParts;
    }

    /**
     * @return the declaration parts after {@code CREATE FUNCTION|PROCEDURE}
     */
    public List<String> getFunctionDeclarationParts() {
        return functionDeclarationParts;
    }

    /**
     * @return the kind of functional statement
     */
    public String getKind() {
        return kind;
    }

    /**
     * @return a whitespace appended String with the declaration parts with some
     *         minimal formatting.
     */
    public String formatDeclaration() {
        StringBuilder declaration = new StringBuilder();
        int currIndex = 0;
        while (currIndex < functionDeclarationParts.size()) {
            String token = functionDeclarationParts.get(currIndex);
            declaration.append(token);
            // if the next token is a ; don't put a space
            if (currIndex + 1 < functionDeclarationParts.size()) {
                // peek ahead just to format nicely
                String nextToken = functionDeclarationParts.get(currIndex + 1);
                if (!nextToken.equals(";")) {
                    declaration.append(" ");
                }
            }
            currIndex++;
        }
        return declaration.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    @Override
    public String toString() {
        return "CREATE " + kind + " " + formatDeclaration();
    }

    public CreateFunctionalStatement withFunctionDeclarationParts(List<String> functionDeclarationParts) {
        this.setFunctionDeclarationParts(functionDeclarationParts);
        return this;
    }

    public CreateFunctionalStatement addFunctionDeclarationParts(String... functionDeclarationParts) {
        List<String> collection = Optional.ofNullable(getFunctionDeclarationParts()).orElseGet(ArrayList::new);
        Collections.addAll(collection, functionDeclarationParts);
        return this.withFunctionDeclarationParts(collection);
    }

    public CreateFunctionalStatement addFunctionDeclarationParts(Collection<String> functionDeclarationParts) {
        List<String> collection = Optional.ofNullable(getFunctionDeclarationParts()).orElseGet(ArrayList::new);
        collection.addAll(functionDeclarationParts);
        return this.withFunctionDeclarationParts(collection);
    }
}
