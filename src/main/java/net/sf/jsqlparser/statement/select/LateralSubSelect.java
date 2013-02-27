package net.sf.jsqlparser.statement.select;


/**
 * A lateral subselect followed by an alias. 
 * @author Tobias Warneke
 */
public class LateralSubSelect implements FromItem {
	private SubSelect subSelect;
	private String alias;

	public void setSubSelect(SubSelect subSelect) {
		this.subSelect = subSelect;
	}

	public SubSelect getSubSelect() {
		return subSelect;
	}
	
	@Override
	public void accept(FromItemVisitor fromItemVisitor) {
		fromItemVisitor.visit(this);
	}

	@Override
	public String getAlias() {
		return alias;
	}

	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	@Override
	public String toString() {
		return "LATERAL" + subSelect.toString() + ((alias != null) ? " AS " + alias : "");
	}
}
