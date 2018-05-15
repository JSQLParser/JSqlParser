package net.sf.jsqlparser.expression.operators.relational;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;

public class TeradataTrimExpression implements Expression
{
	public enum Direction
	{
		LEADING,
		TRAILING,
		BOTH
	}

	Direction direction;
	Expression removalCharExpression = null;
	Expression targetStringExpression = null;
	Expression collationNameExpression = null;


	@Override
	public void accept(ExpressionVisitor expressionVisitor) {
		expressionVisitor.visit(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TRIM(");
		if(direction != null)
		{
			sb.append(direction + " ");
		}
		if(removalCharExpression != null)
		{
			sb.append(removalCharExpression + " ");
		}
		if(direction != null || removalCharExpression != null)
		{
			sb.append("FROM ");
		}
		sb.append(targetStringExpression);
		if(collationNameExpression != null)
		{
			sb.append(" COLLATE " + collationNameExpression);
		}
		sb.append(")");
		return sb.toString();
	}

	public Expression getRemovalCharExpression()
	{
		return removalCharExpression;
	}

	public void setRemovalCharExpression(Expression removalCharExpression)
	{
		this.removalCharExpression = removalCharExpression;
	}

	public Expression getTargetStringExpression()
	{
		return targetStringExpression;
	}

	public void setTargetStringExpression(Expression targetStringExpression)
	{
		this.targetStringExpression = targetStringExpression;
	}

	public Expression getCollationNameExpression()
	{
		return collationNameExpression;
	}

	public void setCollationNameExpression(Expression collationNameExpression)
	{
		this.collationNameExpression = collationNameExpression;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public void setDirection(Direction direction)
	{
		this.direction = direction;
	}
}
