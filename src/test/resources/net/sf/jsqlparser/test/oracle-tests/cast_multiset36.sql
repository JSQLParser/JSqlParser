select owner
     ,      object_type
     ,      set(
               cast(
                  collect(object_name)
                     as varchar2_ntt)) as object_names
     from   all_objects
     group  by
            owner
     ,      object_type