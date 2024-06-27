/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.create.table;

import net.sf.jsqlparser.statement.select.PlainSelect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

public class ColDataType implements Serializable {

    private String dataType;
    private List<String> argumentsStringList;
    private String characterSet;
    private List<Integer> arrayData = new ArrayList<Integer>();

    public ColDataType() {
        // empty constructor
    }

    public ColDataType(String dataType, int precision, int scale) {
        this.dataType = dataType;

        if (precision >= 0) {
            this.dataType += " (" + (precision == Integer.MAX_VALUE ? "MAX" : precision);
            if (scale >= 0) {
                this.dataType += ", " + scale;
            }
            this.dataType += ")";
        }
    }

    public ColDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<String> getArgumentsStringList() {
        return argumentsStringList;
    }

    public void setArgumentsStringList(List<String> list) {
        argumentsStringList = list;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String string) {
        dataType = string;
    }

    public void setDataType(List<String> list) {
        dataType = list.stream().collect(joining("."));
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public List<Integer> getArrayData() {
        return arrayData;
    }

    public void setArrayData(List<Integer> arrayData) {
        this.arrayData = arrayData;
    }

    @Override
    public String toString() {
        StringBuilder arraySpec = new StringBuilder();
        for (Integer item : arrayData) {
            arraySpec.append("[");
            if (item != null) {
                arraySpec.append(item);
            }
            arraySpec.append("]");
        }
        return dataType
                + (argumentsStringList != null
                        ? " " + PlainSelect.getStringList(argumentsStringList, true, true)
                        : "")
                + arraySpec.toString()
                + (characterSet != null ? " CHARACTER SET " + characterSet : "");
    }

    public ColDataType withDataType(String dataType) {
        this.setDataType(dataType);
        return this;
    }

    public ColDataType withArgumentsStringList(List<String> argumentsStringList) {
        this.setArgumentsStringList(argumentsStringList);
        return this;
    }

    public ColDataType withCharacterSet(String characterSet) {
        this.setCharacterSet(characterSet);
        return this;
    }

    public ColDataType withArrayData(List<Integer> arrayData) {
        this.setArrayData(arrayData);
        return this;
    }

    public ColDataType addArgumentsStringList(String... argumentsStringList) {
        List<String> collection =
                Optional.ofNullable(getArgumentsStringList()).orElseGet(ArrayList::new);
        Collections.addAll(collection, argumentsStringList);
        return this.withArgumentsStringList(collection);
    }

    public ColDataType addArgumentsStringList(Collection<String> argumentsStringList) {
        List<String> collection =
                Optional.ofNullable(getArgumentsStringList()).orElseGet(ArrayList::new);
        collection.addAll(argumentsStringList);
        return this.withArgumentsStringList(collection);
    }

    public ColDataType addArrayData(Integer... arrayData) {
        List<Integer> collection = Optional.ofNullable(getArrayData()).orElseGet(ArrayList::new);
        Collections.addAll(collection, arrayData);
        return this.withArrayData(collection);
    }

    public ColDataType addArrayData(Collection<Integer> arrayData) {
        List<Integer> collection = Optional.ofNullable(getArrayData()).orElseGet(ArrayList::new);
        collection.addAll(arrayData);
        return this.withArrayData(collection);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ColDataType)) {
            return false;
        }

        ColDataType that = (ColDataType) o;
        return dataType.equalsIgnoreCase(that.dataType)
                && Objects.equals(argumentsStringList, that.argumentsStringList)
                && Objects.equals(characterSet, that.characterSet)
                && Objects.equals(arrayData, that.arrayData);
    }

    @Override
    public int hashCode() {
        int result = dataType.hashCode();
        result = 31 * result + Objects.hashCode(argumentsStringList);
        result = 31 * result + Objects.hashCode(characterSet);
        result = 31 * result + Objects.hashCode(arrayData);
        return result;
    }
}
