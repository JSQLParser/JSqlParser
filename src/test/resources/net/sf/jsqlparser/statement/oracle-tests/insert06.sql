insert all 
when (deptno=10) then
  into emp_10 (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
when (deptno=20) then
  into emp_20 (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
when (deptno<=30) then
  into emp_30 (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
else
  into leftover (empno,ename,job,mgr,sal,deptno)
  values (empno,ename,job,mgr,sal,deptno)
select * from emp
