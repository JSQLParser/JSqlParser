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

import net.sf.jsqlparser.expression.IntervalQualifier;
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

    public enum Signedness {
        SIGNED, UNSIGNED
    }

    private String dataType;
    private List<String> argumentsStringList;
    private String characterSet;
    private IntervalQualifier intervalQualifier;
    private List<Integer> arrayData = new ArrayList<Integer>();
    private Signedness signedness;
    private boolean zerofill;
    private Integer precision;
    private Integer scale;

    public ColDataType() {
        // empty constructor
    }

    public ColDataType(String dataType, int precision, int scale) {
        this.dataType = dataType;

        if (precision >= 0) {
            this.precision = precision;
            this.dataType += " (" + (precision == Integer.MAX_VALUE ? "MAX" : precision);
            if (scale >= 0) {
                this.scale = scale;
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

    public IntervalQualifier getIntervalQualifier() {
        return intervalQualifier;
    }

    public void setIntervalQualifier(IntervalQualifier intervalQualifier) {
        this.intervalQualifier = intervalQualifier;
    }

    public List<Integer> getArrayData() {
        return arrayData;
    }

    public void setArrayData(List<Integer> arrayData) {
        this.arrayData = arrayData;
    }

    public Signedness getSignedness() {
        return signedness;
    }

    public void setSignedness(Signedness signedness) {
        this.signedness = signedness;
    }

    public boolean isZerofill() {
        return zerofill;
    }

    public void setZerofill(boolean zerofill) {
        this.zerofill = zerofill;
    }

    /**
     * The first numeric type parameter, e.g. {@code 255} for {@code VARCHAR(255)} or {@code 10} for
     * {@code DECIMAL(10, 2)}. {@code MAX} is reported as {@link Integer#MAX_VALUE}. Returns
     * {@code null} when the type carries no numeric parameters, e.g. {@code INT} or
     * {@code ENUM('a', 'b')}.
     */
    public Integer getPrecision() {
        return precision;
    }

    public void setPrecision(Integer precision) {
        this.precision = precision;
    }

    /**
     * The second numeric type parameter, e.g. {@code 2} for {@code DECIMAL(10, 2)}. Returns
     * {@code null} when absent.
     */
    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
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
                + (intervalQualifier != null ? " " + intervalQualifier.toString() : "")
                + (argumentsStringList != null
                        ? " " + PlainSelect.getStringList(argumentsStringList, true, true)
                        : "")
                + (signedness != null ? " " + signedness : "")
                + (zerofill ? " ZEROFILL" : "")
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

    public ColDataType withIntervalQualifier(IntervalQualifier intervalQualifier) {
        this.setIntervalQualifier(intervalQualifier);
        return this;
    }

    public ColDataType withArrayData(List<Integer> arrayData) {
        this.setArrayData(arrayData);
        return this;
    }

    public ColDataType withSignedness(Signedness signedness) {
        setSignedness(signedness);
        return this;
    }

    public ColDataType withZerofill(boolean zerofill) {
        setZerofill(zerofill);
        return this;
    }

    public ColDataType withPrecision(Integer precision) {
        this.setPrecision(precision);
        return this;
    }

    public ColDataType withScale(Integer scale) {
        this.setScale(scale);
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
                && Objects.equals(intervalQualifier, that.intervalQualifier)
                && Objects.equals(arrayData, that.arrayData)
                && signedness == that.signedness
                && zerofill == that.zerofill;
    }

    @Override
    public int hashCode() {
        int result = dataType.hashCode();
        result = 31 * result + Objects.hashCode(argumentsStringList);
        result = 31 * result + Objects.hashCode(characterSet);
        result = 31 * result + Objects.hashCode(intervalQualifier);
        result = 31 * result + Objects.hashCode(arrayData);
        result = 31 * result + Objects.hashCode(signedness);
        result = 31 * result + Boolean.hashCode(zerofill);
        return result;
    }
}
