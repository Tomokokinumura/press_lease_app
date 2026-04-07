CREATE TABLE IF NOT EXISTS master_setting (
    id INT AUTO_INCREMENT PRIMARY KEY,
    master_text VARCHAR(1000),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO master_setting (master_text)
SELECT 'ここにExcelに表示する固定文言を入力'
WHERE NOT EXISTS (
    SELECT 1
    FROM master_setting
);
