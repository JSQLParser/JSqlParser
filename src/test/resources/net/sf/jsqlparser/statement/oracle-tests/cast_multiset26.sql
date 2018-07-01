select deptno
     ,      cast(
               collect(
                  empsal_ot(ename, sal) order by sal
                  ) as empsal_ntt) as empsals
     from   emp
     group  by
            deptno