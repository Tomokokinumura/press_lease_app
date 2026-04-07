SET @returned_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE slip_detail ADD COLUMN returned BOOLEAN DEFAULT FALSE',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'slip_detail'
      AND column_name = 'returned'
);
PREPARE returned_stmt FROM @returned_sql;
EXECUTE returned_stmt;
DEALLOCATE PREPARE returned_stmt;

SET @returned_date_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE slip_detail ADD COLUMN returned_date DATE',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'slip_detail'
      AND column_name = 'returned_date'
);
PREPARE returned_date_stmt FROM @returned_date_sql;
EXECUTE returned_date_stmt;
DEALLOCATE PREPARE returned_date_stmt;
