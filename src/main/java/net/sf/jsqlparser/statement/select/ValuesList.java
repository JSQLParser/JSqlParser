/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2019 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.statement.select;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;

@SuppressWarnings({"PMD.UncommentedEmptyMethodBody"})
public class ValuesList implements FromItem {

  private Alias alias;
  private MultiExpressionList multiExpressionList;
    private boolean noBrackets = false;
    private List<String> columnNames;

    public ValuesList() {
  }

  public ValuesList(MultiExpressionList multiExpressionList) {
    this.multiExpressionList = multiExpressionList;
  }

  @Override
  public void accept(FromItemVisitor fromItemVisitor) {
    fromItemVisitor.visit(this);
  }

  @Override
  public Alias getAlias() {
    return alias;
  }

  @Override
  public void setAlias(Alias alias) {
    this.alias = alias;
  }

  @Override
  public Pivot getPivot() {
    return null;
  }

  @Override
    public void setPivot(Pivot pivot) {
    }

  @Override
  public UnPivot getUnPivot() {
    return null;
  }

  @Override
    public void setUnPivot(UnPivot unpivot) {
    }

  public MultiExpressionList getMultiExpressionList() {
    return multiExpressionList;
  }

  public void setMultiExpressionList(MultiExpressionList multiExpressionList) {
    this.multiExpressionList = multiExpressionList;
  }

  public boolean isNoBrackets() {
        return noBrackets;
  }

  public void setNoBrackets(boolean noBrackets) {
        this.noBrackets = noBrackets;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();

    b.append("(VALUES ");
        for (Iterator<ExpressionList> it = getMultiExpressionList().getExprList().iterator(); it.
                hasNext();) {
      b.append(PlainSelect.getStringList(it.next().getExpressions(), true, !isNoBrackets()));
      if (it.hasNext()) {
        b.append(", ");
      }
    }
    b.append(")");
    if (alias != null) {
      b.append(alias.toString());

      if (columnNames != null) {
        b.append("(");
                for (Iterator<String> it = columnNames.iterator(); it.hasNext();) {
          b.append(it.next());
          if (it.hasNext()) {
            b.append(", ");
          }
        }
        b.append(")");
      }
    }
    return b.toString();
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public void setColumnNames(List<String> columnNames) {
    this.columnNames = columnNames;
  }

  @Override
  public ValuesList withAlias(Alias alias) {
    return (ValuesList) FromItem.super.withAlias(alias);
  }

  @Override
  public ValuesList withPivot(Pivot pivot) {
    return (ValuesList) FromItem.super.withPivot(pivot);
  }

  @Override
  public ValuesList withUnPivot(UnPivot unpivot) {
    return (ValuesList) FromItem.super.withUnPivot(unpivot);
  }

  public ValuesList withMultiExpressionList(MultiExpressionList multiExpressionList) {
    this.setMultiExpressionList(multiExpressionList);
    return this;
  }

  public ValuesList withNoBrackets(boolean noBrackets) {
    this.setNoBrackets(noBrackets);
    return this;
  }
  
  public ValuesList withColumnNames(List<String> columnNames) {
    this.setColumnNames(columnNames);
    return this;
  }

  public ValuesList addColumnNames(String... columnNames) {
    List<String> collection = Optional.ofNullable(getColumnNames()).orElseGet(ArrayList::new);
    Collections.addAll(collection, columnNames);
    return this.withColumnNames(collection);
  }

  public ValuesList addColumnNames(Collection<String> columnNames) {
    List<String> collection = Optional.ofNullable(getColumnNames()).orElseGet(ArrayList::new);
    collection.addAll(columnNames);
    return this.withColumnNames(collection);
  }
}
