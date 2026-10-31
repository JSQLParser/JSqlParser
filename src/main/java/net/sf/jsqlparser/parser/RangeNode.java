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
 * case it stands in for the tree node, answering its parent, children and id, with the wider range.
 */
final class RangeNode extends Node {
    private final Node treeNode;

    RangeNode(Token first, Token last, Node treeNode) {
        super(CCJSqlParserTreeConstants.JJTEXPRESSION);
        this.treeNode = treeNode;
        jjtSetFirstToken(first);
        jjtSetLastToken(last);
    }

    @Override
    public Node jjtGetParent() {
        return treeNode == null ? null : treeNode.jjtGetParent();
    }

    @Override
    public Node[] jjtGetChildren() {
        return treeNode == null ? super.jjtGetChildren() : treeNode.jjtGetChildren();
    }

    @Override
    public Node jjtGetChild(int i) {
        return treeNode == null ? super.jjtGetChild(i) : treeNode.jjtGetChild(i);
    }

    @Override
    public int jjtGetNumChildren() {
        return treeNode == null ? super.jjtGetNumChildren() : treeNode.jjtGetNumChildren();
    }

    @Override
    public Object childrenAccept(CCJSqlParserVisitor visitor, Object data) {
        return treeNode == null ? super.childrenAccept(visitor, data)
                : treeNode.childrenAccept(visitor, data);
    }

    @Override
    public int getId() {
        return treeNode == null ? super.getId() : treeNode.getId();
    }

    @Override
    public String toString() {
        return treeNode == null ? super.toString() : treeNode.toString();
    }

    @Override
    public void dump(String prefix) {
        if (treeNode == null) {
            super.dump(prefix);
        } else {
            treeNode.dump(prefix);
        }
    }
}
