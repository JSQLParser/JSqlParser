/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.deparser;

import net.sf.jsqlparser.statement.create.view.AlterView;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectVisitor;

public class AlterViewDeParser {

    protected StringBuilder buffer;
    private SelectVisitor selectVisitor;

    public AlterViewDeParser(StringBuilder buffer) {
        SelectDeParser selectDeParser = new SelectDeParser();
        selectDeParser.setBuffer(buffer);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
        selectDeParser.setExpressionVisitor(expressionDeParser);
        selectVisitor = selectDeParser;
        this.buffer = buffer;
    }

    public AlterViewDeParser(StringBuilder buffer, SelectVisitor selectVisitor) {
        this.buffer = buffer;
        this.selectVisitor = selectVisitor;
    }

    public void deParse(AlterView alterView) {
        if(alterView.isUseReplace()){
            buffer.append("REPLACE ");
        }else{
            buffer.append("ALTER ");
        }
        buffer.append("VIEW ").append(alterView.getView().getFullyQualifiedName());
        if (alterView.getColumnNames() != null) {
            buffer.append(PlainSelect.getStringList(alterView.getColumnNames(), true, true));
        }
        buffer.append(" AS ");

        alterView.getSelectBody().accept(selectVisitor);
    }

    public StringBuilder getBuffer() {
        return buffer;
    }

    public void setBuffer(StringBuilder buffer) {
        this.buffer = buffer;
    }
}
