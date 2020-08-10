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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.select.PlainSelect;

public class AlterExpression {

    private AlterOperation operation;
    private String optionalSpecifier;
    private String columnName;
    private String columnOldName;
    // private ColDataType dataType;

    private List<ColumnDataType> colDataTypeList;
    private List<ColumnDropNotNull> columnDropNotNullList;

    private List<String> pkColumns;
    private List<String> ukColumns;
    private String ukName;
    private Index index = null;
    private String constraintName;
    private boolean constraintIfExists;

    private Set<ReferentialAction> referentialActions = new LinkedHashSet<>(2);

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

    /**
     * @param type
     * @param action
     */
    public void setReferentialAction(Type type, Action action) {
        setReferentialAction(type, action, true);
    }

    /**
     * @param type
     */
    public void removeReferentialAction(Type type) {
        setReferentialAction(type, null, false);
    }

    /**
     * @param type
     * @return
     */
    public ReferentialAction getReferentialAction(Type type) {
        return referentialActions.stream().filter(ra -> type.equals(ra.getType())).findFirst().orElse(null);
    }

    private void setReferentialAction(Type type, Action action, boolean set) {
        ReferentialAction found = getReferentialAction(type);
        if (set) {
            if (found == null) {
                referentialActions.add(new ReferentialAction(type, action));
            } else {
                found.setAction(action);
            }
        } else if (found != null) {
            referentialActions.remove(found);
        }
    }
    /**
     * @return
     * @deprecated use {@link #getOnDeleteReferentialAction()}
     */
    @Deprecated
    public boolean isOnDeleteCascade() {
        ReferentialAction found = getReferentialAction(Type.DELETE);
        return found != null && Action.CASCADE.equals(found.getAction());
    }

    /**
     * @return
     * @deprecated use {@link #setOnDeleteReferentialAction(Action)
     */
    @Deprecated
    public void setOnDeleteCascade(boolean onDeleteCascade) {
        setReferentialAction(Type.DELETE, Action.CASCADE, onDeleteCascade);
    }

    /**
     * @return
     * @deprecated use {@link #getOnDeleteReferentialAction()}
     */
    @Deprecated
    public boolean isOnDeleteRestrict() {
        ReferentialAction found = getReferentialAction(Type.DELETE);
        return found != null && Action.RESTRICT.equals(found.getAction());
    }

    /**
     * @return
     * @deprecated use {@link #setOnDeleteReferentialAction(Action)
     */
    @Deprecated
    public void setOnDeleteRestrict(boolean onDeleteRestrict) {
        setReferentialAction(Type.DELETE, Action.RESTRICT, onDeleteRestrict);
    }

    /**
     * @return
     * @deprecated use {@link #getOnDeleteReferentialAction()}
     */
    @Deprecated
    public boolean isOnDeleteSetNull() {
        ReferentialAction found = getReferentialAction(Type.DELETE);
        return found != null && Action.SET_NULL.equals(found.getAction());
    }

    /**
     * @return
     * @deprecated use {@link #setOnDeleteReferentialAction(Action)
     */
    @Deprecated
    public void setOnDeleteSetNull(boolean onDeleteSetNull) {
        setReferentialAction(Type.DELETE, Action.SET_NULL, onDeleteSetNull);
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
            colDataTypeList = new ArrayList<>();
        }
        colDataTypeList.add(columnDataType);
    }

    public void addColDropNotNull(ColumnDropNotNull columnDropNotNull) {
        if (columnDropNotNullList == null) {
            columnDropNotNullList = new ArrayList<>();
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
            parameters = new ArrayList<>();
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
            b.append("FOREIGN KEY (").append(PlainSelect.getStringList(fkColumns)).append(") REFERENCES ")
            .append(fkSourceTable).append(" (").append(
                    PlainSelect.getStringList(fkSourceColumns))
            .append(")");
            referentialActions.forEach(b::append);
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
