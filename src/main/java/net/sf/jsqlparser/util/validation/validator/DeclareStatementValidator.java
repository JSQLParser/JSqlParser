/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.validation.validator;

import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.DeclareStatement;
import net.sf.jsqlparser.util.validation.ValidationCapability;

/**
 * @author gitmotte
 */
public class DeclareStatementValidator extends AbstractValidator<DeclareStatement> {

    @Override
    public void validate(DeclareStatement declare) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.declare);
        }
        validateOptionalExpression(declare.getUserVariable());
        //
        //
        //        if (declare.getDeclareType() == DeclareType.TABLE) {
        //            buffer.append(" TABLE (");
        //            for (int i = 0; i < declare.getColumnDefinitions().size(); i++) {
        //                if (i > 0) {
        //                    buffer.append(", ");
        //                }
        //                buffer.append(declare.getColumnDefinitions().get(i).toString());
        //            }
        //            buffer.append(")");
        //        } else {
        //            if (declare.getTypeDefinitions() != null) {
        //                for (int i = 0; i < declare.getTypeDefinitions().size(); i++) {
        //                    if (i > 0) {
        //                        buffer.append(", ");
        //                    }
        //                    DeclareStatement.TypeDefExpr type = declare.getTypeDefinitions().get(i);
        //                    if (type.userVariable != null) {
        //                        type.userVariable.accept(expressionVisitor);
        //                        buffer.append(" ");
        //                    }
        //                    buffer.append(type.colDataType.toString());
        //                    if (type.defaultExpr != null) {
        //                        buffer.append(" = ");
        //                        type.defaultExpr.accept(expressionVisitor);
        //                    }
        //                }
        //            }
        //        }
    }

}