package net.sf.jsqlparser.util;

import java.util.LinkedList;
import java.util.List;
import net.sf.jsqlparser.statement.select.AllColumns;
import net.sf.jsqlparser.statement.select.AllTableColumns;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SelectItemVisitor;
import net.sf.jsqlparser.statement.select.SelectVisitor;
import net.sf.jsqlparser.statement.select.SetOperationList;

/**
 * Add aliases to every column and expression selected by a
 * select - statement. Existing aliases are recognized and
 * preserved. This class standard uses a prefix of A and a counter
 * to generate new aliases (e.g. A1, A5, ...). This behaviour
 * can be altered.
 * 
 * @author tw
 */
public class AddAliasesVisitor implements SelectVisitor, SelectItemVisitor {

	private List<String> aliases = new LinkedList<String>();
	private boolean firstRun = true;
	private int counter = 0;
	private String prefix = "A";

	@Override
	public void visit(PlainSelect plainSelect) {
		firstRun = true;
		counter = 0;
		aliases.clear();
		for (SelectItem item : plainSelect.getSelectItems()) {
			item.accept(this);
		}
		firstRun = false;
		for (SelectItem item : plainSelect.getSelectItems()) {
			item.accept(this);
		}
	}

	@Override
	public void visit(SetOperationList setOpList) {
		for (PlainSelect select : setOpList.getPlainSelects()) {
			select.accept(this);
		}
	}

	@Override
	public void visit(AllColumns allColumns) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void visit(AllTableColumns allTableColumns) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void visit(SelectExpressionItem selectExpressionItem) {
		if (firstRun) {
			if (selectExpressionItem.getAlias() != null) {
				aliases.add(selectExpressionItem.getAlias().toUpperCase());
			}
		} else {
			if (selectExpressionItem.getAlias() == null) {

				while (true) {
					String alias = getNextAlias().toUpperCase();
					if (!aliases.contains(alias)) {
						aliases.add(alias);
						selectExpressionItem.setAlias(alias);
						break;
					}
				}
			}
		}
	}

	/**
	 * Calculate next alias name to use.
	 * @return 
	 */
	protected String getNextAlias() {
		counter++;
		return prefix + counter;
	}

	/**
	 * Set alias prefix.
	 *
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
}
