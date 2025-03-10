/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.parser.ASTNodeAccessImpl;

public abstract class PipeOperator extends ASTNodeAccessImpl {
    public abstract <T, S> T accept(PipeOperatorVisitor<T, S> visitor, S context);
}
