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

import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;
import java.util.*;

@SuppressWarnings({"PMD.CyclomaticComplexity"})
public class AlterExpression implements Serializable {

    private final Set<ReferentialAction> referentialActions = new LinkedHashSet<>(2);
    private AlterOperation operation;
    private String optionalSpecifier;
    private String newTableName;
    private String columnName;
    // private ColDataType dataType;
    private String columnOldName;
    private List<ColumnDataType> colDataTypeList;
    private List<ColumnDropNotNull> columnDropNotNullList;
    private List<ColumnDropDefault> columnDropDefaultList;
    private List<String> pkColumns;
    private List<String> ukColumns;
    private String ukName;
    private Index index = null;
    private Index oldIndex = null;
    private String constraintName;
    private boolean usingIfExists;
    private List<String> fkColumns;
    private String fkSourceSchema;

    private String fkSourceTable;
    private List<String> fkSourceColumns;
    private boolean uk;
    private boolean useEqual;

    private List<String> partitions;
    private List<PartitionDefinition> partitionDefinitions;
    private List<ConstraintState> constraints;
    private List<String> parameters;

    private ConvertType convertType;
    private boolean hasEqualForCharacterSet;
    private boolean hasEqualForCollate;

    private String characterSet;
    private String collation;
    private String lockOption;
    private String algorithmOption;
    private String engineOption;
    private String commentText;
    private String tableOption;

    private boolean hasColumn = false;
    private boolean hasColumns = false;


    private boolean useBrackets = false;

    private boolean useIfNotExists = false;

    public Index getOldIndex() {
        return oldIndex;
    }

    public void setOldIndex(Index oldIndex) {
        this.oldIndex = oldIndex;
    }

    public boolean hasColumn() {
        return hasColumn;
    }

    public boolean hasColumns() {
        return hasColumns;
    }

    public boolean useBrackets() {
        return useBrackets;
    }

    public void useBrackets(boolean useBrackets) {
        this.useBrackets = useBrackets;
    }

    public void hasColumn(boolean hasColumn) {
        this.hasColumn = hasColumn;
    }

    public void hasColumns(boolean hasColumns) {
        this.hasColumns = hasColumns;
    }

    public String getFkSourceSchema() {
        return fkSourceSchema;
    }

    public void setFkSourceSchema(String fkSourceSchema) {
        this.fkSourceSchema = fkSourceSchema;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getTableOption() {
        return tableOption;
    }

    public void setTableOption(String tableOption) {
        this.tableOption = tableOption;
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

    public AlterExpression withReferentialAction(Type type, Action action) {
        setReferentialAction(type, action);
        return this;
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
        return referentialActions.stream()
                .filter(ra -> type.equals(ra.getType()))
                .findFirst()
                .orElse(null);
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
     * @deprecated use {@link #getReferentialAction(ReferentialAction.Type)}
     */
    @Deprecated
    public boolean isOnDeleteCascade() {
        ReferentialAction found = getReferentialAction(Type.DELETE);
        return found != null && Action.CASCADE.equals(found.getAction());
    }

    /**
     * @param onDeleteCascade
     * @deprecated use
     *             {@link #setReferentialAction(ReferentialAction.Type, ReferentialAction.Action, boolean)}
     */
    @Deprecated
    public void setOnDeleteCascade(boolean onDeleteCascade) {
        setReferentialAction(Type.DELETE, Action.CASCADE, onDeleteCascade);
    }

    /**
     * @return
     * @deprecated use {@link #getReferentialAction(ReferentialAction.Type)}
     */
    @Deprecated
    public boolean isOnDeleteRestrict() {
        ReferentialAction found = getReferentialAction(Type.DELETE);
        return found != null && Action.RESTRICT.equals(found.getAction());
    }

    /**
     * @param onDeleteRestrict
     * @deprecated use
     *             {@link #setReferentialAction(ReferentialAction.Type, ReferentialAction.Action, boolean)}
     */
    @Deprecated
    public void setOnDeleteRestrict(boolean onDeleteRestrict) {
        setReferentialAction(Type.DELETE, Action.RESTRICT, onDeleteRestrict);
    }

    /**
     * @return
     * @deprecated use {@link #getReferentialAction(ReferentialAction.Type)}
     */
    @Deprecated
    public boolean isOnDeleteSetNull() {
        ReferentialAction found = getReferentialAction(Type.DELETE);
        return found != null && Action.SET_NULL.equals(found.getAction());
    }

    /**
     * @param onDeleteSetNull
     * @deprecated use
     *             {@link #setReferentialAction(ReferentialAction.Type, ReferentialAction.Action, boolean)}
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

    public void addColDropDefault(ColumnDropDefault columnDropDefault) {
        if (columnDropDefaultList == null) {
            columnDropDefaultList = new ArrayList<>();
        }
        columnDropDefaultList.add(columnDropDefault);
    }

    public List<String> getFkSourceColumns() {
        return fkSourceColumns;
    }

    public void setFkSourceColumns(List<String> fkSourceColumns) {
        this.fkSourceColumns = fkSourceColumns;
    }

    public String getNewTableName() {
        return newTableName;
    }

    public void setNewTableName(String newTableName) {
        this.newTableName = newTableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Deprecated
    public String getColOldName() {
        return getColumnOldName();
    }

    @Deprecated
    public void setColOldName(String columnOldName) {
        setColumnOldName(columnOldName);
    }

    public String getColumnOldName() {
        return columnOldName;
    }

    public void setColumnOldName(String columnOldName) {
        this.columnOldName = columnOldName;
    }

    public String getConstraintName() {
        return this.constraintName;
    }

    public void setConstraintName(final String constraintName) {
        this.constraintName = constraintName;
    }

    public boolean isUsingIfExists() {
        return usingIfExists;
    }

    public void setUsingIfExists(boolean usingIfExists) {
        this.usingIfExists = usingIfExists;
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

    public ConvertType getConvertType() {
        return convertType;
    }

    public void setConvertType(ConvertType convertType) {
        this.convertType = convertType;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public String getLockOption() {
        return lockOption;
    }

    public void setLockOption(String lockOption) {
        this.lockOption = lockOption;
    }

    public String getAlgorithmOption() {
        return algorithmOption;
    }

    public void setAlgorithmOption(String algorithmOption) {
        this.algorithmOption = algorithmOption;
    }

    public String getEngineOption() {
        return engineOption;
    }

    public void setEngineOption(String engineOption) {
        this.engineOption = engineOption;
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

    public boolean isUseIfNotExists() {
        return useIfNotExists;
    }

    public void setUseIfNotExists(boolean useIfNotExists) {
        this.useIfNotExists = useIfNotExists;
    }

    public AlterExpression withUserIfNotExists(boolean userIfNotExists) {
        this.useIfNotExists = userIfNotExists;
        return this;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.SwitchStmtsShouldHaveDefault"})
    public String toString() {

        StringBuilder b = new StringBuilder();

        if (operation == AlterOperation.UNSPECIFIC) {
            b.append(optionalSpecifier);
        } else if (operation == AlterOperation.SET_TABLE_OPTION) {
            b.append(tableOption);
        } else if (operation == AlterOperation.ENGINE) {
            b.append("ENGINE ");
            if (useEqual) {
                b.append("= ");
            }
            b.append(engineOption);
        } else if (operation == AlterOperation.ALGORITHM) {
            b.append("ALGORITHM ");
            if (useEqual) {
                b.append("= ");
            }
            b.append(algorithmOption);
        } else if (operation == AlterOperation.LOCK) {
            b.append("LOCK ");
            if (useEqual) {
                b.append("= ");
            }
            b.append(lockOption);
        } else if (getOldIndex() != null) {
            b.append("RENAME");
            switch (operation) {
                case RENAME_KEY:
                    b.append(" KEY ");
                    break;
                case RENAME_INDEX:
                    b.append(" INDEX ");
                    break;
                case RENAME_CONSTRAINT:
                    b.append(" CONSTRAINT ");
                    break;
            }
            b.append(getOldIndex().getName()).append(" TO ").append(getIndex().getName());
        } else if (operation == AlterOperation.RENAME_TABLE) {

            b.append("RENAME TO ").append(newTableName);
        } else if (operation == AlterOperation.DROP_PRIMARY_KEY) {

            b.append("DROP PRIMARY KEY ");
        } else if (operation == AlterOperation.CONVERT) {
            if (convertType == ConvertType.CONVERT_TO) {
                b.append("CONVERT TO CHARACTER SET ");
            } else if (convertType == ConvertType.DEFAULT_CHARACTER_SET) {
                b.append("DEFAULT CHARACTER SET ");
                if (hasEqualForCharacterSet) {
                    b.append("= ");
                }
            } else if (convertType == ConvertType.CHARACTER_SET) {
                b.append("CHARACTER SET ");
                if (hasEqualForCharacterSet) {
                    b.append("= ");
                }
            }

            if (getCharacterSet() != null) {
                b.append(getCharacterSet());
            }

            if (getCollation() != null) {
                b.append(" COLLATE ");
                if (hasEqualForCollate) {
                    b.append("= ");
                }
                b.append(getCollation());
            }
        } else if (operation == AlterOperation.DROP_UNIQUE) {

            b.append("DROP UNIQUE (").append(PlainSelect.getStringList(pkColumns)).append(')');
        } else if (operation == AlterOperation.DROP_FOREIGN_KEY) {

            b.append("DROP FOREIGN KEY (").append(PlainSelect.getStringList(pkColumns)).append(')');
        } else if (operation == AlterOperation.DROP && columnName == null && pkColumns != null
                && !pkColumns.isEmpty()) {
            // Oracle Multi Column Drop
            b.append("DROP (").append(PlainSelect.getStringList(pkColumns)).append(')');
        } else if (operation == AlterOperation.TRUNCATE_PARTITION
                && partitions != null) {
            b.append("TRUNCATE PARTITION ").append(PlainSelect.getStringList(partitions));
        } else {
            if (operation == AlterOperation.COMMENT_WITH_EQUAL_SIGN) {
                b.append("COMMENT =").append(" ");
            } else {
                b.append(operation).append(" ");
            }
            if (commentText != null) {
                if (columnName != null) {
                    b.append(columnName).append(" COMMENT ");
                }
                b.append(commentText);
            } else if (columnName != null) {
                if (hasColumn) {
                    b.append("COLUMN ");
                }
                if (usingIfExists) {
                    b.append("IF EXISTS ");
                }
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
                    if (hasColumn) {
                        b.append("COLUMN ");
                    } else if (hasColumns) {
                        b.append("COLUMNS ");
                    }
                    if (useIfNotExists
                            && operation == AlterOperation.ADD) {
                        b.append("IF NOT EXISTS ");
                    }
                }
                if (useBrackets && colDataTypeList.size() == 1) {
                    b.append(" ( ");
                }
                b.append(PlainSelect.getStringList(colDataTypeList));
                if (useBrackets && colDataTypeList.size() == 1) {
                    b.append(" ) ");
                }
                if (colDataTypeList.size() > 1) {
                    b.append(")");
                }
            } else if (getColumnDropNotNullList() != null) {
                b.append("COLUMN ");
                b.append(PlainSelect.getStringList(columnDropNotNullList));
            } else if (columnDropDefaultList != null && !columnDropDefaultList.isEmpty()) {
                b.append("COLUMN ");
                b.append(PlainSelect.getStringList(columnDropDefaultList));
            } else if (constraintName != null) {
                b.append("CONSTRAINT ");
                if (usingIfExists) {
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
                b.append("FOREIGN KEY (")
                        .append(PlainSelect.getStringList(fkColumns))
                        .append(") REFERENCES ")
                        .append(
                                fkSourceSchema != null && fkSourceSchema.trim().length() > 0
                                        ? fkSourceSchema + "."
                                        : "")
                        .append(fkSourceTable)
                        .append(" (")
                        .append(PlainSelect.getStringList(fkSourceColumns))
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
        }

        if (parameters != null && !parameters.isEmpty()) {
            b.append(' ').append(PlainSelect.getStringList(parameters, false, false));
        }

        if (index != null && index.getCommentText() != null) {
            // `USING` is a parameters
            b.append(" COMMENT ").append(index.getCommentText());
        }

        return b.toString();
    }

    public AlterExpression withOperation(AlterOperation operation) {
        this.setOperation(operation);
        return this;
    }

    public AlterExpression withOptionalSpecifier(String optionalSpecifier) {
        this.setOptionalSpecifier(optionalSpecifier);
        return this;
    }

    public AlterExpression withColumnName(String columnName) {
        this.setColumnName(columnName);
        return this;
    }

    public AlterExpression withPkColumns(List<String> pkColumns) {
        this.setPkColumns(pkColumns);
        return this;
    }

    public AlterExpression withUkColumns(List<String> ukColumns) {
        this.setUkColumns(ukColumns);
        return this;
    }

    public AlterExpression withUkName(String ukName) {
        this.setUkName(ukName);
        return this;
    }

    public AlterExpression withIndex(Index index) {
        this.setIndex(index);
        return this;
    }

    public AlterExpression withConstraintName(String constraintName) {
        this.setConstraintName(constraintName);
        return this;
    }

    public AlterExpression withUsingIfExists(boolean usingIfExists) {
        this.setUsingIfExists(usingIfExists);
        return this;
    }

    public AlterExpression withOnDeleteRestrict(boolean onDeleteRestrict) {
        this.setOnDeleteRestrict(onDeleteRestrict);
        return this;
    }

    public AlterExpression withOnDeleteSetNull(boolean onDeleteSetNull) {
        this.setOnDeleteSetNull(onDeleteSetNull);
        return this;
    }

    public AlterExpression withOnDeleteCascade(boolean onDeleteCascade) {
        this.setOnDeleteCascade(onDeleteCascade);
        return this;
    }

    public AlterExpression withFkColumns(List<String> fkColumns) {
        this.setFkColumns(fkColumns);
        return this;
    }

    public AlterExpression withFkSourceSchema(String fkSourceSchema) {
        this.setFkSourceTable(fkSourceSchema);
        return this;
    }

    public AlterExpression withFkSourceTable(String fkSourceTable) {
        this.setFkSourceTable(fkSourceTable);
        return this;
    }

    public AlterExpression withFkSourceColumns(List<String> fkSourceColumns) {
        this.setFkSourceColumns(fkSourceColumns);
        return this;
    }

    public AlterExpression withUk(boolean uk) {
        this.setUk(uk);
        return this;
    }

    public AlterExpression withUseEqual(boolean useEqual) {
        this.setUseEqual(useEqual);
        return this;
    }

    public AlterExpression withConstraints(List<ConstraintState> constraints) {
        this.setConstraints(constraints);
        return this;
    }

    public AlterExpression withCommentText(String commentText) {
        this.setCommentText(commentText);
        return this;
    }

    public AlterExpression withColumnOldName(String columnOldName) {
        setColumnOldName(columnOldName);
        return this;
    }

    public AlterExpression addPkColumns(String... pkColumns) {
        List<String> collection = Optional.ofNullable(getPkColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, pkColumns);
        return this.withPkColumns(collection);
    }

    public AlterExpression addPkColumns(Collection<String> pkColumns) {
        List<String> collection = Optional.ofNullable(getPkColumns()).orElseGet(ArrayList::new);
        collection.addAll(pkColumns);
        return this.withPkColumns(collection);
    }

    public AlterExpression addUkColumns(String... ukColumns) {
        List<String> collection = Optional.ofNullable(getUkColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, ukColumns);
        return this.withUkColumns(collection);
    }

    public AlterExpression addUkColumns(Collection<String> ukColumns) {
        List<String> collection = Optional.ofNullable(getUkColumns()).orElseGet(ArrayList::new);
        collection.addAll(ukColumns);
        return this.withUkColumns(collection);
    }

    public AlterExpression addFkColumns(String... fkColumns) {
        List<String> collection = Optional.ofNullable(getFkColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, fkColumns);
        return this.withFkColumns(collection);
    }

    public AlterExpression addFkColumns(Collection<String> fkColumns) {
        List<String> collection = Optional.ofNullable(getFkColumns()).orElseGet(ArrayList::new);
        collection.addAll(fkColumns);
        return this.withFkColumns(collection);
    }

    public AlterExpression addFkSourceColumns(String... fkSourceColumns) {
        List<String> collection =
                Optional.ofNullable(getFkSourceColumns()).orElseGet(ArrayList::new);
        Collections.addAll(collection, fkSourceColumns);
        return this.withFkSourceColumns(collection);
    }

    public AlterExpression addFkSourceColumns(Collection<String> fkSourceColumns) {
        List<String> collection =
                Optional.ofNullable(getFkSourceColumns()).orElseGet(ArrayList::new);
        collection.addAll(fkSourceColumns);
        return this.withFkSourceColumns(collection);
    }

    public AlterExpression addConstraints(ConstraintState... constraints) {
        List<ConstraintState> collection =
                Optional.ofNullable(getConstraints()).orElseGet(ArrayList::new);
        Collections.addAll(collection, constraints);
        return this.withConstraints(collection);
    }

    public AlterExpression addConstraints(Collection<? extends ConstraintState> constraints) {
        List<ConstraintState> collection =
                Optional.ofNullable(getConstraints()).orElseGet(ArrayList::new);
        collection.addAll(constraints);
        return this.withConstraints(collection);
    }

    public List<String> getPartitions() {
        return partitions;
    }

    public void setPartitions(List<String> partitions) {
        this.partitions = partitions;
    }

    public List<PartitionDefinition> getPartitionDefinitions() {
        return partitionDefinitions;
    }

    public void setPartitionDefinitions(List<PartitionDefinition> partitionDefinition) {
        this.partitionDefinitions = partitionDefinition;
    }

    public void setHasEqualForCharacterSet(boolean hasEqualForCharacterSet) {
        this.hasEqualForCharacterSet = hasEqualForCharacterSet;
    }

    public void setHasEqualForCollate(boolean hasEqualForCollate) {
        this.hasEqualForCollate = hasEqualForCollate;
    }

    public static final class ColumnDataType extends ColumnDefinition {

        private final boolean withType;

        public ColumnDataType(boolean withType) {
            super();
            this.withType = withType;
        }

        public ColumnDataType(
                String columnName, boolean withType, ColDataType colDataType,
                List<String> columnSpecs) {
            super(columnName, colDataType, columnSpecs);
            this.withType = withType;
        }

        @Override
        public String toString() {
            return getColumnName() + (withType ? " TYPE " : getColDataType() == null ? "" : " ")
                    + toStringDataTypeAndSpec();
        }

        @Override
        public ColumnDataType withColDataType(ColDataType colDataType) {
            return (ColumnDataType) super.withColDataType(colDataType);
        }

        @Override
        public ColumnDataType withColumnName(String columnName) {
            return (ColumnDataType) super.withColumnName(columnName);
        }

        @Override
        public ColumnDataType addColumnSpecs(String... columnSpecs) {
            return (ColumnDataType) super.addColumnSpecs(columnSpecs);
        }

        @Override
        public ColumnDataType addColumnSpecs(Collection<String> columnSpecs) {
            return (ColumnDataType) super.addColumnSpecs(columnSpecs);
        }

        @Override
        public ColumnDataType withColumnSpecs(List<String> columnSpecs) {
            return (ColumnDataType) super.withColumnSpecs(columnSpecs);
        }
    }

    public static final class ColumnDropNotNull implements Serializable {

        private final String columnName;
        private final boolean withNot;

        public ColumnDropNotNull(String columnName) {
            this(columnName, false);
        }

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
            return columnName + " DROP" + (withNot ? " NOT " : " ") + "NULL";
        }
    }

    public static final class ColumnDropDefault implements Serializable {

        private final String columnName;

        public ColumnDropDefault(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

        @Override
        public String toString() {
            return columnName + " DROP DEFAULT";
        }
    }

    public enum ConvertType {
        CONVERT_TO, DEFAULT_CHARACTER_SET, CHARACTER_SET
    }
}
