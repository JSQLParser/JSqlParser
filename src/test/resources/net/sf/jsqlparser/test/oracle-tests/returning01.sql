update emp
set ename = lower(ename)
where job = :jobs(i)
returning empno
bulk collect into :empnos
