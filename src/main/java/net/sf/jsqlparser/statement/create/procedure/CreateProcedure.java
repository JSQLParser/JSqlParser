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

import net.sf.jsqlparser.statement.CreateFunctionalStatement;

import java.util.List;

/**
 * A {@code CREATE PROCEDURE} statement
 */
public class CreateProcedure extends CreateFunctionalStatement {

    public CreateProcedure(List<String> functionDeclarationParts) {
        super("PROCEDURE", functionDeclarationParts);
    }

}
