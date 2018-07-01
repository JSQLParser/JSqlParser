  insert into emp
  (empno, ename)
  values
  (seq_emp.nextval, 'morgan')
  returning rowid
  into r
