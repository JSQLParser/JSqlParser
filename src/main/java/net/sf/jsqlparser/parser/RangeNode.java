/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.parser;

/**
 * A node outside the parse tree that records the tokens an object spans. It serves an object no
 * grammar rule opens a tree node for, or one whose text runs past its tree node. In that second
 * case it answers the tree node's parent, which the object's {@code getParent()} then reports.
 */
public final class RangeNode extends Node {
    private final Node treeNode;

    public RangeNode(Token first, Token last, Node treeNode) {
        super(CCJSqlParserTreeConstants.JJTEXPRESSION);
        this.treeNode = treeNode;
        jjtSetFirstToken(first);
        jjtSetLastToken(last);
    }

    @Override
    public Node jjtGetParent() {
        return treeNode == null ? null : treeNode.jjtGetParent();
    }
}
