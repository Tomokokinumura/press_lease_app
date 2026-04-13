CREATE TABLE IF NOT EXISTS master_setting (
    id SERIAL PRIMARY KEY,
    master_text VARCHAR(1000),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO master_setting (master_text)
SELECT 'ここにExcelに表示する固定文言を登録してください。'
WHERE NOT EXISTS (
    SELECT 1
    FROM master_setting
);
