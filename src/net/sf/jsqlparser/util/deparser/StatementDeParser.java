package net.sf.jsqlparser.util.deparser;

import java.util.Iterator;

import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.WithItem;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class StatementDeParser implements StatementVisitor {
	protected StringBuffer buffer;

	public StatementDeParser(StringBuffer buffer) {
		this.buffer = buffer;
	}

	public void visit(CreateTable createTable) {
		CreateTableDeParser createTableDeParser = new CreateTableDeParser(buffer);
		createTableDeParser.deParse(createTable);
	}

	public void visit(Delete delete) {
		SelectDeParser selectDeParser = new SelectDeParser();
		selectDeParser.setBuffer(buffer);
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
		selectDeParser.setExpressionVisitor(expressionDeParser);
		DeleteDeParser deleteDeParser = new DeleteDeParser(expressionDeParser, buffer);
		deleteDeParser.deParse(delete);
	}

	public void visit(Drop drop) {
		// TODO Auto-generated method stub

	}

	public void visit(Insert insert) {
		SelectDeParser selectDeParser = new SelectDeParser();
		selectDeParser.setBuffer(buffer);
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
		selectDeParser.setExpressionVisitor(expressionDeParser);
		InsertDeParser insertDeParser = new InsertDeParser(expressionDeParser, selectDeParser, buffer);
		insertDeParser.deParse(insert);

	}

	public void visit(Replace replace) {
		SelectDeParser selectDeParser = new SelectDeParser();
		selectDeParser.setBuffer(buffer);
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
		selectDeParser.setExpressionVisitor(expressionDeParser);
		ReplaceDeParser replaceDeParser = new ReplaceDeParser(expressionDeParser, selectDeParser, buffer);
		replaceDeParser.deParse(replace);
	}

	public void visit(Select select) {
		SelectDeParser selectDeParser = new SelectDeParser();
		selectDeParser.setBuffer(buffer);
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
		selectDeParser.setExpressionVisitor(expressionDeParser);
		if (select.getWithItemsList() != null && !select.getWithItemsList().isEmpty()) {
			buffer.append("WITH ");
			for (Iterator iter = select.getWithItemsList().iterator(); iter.hasNext();) {
				WithItem withItem = (WithItem)iter.next();
				buffer.append(withItem);
				if (iter.hasNext())
					buffer.append(",");
				buffer.append(" ");
			}
		}
		select.getSelectBody().accept(selectDeParser);

	}

	public void visit(Truncate truncate) {
		// TODO Auto-generated method stub

	}

	public void visit(Update update) {
		SelectDeParser selectDeParser = new SelectDeParser();
		selectDeParser.setBuffer(buffer);
		ExpressionDeParser expressionDeParser = new ExpressionDeParser(selectDeParser, buffer);
		UpdateDeParser updateDeParser = new UpdateDeParser(expressionDeParser, buffer);
		selectDeParser.setExpressionVisitor(expressionDeParser);
		updateDeParser.deParse(update);

	}

	public StringBuffer getBuffer() {
		return buffer;
	}

	public void setBuffer(StringBuffer buffer) {
		this.buffer = buffer;
	}

}
