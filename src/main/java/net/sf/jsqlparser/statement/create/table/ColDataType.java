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
import java.math.BigInteger;
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

    public enum TypeModifier {
        SIGNED, UNSIGNED, ZEROFILL
    }

    public enum NationalCharacterType {
        CHAR, VARCHAR
    }

    private String dataType;
    private List<String> argumentsStringList;
    private String characterSet;
    private boolean useCharsetKeyword;
    private IntervalQualifier intervalQualifier;
    private List<Integer> arrayData = new ArrayList<Integer>();
    private Signedness signedness;
    private boolean zerofill;
    private BigInteger precision;
    private boolean maxPrecision;
    private Integer scale;
    private List<TypeModifier> typeModifiers;
    private NationalCharacterType nationalCharacterType;
    private XmlTypeModifier xmlTypeModifier;

    public ColDataType() {
        // empty constructor
    }

    public ColDataType(String dataType, int precision, int scale) {
        this(dataType);
        setNumericTypeParameters(precision < 0 ? null
                : precision == Integer.MAX_VALUE ? "MAX" : Integer.toString(precision),
                scale < 0 ? null : Integer.valueOf(scale));
    }

    /**
     * Creates a parameterized type, using {@code null} for an omitted parameter. Unlike the legacy
     * primitive constructor, this accepts negative scales, including {@code -1}. The legacy
     * {@link Integer#MAX_VALUE} precision sentinel continues to represent MAX.
     */
    public static ColDataType fromNumericParameters(String dataType, Integer precision,
            Integer scale) {
        return fromTypeParameters(dataType, precision == null ? null
                : precision == Integer.MAX_VALUE ? "MAX" : precision.toString(), scale);
    }

    /**
     * Creates a type from its numeric parameter spelling. Unlike the legacy primitive constructor,
     * a numeric 2147483647 is distinct from MAX and larger lengths are retained without narrowing.
     */
    public static ColDataType fromTypeParameters(String dataType, String precision, Integer scale) {
        ColDataType type = new ColDataType(dataType);
        type.setNumericTypeParameters(precision, scale);
        return type;
    }

    private void setNumericTypeParameters(String parameter, Integer scale) {
        if (parameter != null) {
            maxPrecision = "MAX".equalsIgnoreCase(parameter);
            precision = maxPrecision ? null : new BigInteger(parameter);
            this.scale = scale;
            dataType += " (" + (maxPrecision ? "MAX" : precision)
                    + (scale != null ? ", " + scale : "") + ")";
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

    /**
     * Returns the type name without parenthesized parameters, retaining qualification, quoting and
     * multiword names such as TIMESTAMP WITH TIME ZONE. The legacy {@link #getDataType()} spelling
     * is unchanged. Quoted parentheses are part of an identifier and are retained.
     */
    public String getBaseTypeName() {
        if (dataType == null) {
            return null;
        }
        StringBuilder name = new StringBuilder();
        int depth = 0;
        char quote = 0;
        for (int i = 0; i < dataType.length(); i++) {
            char c = dataType.charAt(i);
            if (quote != 0) {
                if (depth == 0) {
                    name.append(c);
                }
                if (c == quote) {
                    if (i + 1 < dataType.length() && dataType.charAt(i + 1) == quote) {
                        if (depth == 0) {
                            name.append(quote);
                        }
                        i++;
                    } else {
                        quote = 0;
                    }
                }
            } else if (c == '\'' || c == '"' || c == '`' || c == '[') {
                quote = c == '[' ? ']' : c;
                if (depth == 0) {
                    name.append(c);
                }
            } else if (c == '(') {
                depth++;
                while (name.length() > 0
                        && Character.isWhitespace(name.charAt(name.length() - 1))) {
                    name.setLength(name.length() - 1);
                }
            } else if (c == ')' && depth > 0) {
                depth--;
            } else if (depth == 0) {
                name.append(c);
            }
        }
        return name.toString().trim();
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

    /** Whether the character set clause uses MySQL's CHARSET abbreviation. */
    public boolean isUseCharsetKeyword() {
        return useCharsetKeyword;
    }

    public void setUseCharsetKeyword(boolean useCharsetKeyword) {
        this.useCharsetKeyword = useCharsetKeyword;
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

    /** Returns MySQL numeric modifiers in their original order, including repetitions. */
    public List<TypeModifier> getTypeModifiers() {
        return typeModifiers;
    }

    public void setTypeModifiers(List<TypeModifier> typeModifiers) {
        this.typeModifiers = typeModifiers;
        signedness = null;
        zerofill = false;
        if (typeModifiers != null) {
            for (TypeModifier modifier : typeModifiers) {
                updateEffectiveModifier(modifier);
            }
        }
    }

    public void addTypeModifier(TypeModifier typeModifier) {
        if (typeModifiers == null) {
            typeModifiers = new ArrayList<>();
        }
        typeModifiers.add(typeModifier);
        updateEffectiveModifier(typeModifier);
    }

    private void updateEffectiveModifier(TypeModifier modifier) {
        switch (modifier) {
            case SIGNED:
                signedness = Signedness.SIGNED;
                break;
            case UNSIGNED:
                signedness = Signedness.UNSIGNED;
                break;
            case ZEROFILL:
                zerofill = true;
                break;
            default:
                break;
        }
    }

    public NationalCharacterType getNationalCharacterType() {
        return nationalCharacterType;
    }

    public void setNationalCharacterType(NationalCharacterType nationalCharacterType) {
        this.nationalCharacterType = nationalCharacterType;
    }

    public boolean isNational() {
        return nationalCharacterType != null;
    }

    public XmlTypeModifier getXmlTypeModifier() {
        return xmlTypeModifier;
    }

    public void setXmlTypeModifier(XmlTypeModifier xmlTypeModifier) {
        this.xmlTypeModifier = xmlTypeModifier;
    }

    public ColDataType withXmlTypeModifier(XmlTypeModifier xmlTypeModifier) {
        setXmlTypeModifier(xmlTypeModifier);
        return this;
    }

    /**
     * The first numeric type parameter, e.g. {@code 255} for {@code VARCHAR(255)} or {@code 10} for
     * {@code DECIMAL(10, 2)}. {@code MAX} is reported as {@link Integer#MAX_VALUE}. Returns
     * {@code null} when the type carries no numeric parameters, e.g. {@code INT} or
     * {@code ENUM('a', 'b')}, or when a numeric length exceeds the integer range. Use
     * {@link #getNumericPrecision()} for the full range and {@link #isMaxPrecision()} to
     * distinguish MAX from the numeric value 2147483647.
     */
    public Integer getPrecision() {
        return maxPrecision ? Integer.valueOf(Integer.MAX_VALUE)
                : precision != null && precision.bitLength() < Integer.SIZE
                        ? Integer.valueOf(precision.intValue())
                        : null;
    }

    public void setPrecision(Integer precision) {
        setNumericPrecision(precision == null ? null : BigInteger.valueOf(precision));
    }

    /** Returns the exact numeric parameter, or null for an omitted parameter or MAX. */
    public BigInteger getNumericPrecision() {
        return precision;
    }

    /** Updates numeric metadata without changing the legacy rendered type spelling. */
    public void setNumericPrecision(BigInteger precision) {
        this.precision = precision;
        maxPrecision = false;
    }

    /** Distinguishes the MAX keyword from an equal numeric value in the legacy accessor. */
    public boolean isMaxPrecision() {
        return maxPrecision;
    }

    /**
     * The second numeric type parameter, e.g. {@code 2} for {@code DECIMAL(10, 2)} or {@code -3}
     * for PostgreSQL {@code NUMERIC(2, -3)}. Returns {@code null} when absent.
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
                + (xmlTypeModifier != null ? " " + xmlTypeModifier : "")
                + (argumentsStringList != null
                        ? " " + PlainSelect.getStringList(argumentsStringList, true, true)
                        : "")
                + (typeModifiers != null && !typeModifiers.isEmpty()
                        ? " " + PlainSelect.getStringList(typeModifiers, false, false)
                        : (signedness != null ? " " + signedness : "")
                                + (zerofill ? " ZEROFILL" : ""))
                + arraySpec.toString()
                + (characterSet != null
                        ? (useCharsetKeyword ? " CHARSET " : " CHARACTER SET ") + characterSet
                        : "");
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

    public ColDataType withTypeModifiers(List<TypeModifier> typeModifiers) {
        setTypeModifiers(typeModifiers);
        return this;
    }

    public ColDataType withNationalCharacterType(NationalCharacterType nationalCharacterType) {
        setNationalCharacterType(nationalCharacterType);
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
                && useCharsetKeyword == that.useCharsetKeyword
                && Objects.equals(intervalQualifier, that.intervalQualifier)
                && Objects.equals(arrayData, that.arrayData)
                && signedness == that.signedness
                && zerofill == that.zerofill
                && Objects.equals(typeModifiers, that.typeModifiers)
                && Objects.equals(xmlTypeModifier, that.xmlTypeModifier)
                && nationalCharacterType == that.nationalCharacterType;
    }

    @Override
    public int hashCode() {
        // Use the same per-code-point case folding as String.equalsIgnoreCase, including Unicode.
        int result = dataType.codePoints()
                .map(c -> Character.toLowerCase(Character.toUpperCase(c)))
                .reduce(0, (hash, c) -> 31 * hash + c);
        result = 31 * result + Objects.hashCode(argumentsStringList);
        result = 31 * result + Objects.hashCode(characterSet);
        result = 31 * result + Boolean.hashCode(useCharsetKeyword);
        result = 31 * result + Objects.hashCode(intervalQualifier);
        result = 31 * result + Objects.hashCode(arrayData);
        result = 31 * result + Objects.hashCode(signedness);
        result = 31 * result + Boolean.hashCode(zerofill);
        result = 31 * result + Objects.hashCode(typeModifiers);
        result = 31 * result + Objects.hashCode(nationalCharacterType);
        result = 31 * result + Objects.hashCode(xmlTypeModifier);
        return result;
    }
}
