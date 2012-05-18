package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

/**
 * Einzelne Set-Operation. Diese geben nur noch den einzelnen Operationsnamen aus.
 * Durch die SetOperationList wird alles zusammengehalten.
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
