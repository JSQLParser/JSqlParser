/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2026 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.operator;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.parser.ASTNodeAccessImpl;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.StatementVisitor;
import net.sf.jsqlparser.statement.create.table.ColDataType;

/**
 * PostgreSQL {@code CREATE OPERATOR CLASS name [DEFAULT] FOR TYPE type USING index_method
 * [FAMILY family] AS member [, ...]}. Names retain SQL identifier quoting.
 */
public class CreateOperatorClass extends ASTNodeAccessImpl implements Statement {
    private String name;
    private boolean defaultClass;
    private ColDataType dataType;
    private String accessMethod;
    private String family;
    private List<OperatorClassItem> items = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Whether the class is the {@code DEFAULT} operator class of its type.
     *
     * @return {@code true} when DEFAULT is written
     */
    public boolean isDefaultClass() {
        return defaultClass;
    }

    public void setDefaultClass(boolean defaultClass) {
        this.defaultClass = defaultClass;
    }

    public ColDataType getDataType() {
        return dataType;
    }

    public void setDataType(ColDataType dataType) {
        this.dataType = dataType;
    }

    /**
     * The index access method after {@code USING}, such as {@code btree}.
     *
     * @return the access method
     */
    public String getAccessMethod() {
        return accessMethod;
    }

    public void setAccessMethod(String accessMethod) {
        this.accessMethod = accessMethod;
    }

    /**
     * The operator family the class is added to.
     *
     * @return the family name, or {@code null} without a FAMILY clause
     */
    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public List<OperatorClassItem> getItems() {
        return items;
    }

    public void setItems(List<OperatorClassItem> items) {
        this.items = items;
    }

    @Override
    public StringBuilder appendTo(StringBuilder builder) {
        builder.append("CREATE OPERATOR CLASS ").append(name);
        if (defaultClass) {
            builder.append(" DEFAULT");
        }
        builder.append(" FOR TYPE ").append(dataType).append(" USING ").append(accessMethod);
        if (family != null) {
            builder.append(" FAMILY ").append(family);
        }
        return builder.append(" AS ").append(OperatorClassItem.toString(items));
    }

    @Override
    public String toString() {
        return appendTo(new StringBuilder()).toString();
    }

    @Override
    public <T, S> T accept(StatementVisitor<T> statementVisitor, S context) {
        return statementVisitor.visit(this, context);
    }
}
