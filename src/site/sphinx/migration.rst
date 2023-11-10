*********************************
Migration to 4.7
*********************************

The new version of JSQLParser 4.7 is a rewrite in order to simplify accessing the SQL's Abstract Syntax Tree (AST). Quite a few redundant classes have been removed or merged.

As always, such a major improvement comes at a certain cost, which is breaking the previous API. Following the guidance below, the new API can be adopted easily although you are welcome to lodge a support request when any questions or concerns arise.

`Values` Clause
---------------------------------
The ``ValueListExpression`` has been replaced by ``Values``, which implements ``Select`` `Statement` and `Expression`.

The ``ValuesStatement`` has been replaced by ``Values``, which implements ``Select`` `Statement` and `Expression`.

.. tab:: Statement

    .. code-block:: SQL

        VALUES ( 1, 2, 3 )
        ;


    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.Values
            └─ParenthesedExpressionList: (1, 2, 3)


    .. code-block:: JAVA

        Values values = (Values) CCJSqlParserUtil.parse(sqlStr);
        assertEquals( 3, values.getExpressions().size() );


.. tab:: Sub-query

    .. code-block:: SQL

        SELECT *
        FROM ( VALUES 1, 2, 3 )
        ;


    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.PlainSelect
            ├─selectItems: statement.select.SelectItem
            │  └─AllColumns: *
            └─fromItem: statement.select.ParenthesedSelect
               └─select: statement.select.Values
                  └─ExpressionList: 1, 2, 3


    .. code-block:: JAVA

        PlainSelect select = (PlainSelect)  CCJSqlParserUtil.parse(sqlStr);
        ParenthesedSelect subSelect = (ParenthesedSelect) select.getFromItem();
        Values values = (Values) subSelect.getSelect();
        assertEquals( 3, values.getExpressions().size() );


.. tab:: Expression

    .. code-block:: SQL

        UPDATE test
        SET (   a
                , b
                , c ) = ( VALUES 1, 2, 3 )
        ;


    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.update.Update
            ├─Table: test
            └─updateSets: statement.update.UpdateSet
               ├─ParenthesedExpressionList: (a, b, c)
               └─ExpressionList: (VALUES 1, 2, 3)


    .. code-block:: JAVA

        Update update = (Update)  CCJSqlParserUtil.parse(sqlStr);
        UpdateSet updateSet = update.getUpdateSets().get(0);
        ParenthesedSelect subSelect = (ParenthesedSelect) updateSet.getValues().get(0);
        Values values = (Values) subSelect.getSelect();
        assertEquals( 3, values.getExpressions().size() );


.. tab:: Clause

    .. code-block:: SQL

        INSERT INTO test
        VALUES ( 1, 2, 3 )
        ;

    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.insert.Insert
            ├─Table: test
            └─select: statement.select.Values
               └─ParenthesedExpressionList: (1, 2, 3)


    .. code-block:: JAVA

        Insert insert = (Insert)  CCJSqlParserUtil.parse(sqlStr);
        Values values = (Values) insert.getSelect();
        Assertions.assertEquals(3, values.getExpressions().size());


`Expression` Lists
---------------------------------

The class ``ExpressionList`` directly extends ``List<Expression>`` directly and so ``ExpressionList.getExpressions()`` is obsolete.

Any instance of `List<Expression>` is considered an Anti Pattern and the class ``ExpressionList<T extends Expression>`` shall be used instead.

``ItemsList`` has been removed and ``ExpressionList`` is used instead.

``MultiExpressionList`` has been removed and ``ExpressionList`` is used instead (with ``ExpressionList`` elements).

.. tab:: ExpressionList

    .. code-block:: SQL

        SELECT Function( a, b, c )
        FROM dual
        GROUP BY    a
                    , b
                    , c
        ;


    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.PlainSelect
            ├─selectItems: statement.select.SelectItem
            │  └─expression: expression.Function
            │     └─ExpressionList: a, b, c
            ├─Table: dual
            └─groupBy: statement.select.GroupByElement
               └─ExpressionList: a, b, c


    .. code-block:: JAVA

        PlainSelect select = (PlainSelect)  CCJSqlParserUtil.parse(sqlStr);
        Function function = (Function) select.getSelectItem(0).getExpression();
        assertEquals(3, function.getParameters().size());

        ExpressionList<?> groupByExpressions=select.getGroupBy().getGroupByExpressionList();
        assertEquals(3, groupByExpressions.size());


.. tab:: Wrapped ExpressionList

    .. code-block:: SQL

        SELECT ( ( 1, 2, 3 ), ( 4, 5, 6 ), ( 7, 8, 9 ) )
        ;


    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.PlainSelect
            └─selectItems: statement.select.SelectItem
               └─ParenthesedExpressionList: ((1, 2, 3), (4, 5, 6), (7, 8, 9))


    .. code-block:: JAVA

        PlainSelect select = (PlainSelect)  CCJSqlParserUtil.parse(sqlStr);
        ParenthesedExpressionList<?> expressionList = (ParenthesedExpressionList<?>) select.getSelectItem(0).getExpression();

        ParenthesedExpressionList<?> expressionList1 = (ParenthesedExpressionList<?>) expressionList.get(0);
        assertEquals(3, expressionList1.size());


Generic `SelectItem`
---------------------------------

The class ``SelectItem<T extends Expression>`` is now generic and various derivatives (e. |_| g. ``SelectExpressionItem``, ``FunctionItem``, ``ExpressionListItem``) have been removed.


`Select` Statement
---------------------------------

``SelectBody`` has been removed and `PlainSelect` can be used directly

``SubJoin`` has been replaced by `ParenthesedFromItem`` (implementing a ``FromItem`` with a regular list of ``Join``)

``SubSelect`` has been removed and any instance of ``Select`` (`PlainSelect`, `Values` or `SetOperationList`) can be used instead

.. tab:: Select

    .. code-block:: SQL

        (
            SELECT *
            FROM (  SELECT 1 )
            UNION ALL
            SELECT *
            FROM ( VALUES 1, 2, 3 )
            UNION ALL
            VALUES ( 1, 2, 3 ) )
        ;

    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.ParenthesedSelect
            └─select: statement.select.SetOperationList
               ├─selects: statement.select.PlainSelect
               │  ├─selectItems: statement.select.SelectItem
               │  │  └─AllColumns: *
               │  └─fromItem: statement.select.ParenthesedSelect
               │     └─select: statement.select.PlainSelect
               │        └─selectItems: statement.select.SelectItem
               │           └─LongValue: 1
               ├─selects: statement.select.PlainSelect
               │  ├─selectItems: statement.select.SelectItem
               │  │  └─AllColumns: *
               │  └─fromItem: statement.select.ParenthesedSelect
               │     └─select: statement.select.Values
               │        └─ExpressionList: 1, 2, 3
               ├─selects: statement.select.Values
               │  └─ParenthesedExpressionList: (1, 2, 3)
               ├─UnionOp: UNION ALL
               └─UnionOp: UNION ALL


    .. code-block:: JAVA

        ParenthesedSelect parenthesedSelect = (ParenthesedSelect)  CCJSqlParserUtil.parse(sqlStr);
        SetOperationList setOperationList = parenthesedSelect.getSetOperationList();

        PlainSelect select1 = (PlainSelect) setOperationList.getSelect(0);
        PlainSelect subSelect1 = ((ParenthesedSelect) select1.getFromItem()).getPlainSelect();
        Assertions.assertEquals( 1L, subSelect1.getSelectItem(0).getExpression(LongValue.class).getValue());

        Values values = (Values) setOperationList.getSelect(2);
        Assertions.assertEquals( 3, values.getExpressions().size());



.. tab:: Join

    .. code-block:: SQL

        SELECT *
        FROM a
          INNER JOIN (  b
                          LEFT JOIN c
                            ON b.d = c.d )
            ON a.e = b.e
        ;

    .. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.PlainSelect
            ├─selectItems: statement.select.SelectItem
            │  └─AllColumns: *
            ├─Table: a
            └─joins: statement.select.Join
               ├─rightItem: statement.select.ParenthesedFromItem
               │  ├─Table: b
               │  └─joins: statement.select.Join
               │     ├─Table: c
               │     └─onExpressions: expression.operators.relational.EqualsTo
               │        ├─Column: b.d
               │        └─Column: c.d
               └─onExpressions: expression.operators.relational.EqualsTo
                  ├─Column: a.e
                  └─Column: b.e


    .. code-block:: JAVA

        PlainSelect select = (PlainSelect)  CCJSqlParserUtil.parse(sqlStr);
        Table aTable = (Table) select.getFromItem();

        ParenthesedFromItem fromItem = (ParenthesedFromItem) select.getJoin(0).getFromItem();
        Table bTable = (Table) fromItem.getFromItem();

        Join join = fromItem.getJoin(0);
        Table cTable = (Table) join.getFromItem();

        assertEquals("c", cTable.getName());


Brackets
---------------------------------

Any `hasBrackets()`, `isUsingBrackets()` and similar methods have been removed; instead the Parser will return a ``ParenthesedExpressionList`` or ``ParenthesedSelect`` or ``ParenthesedFromItem`` or ``Parenthesis`` wrapping the object within brackets.

This allows for much better bracket handling.

.. code-block:: SQL
        :caption: `Parenthesis` and Brackets example

        ( SELECT ( 1 ) )
        ;


.. code-block:: TEXT

        SQL Text
         └─Statements: statement.select.ParenthesedSelect
            └─select: statement.select.PlainSelect
               └─selectItems: statement.select.SelectItem
                  └─expression: expression.Parenthesis
                     └─LongValue: 1


.. code-block:: JAVA

        ParenthesedSelect parenthesedSelect = (ParenthesedSelect)  CCJSqlParserUtil.parse(sqlStr);
        SetOperationList setOperationList = parenthesedSelect.getSetOperationList();

        PlainSelect select1 = (PlainSelect) setOperationList.getSelect(0);
        PlainSelect subSelect1 = ((ParenthesedSelect) select1.getFromItem()).getPlainSelect();
        Assertions.assertEquals( 1L, subSelect1.getSelectItem(0).getExpression(LongValue.class).getValue());

        Values values = (Values) setOperationList.getSelect(2);
        Assertions.assertEquals( 3, values.getExpressions().size());



`UpdateSet` clause
---------------------------------

A ``List<UpdateSet>`` is used for any `Set` clause within `Insert`, `Update`, `Upsert` or `Merge` statements.


.. code-block:: SQL
        :caption: `UpdateSet` example

        UPDATE a
        SET (   a
                , b
                , c ) = (   1
                            , 2
                            , 3 )
            , d = 4
        ;


.. code-block:: TEXT

        SQL Text
         └─Statements: statement.update.Update
            ├─Table: a
            ├─updateSets: statement.update.UpdateSet
            │  ├─ParenthesedExpressionList: (a, b, c)
            │  └─ParenthesedExpressionList: (1, 2, 3)
            └─updateSets: statement.update.UpdateSet
               ├─ExpressionList: d
               └─ExpressionList: 4


.. code-block:: JAVA

        Update update = (Update)  CCJSqlParserUtil.parse(sqlStr);
        UpdateSet updateSet1 = update.getUpdateSet(0);
        Assertions.assertEquals( 3, updateSet1.getColumns().size());
        Assertions.assertEquals( 3, updateSet1.getValues().size());

        UpdateSet updateSet2 = update.getUpdateSet(1);
        Assertions.assertEquals( "d", updateSet2.getColumn(0).getColumnName());
        Assertions.assertEquals( 4L,  ((LongValue) updateSet2.getValue(0)).getValue() );


`Statements` collection
---------------------------------

The ``Statements`` class extends `List<Statement>` directly and so ``Statements.getStatements()`` is obsolete.

