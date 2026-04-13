CREATE TABLE IF NOT EXISTS slip (
    id SERIAL PRIMARY KEY,
    slip_no VARCHAR(20),
    staff_name VARCHAR(50),
    customer_name VARCHAR(100),
    contact_info VARCHAR(100),
    email_address VARCHAR(100),
    loan_date DATE,
    return_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS slip_detail (
    id SERIAL PRIMARY KEY,
    slip_id INT,
    code VARCHAR(20),
    name VARCHAR(100),
    price INT,
    tax_price INT,
    credit VARCHAR(100),
    media_name VARCHAR(100),
    release_date DATE,
    note VARCHAR(255),
    returned BOOLEAN DEFAULT FALSE,
    returned_date DATE,
    CONSTRAINT fk_slip_detail_slip
        FOREIGN KEY (slip_id) REFERENCES slip(id)
);
