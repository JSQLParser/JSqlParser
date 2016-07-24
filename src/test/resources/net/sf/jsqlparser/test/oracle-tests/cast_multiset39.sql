select varchar2_ntt('a','b','c')
               multiset except
                  varchar2_ntt('b','c','d') as multiset_except
     from   dual