  insert into emp
  (empno, ename)
  values
  (seq_emp.nextval, 'morgan')
  returning empno
  into x
