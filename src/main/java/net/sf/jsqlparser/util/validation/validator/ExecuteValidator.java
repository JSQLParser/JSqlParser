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
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.execute.Execute.ExecType;
import net.sf.jsqlparser.util.validation.ValidationCapability;
import net.sf.jsqlparser.util.validation.metadata.NamedObject;

/**
 * @author gitmotte
 */
public class ExecuteValidator extends AbstractValidator<Execute> {


    @Override
    public void validate(Execute execute) {
        for (ValidationCapability c : getCapabilities()) {
            validateFeature(c, Feature.execute);
            validateFeature(c, ExecType.EXECUTE.equals(execute.getExecType()), Feature.executeExecute);
            validateFeature(c, ExecType.EXEC.equals(execute.getExecType()), Feature.executeExec);
            validateFeature(c, ExecType.CALL.equals(execute.getExecType()), Feature.executeCall);
            validateName(NamedObject.procedure, execute.getName());
        }

        validateOptionalItemsList(execute.getExprList());
    }

}
