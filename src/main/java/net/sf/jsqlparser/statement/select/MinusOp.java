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
