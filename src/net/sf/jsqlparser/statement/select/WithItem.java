package net.sf.jsqlparser.statement.select;

import java.util.List;
import net.sf.jsqlparser.statement.select.SelectBody;
/**
 * One of the parts of a "WITH" clause of a "SELECT" statement  
 */
public class WithItem {
	private String name;
	private	List withItemList;
	private SelectBody selectBody;

	/**
	 * The name of this WITH item (for example, "myWITH" in "WITH myWITH AS (SELECT A,B,C))"
	 * @return the name of this WITH
	 */
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * The {@link SelectBody} of this WITH item is the part after the "AS" keyword
	 * @return {@link SelectBody} of this WITH item
	 */
	public SelectBody getSelectBody() {
		return selectBody;
	}
	public void setSelectBody(SelectBody selectBody) {
		this.selectBody = selectBody;
	}
	
	/**
	 * The {@link SelectItem}s in this WITH (for example the A,B,C in "WITH mywith (A,B,C) AS ...")
	 * @return a list of {@link SelectItem}s
	 */
	public List getWithItemList() {
		return withItemList;
	}
	public void setWithItemList(List withItemList) {
		this.withItemList = withItemList;
	}
	
	public String toString() {
		return name + ((withItemList != null)?" " + PlainSelect.getStringList(withItemList, true, true):"") + " AS (" + selectBody + ")";
	}
	
	
}
