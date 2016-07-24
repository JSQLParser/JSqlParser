insert into (
  select empno, ename, job, sal, deptno
  from emp)
  values
  (1, 'morgan', 'dba', '1', 40)
