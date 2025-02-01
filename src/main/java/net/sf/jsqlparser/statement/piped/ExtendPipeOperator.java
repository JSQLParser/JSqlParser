package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

public class ExtendPipeOperator extends SelectPipeOperator {
    public ExtendPipeOperator(SelectItem<?> selectItem) {
        super("EXTEND", selectItem);
    }
}
