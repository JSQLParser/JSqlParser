*********************************
Migration to JSQLParser Version 5
*********************************

* ``ValueListExpression`` has been replaced by ``Values``, which implements ``Select`` `Statement` and `Expression`
* ``ValuesStatement`` has been replaced by ``Values``, which implements ``Select`` `Statement` and `Expression`
* ``ItemsList`` has been removed and ``ExpressionList`` is used instead
* ``MultiExpressionList`` has been removed and ``ExpressionList`` is used instead (with ``ExpressionList`` elements)
* ``SelectBody`` has been removed and `PlainSelect` can be used directly
* ``SubJoin`` has been removed, using normal ```ParenthesedFromItem`` instead
* ``SubSelect`` has been removed and any instance of ``Select`` (`PlainSelect`, `Values` or `SetOperationList`) can be used instead
*

* `hasBrackets()`, 'isUsingBrackets()' and similar methods have been removed, instead the Parser will return ``ParenthesedExpressionList`` or ``ParenthesedSelect`` or ```ParenthesedFromItem`` or ``Parenthesis`` wrapping the object within brackets

* any instance of `List<Expression>` is considered an Anti Pattern and `ExpressionList<?>` shall be used instead

* ``List<UpdateSet>`` is used for any `Set` clause within `Insert`, `Update`, `Upsert` or `Merge` statements

* ``Statements`` extends `List<Statement>` directly and so ``Statements.getStatements()`` is obsolete

