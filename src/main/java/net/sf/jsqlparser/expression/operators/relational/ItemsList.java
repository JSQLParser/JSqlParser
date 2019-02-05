/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.expression.operators.relational;

/**
 * Values of an "INSERT" statement (for example a SELECT or a list of expressions)
 */
public interface ItemsList {

    void accept(ItemsListVisitor itemsListVisitor);
}
