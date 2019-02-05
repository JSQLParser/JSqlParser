/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.create.view.TemporaryOption;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.WithItem;

/**
 * A class to de-parse (that is, tranform from JSqlParser hierarchy into a string) a
 * {@link net.sf.jsqlparser.statement.create.view.CreateView}
 */
public class CreateViewDeParser {

    protected StringBuilder buffer;
    private final SelectVisitor selectVisitor;

    /**
     * @param buffer the buffer that will be filled with the select
     */
    public CreateViewDeParser(StringBuilder buffer) {
        SelectDeParser selectDeParser = new SelectDeParser();
        selectDeParser.setBuffer(buffer);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
        selectDeParser.setExpressionVisitor(expressionDeParser);
        selectVisitor = selectDeParser;
        this.buffer = buffer;
    }

    public CreateViewDeParser(StringBuilder buffer, SelectVisitor selectVisitor) {
        this.buffer = buffer;
        this.selectVisitor = selectVisitor;
    }

    public void deParse(CreateView createView) {
        buffer.append("CREATE ");
        if (createView.isOrReplace()) {
            buffer.append("OR REPLACE ");
        }
        switch (createView.getForce()) {
            case FORCE:
                buffer.append("FORCE ");
                break;
            case NO_FORCE:
                buffer.append("NO FORCE ");
                break;
        }
        if (createView.getTemporary() != TemporaryOption.NONE) {
            buffer.append(createView.getTemporary().name()).append(" ");
        }
        if (createView.isMaterialized()) {
            buffer.append("MATERIALIZED ");
        }
        buffer.append("VIEW ").append(createView.getView().getFullyQualifiedName());
        if (createView.getColumnNames() != null) {
            buffer.append(PlainSelect.getStringList(createView.getColumnNames(), true, true));
        }
        buffer.append(" AS ");

        Select select = createView.getSelect();
        if (select.getWithItemsList() != null) {
            buffer.append("WITH ");
            boolean first = true;
            for (WithItem item : select.getWithItemsList()) {
                if (!first) {
                    buffer.append(", ");
                } else {
                    first = false;
                }

                item.accept(selectVisitor);
            }
            buffer.append(" ");
        }
        createView.getSelect().getSelectBody().accept(selectVisitor);
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }
}
