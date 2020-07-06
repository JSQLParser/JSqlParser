/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.alter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class AlterExpression {

    private AlterOperation operation;
    private String optionalSpecifier;
    private String columnName;
    private String columnOldName;
    //private ColDataType dataType;

    private List<ColumnDataType> colDataTypeList;
    private List<ColumnDropNotNull> columnDropNotNullList;

    private List<String> pkColumns;
    private List<String> ukColumns;
    private String ukName;
    private Index index = null;
    private String constraintName;
    private boolean constraintIfExists;
    private boolean onDeleteRestrict;
    private boolean onDeleteSetNull;
    private boolean onDeleteCascade;
    private List<String> fkColumns;
    private String fkSourceTable;
    private List<String> fkSourceColumns;
    private boolean uk;
    private boolean useEqual;

    private List<ConstraintState> constraints;
    private List<String> parameters;
    private String commentText;

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public AlterOperation getOperation() {
        return operation;
    }

    public void setOperation(AlterOperation operation) {
        this.operation = operation;
    }

    public String getOptionalSpecifier() {
        return optionalSpecifier;
    }

    public void setOptionalSpecifier(String optionalSpecifier) {
        this.optionalSpecifier = optionalSpecifier;
    }

    public boolean isOnDeleteCascade() {
        return onDeleteCascade;
    }

    public void setOnDeleteCascade(boolean onDeleteCascade) {
        this.onDeleteCascade = onDeleteCascade;
    }

    public boolean isOnDeleteRestrict() {
        return onDeleteRestrict;
    }

    public void setOnDeleteRestrict(boolean onDeleteRestrict) {
        this.onDeleteRestrict = onDeleteRestrict;
    }

    public boolean isOnDeleteSetNull() {
        return onDeleteSetNull;
    }

    public void setOnDeleteSetNull(boolean onDeleteSetNull) {
        this.onDeleteSetNull = onDeleteSetNull;
    }

    public List<String> getFkColumns() {
        return fkColumns;
    }

    public void setFkColumns(List<String> fkColumns) {
        this.fkColumns = fkColumns;
    }

    public String getFkSourceTable() {
        return fkSourceTable;
    }

    public void setFkSourceTable(String fkSourceTable) {
        this.fkSourceTable = fkSourceTable;
    }

    public List<ColumnDataType> getColDataTypeList() {
        return colDataTypeList;
    }

    public void addColDataType(String columnName, ColDataType colDataType) {
        addColDataType(new ColumnDataType(columnName, false, colDataType, null));
    }

    public void addColDataType(ColumnDataType columnDataType) {
        if (colDataTypeList == null) {
            colDataTypeList = new ArrayList<ColumnDataType>();
        }
        colDataTypeList.add(columnDataType);
    }

    public void addColDropNotNull(ColumnDropNotNull columnDropNotNull) {
        if (columnDropNotNullList == null) {
            columnDropNotNullList = new ArrayList<ColumnDropNotNull>();
        }
        columnDropNotNullList.add(columnDropNotNull);
    }

    public List<String> getFkSourceColumns() {
        return fkSourceColumns;
    }

    public void setFkSourceColumns(List<String> fkSourceColumns) {
        this.fkSourceColumns = fkSourceColumns;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColOldName() {
        return columnOldName;
    }

    public void setColOldName(String columnOldName) {
        this.columnOldName = columnOldName;
    }

    public String getConstraintName() {
        return this.constraintName;
    }

    public void setConstraintName(final String constraintName) {
        this.constraintName = constraintName;
    }

    public boolean isConstraintIfExists() {
        return constraintIfExists;
    }

    public void setConstraintIfExists(boolean constraintIfExists) {
        this.constraintIfExists = constraintIfExists;
    }

    public List<String> getPkColumns() {
        return pkColumns;
    }

    public void setPkColumns(List<String> pkColumns) {
        this.pkColumns = pkColumns;
    }

    public List<String> getUkColumns() {
        return ukColumns;
    }

    public void setUkColumns(List<String> ukColumns) {
        this.ukColumns = ukColumns;
    }

    public String getUkName() {
        return ukName;
    }

    public void setUkName(String ukName) {
        this.ukName = ukName;
    }

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public List<ConstraintState> getConstraints() {
        return constraints;
    }

    public void setConstraints(List<ConstraintState> constraints) {
        this.constraints = constraints;
    }

    public List<ColumnDropNotNull> getColumnDropNotNullList() {
        return columnDropNotNullList;
    }

    public void addParameters(String... params) {
        if (parameters == null) {
            parameters = new ArrayList<String>();
        }
        parameters.addAll(Arrays.asList(params));
    }

    public List<String> getParameters() {
        return parameters;
    }

    public boolean getUseEqual() {
        return useEqual;
    }

    public void setUseEqual(boolean useEqual) {
        this.useEqual = useEqual;
    }

    public boolean getUk() {
        return uk;
    }

    public void setUk(boolean uk) {
        this.uk = uk;
    }

    @Override
    public String toString() {

        StringBuilder b = new StringBuilder();

        b.append(operation).append(" ");

        if (commentText != null) {
            if (columnName != null) {
                b.append(columnName).append(" COMMENT ");
            }
            b.append(commentText);
        } else if (columnName != null) {
            b.append("COLUMN ");
            if (operation == AlterOperation.RENAME) {
                b.append(columnOldName).append(" TO ");
            }
            b.append(columnName);
        } else if (getColDataTypeList() != null) {
            if (operation == AlterOperation.CHANGE) {
                if (optionalSpecifier != null) {
                    b.append(optionalSpecifier).append(" ");
                }
                b.append(columnOldName).append(" ");
            } else if (colDataTypeList.size() > 1) {
                b.append("(");
            } else {
                b.append("COLUMN ");
            }
            b.append(PlainSelect.getStringList(colDataTypeList));
            if (colDataTypeList.size() > 1) {
                b.append(")");
            }
        } else if (getColumnDropNotNullList() != null) {
            if (operation == AlterOperation.CHANGE) {
                if (optionalSpecifier != null) {
                    b.append(optionalSpecifier).append(" ");
                }
                b.append(columnOldName).append(" ");
            } else if (columnDropNotNullList.size() > 1) {
                b.append("(");
            } else {
                b.append("COLUMN ");
            }
            b.append(PlainSelect.getStringList(columnDropNotNullList));
            if (columnDropNotNullList.size() > 1) {
                b.append(")");
            }
        } else if (constraintName != null) {
            b.append("CONSTRAINT ");
            if (constraintIfExists) {
                b.append("IF EXISTS ");
            }
            b.append(constraintName);
        } else if (pkColumns != null) {
            b.append("PRIMARY KEY (").append(PlainSelect.getStringList(pkColumns)).append(')');
        } else if (ukColumns != null) {
            b.append("UNIQUE");
            if (ukName != null) {
                if (getUk()) {
                    b.append(" KEY ");
                } else {
                    b.append(" INDEX ");
                }
                b.append(ukName);
            }
            b.append(" (").append(PlainSelect.getStringList(ukColumns)).append(")");
        } else if (fkColumns != null) {
            b.append("FOREIGN KEY (").append(PlainSelect.getStringList(fkColumns)).
                    append(") REFERENCES ").append(fkSourceTable).append(" (").append(
                    PlainSelect.getStringList(fkSourceColumns)).append(")");
            if (isOnDeleteCascade()) {
                b.append(" ON DELETE CASCADE");
            } else if (isOnDeleteRestrict()) {
                b.append(" ON DELETE RESTRICT");
            } else if (isOnDeleteSetNull()) {
                b.append(" ON DELETE SET NULL");
            }
        } else if (index != null) {
            b.append(index);
        }
        if (getConstraints() != null && !getConstraints().isEmpty()) {
            b.append(' ').append(PlainSelect.getStringList(constraints, false, false));
        }
        if (getUseEqual()) {
            b.append('=');
        }
        if (parameters != null && !parameters.isEmpty()) {
            b.append(' ').append(PlainSelect.getStringList(parameters, false, false));
        }

        return b.toString();
    }

    public AlterExpression operation(AlterOperation operation) {
        this.setOperation(operation);
        return this;
    }

    public AlterExpression optionalSpecifier(String optionalSpecifier) {
        this.setOptionalSpecifier(optionalSpecifier);
        return this;
    }

    public AlterExpression columnName(String columnName) {
        this.setColumnName(columnName);
        return this;
    }

    public AlterExpression pkColumns(List<String> pkColumns) {
        this.setPkColumns(pkColumns);
        return this;
    }

    public AlterExpression ukColumns(List<String> ukColumns) {
        this.setUkColumns(ukColumns);
        return this;
    }

    public AlterExpression ukName(String ukName) {
        this.setUkName(ukName);
        return this;
    }

    public AlterExpression index(Index index) {
        this.setIndex(index);
        return this;
    }

    public AlterExpression constraintName(String constraintName) {
        this.setConstraintName(constraintName);
        return this;
    }

    public AlterExpression constraintIfExists(boolean constraintIfExists) {
        this.setConstraintIfExists(constraintIfExists);
        return this;
    }

    public AlterExpression onDeleteRestrict(boolean onDeleteRestrict) {
        this.setOnDeleteRestrict(onDeleteRestrict);
        return this;
    }

    public AlterExpression onDeleteSetNull(boolean onDeleteSetNull) {
        this.setOnDeleteSetNull(onDeleteSetNull);
        return this;
    }

    public AlterExpression onDeleteCascade(boolean onDeleteCascade) {
        this.setOnDeleteCascade(onDeleteCascade);
        return this;
    }

    public AlterExpression fkColumns(List<String> fkColumns) {
        this.setFkColumns(fkColumns);
        return this;
    }

    public AlterExpression fkSourceTable(String fkSourceTable) {
        this.setFkSourceTable(fkSourceTable);
        return this;
    }

    public AlterExpression fkSourceColumns(List<String> fkSourceColumns) {
        this.setFkSourceColumns(fkSourceColumns);
        return this;
    }

    public AlterExpression uk(boolean uk) {
        this.setUk(uk);
        return this;
    }

    public AlterExpression useEqual(boolean useEqual) {
        this.setUseEqual(useEqual);
        return this;
    }

    public AlterExpression constraints(List<ConstraintState> constraints) {
        this.setConstraints(constraints);
        return this;
    }

    public AlterExpression commentText(String commentText) {
        this.setCommentText(commentText);
        return this;
    }

    public AlterExpression addPkColumns(String... pkColumns) {
        List<String> collection = Optional.ofNullable(getPkColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, pkColumns);
        return this.pkColumns(collection);
    }

    public AlterExpression addPkColumns(Collection<String> pkColumns) {
        List<String> collection = Optional.ofNullable(getPkColumns()).orElseGet(ArrayList::new);
        collection.addAll(pkColumns);
        return this.pkColumns(collection);
    }

    public AlterExpression addUkColumns(String... ukColumns) {
        List<String> collection = Optional.ofNullable(getUkColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, ukColumns);
        return this.ukColumns(collection);
    }

    public AlterExpression addUkColumns(Collection<String> ukColumns) {
        List<String> collection = Optional.ofNullable(getUkColumns()).orElseGet(ArrayList::new);
        collection.addAll(ukColumns);
        return this.ukColumns(collection);
    }

    public AlterExpression addFkColumns(String... fkColumns) {
        List<String> collection = Optional.ofNullable(getFkColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, fkColumns);
        return this.fkColumns(collection);
    }

    public AlterExpression addFkColumns(Collection<String> fkColumns) {
        List<String> collection = Optional.ofNullable(getFkColumns()).orElseGet(ArrayList::new);
        collection.addAll(fkColumns);
        return this.fkColumns(collection);
    }

    public AlterExpression addFkSourceColumns(String... fkSourceColumns) {
        List<String> collection = Optional.ofNullable(getFkSourceColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, fkSourceColumns);
        return this.fkSourceColumns(collection);
    }

    public AlterExpression addFkSourceColumns(Collection<String> fkSourceColumns) {
        List<String> collection = Optional.ofNullable(getFkSourceColumns()).orElseGet(ArrayList::new);
        collection.addAll(fkSourceColumns);
        return this.fkSourceColumns(collection);
    }

    public AlterExpression addConstraints(ConstraintState... constraints) {
        List<ConstraintState> collection = Optional.ofNullable(getConstraints()).orElseGet(ArrayList::new);
        Collections.addAll(collection, constraints);
        return this.constraints(collection);
    }

    public AlterExpression addConstraints(Collection<? extends ConstraintState> constraints) {
        List<ConstraintState> collection = Optional.ofNullable(getConstraints()).orElseGet(ArrayList::new);
        collection.addAll(constraints);
        return this.constraints(collection);
    }

    public final static class ColumnDataType extends ColumnDefinition {

        private final boolean withType;

        public ColumnDataType(String columnName, boolean withType, ColDataType colDataType, List<String> columnSpecs) {
            super(columnName, colDataType, columnSpecs);
            this.withType = withType;
        }

        @Override
        public String toString() {
            return getColumnName() + (withType ? " TYPE " : " ") + toStringDataTypeAndSpec();
        }

        @Override()
        public ColumnDataType colDataType(ColDataType colDataType) {
            return (ColumnDataType) super.colDataType(colDataType);
        }

        @Override()
        public ColumnDataType columnName(String columnName) {
            return (ColumnDataType) super.columnName(columnName);
        }

        @Override()
        public ColumnDataType addColumnSpecs(String... columnSpecs) {
            return (ColumnDataType) super.addColumnSpecs(columnSpecs);
        }

        @Override()
        public ColumnDataType addColumnSpecs(Collection<String> columnSpecs) {
            return (ColumnDataType) super.addColumnSpecs(columnSpecs);
        }

    }

    public final static class ColumnDropNotNull {

        private final String columnName;
        private final boolean withNot;

        public ColumnDropNotNull(String columnName, boolean withNot) {
            this.columnName = columnName;
            this.withNot = withNot;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isWithNot() {
            return withNot;
        }

        @Override
        public String toString() {
            return columnName + " DROP"
                    + (withNot ? " NOT " : " ") + "NULL";
        }
    }
}
