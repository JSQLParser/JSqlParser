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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ASTNodeAccessImpl implements ASTNodeAccess {

    private transient Node node;

    @Override
    public Node getASTNode() {
        return node;
    }

    @Override
    public void setASTNode(Node node) {
        this.node = node;
    }

    /**
     * The comments preceding this node's first token, in source order, with the original token
     * images. Line comments do not include their line terminator; callers must add a line break
     * when prepending them to SQL. Empty for nodes built programmatically, which carry no AST.
     */
    public List<String> getLeadingComments() {
        Node node = getASTNode();
        if (node == null) {
            return new ArrayList<>();
        }
        List<String> comments = new ArrayList<>();
        for (Token specialToken =
                node.jjtGetFirstToken().specialToken; specialToken != null; specialToken =
                        specialToken.specialToken) {
            comments.add(specialToken.image);
        }
        Collections.reverse(comments);
        return comments;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        // don't add spaces around the following punctuation
        final Set<String> punctuation = new TreeSet<>(Set.of(".", "[", "]"));

        Node Node = getASTNode();
        if (Node != null) {
            Token token = Node.jjtGetFirstToken();
            Token lastToken = Node.jjtGetLastToken();
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

    public ASTNodeAccess getParent() {
        Node parent = (Node) node.jjtGetParent();
        while (parent.jjtGetValue() == null) {
            parent = (Node) parent.jjtGetParent();
        }

        return ASTNodeAccess.class.cast(parent.jjtGetValue());
    }

    public <T extends ASTNodeAccess> T getParent(Class<T> clazz) {
        Node parent = (Node) node.jjtGetParent();
        while (parent.jjtGetValue() == null || !clazz.isInstance(parent.jjtGetValue())) {
            parent = (Node) parent.jjtGetParent();
        }

        return clazz.cast(parent.jjtGetValue());
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
