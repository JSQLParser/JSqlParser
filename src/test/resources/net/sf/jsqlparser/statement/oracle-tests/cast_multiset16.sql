select deptno
     ,      collect(ename) as emps
     from   emp
     group  by
            deptno
	