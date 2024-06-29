*********************************
Migration to 5.0
*********************************

The new JSQLParser 5 introduces API-breaking changes:

1. **Dependency on Java 11 or newer**

2. **Reworked AST Visitors**

   The AST Visitors have been reworked to pass a Generic Context from the Root Node down to the Leaves.

   .. code-block:: java
      :caption: Generic Interface

      public interface SelectVisitor<T> {

          <S> T visit(PlainSelect plainSelect, S context);

          default void visit(PlainSelect plainSelect) {
              this.visit(plainSelect, null);
          }

      }

   .. code-block:: java
      :caption: Sample Implementation

      public class JSQLColumnResolver
          implements SelectVisitor<JdbcResultSetMetaData>, FromItemVisitor<JdbcResultSetMetaData> {

          @Override
          public <S> JdbcResultSetMetaData visit(PlainSelect select, S context) {
              if (context instanceof JdbcMetaData) {
                  return visit(select, (JdbcMetaData) context);
              }
              return null;
          }

          public JdbcResultSetMetaData visit(PlainSelect select, JdbcMetaData metaData) {
              JdbcResultSetMetaData resultSetMetaData = new JdbcResultSetMetaData();

              // Logic to retrieve the column information
              resultSetMetaData = getColumn(metaData, select.getFromItem(), select.getJoins());

              return resultSetMetaData;
          }
      }

3. **Generic Result from Leaves to Root**

   Node objects now return a Generic Result from the Leaves up to the Root.

   .. code-block:: java
      :caption: AST Node

      public class PlainSelect extends Select {
          @Override
          public <T, S> T accept(SelectVisitor<T> selectVisitor, S context) {
              return selectVisitor.visit(this, context);
          }
      }

How is this useful? Consider resolving the `AllColumns` ``*`` or `AllTableColumns` ``t.*`` expressions to retrieve the actual column names. This process depends on the database's physical metadata and the context of the current scope, including virtual data frames (like sub-selects and with-clauses).

Therefore, every branch of the AST must receive scoped metadata from its parent node. Each AST node must receive the resolved columns from its child nodes. A global result object (like the `StringBuilder` in the `DepParser` implementations) is inadequate.

Alternatively, consider substituting `TimeValueKey` (``CURRENT_DATE``, ``CURRENT_TIME``, etc.) with actual date or time values. You can push a simple `Map` of key/value pairs down to the Expression Visitor:

   .. code-block:: java
      :caption: Expression Visitor

      @Override
      public <S> StringBuilder visit(TimeKeyExpression expression, S context) {
          if (context instanceof Map) {
              return visit(expression, (Map<String, Object>) substitutions);
          } else {
            return expression.toString();
          }
      }

      public StringBuilder visit(TimeKeyExpression expression, Map<String, Object> substitutions) {
          // Remove possible trailing brackets "()"
          String value = expression.getStringValue().toUpperCase().replaceAll("[()]", "");

          if (substitutions.containsKey(value)) {
              // @todo: Cast Date/Time types
              return castDateTime(substitutions.get(value).toString()).accept(this, null);
          } else {
              return super.visit(expression, null);
          }
      }

Another advantage is parallel processing: Without relying on a global result object, the AST can be traversed in parallel (whereas it currently must be traversed strictly in serial).

Finally, any child node can now know its parent and identify who called it.
