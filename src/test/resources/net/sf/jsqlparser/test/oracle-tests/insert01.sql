insert
  when mod( object_id, 2 ) = 1 then
    into t1 ( x, y ) values ( s.nextval, object_id )
  when mod( object_id, 2 ) = 0 then
    into t2 ( x, y ) values ( s.nextval, created )
select object_id, created from all_objects
