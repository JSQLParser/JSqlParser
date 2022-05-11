/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectItem;

import java.util.List;
import java.util.Objects;

/**
 * T-SQL Output Clause
 *
 * @see <a href="https://docs.microsoft.com/en-us/sql/t-sql/queries/output-clause-transact-sql?view=sql-server-ver15">OUTPUT Clause (Transact-SQL)</a>
 *
 * <pre>
 * &lt;OUTPUT_CLAUSE&gt; ::=
 * {
 *     [ OUTPUT &lt;dml_select_list&gt; INTO { @table_variable | output_table } [ ( column_list ) ] ]
 *     [ OUTPUT &lt;dml_select_list&gt; ]
 * }
 * &lt;dml_select_list&gt; ::=
 * { &lt;column_name&gt; | scalar_expression } [ [AS] column_alias_identifier ]
 *     [ ,...n ]
 *
 * &lt;column_name&gt; ::=
 * { DELETED | INSERTED | from_table_name } . { * | column_name }
 *     | $action
 * </pre>
 */
public class OutputClause {
    List<SelectItem> selectItemList;
    UserVariable tableVariable;
    Table outputTable;
    List<String> columnList;

    public OutputClause(List<SelectItem> selectItemList, UserVariable tableVariable, Table outputTable, List<String> columnList) {
        this.selectItemList = Objects.requireNonNull(selectItemList, "The Select List of the Output Clause must not be null.");
        this.tableVariable = tableVariable;
        this.outputTable = outputTable;
        this.columnList = columnList;
    }

    public List<SelectItem> getSelectItemList() {
        return selectItemList;
    }

    public void setSelectItemList(List<SelectItem> selectItemList) {
        this.selectItemList = selectItemList;
    }

    public UserVariable getTableVariable() {
        return tableVariable;
    }

    public void setTableVariable(UserVariable tableVariable) {
        this.tableVariable = tableVariable;
    }

    public Table getOutputTable() {
        return outputTable;
    }

    public void setOutputTable(Table outputTable) {
        this.outputTable = outputTable;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<String> columnList) {
        this.columnList = columnList;
    }

    public StringBuilder appendTo(StringBuilder builder) {
        builder.append(" OUTPUT ");
        PlainSelect.appendStringListTo(builder, selectItemList, true, false);

        if (tableVariable != null) {
            builder.append(" INTO ").append(tableVariable);
        } else if (outputTable != null) {
            builder.append(" INTO ").append(outputTable);
        }

        PlainSelect.appendStringListTo(builder, columnList, true, false);

        return builder.append(" ");
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }
}
