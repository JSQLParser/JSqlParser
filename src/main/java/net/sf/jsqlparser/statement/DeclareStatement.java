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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.UserVariable;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

public final class DeclareStatement implements Statement {

    private UserVariable userVariable = null;

    private DeclareType declareType = DeclareType.TYPE;

    private String typeName;

    private List<TypeDefExpr> typeDefExprList = new ArrayList<>();

    private List<ColumnDefinition> columnDefinitions = new ArrayList<>();

    public DeclareStatement() {
    }

    public void setUserVariable(UserVariable userVariable) {
        this.userVariable = userVariable;
    }

    public UserVariable getUserVariable() {
        return userVariable;
    }

    /**
     * @return the {@link DeclareType}
     * @deprecated use {@link #getDeclareType()}
     */
    @Deprecated
    public DeclareType getType() {
        return getDeclareType();
    }

    /**
     * @return the {@link DeclareType}
     */
    public DeclareType getDeclareType() {
        return declareType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setDeclareType(DeclareType declareType) {
        this.declareType = declareType;
    }

    public void addType(ColDataType colDataType, Expression defaultExpr) {
        addTypeDefExprList(new TypeDefExpr(colDataType, defaultExpr));
    }

    public void addType(UserVariable userVariable, ColDataType colDataType, Expression defaultExpr) {
        addTypeDefExprList(new TypeDefExpr(userVariable, colDataType, defaultExpr));
    }

    public DeclareStatement addTypeDefExprList(TypeDefExpr... typeDefExpressions) {
        List<TypeDefExpr> collection = Optional.ofNullable(getTypeDefExprList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, typeDefExpressions);
        return this.withTypeDefExprList(collection);
    }

    public DeclareStatement addTypeDefExprList(Collection<? extends TypeDefExpr> typeDefExpressions) {
        List<TypeDefExpr> collection = Optional.ofNullable(getTypeDefExprList()).orElseGet(ArrayList::new);
        collection.addAll(typeDefExpressions);
        return this.withTypeDefExprList(collection);
    }

    public DeclareStatement withTypeDefExprList(List<TypeDefExpr> typeDefExpressions) {
        setTypeDefExprList(typeDefExpressions);
        return this;
    }

    public void setTypeDefExprList(List<TypeDefExpr> expr) {
        this.typeDefExprList = expr;
    }

    public List<TypeDefExpr> getTypeDefExprList() {
        return this.typeDefExprList;
    }

    public void addColumnDefinition(ColumnDefinition colDef) {
        columnDefinitions.add(colDef);
    }

    public void setColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    public List<ColumnDefinition> getColumnDefinitions() {
        return columnDefinitions;
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
        if (declareType == DeclareType.AS) {
            b.append(userVariable.toString());
            b.append(" AS ").append(typeName);
        } else {
            if (declareType == DeclareType.TABLE) {
                b.append(userVariable.toString());
                b.append(" TABLE (");
                for (int i = 0; i < columnDefinitions.size(); i++) {
                    if (i > 0) {
                        b.append(", ");
                    }
                    b.append(columnDefinitions.get(i).toString());
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
    public void accept(StatementVisitor statementVisitor) {
        statementVisitor.visit(this);
    }

    public DeclareStatement withUserVariable(UserVariable userVariable) {
        this.setUserVariable(userVariable);
        return this;
    }

    public DeclareStatement withTypeName(String typeName) {
        this.setTypeName(typeName);
        return this;
    }

    public DeclareStatement withDeclareType(DeclareType declareType) {
        this.setDeclareType(declareType);
        return this;
    }

    public DeclareStatement withColumnDefinitions(List<ColumnDefinition> columnDefinitions) {
        this.setColumnDefinitions(columnDefinitions);
        return this;
    }

    public DeclareStatement addColumnDefinitions(ColumnDefinition... statements) {
        List<ColumnDefinition> collection = Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        Collections.addAll(collection, statements);
        return this.withColumnDefinitions(collection);
    }

    public DeclareStatement addColumnDefinitions(Collection<? extends ColumnDefinition> columnDefinitions) {
        List<ColumnDefinition> collection = Optional.ofNullable(getColumnDefinitions()).orElseGet(ArrayList::new);
        collection.addAll(columnDefinitions);
        return this.withColumnDefinitions(collection);
    }

    public static class TypeDefExpr implements Serializable {

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
