*********************************
Migration to 5.0
*********************************

`Values` Clause
---------------------------------
The ``ValueListExpression`` has been replaced by ``Values``, which implements ``Select`` `Statement` and `Expression`.

The ``ValuesStatement`` has been replaced by ``Values``, which implements ``Select`` `Statement` and `Expression`.

.. tab:: Statement

    .. code-block:: SQL
        :caption: `VALUES` examples

        VALUES ( 1, 2, 3 )
        ;


    .. code-block:: TEXT
        :caption: AST for the `VALUES` examples

        SQL Text
         └─Statements: statement.select.Values
            └─ParenthesedExpressionList: (1, 2, 3)

    .. raw:: html

        <pre>
        SQL Text
         └─<span style="color: #000080;">Statements</span>: <span style="color: #808000;">statement.select.PlainSelect</span>
            ├─<span style="color: #000080;">selectItems</span>: <span style="color: #808000;">statement.select.SelectItem</span>
            │  └─<span style="color: #000080;">AllColumns</span>: <span style="color: #808000;">*</span>
            ├─<span style="color: #000080;">Table</span>: <span style="color: #808000;">cfe.test</span>
            └─<span style="color: #000080;">where</span>: <span style="color: #808000;">expression.operators.relational.EqualsTo</span>
               ├─<span style="color: #000080;">Column</span>: <span style="color: #808000;">a</span>
               └─<span style="color: #000080;">Column</span>: <span style="color: #808000;">b</span>

        </pre>



.. tab:: Sub-query

    .. code-block:: SQL
        :caption: `VALUES` examples

        SELECT *
        FROM ( VALUES 1, 2, 3 )
        ;


    .. code-block:: TEXT
        :caption: AST for the `VALUES` examples

        SQL Text
         └─Statements: statement.select.PlainSelect
            ├─selectItems: statement.select.SelectItem
            │  └─AllColumns: *
            └─fromItem: statement.select.ParenthesedSelect
               └─select: statement.select.Values
                  └─ExpressionList: 1, 2, 3

.. tab:: Expression

    .. code-block:: SQL
        :caption: `VALUES` examples

        UPDATE test
        SET (   a
                , b
                , c ) = ( VALUES 1, 2, 3 )
        ;


    .. code-block:: TEXT
        :caption: AST for the `VALUES` examples

        SQL Text
         └─Statements: statement.update.Update
            ├─Table: test
            └─updateSets: statement.update.UpdateSet
               ├─ParenthesedExpressionList: (a, b, c)
               └─ExpressionList: (VALUES 1, 2, 3)

.. tab:: Clause

    .. code-block:: SQL
        :caption: `VALUES` examples

        INSERT INTO test
        VALUES ( 1, 2, 3 )
        ;

    .. code-block:: TEXT
        :caption: AST for the `VALUES` examples

        SQL Text
         └─Statements: statement.insert.Insert
            ├─Table: test
            └─select: statement.select.Values
               └─ParenthesedExpressionList: (1, 2, 3)

`Expression` Lists
---------------------------------

The class ``ExpressionList`` extends ``List<Expression>`` directly and so ``ExpressionList.getExpressions()`` is obsolete.

Any instance of `List<Expression>` is considered an Anti Pattern and the class ``ExpressionList<T extends Expression>`` shall be used instead.

``ItemsList`` has been removed and ``ExpressionList`` is used instead.

``MultiExpressionList`` has been removed and ``ExpressionList`` is used instead (with ``ExpressionList`` elements).

Generic `SelectItem`
---------------------------------

The class ``SelectItem<T extends Expression>`` is now generic and various derivatives (e. |_| g. ``SelectExpressionItem``, ``FunctionItem``, ``ExpressionListItem``) have been removed.


`Select` Statement
---------------------------------

``SelectBody`` has been removed and `PlainSelect` can be used directly

``SubJoin`` has been replaced by `ParenthesedFromItem`` (implementing a ``FromItem`` with a regular list of ``Join``)

``SubSelect`` has been removed and any instance of ``Select`` (`PlainSelect`, `Values` or `SetOperationList`) can be used instead

Brackets
---------------------------------

Any `hasBrackets()`, `isUsingBrackets()` and similar methods have been removed; instead the Parser will return a ``ParenthesedExpressionList`` or ``ParenthesedSelect`` or ```ParenthesedFromItem`` or ``Parenthesis`` wrapping the object within brackets.

This allows for much better bracket handling.

`UpdateSet` clause
---------------------------------

A ``List<UpdateSet>`` is used for any `Set` clause within `Insert`, `Update`, `Upsert` or `Merge` statements.

`Statements` collection
---------------------------------

The ``Statements`` class extends `List<Statement>` directly and so ``Statements.getStatements()`` is obsolete.

