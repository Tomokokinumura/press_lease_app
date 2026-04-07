SET @project_name_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE slip_media ADD COLUMN project_name VARCHAR(100)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'slip_media'
      AND column_name = 'project_name'
);
PREPARE project_name_stmt FROM @project_name_sql;
EXECUTE project_name_stmt;
DEALLOCATE PREPARE project_name_stmt;
