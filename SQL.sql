
-- DROP OLD TABLES IF THEY EXIST (drop order avoids FK conflicts)
DROP SCHEMA IF EXISTS westsec_chat CASCADE;
DROP TABLE IF EXISTS channel_users CASCADE;
DROP TABLE IF EXISTS message CASCADE;
DROP TABLE IF EXISTS channel CASCADE;
DROP TABLE IF EXISTS users CASCADE;
-- USERS TABLE
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    birthdate DATE NOT NULL,
    address VARCHAR(255) NOT NULL,
    zip_code VARCHAR(50) NOT NULL,
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50) NOT NULL,
    salt VARCHAR(64),
    secret_key VARCHAR(255),
    active_status INTEGER DEFAULT 0,  -- default to inactive status
    CONSTRAINT user_email_unique UNIQUE(email)  -- Ensure email is unique
);

-- CHANNEL TABLE
CREATE TABLE channel (
    channel_id SERIAL PRIMARY KEY,
    channel_name VARCHAR(255),
    creator INT,
    creation_timestamp DATE,
    FOREIGN KEY (creator) REFERENCES users(id) ON DELETE SET NULL
);

-- M:N RELATION BETWEEN USERS AND CHANNELS
CREATE TABLE channel_users (
    user_id INT NOT NULL,
    channel_id INT NOT NULL,
    PRIMARY KEY (user_id, channel_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (channel_id) REFERENCES channel(channel_id)
);

CREATE TABLE message (
    msg_id SERIAL PRIMARY KEY,
    message VARCHAR(1024),
    msg_timestamp TIMESTAMP NOT NULL,
    sender_user_id INT REFERENCES users(id),
    recipient_user_id INT REFERENCES users(id),
    channel_id INT REFERENCES channel(channel_id),
    file_path VARCHAR(512)
);


INSERT INTO users(username, password, name, surname, birthdate, address, zip_code, city, country, email, phone, secret_key, active_status)
VALUES
 ('knut', 'e46047779e3d9eaa096d218ad7e4fb85acf573c34385eabf86b47ee4880b6' ,'Knut', 'Nilsen', CAST('1987-05-25' AS DATE), 'Linnebråteveien 11', '1455', 'NORDRE FROGN', 'NORWAY', 'knut@westsec.no', '+4792241965', '', 0),
 ('g-t', '888864e7d937d61713cf02a7e44b1e13164b44e414f16ad8682131d1b27e7a', 'Glenn-Thomas', 'Boine', CAST('1989-02-18' AS DATE), 'Yoghurtveien 1', '1279', 'Oslo', 'Norway', 'gt@westsec.no', '+4790125901', '', 0),
 ('tester1', 'af92a968aca8ab2476fd3961898bb37c30deab9432147fd7245cc02461dbcc3', 'Test', 'Testesen', CAST('0001-01-01' AS DATE), 'test 1', 'Oslo', '1279', 'Norway', 'test1@test.no', '+4700000000', '', 0),
 ('tester2', 'af92a968aca8ab2476fd3961898bb37c30deab9432147fd7245cc02461dbcc3', 'Test', 'Testesen', CAST('0001-01-01' AS DATE), 'test 2', 'Oslo', '1279', 'Norway', 'test2@test.no', '+4700000000', '', 0),
 ('test1', '18f5288c1c4418132f8286b5167c3e4793d3353fa25c4d5458ef544862317a34', 'Test', 'User', CAST('1990-01-01' AS DATE), '123 Main St', '12345', 'Testville', 'Testland', 'testuser1@example.com', '+1234567890', '', 0),
 ('test', '18f5288c1c4418132f8286b5167c3e4793d3353fa25c4d5458ef544862317a34', 'Test', 'User', CAST('1990-01-01' AS DATE), '123 Main St', '12345', 'Testville', 'Testland', 'test@example.com', '+1234567890', '', 0),
 ('testuser01', '888864e7d937d61713cf02a7e44b1e13164b44e414f16ad8682131d1b27e7a', 'Test', 'User', CAST('1990-01-01' AS DATE), '123 Main St', '12345', 'Testville', 'Testland', 'testuser@example.com', '+1234567890', '', 0);

