select t.*, connect_by_root id
from test t
start with t.id = 1
connect by prior t.id = t.parent_id
order siblings by t.some_text
