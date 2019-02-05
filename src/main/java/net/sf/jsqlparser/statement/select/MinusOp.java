/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2013 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

/**
 *
 * @author tw
 */
public class MinusOp extends SetOperation {

    public MinusOp() {
        super(SetOperationType.MINUS);
    }
}
