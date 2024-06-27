/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression;

import net.sf.jsqlparser.Model;
import net.sf.jsqlparser.parser.ASTNodeAccess;

public interface Expression extends ASTNodeAccess, Model {

    <T, S> T accept(ExpressionVisitor<T> expressionVisitor, S context);

    default <T> void accept(ExpressionVisitor<T> expressionVisitor) {
        this.accept(expressionVisitor, null);
    }

    ;


}
