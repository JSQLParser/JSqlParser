DO $$
BEGIN
	IF NOT EXISTS( select 1 from comm.permission_operation where permission_operation_code = 'ecg_report_time_modify') and EXISTS( select 1 from comm.permission where permission_code = 'data_modify')
	 THEN
		INSERT INTO comm.permission_operation
		  (permission_operation_id,
		   permission_id,
		   permission_operation_code,
		   permission_operation_name,
		   "type",
		   "version",
		   his_org_id,
		   his_creater_id,
		   his_creater_name,
		   his_create_time,
		   his_updater_id,
		   his_update_time)
		   VALUES
			 ((select max(permission_operation_id) + 1 from comm.permission_operation),
			 (select permission_id from comm.permission where permission_code = 'data_modify' limit 1),
			 'ecg_report_time_modify',
			 '心电报告时间修改',
			 '1',
			 0,
			 (select his_org_id from comm.hospital limit 1),
			 1,
			 '系统管理员',
			 now(),
			 1,
			 now()) on conflict(permission_operation_id) do nothing;
	END IF;
END $$;
