/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2015 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

/**
 *
 * @author toben
 */
public class ASTNodeAccessImpl implements ASTNodeAccess {

    private SimpleNode node;

    @Override
    public SimpleNode getASTNode() {
        return node;
    }

    @Override
    public void setASTNode(SimpleNode node) {
        this.node = node;
    }

}
