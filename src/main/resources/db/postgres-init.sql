CREATE TABLE IF NOT EXISTS todo (
    id SERIAL PRIMARY KEY,
    title VARCHAR(100)
);

INSERT INTO todo (title)
SELECT 'テストデータ'
WHERE NOT EXISTS (
    SELECT 1 FROM todo WHERE title = 'テストデータ'
);
