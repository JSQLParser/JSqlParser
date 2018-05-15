package net.sf.jsqlparser.expression;

public class TeradataCastExpression extends CastExpression
{
	private Expression formatExpression = null;

	@Override
	public String toString() {
		if (isUseCastKeyword()) {
			return "CAST(" + getLeftExpression() + " AS " + getType().toString() + (formatExpression != null ? (" FORMAT " + formatExpression) : "") + ")";
		} else {
			return super.toString();
		}
	}

	public Expression getFormatExpression()
	{
		return formatExpression;
	}

	public void setFormatExpression(Expression formatExpression)
	{
		this.formatExpression = formatExpression;
	}

}
