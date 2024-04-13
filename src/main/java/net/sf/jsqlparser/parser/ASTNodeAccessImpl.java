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

import java.util.Set;
import java.util.TreeSet;

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
        // don't add spaces around the following punctuation
        final Set<String> punctuation = new TreeSet<>(Set.of(".", "[", "]"));

        SimpleNode simpleNode = getASTNode();
        if (simpleNode != null) {
            Token token = simpleNode.jjtGetFirstToken();
            Token lastToken = simpleNode.jjtGetLastToken();
            Token prevToken = null;
            while (token.next != null && token.absoluteEnd <= lastToken.absoluteEnd) {
                if (!punctuation.contains(token.image)
                        && (prevToken == null || !punctuation.contains(prevToken.image))) {
                    builder.append(" ");
                }
                builder.append(token.image);
                prevToken = token;
                token = token.next;
            }
        }
        return builder;
    }

}
