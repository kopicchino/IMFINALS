<<<<<<< HEAD
-- ============================================================
-- Customer Intelligence & Record Management System
-- Database Schema v2.0 — Normalized to 3NF
-- ============================================================
-- NORMALIZATION NOTES:
--   1NF: All columns hold atomic values; no repeating groups.
--   2NF: Every non-key column depends on the WHOLE primary key.
--   3NF: No transitive dependencies; e.g. brand and category data
--        live in their own tables, not repeated inside products.
-- ============================================================

-- Drop tables in safe reverse-dependency order
DROP TABLE IF EXISTS customer_interactions;
DROP TABLE IF EXISTS targeted_offers;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS customer_cards;
DROP TABLE IF EXISTS customer_profiles;
DROP TABLE IF EXISTS cart;
DROP TABLE IF EXISTS verification_codes;
DROP TABLE IF EXISTS inventory_logs;
DROP TABLE IF EXISTS sale_items;
DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS brands;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;

-- ============================================================
-- TABLE 1: users
-- Stores login credentials, contact info, and account state.
-- IMPORTANT: full_name is NOT unique — two customers can both
-- be named "May". The INT primary key `id` is the only reliable
-- differentiator. Always GROUP BY u.id, never by u.full_name
-- alone, to keep same-name customers as distinct rows.
-- ============================================================
CREATE TABLE users (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    full_name  VARCHAR(100) NOT NULL,               -- NOT UNIQUE: two customers can share a name
    email      VARCHAR(100),
    phone      VARCHAR(50),
    address    TEXT,
    user_type  ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    is_active  BOOLEAN DEFAULT TRUE,
    is_locked  BOOLEAN DEFAULT FALSE,               -- Account-level fraud lock
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- TABLE 2: categories
-- Lookup table separating product classification from product
-- data (3NF: category name does not depend on product).
-- ============================================================
CREATE TABLE categories (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ============================================================
-- TABLE 3: brands
-- Separate brand entity prevents redundant brand strings in
-- products and supports brand-loyalty analytics via JOIN.
-- ============================================================
CREATE TABLE brands (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- ============================================================
-- TABLE 4: suppliers
-- Supplier contact info is transitive data; belongs here, not
-- in the products table (3NF compliance).
-- ============================================================
CREATE TABLE suppliers (
    id      INT PRIMARY KEY AUTO_INCREMENT,
    name    VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    email   VARCHAR(100),
    address TEXT
);

-- ============================================================
-- TABLE 5: products
-- References categories, brands, suppliers via foreign keys.
-- selling_price is stored for POS snapshot accuracy; it is
-- NOT derived at query time (prevents stale joins on price
-- changes — satisfies 3NF for historical price integrity).
-- ============================================================
CREATE TABLE products (
    id                 INT PRIMARY KEY AUTO_INCREMENT,
    name               VARCHAR(200) NOT NULL,
    category_id        INT,
    brand_id           INT,
    supplier_id        INT,
    cost_price         DECIMAL(10,2) NOT NULL,
    markup_percentage  DECIMAL(5,2)  NOT NULL,
    selling_price      DECIMAL(10,2) NOT NULL,
    stock_quantity     INT DEFAULT 0,
    description        TEXT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    FOREIGN KEY (brand_id)    REFERENCES brands(id)     ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)  ON DELETE SET NULL,
    -- Index for GROUP BY / JOIN on brand and category analytics
    INDEX idx_brand_id    (brand_id),
    INDEX idx_category_id (category_id)
);

-- ============================================================
-- TABLE 6: sales
-- Point-of-sale transaction header. customer_name and address
-- are snapshot-stored here so historical orders are accurate
-- even if the customer later updates their profile.
-- user_id links to the registered account for analytics.
-- ============================================================
CREATE TABLE sales (
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT,
    sale_date        DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal         DECIMAL(10,2) NOT NULL,
    tax              DECIMAL(10,2) DEFAULT 0,
    delivery_fee     DECIMAL(10,2) DEFAULT 0,
    total            DECIMAL(10,2) NOT NULL,
    customer_name    VARCHAR(100),
    delivery_address TEXT,
    delivery_phone   VARCHAR(50),
    status           ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'COMPLETED',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    -- Supports GROUP BY u.id queries in analytics
    INDEX idx_user_id (user_id)
);

-- ============================================================
-- TABLE 7: sale_items
-- Line-item detail for each sale. unit_price is snapshot-stored
-- so historical reports reflect the price at time of purchase.
-- ============================================================
CREATE TABLE sale_items (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    sale_id     INT NOT NULL,
    product_id  INT NOT NULL,
    quantity    INT NOT NULL,
    unit_price  DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id)    REFERENCES sales(id)    ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    -- Supports GROUP BY p.id for Top Products by Revenue queries
    INDEX idx_product_id (product_id),
    INDEX idx_sale_id    (sale_id)
);

-- ============================================================
-- TABLE 8: inventory_logs
-- Immutable log of every stock movement (IN/OUT/ADJUSTMENT).
-- Stored separately from products to avoid update anomalies.
-- ============================================================
CREATE TABLE inventory_logs (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    product_id     INT NOT NULL,
    change_type    ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity       INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock      INT NOT NULL,
    notes          TEXT,
    log_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE 9: verification_codes
-- 2FA codes for login, registration, and password reset flows.
-- Separated from users so expired codes can be purged without
-- touching the user record.
-- ============================================================
CREATE TABLE verification_codes (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    user_id    INT NOT NULL,
    code       VARCHAR(6) NOT NULL,
    type       ENUM('LOGIN', 'REGISTRATION', 'PASSWORD_RESET') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used    BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_code (user_id, code),
    INDEX idx_expires   (expires_at)
);

-- ============================================================
-- TABLE 10: cart
-- Persistent shopping cart items per customer.
-- UNIQUE(user_id, product_id) prevents duplicate cart entries.
-- ============================================================
CREATE TABLE cart (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    user_id    INT NOT NULL,
    product_id INT NOT NULL,
    quantity   INT NOT NULL,
    added_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
=======
/*E pos_db;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(50),
    address TEXT,
    user_type ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE brands (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

CREATE TABLE suppliers (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    contact VARCHAR(50),
    email VARCHAR(100),
    address TEXT
);

CREATE TABLE products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    category_id INT,
    brand_id INT,
    supplier_id INT,
    cost_price DECIMAL(10,2) NOT NULL,
    markup_percentage DECIMAL(5,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    stock_quantity INT DEFAULT 0,
    description TEXT,
    image_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    FOREIGN KEY (brand_id) REFERENCES brands(id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(id)
);

CREATE TABLE sales (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    sale_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) DEFAULT 0,
    total DECIMAL(10,2) NOT NULL,
    customer_name VARCHAR(100),
    status ENUM('PENDING', 'COMPLETED', 'CANCELLED') DEFAULT 'COMPLETED',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE sale_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE inventory_logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    change_type ENUM('IN', 'OUT', 'ADJUSTMENT') NOT NULL,
    quantity INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    notes TEXT,
    log_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Verification codes table for 2FA
CREATE TABLE verification_codes (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    code VARCHAR(6) NOT NULL,
    type ENUM('LOGIN', 'REGISTRATION', 'PASSWORD_RESET') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_code (user_id, code),
    INDEX idx_expires (expires_at)
);

-- Shopping cart for customers
CREATE TABLE cart (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_product (user_id, product_id)
);

<<<<<<< HEAD
-- ============================================================
-- TABLE 11: customer_profiles
-- Behavioral intelligence separated from login data for
-- PH Data Privacy Act of 2012 compliance. Profiling only
-- executes when consent_dpa = TRUE.
-- ============================================================
CREATE TABLE customer_profiles (
    user_id                INT PRIMARY KEY,
    segment                VARCHAR(100) DEFAULT 'Standard Consumer',
    predictive_preferences TEXT,
    dynamic_tags           VARCHAR(255) DEFAULT '',  -- Comma-separated behavioral tags
    consent_dpa            BOOLEAN DEFAULT FALSE,    -- PH DPA 2012 consent gate
    risk_score             DECIMAL(5,2) DEFAULT 0.00,
    created_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE 12: customer_cards
-- Tokenized payment card records with status lifecycle.
-- card_number_token: full encrypted token (never displayed).
-- card_number_masked: last-4 display only.
-- Status lifecycle: Active -> Reported -> Locked -> Under Review -> Resolved/Replaced
-- ============================================================
CREATE TABLE customer_cards (
    id                  INT PRIMARY KEY AUTO_INCREMENT,
    user_id             INT NOT NULL,
    card_number_token   VARCHAR(255) NOT NULL,
    card_number_masked  VARCHAR(30)  NOT NULL,
    status              ENUM('Active','Reported','Locked','Under Review','Resolved/Replaced') DEFAULT 'Active',
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ============================================================
-- TABLE 13: audit_logs
-- Tamper-proof compliance trail. All fraud events, locks,
-- and consent changes are logged here with actor, time, and IP.
-- actor_id references users so the GROUP BY analytics query
-- can attribute events to named administrators.
-- ============================================================
CREATE TABLE audit_logs (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    event_time  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_type  VARCHAR(100) NOT NULL,
    actor_id    INT,
    description VARCHAR(255) NOT NULL,
    details     TEXT,
    ip_address  VARCHAR(45),
    FOREIGN KEY (actor_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_event_type (event_type),
    INDEX idx_actor_id   (actor_id)
);

-- ============================================================
-- TABLE 14: targeted_offers
-- Maps lifestyle segments to specific product promotions.
-- trigger_condition defines the behavioral threshold rule.
-- ============================================================
CREATE TABLE targeted_offers (
    id                INT PRIMARY KEY AUTO_INCREMENT,
    title             VARCHAR(150) NOT NULL,
    description       TEXT,
    segment           VARCHAR(100) NOT NULL,
    product_id        INT NOT NULL,
    promo_code        VARCHAR(50),
    trigger_condition VARCHAR(255),
    is_active         BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_segment (segment)
);

-- ============================================================
-- TABLE 15: customer_interactions
-- 360 degree customer view: all touchpoints (transactions,
-- calls, complaints) stored as typed interaction records.
-- ============================================================
CREATE TABLE customer_interactions (
    id               INT PRIMARY KEY AUTO_INCREMENT,
    user_id          INT NOT NULL,
    type             ENUM('Transaction','Call','Complaint') NOT NULL,
    description      VARCHAR(255) NOT NULL,
    interaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);


-- ============================================================
-- SEED DATA
-- ============================================================

-- Administrators
INSERT INTO users (username, password, full_name, email, phone, address, user_type) VALUES
('admin',         'admin123',  'System Administrator', 'admin@inventory.com',    '0917-1234567', 'Manila, Philippines',  'ADMIN'),
('fraud_analyst', 'fraud123',  'Fraud Team Lead',      'compliance@secure.com',  '0917-7654321', 'BGC, Taguig',          'ADMIN');

-- Sample Customers (user IDs 3-7)
-- NOTE: Two customers are deliberately named "May" (IDs 6 and 7) to demonstrate
-- that GROUP BY u.id keeps them as separate rows even with identical full_name values.
INSERT INTO users (username, password, full_name, email, phone, address, user_type) VALUES
('john_doe',     'customer123', 'John Doe',   'john.doe@gmail.com',    '0918-1112222', '123 Main St, Quezon City',    'CUSTOMER'),
('alice_travels', 'customer123', 'Alice Vance','alice.v@gmail.com',     '0919-3334444', '456 Resort Rd, Makati City',  'CUSTOMER'),
('bob_tech',     'customer123', 'Bob Smith',  'bob.tech@outlook.com',  '0920-5556666', '789 Silicon Ave, Pasig City', 'CUSTOMER'),
('may_santos',   'customer123', 'May Santos', 'may.s@gmail.com',       '0921-7778888', '10 Sampaguita St, Marikina',  'CUSTOMER'),
('may_reyes',    'customer123', 'May Reyes',  'may.r@yahoo.com',       '0922-9990000', '22 Rosal Ave, Antipolo',      'CUSTOMER');

-- Product Categories
INSERT INTO categories (name, description) VALUES
('Electronics',       'Electronic devices and premium gadgets'),
('Clothing',          'Apparel and sportswear'),
('Food & Beverages',  'Organic food, snacks, and dining provisions'),
('Sports & Outdoors', 'Sports gear, outdoor exercise equipment, and active accessories');

-- Brands
INSERT INTO brands (name, description) VALUES
('Apple',   'Premium technology brand'),
('Samsung', 'Leading general consumer electronics manufacturer'),
('Nike',    'Activewear and running equipment'),
('Generic', 'Everyday budget utility merchandise');

-- Suppliers
INSERT INTO suppliers (name, contact, email, address) VALUES
('Tech Distributors PH',   '02-8888-0101', 'info@techdist.com.ph',       'Silicon Tower, Mandaluyong City'),
('Fashion Wholesale Corp',  '02-8999-0202', 'sales@fashionwholesale.com', 'Garment Bldg, Pasay City'),
('Wellness Food Packers',   '02-8777-0303', 'packers@wellnessfoods.com',  'Industrial Park, Laguna');

-- Products (category_id, brand_id, supplier_id match above inserts)
INSERT INTO products (name, category_id, brand_id, supplier_id, cost_price, markup_percentage, selling_price, stock_quantity, description) VALUES
('MacBook Pro 14',            1, 1, 1, 80000.00, 20.00, 96000.00, 15, 'High-performance M3 laptop for premium developers'),
('iPhone 15 Pro Max',         1, 1, 1, 65000.00, 15.00, 74750.00, 25, 'Premium titanium tech smartphone'),
('Galaxy S24 Ultra',          1, 2, 1, 55000.00, 18.18, 65000.00, 20, 'Samsung flagship device with advanced AI integration'),
('Wireless Airbuds Pro',      1, 1, 1,  6000.00, 50.00,  9000.00, 40, 'Premium acoustic active noise-cancelling earbuds'),
('Air Zoom Running Shoes',    2, 3, 2,  4000.00, 50.00,  6000.00, 30, 'High-traction performance athletics footwear'),
('DryFit Training T-Shirt',   2, 3, 2,  1000.00, 50.00,  1500.00, 100,'Breathable training gear for gym exercise'),
('Yoga Mat Non-Slip',         4, 3, 2,  1200.00, 50.00,  1800.00, 60, 'Perfect cushion density for travel and outdoor exercise'),
('Stainless Steel Hydration Flask', 4, 4, 3, 600.00, 66.67, 1000.00, 80, 'Vacuum insulated container for hot and cold travel dining');

-- Customer Behavioral Profiles (user IDs 3-7)
INSERT INTO customer_profiles (user_id, segment, predictive_preferences, dynamic_tags, consent_dpa, risk_score) VALUES
(3, 'Standard Consumer',    'Looking for affordable casual wear',                                        '#new-consumer',                    TRUE,  0.00),
(4, 'Travel Enthusiast',    'High potential for sports gear, hydration items, and running shoes',        '#frequent-traveler, #high-spender', TRUE,  0.00),
(5, 'Premium Tech Consumer','Anticipated interest in Apple accessories and M3 upgrades',                 '#premium-tech, #brand-loyal',       TRUE,  5.00),
(6, 'Standard Consumer',    'Interest in everyday apparel and food items',                               '#new-consumer',                     TRUE,  0.00),
(7, 'Standard Consumer',    'Potential for Sports & Outdoors category based on browsing',                '#new-consumer',                     TRUE,  0.00);

-- Payment Card Records
INSERT INTO customer_cards (user_id, card_number_token, card_number_masked, status) VALUES
(3, 'tok_visa_411111xxxxxx1111_98765', 'Visa ending in 1111',       'Active'),
(4, 'tok_mc_555555xxxxxx5555_43210',  'Mastercard ending in 5555',  'Active'),
(5, 'tok_amex_3782xxxxxxx2002_74382', 'Amex ending in 2002',        'Active'),
(6, 'tok_visa_411111xxxxxx2222_11111','Visa ending in 2222',        'Active'),
(7, 'tok_mc_555555xxxxxx6666_22222',  'Mastercard ending in 6666',  'Active');

-- 360-Degree Customer Interaction Touchpoints
INSERT INTO customer_interactions (user_id, type, description) VALUES
(3, 'Call',      'Assisted with account registration verification codes'),
(4, 'Call',      'Enquired about shipping schedules to resort hotels'),
(4, 'Complaint', 'Investigated minor box scratch on Nike shoes delivery'),
(5, 'Call',      'Inquired if MacBook Pro M3 is eligible for corporate discounts'),
(6, 'Call',      'Asked about return policy for clothing items'),
(7, 'Complaint', 'Reported delayed delivery of sports gear order');

-- Targeted Offers / Campaigns
INSERT INTO targeted_offers (title, description, segment, product_id, promo_code, trigger_condition) VALUES
('Traveler Insurance Bundle',       'Enjoy 20% off travel insurance when linked to active sports gear purchase',       'Travel Enthusiast',    8, 'TRAVEL20',  '3+ travel transactions in 90 days'),
('Ultimate Wireless Earbuds Cross-Sell', 'Upgrade your sound to match your MacBook. Save 1,500 instantly.',           'Premium Tech Consumer',4, 'TECHBUDS',  'Holds premium computing model but no audio accessories'),
('Welcome Discount',                 'Get 500 off on any lifestyle catalog item',                                      'Standard Consumer',    6, 'NEWSTYLE',  'Newly registered account'),
('Galaxy Upgrade Offer',             'Switch from Samsung to Apple ecosystem with exclusive bundle pricing.',          'Premium Tech Consumer',1, 'GALAXYUP',  'Galaxy S24 owner with 2+ electronics orders');

-- ============================================================
-- SALES SEED DATA
-- Designed to produce a meaningful Top Customers ranking:
--   1st: Bob Smith   (user 5) — highest spender, premium tech
--   2nd: Alice Vance (user 4) — travel enthusiast
--   3rd: John Doe    (user 3) — standard consumer
--   4th: May Santos  (user 6) — demonstrates same-name split
--   5th: May Reyes   (user 7) — demonstrates same-name split
-- ============================================================

-- Order 1: John Doe — DryFit T-Shirt
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(1, 3, '2026-04-20 14:30:00', 1500.00, 180.00, 150.00, 1830.00, 'John Doe', '123 Main St, Quezon City', '0918-1112222', 'COMPLETED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(1, 6, 1, 1500.00, 1500.00);

-- Order 2: John Doe — Yoga Mat (second order to pad his total)
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(2, 3, '2026-04-28 10:00:00', 1800.00, 216.00, 150.00, 2166.00, 'John Doe', '123 Main St, Quezon City', '0918-1112222', 'COMPLETED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(2, 7, 1, 1800.00, 1800.00);

-- Order 3: Alice Vance — Running Shoes x2, Yoga Mat, Flask
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(3, 4, '2026-05-15 10:15:00', 14800.00, 1776.00, 150.00, 16726.00, 'Alice Vance', '456 Resort Rd, Makati City', '0919-3334444', 'COMPLETED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(3, 5, 2, 6000.00, 12000.00),
(3, 7, 1, 1800.00,  1800.00),
(3, 8, 1, 1000.00,  1000.00);

-- Order 4: Alice Vance — Galaxy S24 Ultra (second order)
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(4, 4, '2026-05-18 15:30:00', 65000.00, 7800.00, 150.00, 72950.00, 'Alice Vance', '456 Resort Rd, Makati City', '0919-3334444', 'COMPLETED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(4, 3, 1, 65000.00, 65000.00);

-- Order 5: Bob Smith — MacBook Pro + iPhone (premium tech)
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(5, 5, '2026-05-20 16:45:00', 170750.00, 20490.00, 150.00, 191390.00, 'Bob Smith', '789 Silicon Ave, Pasig City', '0920-5556666', 'COMPLETED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(5, 1, 1, 96000.00, 96000.00),
(5, 2, 1, 74750.00, 74750.00);

-- Order 6: Bob Smith — Wireless Airbuds (cross-sell)
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(6, 5, '2026-05-22 09:00:00', 9000.00, 1080.00, 150.00, 10230.00, 'Bob Smith', '789 Silicon Ave, Pasig City', '0920-5556666', 'PENDING');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(6, 4, 1, 9000.00, 9000.00);

-- Order 7: May Santos (user 6) — Running Shoes
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(7, 6, '2026-05-21 11:20:00', 6000.00, 720.00, 150.00, 6870.00, 'May Santos', '10 Sampaguita St, Marikina', '0921-7778888', 'COMPLETED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(7, 5, 1, 6000.00, 6000.00);

-- Order 8: May Reyes (user 7) — DryFit T-Shirt x2 + Flask
-- This order demonstrates: May Santos and May Reyes share a first name but are
-- different customers (IDs 6 and 7). GROUP BY u.id keeps them separate.
INSERT INTO sales (id, user_id, sale_date, subtotal, tax, delivery_fee, total, customer_name, delivery_address, delivery_phone, status) VALUES
(8, 7, '2026-05-23 14:00:00', 4000.00, 480.00, 150.00, 4630.00, 'May Reyes', '22 Rosal Ave, Antipolo', '0922-9990000', 'CANCELLED');
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, total_price) VALUES
(8, 6, 2, 1500.00, 3000.00),
(8, 8, 1, 1000.00, 1000.00);

-- Initial Security Audit Entry
INSERT INTO audit_logs (event_type, actor_id, description, details, ip_address) VALUES
('SYSTEM_INITIALIZATION', 1,
 'Database schema v2.0 initialized with 3NF normalization, GROUP BY performance indexes, and customer differentiation seed data.',
 'Users table full_name column is NOT UNIQUE by design — customer identity is always resolved by the INT primary key id.',
 '127.0.0.1');
=======
-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, full_name, email, user_type) VALUES
('admin', 'admin123', 'System Administrator', 'admin@inventory.com', 'ADMIN');

-- Insert sample customer (password: customer123)
INSERT INTO users (username, password, full_name, email, phone, address, user_type) VALUES
('customer1', 'customer123', 'John Doe', 'john@email.com', '555-1234', '123 Main St', 'CUSTOMER');

-- Insert sample data
INSERT INTO categories (name, description) VALUES
('Electronics', 'Electronic devices and gadgets'),
('Clothing', 'Apparel and fashion items'),
('Food & Beverages', 'Food and drink products'),
('Books & Media', 'Books, magazines, and media'),
('Home & Garden', 'Home improvement and garden supplies'),
('Sports & Outdoors', 'Sports equipment and outdoor gear');

INSERT INTO brands (name, description) VALUES
('Samsung', 'Electronics manufacturer'),
('Nike', 'Sports and lifestyle brand'),
('Generic', 'Generic brand items'),
('Apple', 'Technology company'),
('Sony', 'Electronics and entertainment'),
('Adidas', 'Sports brand');

INSERT INTO suppliers (name, contact, email, address) VALUES
('Tech Suppliers Inc', '555-0101', 'contact@techsuppliers.com', '123 Tech Street, Silicon Valley'),
('Fashion Wholesale Co', '555-0102', 'info@fashionwholesale.com', '456 Fashion Ave, New York'),
('Food Distributors Ltd', '555-0103', 'sales@fooddist.com', '789 Market Road, Chicago');

-- Sample products with better descriptions
INSERT INTO products (name, category_id, brand_id, supplier_id, cost_price, markup_percentage, selling_price, stock_quantity, description) VALUES
('Smartphone Galaxy S24', 1, 1, 1, 800.00, 25.00, 1000.00, 50, 'Latest Samsung flagship smartphone with advanced features'),
('Running Shoes Air Max', 2, 2, 2, 120.00, 40.00, 168.00, 75, 'Premium running shoes with excellent cushioning'),
('Laptop MacBook Pro', 1, 4, 1, 1500.00, 20.00, 1800.00, 30, 'High-performance laptop for professionals'),
('Wireless Earbuds', 1, 1, 1, 80.00, 50.00, 120.00, 100, 'Bluetooth wireless earbuds with noise cancellation'),
('Cotton T-Shirt', 2, 2, 2, 15.00, 66.67, 25.00, 200, 'Comfortable cotton t-shirt, various colors available'),
('Smart Watch', 1, 4, 1, 250.00, 40.00, 350.00, 60, 'Fitness tracking smart watch with heart rate monitor'),
('Gaming Headset', 1, 5, 1, 60.00, 50.00, 90.00, 80, 'Professional gaming headset with surround sound'),
('Yoga Mat', 6, 2, 2, 20.00, 50.00, 30.00, 120, 'Non-slip yoga mat for exercise and meditation'),
('Backpack', 2, 6, 2, 35.00, 42.86, 50.00, 90, 'Durable backpack with multiple compartments'),
('Water Bottle', 6, 3, 3, 8.00, 62.50, 13.00, 150, 'Insulated stainless steel water bottle');
>>>>>>> 0bc87d04903327a398d57bc0ad7a11b23bfb99e6
