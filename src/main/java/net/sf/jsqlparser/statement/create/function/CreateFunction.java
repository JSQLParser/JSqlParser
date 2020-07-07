/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.function;

import java.util.Collection;
import java.util.List;

import net.sf.jsqlparser.statement.CreateFunctionalStatement;

/**
 * A {@code CREATE PROCEDURE} statement
 */
public class CreateFunction extends CreateFunctionalStatement {

    public CreateFunction() {
        super("FUNCTION");
    }

    public CreateFunction(List<String> functionDeclarationParts) {
        super("FUNCTION", functionDeclarationParts);
    }

    @Override()
    public CreateFunction withFunctionDeclarationParts(List<String> functionDeclarationParts) {
        return (CreateFunction) super.withFunctionDeclarationParts(functionDeclarationParts);
    }

    @Override()
    public CreateFunction addFunctionDeclarationParts(String... functionDeclarationParts) {
        return (CreateFunction) super.addFunctionDeclarationParts(functionDeclarationParts);
    }

    @Override()
    public CreateFunction addFunctionDeclarationParts(Collection<String> functionDeclarationParts) {
        return (CreateFunction) super.addFunctionDeclarationParts(functionDeclarationParts);
    }

}
