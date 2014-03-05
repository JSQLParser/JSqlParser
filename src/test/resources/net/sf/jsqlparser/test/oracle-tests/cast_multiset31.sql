select e.deptno
     ,      cast(
               multiset(
                  select e2.ename
                  from   emp e2
                  where  e2.deptno = e.deptno
                  order  by
                         e2.hiredate
                  ) as varchar2_ntt) as ordered_emps
     from   emp e
     group  by
            e.deptno
     order  by
            e.deptno