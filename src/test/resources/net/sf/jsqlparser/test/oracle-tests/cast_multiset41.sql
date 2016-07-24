select *
     from   table(
               powermultiset_by_cardinality(
                  varchar2_ntt('a','b','c','d','d'), 3))