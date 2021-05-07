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
import java.util.List;
import java.util.Optional;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;

public class WithItem implements SelectBody {

  private String name;
  private List<SelectItem> withItemList;
  private ItemsList itemsList;
  private boolean useValues = true;
  private SubSelect subSelect;
  private boolean recursive;

  /**
   * Get the values (as VALUES (...) or SELECT)
   *
   * @return the values of the insert
   */
  public ItemsList getItemsList() {
    return itemsList;
  }

  public void setItemsList(ItemsList list) {
    itemsList = list;
  }

  public boolean isUseValues() {
    return useValues;
  }

  public void setUseValues(boolean useValues) {
    this.useValues = useValues;
  }

  public WithItem withItemsList(ItemsList itemsList) {
    this.setItemsList(itemsList);
    return this;
  }

  public <E extends ItemsList> E getItemsList(Class<E> type) {
    return type.cast(getItemsList());
  }

  public WithItem withUseValues(boolean useValues) {
    this.setUseValues(useValues);
    return this;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  public SubSelect getSubSelect() {
    return subSelect.withUseBrackets(false);
  }

  public void setSubSelect(SubSelect subSelect) {
    this.subSelect = subSelect.withUseBrackets(false);
  }

  /**
   * The {@link SelectItem}s in this WITH (for example the A,B,C in "WITH mywith (A,B,C) AS ...")
   *
   * @return a list of {@link SelectItem}s
   */
  public List<SelectItem> getWithItemList() {
    return withItemList;
  }

  public void setWithItemList(List<SelectItem> withItemList) {
    this.withItemList = withItemList;
  }

  @Override
  public String toString() {
    return (recursive ? "RECURSIVE " : "")
        + name
        + ((withItemList != null) ? " " + PlainSelect.getStringList(withItemList, true, true) : "")
        + " AS "
        + (subSelect.isUseBrackets() ? "" : "(")
        + subSelect
        + (subSelect.isUseBrackets() ? "" : ")");
  }

  @Override
  public void accept(SelectVisitor visitor) {
    visitor.visit(this);
  }

  public WithItem withName(String name) {
    this.setName(name);
    return this;
  }

  public WithItem withWithItemList(List<SelectItem> withItemList) {
    this.setWithItemList(withItemList);
    return this;
  }

  public WithItem withSubSelect(SubSelect subSelect) {
    this.setSubSelect(subSelect);
    return this;
  }

  public WithItem withRecursive(boolean recursive) {
    this.setRecursive(recursive);
    return this;
  }

  public WithItem addWithItemList(SelectItem... withItemList) {
    List<SelectItem> collection = Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
    Collections.addAll(collection, withItemList);
    return this.withWithItemList(collection);
  }

  public WithItem addWithItemList(Collection<? extends SelectItem> withItemList) {
    List<SelectItem> collection = Optional.ofNullable(getWithItemList()).orElseGet(ArrayList::new);
    collection.addAll(withItemList);
    return this.withWithItemList(collection);
  }

  public <E extends SubSelect> E getSubSelect(Class<E> type) {
    return type.cast(getSubSelect());
  }
}
