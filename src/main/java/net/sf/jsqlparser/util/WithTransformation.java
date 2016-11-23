/*
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2016 JSQLParser
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

/**
 * @author mathew joseph (MathewJoseph31)
 * 
 */
package net.sf.jsqlparser.util;

import java.util.List;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.AllComparisonExpression;
import net.sf.jsqlparser.expression.AnyComparisonExpression;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SelectExpressionItem;
import net.sf.jsqlparser.statement.select.SelectItem;
import net.sf.jsqlparser.statement.select.SetOperationList;
import net.sf.jsqlparser.statement.select.SubJoin;
import net.sf.jsqlparser.statement.select.SubSelect;
import net.sf.jsqlparser.statement.select.WithItem;

public class WithTransformation {
	SelectBody selectBody;
	java.util.logging.Logger logger=java.util.logging.Logger.getLogger(WithTransformation.class.getName());
	
	public WithTransformation(SelectBody selBody,List<WithItem> withItemsList) {
		selectBody=selBody;
		if(withItemsList==null||withItemsList.isEmpty())
			return;
		for(int i=0;i<withItemsList.size();i++){
			// normalize column names in with items eg: With A(a) as (select name from ...)
			// is normalized to With A(a) as (select name as a from ...)
			WithItem srcWithItem=normalizeWithItem(withItemsList.get(i));
			logger.info("normalized with item"+srcWithItem);
			// now translate the subsequent with items by substituting the definition 
			// of withItem under consideration in their select bodies
			for(int j=i+1;j<withItemsList.size();j++){
				WithItem tarWithItem=withItemsList.get(j);
				//withItems select body can be a PlainSelect or a SetOperation
				if(tarWithItem.getSelectBody() instanceof PlainSelect){
					PlainSelect tarSelectClause =(PlainSelect) tarWithItem.getSelectBody();
					transformPlainSelectForWithAs(srcWithItem, tarSelectClause);
				}
				else if(tarWithItem.getSelectBody() instanceof SetOperationList){
					SetOperationList setOpList=(SetOperationList)tarWithItem.getSelectBody();
					transformSetOperationListForWithAs(srcWithItem, setOpList);
				}
			}
			
			// Now  translate the select body of the input select statement  by substituting the definition 
			// of withItem under consideration in its select bodies

			//select body can be a PlainSelect or a SetOperation
			if(selectBody instanceof PlainSelect){
				PlainSelect tarSelectClause =(PlainSelect) selectBody;
				transformPlainSelectForWithAs(srcWithItem, tarSelectClause);
			}
			else if(selectBody instanceof SetOperationList){
				SetOperationList setOpList=(SetOperationList)selectBody;
				transformSetOperationListForWithAs(srcWithItem, setOpList);
			}
		}

	}
	
	/** @author mathew
	 *  Transforms subjoin statements in from items eg: (A Natural Join B)
	 *  Splits the subjoin into Left (in this case <A>), and Join ( in the above case <Natural Join B>), deal with them separately 
	 */

	SubJoin transformSubJoinForWithAs(WithItem srcWithItem, SubJoin subJoin){
		FromItem leftFromItem=subJoin.getLeft();
		//checks the left item (assumed to be a  Table), if its name is the same as the name of the 
		// input with item, the name is substitued by its corresponding definition
		if(leftFromItem instanceof net.sf.jsqlparser.schema.Table){			
			net.sf.jsqlparser.schema.Table tarTable= (net.sf.jsqlparser.schema.Table)leftFromItem;
			if(tarTable.getName().equalsIgnoreCase(srcWithItem.getName())){
				logger.info("found as from item");
				//Get withItem and create new SubSelect if fromItem name is equal to with item name
				Alias a;
				if(tarTable.getAlias()!=null)
					a=new Alias(tarTable.getAlias().getName());
				else
					a = new Alias(srcWithItem.getName());
				a.setUseAs(true);
				SubSelect sub = new SubSelect();
				sub.setSelectBody(srcWithItem.getSelectBody());
				sub.setAlias(a);
				subJoin.setLeft(sub);					
			}
		}
		//checks if the left item (assumed to be a  Table or SubJoin itself) is a SubJoin, if then recursive call
		else if(leftFromItem instanceof SubJoin){
			transformSubJoinForWithAs(srcWithItem,(SubJoin)leftFromItem);
		}
		// if fromitem is a subselect , then call the corresponding method that handles it
		else if(leftFromItem instanceof SubSelect){
			logger.info("processing subselect");
			SubSelect leftSubSelect=(SubSelect) leftFromItem;
			transformSubSelectForWithAs(srcWithItem, leftSubSelect);
		}
		// deals with the right item of the subjoin
		FromItem rightFromItem=subJoin.getJoin().getRightItem();
		//checks if the right item (assumed to be a  Table or SubJoin itself) is a Table, then if its name is the same as the name of the 
		// input with item, the name is substituted by its corresponding definition

		if(rightFromItem instanceof net.sf.jsqlparser.schema.Table){			
			net.sf.jsqlparser.schema.Table tarTable= (net.sf.jsqlparser.schema.Table)rightFromItem;
			if(tarTable.getName().equalsIgnoreCase(srcWithItem.getName())){
				logger.info("found as from item");
				//Get withItem and create new SubSelect if fromItem name is equal to with item name
				Alias a;
				if(tarTable.getAlias()!=null)
					a=new Alias(tarTable.getAlias().getName());
				else
					a = new Alias(srcWithItem.getName());
				a.setUseAs(true);
				SubSelect sub = new SubSelect();
				sub.setSelectBody(srcWithItem.getSelectBody());
				sub.setAlias(a);
				subJoin.getJoin().setRightItem(sub);					
			}
		}
		//checks if the right item (assumed to be a  Table or SubJoin itself) is a SubJoin, if then recursive call
		else if(rightFromItem instanceof SubJoin){
			transformSubJoinForWithAs(srcWithItem,(SubJoin)rightFromItem);
		}
		else if(rightFromItem instanceof SubSelect){
			logger.info("processing subselect");
			SubSelect rightSubSelect=(SubSelect) rightFromItem;
			transformSubSelectForWithAs(srcWithItem, rightSubSelect);
		}
		return subJoin;
	}

	/** @author mathew
	 *  Transforms the given input PlainSelect statement by substituting  alias name of its first argument
	 *   with its corresponding definition in its select body
	 * 
	 */

	PlainSelect transformPlainSelectForWithAs(WithItem srcWithItem, PlainSelect tarSelectClause){
		// Starts by dealing with from items, a from item can be a Table name, a subselect statement, or a subjoin statement
		FromItem tarFromItem=tarSelectClause.getFromItem();
		// if fromitem is a Table then check if its name is equal to the name of the input withitem, if yes substitutes its occurence
		// by its definition
		if(tarFromItem instanceof net.sf.jsqlparser.schema.Table){
			net.sf.jsqlparser.schema.Table tarTable= (net.sf.jsqlparser.schema.Table)tarFromItem;
			if(tarTable.getName().equalsIgnoreCase(srcWithItem.getName())){
				logger.info("found as from item");
				//Get withItem and create new SubSelect if fromItem name is equal to with item name
				Alias a;
				if(tarTable.getAlias()!=null)
					a=new Alias(tarTable.getAlias().getName());
				else
					a = new Alias(srcWithItem.getName());
				a.setUseAs(true);
				SubSelect sub = new SubSelect();
				sub.setSelectBody(srcWithItem.getSelectBody());
				sub.setAlias(a);
				tarSelectClause.setFromItem(sub);					
			}	
		}
		// if fromitem is a subselect , then call the corresponding method that handles it
		else if(tarFromItem instanceof SubSelect){
			logger.info("processing subselect");
			SubSelect tarSubSelect=(SubSelect) tarFromItem;
			transformSubSelectForWithAs(srcWithItem, tarSubSelect);
		}
		// if fromitem is a subjoin, then call the corresponding method that handles it
		else if(tarFromItem instanceof SubJoin){
			logger.info("processing subjoin");
			SubJoin tarSubJoin=(SubJoin)tarFromItem;
			tarSubJoin=transformSubJoinForWithAs(srcWithItem,tarSubJoin);
		}
	
		// WITH AS names can be in joins as well, call the corresponding method that handles it
		if(tarSelectClause.getJoins() != null ){
			logger.info("processing joins");
			List<Join> joinList = tarSelectClause.getJoins();
			transformJoinsForWithAs(srcWithItem, joinList);

		}
		// Now deal with its where clauses
		transformWhereClauseForWithAs(srcWithItem, tarSelectClause.getWhere());
		return tarSelectClause;
	}
	
	/** @author mathew  
	 *  Extract the select statement from the subselect. It can be either a PlainSelect 
	 *  or a SetOperation, deal with their transformations separately
	 */

	private void transformSubSelectForWithAs(WithItem srcWithItem, SubSelect tarSubSelect) {
		//when the select body is a plain select statement, then call the corresponding method that handles it
		if(tarSubSelect.getSelectBody() instanceof PlainSelect){
			PlainSelect tempSelect=(PlainSelect)tarSubSelect.getSelectBody();
			transformPlainSelectForWithAs(srcWithItem, tempSelect);
		}
		//where the select body is a set operation (union, intersect etc), then call the corresponding method that handles it
		else if(tarSubSelect.getSelectBody() instanceof SetOperationList){
			SetOperationList setOpList=(SetOperationList)tarSubSelect.getSelectBody();
			transformSetOperationListForWithAs(srcWithItem, setOpList);		
		}

	}
	
	/** @author mathew
	 *  Transforms set operation statements eg: (A Union (B INTERSECT C)
	 *  Splits the operands and deals with them separately 
	 */

	private void transformSetOperationListForWithAs(WithItem srcWithItem, SetOperationList setOpList) {
		// TODO Auto-generated method stub
		for(SelectBody selBody:setOpList.getSelects()){
			if(selBody instanceof PlainSelect){
				PlainSelect tarSelectClause=(PlainSelect) selBody;
				transformPlainSelectForWithAs(srcWithItem, tarSelectClause);
			}
			else if(selBody instanceof SetOperationList){
				transformSetOperationListForWithAs(srcWithItem, (SetOperationList)selBody);
			}
		}

	}

	/** @author mathew
	 *  Transforms joinList in the from clause of a select statement by looking for occurences 
	 *  of the name of the input with item, if any occurence is found its corresponding definition is substituted as a subselect
	 *  Each join item is assumed to be a table, a subselect, or a subjoin
	 */
	private void transformJoinsForWithAs(WithItem srcWithItem, List<Join> joinList){
		for(int k=0; k < joinList.size(); k++){
			Join jcl = joinList.get(k);	
			FromItem tarJoinFromItem=jcl.getRightItem();				
			//if the join item is a table, then call the corresponding method that handles it
			if(tarJoinFromItem instanceof net.sf.jsqlparser.schema.Table){
				net.sf.jsqlparser.schema.Table tarTable= (net.sf.jsqlparser.schema.Table)tarJoinFromItem;
				if(tarTable.getName().equalsIgnoreCase(srcWithItem.getName())){
					logger.info("found with alias in from item of join");
					//Get withItem and create new SubSelect if fromItem name is equal to with item name
					Alias a;
					if(tarTable.getAlias()!=null)
						a=new Alias(tarTable.getAlias().getName());
					else
						a = new Alias(srcWithItem.getName());
					a.setUseAs(true);
					SubSelect sub = new SubSelect();
					sub.setSelectBody(srcWithItem.getSelectBody());
					sub.setAlias(a);
					jcl.setRightItem(sub);
				}	
			}
			//if join item is a subselect, then call the corresponding method that handles it
			else if(tarJoinFromItem instanceof SubSelect){
				logger.info("processing subselect in join");
				SubSelect tarSubSelect=(SubSelect) tarJoinFromItem;
				PlainSelect tempSelect=(PlainSelect)tarSubSelect.getSelectBody();
				transformPlainSelectForWithAs(srcWithItem, tempSelect);
			}
			//if join item is a subjoin, then call the corresponding method that handles it
			else if(tarJoinFromItem instanceof SubJoin){
				logger.info("processing subjoin in join");
				SubJoin tarSubJoin=(SubJoin)tarJoinFromItem;
				tarSubJoin=transformSubJoinForWithAs(srcWithItem,tarSubJoin);
			}

		}
	}
	
	/** @author mathew
	 *  Transforms the expression in the where clause of a select statement by looking for occurences 
	 *  of the name of the input with item, if any occurence is found its corresponding definition is substituted as a subselect
	 */
	private void transformWhereClauseForWithAs(WithItem srcWithItem, Expression whereClause) {
		//if whereClause is a subselect, then call the corresponding method that handles it
		if(whereClause instanceof SubSelect){
			SubSelect subSelect=(SubSelect) whereClause;
			transformPlainSelectForWithAs(srcWithItem, (PlainSelect)(subSelect.getSelectBody()));
		}
		//if whereClause is a BinaryExpression (AND, OR etc.), then 
		//split its operands and recursive calls using them as arguments 
		if(whereClause instanceof BinaryExpression){
			BinaryExpression binExpression = ((BinaryExpression) whereClause);
			transformWhereClauseForWithAs(srcWithItem, binExpression.getLeftExpression());
			transformWhereClauseForWithAs(srcWithItem, binExpression.getRightExpression());
		}
		//if whereClause is a Exists Expression, then recurisively call using its right expression as argument
		else if(whereClause instanceof ExistsExpression){
			logger.info("transforming exists in where clause");
			ExistsExpression existsExpression = (ExistsExpression)whereClause;
			transformWhereClauseForWithAs(srcWithItem,existsExpression.getRightExpression());
		}
		//if whereClause is a InExpression, then handle its left and right operands separately
		else if(whereClause instanceof InExpression){
			logger.info("transforming inExpression in where clause");
			InExpression inExpression = (InExpression)whereClause;
			if (inExpression.getLeftItemsList() instanceof SubSelect){
				transformWhereClauseForWithAs(srcWithItem, (SubSelect)inExpression.getLeftItemsList());
			}
			if(inExpression.getRightItemsList() instanceof SubSelect){
				transformWhereClauseForWithAs(srcWithItem, (SubSelect)inExpression.getRightItemsList());
			}
		}
		//if whereClause is a All comparision Expression, then recurisively call using its subselect as argument
		else if(whereClause instanceof AllComparisonExpression){
			logger.info("transforming all comparison in where clause");
			AllComparisonExpression ace = (AllComparisonExpression) whereClause;
			transformPlainSelectForWithAs(srcWithItem, (PlainSelect)(ace.getSubSelect().getSelectBody()));
		}
		//if whereClause is a Any comparision Expression, then recurisively call using its subselect as argument
		else if(whereClause instanceof AnyComparisonExpression){
			logger.info("transforming any comparison in where clause");
			AnyComparisonExpression ace = (AnyComparisonExpression) whereClause;
			transformPlainSelectForWithAs(srcWithItem, (PlainSelect)(ace.getSubSelect().getSelectBody()));
		}

	}
	
	
	
	/** @author mathew  
	 * 	 normalize column names in with items eg: With A(a) as (select name from ...)			
	 *  is normalized to With A(a) as (select name as a from ...)
	 */
	private WithItem normalizeWithItem(WithItem withItem) {
		// TODO Auto-generated method stub	
		
		if(withItem.getSelectBody() instanceof PlainSelect){
			PlainSelect selectClause =(PlainSelect) withItem.getSelectBody();
			normalizeSelectedColumnsForWithItem(withItem, selectClause);
			
		}
		if(withItem.getSelectBody() instanceof SetOperationList){
			SetOperationList setOpList=(SetOperationList)withItem.getSelectBody();
			normalizeSelectedColumnsForWithItem(withItem, setOpList);

		}
		logger.info(" normalized with item "+withItem.getName()+" withItemBody: "+withItem.getSelectBody());
		return withItem;
	}
	
	
	/** @author mathew  
	 * 	 deals with normalization of Setoperation Queries. Eg. A, B, C are normalized individually 
	 *  in a select query of the form A UNION B UNION C
	 */
	private void normalizeSelectedColumnsForWithItem(WithItem withItem, SetOperationList setOpList) {
		for(SelectBody selBody:setOpList.getSelects()){
			if(selBody instanceof PlainSelect){
			PlainSelect selectClause=(PlainSelect) selBody;
			normalizeSelectedColumnsForWithItem(withItem, selectClause);
			}
			else if(selBody instanceof SetOperationList){
				normalizeSelectedColumnsForWithItem(withItem, (SetOperationList)selBody);
			}
		}
	}

	/** @author mathew  
	 * 	 deals with normalization of columns items in PlainSelect Queries. eg: With A(a) as (select name from ...)			
	 *  is normalized to With A(a) as (select name as a from ...)
	 *  
	 */
	private void normalizeSelectedColumnsForWithItem(WithItem withItem, PlainSelect selectClause) {
		// TODO Auto-generated method stub
		if(withItem.getWithItemList()!=null &&!withItem.getWithItemList().isEmpty() ){
			for(int i=0;i<withItem.getWithItemList().size();i++){
				SelectItem withSelItem=withItem.getWithItemList().get(i);
				if(selectClause.getSelectItems()!=null &&  !selectClause.getSelectItems().isEmpty() && selectClause.getSelectItems().size()>i){
					SelectItem sItem=selectClause.getSelectItems().get(i);
					if(sItem instanceof SelectExpressionItem){
						SelectExpressionItem selExpItem=(SelectExpressionItem)sItem;
						Alias a = new Alias(withSelItem.toString());
						a.setUseAs(true);
						selExpItem.setAlias(a);
					}
				}
			}
		}
	}
	
	public SelectBody getSelectBody(){
		return selectBody;
	}


}
