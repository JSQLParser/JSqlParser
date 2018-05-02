select cast(
               powermultiset(
                  varchar2_ntt('a','b','c')) as varchar2_ntts) as pwrmltset
     from   dual