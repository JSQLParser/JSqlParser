select deptno
,      avg(sal) avg_sal
,      cast
       ( collect(ename)
         as ename_type
       ) enames
from   emp
group
by     deptno
