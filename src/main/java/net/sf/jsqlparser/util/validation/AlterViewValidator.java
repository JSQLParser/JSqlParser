/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation;

import net.sf.jsqlparser.statement.create.view.AlterView;

public class AlterViewValidator extends AbstractValidator<AlterView> {


    @Override
    public void validate(AlterView alterView) {
        //        if (alterView.isUseReplace()) {
        //            buffer.append("REPLACE ");
        //        } else {
        //            buffer.append("ALTER ");
        //        }
        //        buffer.append("VIEW ").append(alterView.getView().getFullyQualifiedName());
        //        if (alterView.getColumnNames() != null) {
        //            buffer.append(PlainSelect.getStringList(alterView.getColumnNames(), true, true));
        //        }
        //        buffer.append(" AS ");
        //
        //        alterView.getSelectBody().accept(selectVisitor);
    }

}
