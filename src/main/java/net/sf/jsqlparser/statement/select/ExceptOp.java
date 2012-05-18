package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

/**
 * 
 * @author tw
 */
public class ExceptOp extends SetOperation {

	public ExceptOp() {
		super(SetOperationType.EXCEPT);
	}
}
