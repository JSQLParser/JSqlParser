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

public final class DeclareStatement implements Statement {

    private UserVariable userVariable = null;
    private DeclareType type = DeclareType.TYPE;
    private String typeName;
    private List<TypeDefExpr> typeDefExprList = new ArrayList<>();

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

    public void addType(String columnName, ColDataType colDataType, Expression defaultExpr) {
        typeDefExprList.add(new TypeDefExpr(columnName, colDataType, defaultExpr));
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
                b.append(" TABLE(");
            }

            for (int i = 0; i < typeDefExprList.size(); i++) {
                if (i > 0) {
                    b.append(", ");
                }
                final TypeDefExpr type = typeDefExprList.get(i);
                if (type.userVariable != null) {
                    b.append(type.userVariable.toString()).append(" ");
                } else if (type.columnName != null) {
                    b.append(type.columnName).append(" ");
                }
                b.append(type.colDataType.toString());
                if (type.defaultExpr != null) {
                    b.append(" = ").append(type.defaultExpr.toString());
                }
            }

            if (type == DeclareType.TABLE) {
                b.append(")");
            }
        }

        return b.toString();
    }

    @Override
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public static class TypeDefExpr {

        public final String columnName;
        public final UserVariable userVariable;
        public final ColDataType colDataType;
        public final Expression defaultExpr;

        public TypeDefExpr(ColDataType colDataType, Expression defaultExpr) {
            this((UserVariable) null, colDataType, defaultExpr);
        }

        public TypeDefExpr(UserVariable userVariable, ColDataType colDataType, Expression defaultExpr) {
            this.userVariable = userVariable;
            this.colDataType = colDataType;
            this.defaultExpr = defaultExpr;
            this.columnName = null;
        }

        public TypeDefExpr(String colName, ColDataType colDataType, Expression defaultExpr) {
            this.userVariable = null;
            this.colDataType = colDataType;
            this.defaultExpr = defaultExpr;
            this.columnName = colName;
        }
    }
}
