select deptno
     ,      cast(collect(ename) as varchar2_ntt) as emps
     from   emp
     group  by
            deptno