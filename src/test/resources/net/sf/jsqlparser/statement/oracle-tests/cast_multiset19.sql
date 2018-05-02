select deptno
     ,      cast(
               collect(ename order by hiredate)
                  as varchar2_ntt) as ordered_emps
     from   emp
     group  by
            deptno
