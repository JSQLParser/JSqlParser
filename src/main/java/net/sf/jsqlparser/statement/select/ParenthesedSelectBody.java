package net.sf.jsqlparser.statement.select;

public class ParenthesedSelectBody extends SelectBody {
    private SelectBody selectBody;

    public SelectBody getSelectBody() {
        return selectBody;
    }

    public void setSelectBody(SelectBody selectBody) {
        this.selectBody = selectBody;
    }

    public ParenthesedSelectBody withSelectBody(SelectBody selectBody) {
        setSelectBody(selectBody);
        return this;
    }

    @Override
    public void accept(SelectVisitor selectVisitor) {
        selectVisitor.visit(this);
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("(").append(selectBody).append(")");

        // limit, offset, fetch, isolation
        super.appendTo(builder);

        return builder;
    }

    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
