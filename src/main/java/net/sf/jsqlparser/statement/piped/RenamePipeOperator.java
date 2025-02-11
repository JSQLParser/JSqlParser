package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

public class RenamePipeOperator extends SelectPipeOperator {
    public RenamePipeOperator(SelectItem<?> selectItem) {
        super("RENAME", selectItem, null);
    }
}
