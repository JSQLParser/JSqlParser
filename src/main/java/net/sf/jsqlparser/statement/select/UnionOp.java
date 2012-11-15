package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.statement.select.SetOperationList.SetOperationType;

/**
 *
 * @author tw
 */
public class UnionOp extends SetOperation {
	private boolean distinct;
	private boolean all;
	
	public UnionOp() {
		super(SetOperationType.UNION);
	}
	
	public boolean isAll() {
		return all;
	}

	public void setAll(boolean all) {
		this.all = all;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
	
	
	@Override
	public String toString() {
		String allDistinct = "";
		if (isAll()) {
			allDistinct = " ALL";
		} else if (isDistinct()) {
			allDistinct = " DISTINCT";
		}	
		return super.toString() + allDistinct;
	}
}
