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

import net.sf.jsqlparser.statement.create.view.AutoRefreshOption;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.create.view.TemporaryOption;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectVisitor;

public class CreateViewDeParser extends AbstractDeParser<CreateView> {

    private final SelectVisitor selectVisitor;

    public CreateViewDeParser(StringBuilder buffer) {
        super(buffer);
        SelectDeParser selectDeParser = new SelectDeParser();
        selectDeParser.setBuffer(buffer);
        ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
        selectDeParser.setExpressionVisitor(expressionDeParser);
        selectVisitor = selectDeParser;
    }

    public CreateViewDeParser(StringBuilder buffer, SelectVisitor selectVisitor) {
        super(buffer);
        this.selectVisitor = selectVisitor;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity"})
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
            case NONE:
                break;
            default:
                // nothing
        }
        if (createView.getTemporary() != TemporaryOption.NONE) {
            buffer.append(createView.getTemporary().name()).append(" ");
        }
        if (createView.isMaterialized()) {
            buffer.append("MATERIALIZED ");
        }
        buffer.append("VIEW ").append(createView.getView().getFullyQualifiedName());
        if (createView.isIfNotExists()) {
            buffer.append(" IF NOT EXISTS");
        }
        if (createView.getAutoRefresh() != AutoRefreshOption.NONE) {
            buffer.append(" AUTO REFRESH ").append(createView.getAutoRefresh().name());
        }
        if (createView.getColumnNames() != null) {
            buffer.append(PlainSelect.getStringList(createView.getColumnNames(), true, true));
        }
        buffer.append(" AS ");

        Select select = createView.getSelect();
        select.accept(selectVisitor);
        if (createView.isWithReadOnly()) {
            buffer.append(" WITH READ ONLY");
        }
    }

}
