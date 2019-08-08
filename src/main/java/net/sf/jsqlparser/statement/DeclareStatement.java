/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement;

import java.util.ArrayList;
import java.util.List;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

public final class DeclareStatement implements Statement {

    private UserVariable userVariable = null;
    private DeclareType type = DeclareType.TYPE;
    private String typeName;
    private List<TypeDefExpr> typeDefExprList = new ArrayList<>();
    private List<ColumnDefinition> colDefs = new ArrayList<>();

    public DeclareStatement() {
    }

    public void setUserVariable(UserVariable userVariable) {
        this.userVariable = userVariable;
    }

    public UserVariable getUserVariable() {
        return userVariable;
    }

    public DeclareType getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setDeclareType(DeclareType type) {
        this.type = type;
    }

    public void addType(ColDataType colDataType, Expression defaultExpr) {
        typeDefExprList.add(new TypeDefExpr(colDataType, defaultExpr));
    }

    public void addType(UserVariable userVariable, ColDataType colDataType, Expression defaultExpr) {
        typeDefExprList.add(new TypeDefExpr(userVariable, colDataType, defaultExpr));
    }

    public void addColumnDefinition(ColumnDefinition colDef) {
        colDefs.add(colDef);
    }

    public List<ColumnDefinition> getColumnDefinitions() {
        return colDefs;
    }

    public List<TypeDefExpr> getTypeDefinitions() {
        return typeDefExprList;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("DECLARE ");
        if (type == DeclareType.AS) {
            b.append(userVariable.toString());
            b.append(" AS ").append(typeName);
        } else {
            if (type == DeclareType.TABLE) {
                b.append(userVariable.toString());
                b.append(" TABLE (");
                for (int i = 0; i < colDefs.size(); i++) {
                    if (i > 0) {
                        b.append(", ");
                    }
                    b.append(colDefs.get(i).toString());
                }
                b.append(")");
            } else {
                for (int i = 0; i < typeDefExprList.size(); i++) {
                    if (i > 0) {
                        b.append(", ");
                    }
                    final TypeDefExpr type = typeDefExprList.get(i);
                    if (type.userVariable != null) {
                        b.append(type.userVariable.toString()).append(" ");
                    }
                    b.append(type.colDataType.toString());
                    if (type.defaultExpr != null) {
                        b.append(" = ").append(type.defaultExpr.toString());
                    }
                }
            }
        }
        return b.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor
    ) {
        statementVisitor.visit(this);
    }

    public static class TypeDefExpr {

        public final UserVariable userVariable;
        public final ColDataType colDataType;
        public final Expression defaultExpr;

        public TypeDefExpr(ColDataType colDataType, Expression defaultExpr) {
            this(null, colDataType, defaultExpr);
        }

        public TypeDefExpr(UserVariable userVariable, ColDataType colDataType, Expression defaultExpr) {
            this.userVariable = userVariable;
            this.colDataType = colDataType;
            this.defaultExpr = defaultExpr;
        }
    }
}
