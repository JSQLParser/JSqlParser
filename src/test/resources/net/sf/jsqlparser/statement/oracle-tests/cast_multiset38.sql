select *
     from   table( varchar2_ntt('a','b','c')
                      multiset union distinct
                         varchar2_ntt('b','c','d') )