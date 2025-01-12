package net.sf.jsqlparser.statement.piped;

import net.sf.jsqlparser.statement.select.SelectItem;

public class WindowPipeOperator extends SelectPipeOperator {
    public WindowPipeOperator(SelectItem<?> selectItem) {
        super("WINDOW", selectItem);
    }
}
