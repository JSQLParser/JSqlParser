package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

/**
 * Single Set-Operation (name). Placeholder for one specific set operation, e.g. 
 * union, intersect.
 * @author tw
 */
public abstract class SetOperation {
	private SetOperationType type;

	public SetOperation(SetOperationType type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type.name();
	}
}
