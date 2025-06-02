/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2025 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

public class ExtendPipeOperator extends SelectPipeOperator {
    public ExtendPipeOperator(SelectItem<?> selectItem) {
        super("EXTEND", selectItem, null);
    }
}
