select *
     from   table(
               set( complex_ntt(complex_ot('data', 'more data', 1),
                                complex_ot('data', 'some data', 2),
                                complex_ot('data', 'dupe data', 3),
                                complex_ot('data', 'dupe data', 3)) ))