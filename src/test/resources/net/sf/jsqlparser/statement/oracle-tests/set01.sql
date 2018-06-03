select nt
     ,      set(nt) as nt_set
     from (
           select varchar2_ntt('a','b','c','c') as nt
           from dual
          )