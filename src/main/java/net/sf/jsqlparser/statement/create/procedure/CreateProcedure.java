/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2020 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.procedure;

import java.util.Collection;
import java.util.List;
import net.sf.jsqlparser.statement.CreateFunctionalStatement;

/**
 * A {@code CREATE PROCEDURE} statement
 */
public class CreateProcedure extends CreateFunctionalStatement {

    public CreateProcedure() {
        super("PROCEDURE");
    }

    public CreateProcedure(List<String> functionDeclarationParts) {
        super("PROCEDURE", functionDeclarationParts);
    }

    @Override
    public CreateProcedure withFunctionDeclarationParts(List<String> functionDeclarationParts) {
        return (CreateProcedure) super.withFunctionDeclarationParts(functionDeclarationParts);
    }

    @Override
    public CreateProcedure addFunctionDeclarationParts(String... functionDeclarationParts) {
        return (CreateProcedure) super.addFunctionDeclarationParts(functionDeclarationParts);
    }

    @Override
    public CreateProcedure addFunctionDeclarationParts(Collection<String> functionDeclarationParts) {
        return (CreateProcedure) super.addFunctionDeclarationParts(functionDeclarationParts);
    }

}
