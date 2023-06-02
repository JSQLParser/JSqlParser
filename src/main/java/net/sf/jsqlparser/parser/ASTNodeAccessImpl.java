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

public class ASTNodeAccessImpl implements ASTNodeAccess {

    private transient SimpleNode node;

    @Override
    public SimpleNode getASTNode() {
        return node;
    }

    @Override
    public void setASTNode(SimpleNode node) {
        this.node = node;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        SimpleNode simpleNode = getASTNode();
        Token token = simpleNode.jjtGetFirstToken();
        Token lastToken = simpleNode.jjtGetLastToken();
        while (token.next != null && token.absoluteEnd <= lastToken.absoluteEnd) {
            builder.append(" ").append(token.image);
            token = token.next;
        }
        return builder;
    }

}
