package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

/**
 *
 * @author tw
 */
public class IntersectOp extends SetOperation {

	public IntersectOp() {
		super(SetOperationType.INTERSECT);
	}
	
}
