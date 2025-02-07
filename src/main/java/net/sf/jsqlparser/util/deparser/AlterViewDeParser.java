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

public class AlterViewDeParser extends AbstractDeParser<AlterView> {

    private final SelectVisitor<StringBuilder> selectVisitor;

    public AlterViewDeParser(StringBuilder buffer) {
        super(buffer);
        SelectDeParser selectDeParser = new SelectDeParser(buffer);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
        selectDeParser.setExpressionVisitor(expressionDeParser);
        selectVisitor = selectDeParser;
    }

    public AlterViewDeParser(StringBuilder buffer, SelectVisitor<StringBuilder> selectVisitor) {
        super(buffer);
        this.selectVisitor = selectVisitor;
    }

    @Override
    public void deParse(AlterView alterView) {
        if (alterView.isUseReplace()) {
            builder.append("REPLACE ");
        } else {
            builder.append("ALTER ");
        }
        builder.append("VIEW ").append(alterView.getView().getFullyQualifiedName());
        if (alterView.getColumnNames() != null) {
            builder.append(PlainSelect.getStringList(alterView.getColumnNames(), true, true));
        }
        builder.append(" AS ");

        alterView.getSelect().accept(selectVisitor, null);
    }

}
