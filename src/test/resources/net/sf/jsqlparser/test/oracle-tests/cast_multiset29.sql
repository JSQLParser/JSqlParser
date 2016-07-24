select cast(
               collect(
                  distinct empsal_ot(ename, sal)
                  ) as empsal_ntt) as empsals
     from   emp