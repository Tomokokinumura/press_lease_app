CREATE TABLE IF NOT EXISTS slip_media (
    id SERIAL PRIMARY KEY,
    slip_id INT,
    media_name VARCHAR(100),
    project_name VARCHAR(100),
    release_date DATE,
    note VARCHAR(255),
    CONSTRAINT fk_slip_media_slip
        FOREIGN KEY (slip_id) REFERENCES slip(id)
);
