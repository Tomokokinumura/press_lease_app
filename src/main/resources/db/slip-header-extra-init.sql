SET @customer_name_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE slip ADD COLUMN customer_name VARCHAR(100)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'slip'
      AND column_name = 'customer_name'
);
PREPARE customer_name_stmt FROM @customer_name_sql;
EXECUTE customer_name_stmt;
DEALLOCATE PREPARE customer_name_stmt;

SET @contact_info_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE slip ADD COLUMN contact_info VARCHAR(100)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'slip'
      AND column_name = 'contact_info'
);
PREPARE contact_info_stmt FROM @contact_info_sql;
EXECUTE contact_info_stmt;
DEALLOCATE PREPARE contact_info_stmt;

SET @email_address_sql = (
    SELECT IF(
        COUNT(*) = 0,
        'ALTER TABLE slip ADD COLUMN email_address VARCHAR(100)',
        'SELECT 1'
    )
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'slip'
      AND column_name = 'email_address'
);
PREPARE email_address_stmt FROM @email_address_sql;
EXECUTE email_address_stmt;
DEALLOCATE PREPARE email_address_stmt;
