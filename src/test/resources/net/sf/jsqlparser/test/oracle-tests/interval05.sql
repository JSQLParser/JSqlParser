-- see metalink note 1056382.1
select 'yes' from dual where (sysdate-5,sysdate) overlaps (sysdate-2,sysdate-1)
