select nt.column_value as distinct_element
     from   table(set(varchar2_ntt('a','b','c','c'))) nt