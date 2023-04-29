package net.sf.jsqlparser.statement.select;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Function;

import java.io.Serializable;

public class LateralView implements Serializable {
    private boolean isUsingOuter = false;
    private Function generatorFunction;
    private Alias tableAlias = null;
    private Alias columnAlias;

    public LateralView(boolean useOuter, Function generatorFunction, Alias tableAlias,
            Alias columnAlias) {
        this.isUsingOuter = useOuter;
        this.generatorFunction = generatorFunction;
        this.tableAlias = tableAlias;
        this.columnAlias = columnAlias;
    }

    public boolean isUsingOuter() {
        return isUsingOuter;
    }

    public void setUsingOuter(boolean useOuter) {
        this.isUsingOuter = useOuter;
    }

    public LateralView withOuter(boolean useOuter) {
        this.setUsingOuter(useOuter);
        return this;
    }

    public Function getGeneratorFunction() {
        return generatorFunction;
    }

    public void setGeneratorFunction(Function generatorFunction) {
        this.generatorFunction = generatorFunction;
    }

    public LateralView withGeneratorFunction(Function generatorFunction) {
        this.setGeneratorFunction(generatorFunction);
        return this;
    }

    public Alias getTableAlias() {
        return tableAlias;
    }

    public void setTableAlias(Alias tableAlias) {
        this.tableAlias = tableAlias;
    }

    public LateralView withTableAlias(Alias tableAlias) {
        // "AS" is not allowed here, so overwrite hard
        this.setTableAlias( tableAlias !=null ? tableAlias.withUseAs(false) : null);
        return this;
    }

    public Alias getColumnAlias() {
        return columnAlias;
    }

    public void setColumnAlias(Alias columnAlias) {
        this.columnAlias = columnAlias;
    }

    public LateralView withColumnAlias(Alias columnAlias) {
        //"AS" is required here, so overwrite
        this.setColumnAlias(columnAlias.withUseAs(true));
        return this;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("LATERAL VIEW");

        if (isUsingOuter) {
            builder.append(" OUTER");
        }

        builder.append(" ").append(generatorFunction);
        if (tableAlias != null) {
            builder.append(" ").append(tableAlias);
        }

        builder.append(" ").append(columnAlias);

        return builder;
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
