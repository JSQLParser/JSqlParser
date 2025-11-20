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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.ReferentialAction;
import net.sf.jsqlparser.statement.ReferentialAction.Action;
import net.sf.jsqlparser.statement.ReferentialAction.Type;
import net.sf.jsqlparser.statement.create.table.ColDataType;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;
import net.sf.jsqlparser.statement.create.table.Index;
import net.sf.jsqlparser.statement.create.table.PartitionDefinition;
import net.sf.jsqlparser.statement.select.PlainSelect;

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
    private List<ColumnSetDefault> columnSetDefaultList;
    private List<ColumnSetVisibility> columnSetVisibilityList;

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
    private boolean defaultCollateSpecified;
    private String lockOption;
    private String algorithmOption;
    private String engineOption;
    private String commentText;
    private String tableOption;

    private boolean hasColumn = false;
    private boolean hasColumns = false;


    private boolean useBrackets = false;

    private boolean useIfNotExists = false;

    private String partitionType;
    private Expression partitionExpression;
    private List<String> partitionColumns;
    private int coalescePartitionNumber;

    private String exchangePartitionTableName;
    private boolean exchangePartitionWithValidation;
    private boolean exchangePartitionWithoutValidation;

    private int keyBlockSize;

    private String constraintSymbol;
    private boolean enforced;
    private String constraintType;
    private boolean invisible;

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

    public List<ColumnDropDefault> getColumnDropDefaultList() {
        return columnDropDefaultList;
    }

    public void addColDropDefault(ColumnDropDefault columnDropDefault) {
        if (columnDropDefaultList == null) {
            columnDropDefaultList = new ArrayList<>();
        }
        columnDropDefaultList.add(columnDropDefault);
    }

    public void addColSetDefault(ColumnSetDefault columnSetDefault) {
        if (columnSetDefaultList == null) {
            columnSetDefaultList = new ArrayList<>();
        }
        columnSetDefaultList.add(columnSetDefault);
    }

    public List<ColumnSetDefault> getColumnSetDefaultList() {
        return columnSetDefaultList;
    }

    public void addColSetVisibility(ColumnSetVisibility columnSetVisibility) {
        if (columnSetVisibilityList == null) {
            columnSetVisibilityList = new ArrayList<>();
        }
        columnSetVisibilityList.add(columnSetVisibility);
    }

    public List<ColumnSetVisibility> getColumnSetVisibilityList() {
        return columnSetVisibilityList;
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

    public void setDefaultCollateSpecified(boolean value) {
        this.defaultCollateSpecified = value;
    }

    public boolean isDefaultCollateSpecified() {
        return defaultCollateSpecified;
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

    public void setPartitionType(String partitionType) {
        this.partitionType = partitionType;
    }

    public String getPartitionType() {
        return partitionType;
    }

    public void setPartitionExpression(Expression partitionExpression) {
        this.partitionExpression = partitionExpression;
    }

    public Expression getPartitionExpression() {
        return partitionExpression;
    }

    public void setPartitionColumns(List<String> partitionColumns) {
        this.partitionColumns = partitionColumns;
    }

    public List<String> getPartitionColumns() {
        return partitionColumns;
    }

    public void setExchangePartitionTableName(String exchangePartitionTableName) {
        this.exchangePartitionTableName = exchangePartitionTableName;
    }

    public String getExchangePartitionTableName() {
        return exchangePartitionTableName;
    }

    public void setCoalescePartitionNumber(int coalescePartitionNumber) {
        this.coalescePartitionNumber = coalescePartitionNumber;
    }

    public int getCoalescePartitionNumber() {
        return coalescePartitionNumber;
    }

    public void setExchangePartitionWithValidation(boolean exchangePartitionWithValidation) {
        this.exchangePartitionWithValidation = exchangePartitionWithValidation;
    }

    public boolean isExchangePartitionWithValidation() {
        return exchangePartitionWithValidation;
    }

    public void setExchangePartitionWithoutValidation(boolean exchangePartitionWithoutValidation) {
        this.exchangePartitionWithoutValidation = exchangePartitionWithoutValidation;
    }

    public boolean isExchangePartitionWithoutValidation() {
        return exchangePartitionWithoutValidation;
    }

    public void setKeyBlockSize(int keyBlockSize) {
        this.keyBlockSize = keyBlockSize;
    }

    public int getKeyBlockSize() {
        return keyBlockSize;
    }

    public String getConstraintSymbol() {
        return constraintSymbol;
    }

    public void setConstraintSymbol(String constraintSymbol) {
        this.constraintSymbol = constraintSymbol;
    }

    public boolean isEnforced() {
        return enforced;
    }

    public void setEnforced(boolean enforced) {
        this.enforced = enforced;
    }

    public String getConstraintType() {
        return constraintType;
    }

    public void setConstraintType(String constraintType) {
        this.constraintType = constraintType;
    }

    public boolean isInvisible() {
        return invisible;
    }

    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity",
            "PMD.ExcessiveMethodLength", "PMD.SwitchStmtsShouldHaveDefault"})
    public String toString() {

        StringBuilder b = new StringBuilder();

        if (operation == AlterOperation.UNSPECIFIC) {
            b.append(optionalSpecifier);
        } else if (operation == AlterOperation.ALTER && constraintType != null
                && constraintSymbol != null) {
            // This is for ALTER INDEX ... INVISIBLE
            b.append("ALTER ").append(constraintType).append(" ").append(constraintSymbol);

            if (invisible) {
                b.append(" INVISIBLE");
            } else if (!isEnforced()) {
                b.append(" NOT ENFORCED");
            } else if (enforced) {
                b.append(" ENFORCED");
            }
        } else if (operation == AlterOperation.ADD && constraintType != null
                && constraintSymbol != null) {
            b.append("ADD CONSTRAINT ").append(constraintType).append(" ").append(constraintSymbol)
                    .append(" ");

            if (index != null && index.getColumnsNames() != null) {
                b.append(" ")
                        .append(PlainSelect.getStringList(index.getColumnsNames(), true, true));
            }
        } else if (operation == AlterOperation.ALTER
                && columnDropDefaultList != null && !columnDropDefaultList.isEmpty()) {
            b.append("ALTER ");
            if (hasColumn) {
                b.append("COLUMN ");
            }
            b.append(PlainSelect.getStringList(columnDropDefaultList));
        } else if (operation == AlterOperation.ALTER
                && columnSetDefaultList != null && !columnSetDefaultList.isEmpty()) {
            b.append("ALTER ");
            if (hasColumn) {
                b.append("COLUMN ");
            }
            b.append(PlainSelect.getStringList(columnSetDefaultList));
        } else if (operation == AlterOperation.ALTER
                && columnSetVisibilityList != null && !columnSetVisibilityList.isEmpty()) {
            b.append("ALTER ");
            if (hasColumn) {
                b.append("COLUMN ");
            }
            b.append(PlainSelect.getStringList(columnSetVisibilityList));
        } else if (operation == AlterOperation.SET_TABLE_OPTION) {
            b.append(tableOption);
        } else if (operation == AlterOperation.DISCARD_TABLESPACE) {
            b.append("DISCARD TABLESPACE");
        } else if (operation == AlterOperation.IMPORT_TABLESPACE) {
            b.append("IMPORT TABLESPACE");
        } else if (operation == AlterOperation.DISABLE_KEYS) {
            b.append("DISABLE KEYS");
        } else if (operation == AlterOperation.ENABLE_KEYS) {
            b.append("ENABLE KEYS");
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
        } else if (operation == AlterOperation.KEY_BLOCK_SIZE) {
            b.append("KEY_BLOCK_SIZE ");
            if (useEqual) {
                b.append("= ");
            }
            b.append(keyBlockSize);
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
        } else if (operation == AlterOperation.COLLATE) {
            if (isDefaultCollateSpecified()) {
                b.append("DEFAULT ");
            }
            b.append("COLLATE ");
            if (hasEqualForCollate) {
                b.append("= ");
            }
            if (getCollation() != null) {
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
        } else if (operation == AlterOperation.DISCARD_PARTITION && partitions != null) {
            b.append("DISCARD PARTITION ").append(PlainSelect.getStringList(partitions));
            if (tableOption != null) {
                b.append(" ").append(tableOption);
            }
        } else if (operation == AlterOperation.IMPORT_PARTITION) {
            b.append("IMPORT PARTITION ").append(PlainSelect.getStringList(partitions));
            if (tableOption != null) {
                b.append(" ").append(tableOption);
            }
        } else if (operation == AlterOperation.TRUNCATE_PARTITION
                && partitions != null) {
            b.append("TRUNCATE PARTITION ").append(PlainSelect.getStringList(partitions));
        } else if (operation == AlterOperation.COALESCE_PARTITION) {
            b.append("COALESCE PARTITION ").append(coalescePartitionNumber);
        } else if (operation == AlterOperation.REORGANIZE_PARTITION
                && partitions != null
                && partitionDefinitions != null) {
            b.append("REORGANIZE PARTITION ")
                    .append(PlainSelect.getStringList(partitions))
                    .append(" INTO (")
                    .append(partitionDefinitions.stream()
                            .map(PartitionDefinition::toString)
                            .collect(Collectors.joining(", ")))
                    .append(")");
        } else if (operation == AlterOperation.EXCHANGE_PARTITION) {
            b.append("EXCHANGE PARTITION ");
            b.append(partitions.get(0)).append(" WITH TABLE ").append(exchangePartitionTableName);
            if (exchangePartitionWithValidation) {
                b.append(" WITH VALIDATION ");
            } else if (exchangePartitionWithoutValidation) {
                b.append(" WITHOUT VALIDATION ");
            }
        } else if (operation == AlterOperation.ANALYZE_PARTITION && partitions != null) {
            b.append("ANALYZE PARTITION ").append(PlainSelect.getStringList(partitions));
        } else if (operation == AlterOperation.CHECK_PARTITION && partitions != null) {
            b.append("CHECK PARTITION ").append(PlainSelect.getStringList(partitions));
        } else if (operation == AlterOperation.OPTIMIZE_PARTITION && partitions != null) {
            b.append("OPTIMIZE PARTITION ").append(PlainSelect.getStringList(partitions));
        } else if (operation == AlterOperation.REBUILD_PARTITION && partitions != null) {
            b.append("REBUILD PARTITION ").append(PlainSelect.getStringList(partitions));
        } else if (operation == AlterOperation.REPAIR_PARTITION && partitions != null) {
            b.append("REPAIR PARTITION ").append(PlainSelect.getStringList(partitions));
        } else if (operation == AlterOperation.REMOVE_PARTITIONING) {
            b.append("REMOVE PARTITIONING");
        } else if (operation == AlterOperation.PARTITION_BY) {
            b.append("PARTITION BY ").append(partitionType).append(" ");
            if (partitionExpression != null) {
                b.append("(").append(partitionExpression).append(") ");
            } else if (partitionColumns != null && !partitionColumns.isEmpty()) {
                b.append("COLUMNS(").append(String.join(", ", partitionColumns)).append(") ");
            }

            b.append("(").append(partitionDefinitions.stream()
                    .map(PartitionDefinition::toString)
                    .collect(Collectors.joining(", ")))
                    .append(")");
        } else {
            if (operation == AlterOperation.COMMENT_WITH_EQUAL_SIGN) {
                b.append("COMMENT =").append(" ");
            } else if (operation == AlterOperation.ENABLE_ROW_LEVEL_SECURITY) {
                b.append("ENABLE ROW LEVEL SECURITY").append(" ");
            } else if (operation == AlterOperation.DISABLE_ROW_LEVEL_SECURITY) {
                b.append("DISABLE ROW LEVEL SECURITY").append(" ");
            } else if (operation == AlterOperation.FORCE_ROW_LEVEL_SECURITY) {
                b.append("FORCE ROW LEVEL SECURITY").append(" ");
            } else if (operation == AlterOperation.NO_FORCE_ROW_LEVEL_SECURITY) {
                b.append("NO FORCE ROW LEVEL SECURITY").append(" ");
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

    public static final class ColumnSetDefault implements Serializable {
        private final String columnName;
        private final String defaultValue;

        public ColumnSetDefault(String columnName, String defaultValue) {
            this.columnName = columnName;
            this.defaultValue = defaultValue;
        }

        public String getColumnName() {
            return columnName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public String toString() {
            return columnName + " SET DEFAULT " + defaultValue;
        }
    }

    public static final class ColumnSetVisibility implements Serializable {
        private final String columnName;
        private final boolean visible;

        public ColumnSetVisibility(String columnName, boolean visible) {
            this.columnName = columnName;
            this.visible = visible;
        }

        public String getColumnName() {
            return columnName;
        }

        public boolean isVisible() {
            return visible;
        }

        @Override
        public String toString() {
            return columnName + " SET " + (visible ? " VISIBLE" : " INVISIBLE");
        }
    }

    public enum ConvertType {
        CONVERT_TO, DEFAULT_CHARACTER_SET, CHARACTER_SET
    }
}
