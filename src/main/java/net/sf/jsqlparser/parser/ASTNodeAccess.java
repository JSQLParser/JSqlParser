/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

import java.io.Serializable;

public interface ASTNodeAccess extends Serializable {

    Node getASTNode();

    void setASTNode(Node node);
}
