-- GURUGE HARDWARE STORE - V1 Init Schema
-- MySQL 8+ compatible

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    module VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_perm FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(30) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    role_id BIGINT NOT NULL,
    supplier_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    last_login_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_users_role (role_id),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(150),
    address VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_customers_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(500),
    parent_id BIGINT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_cat_parent (parent_id),
    INDEX idx_cat_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS brands (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    slug VARCHAR(140) NOT NULL UNIQUE,
    description VARCHAR(500),
    logo_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS units (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(20) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS suppliers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(180) NOT NULL,
    contact_person VARCHAR(150),
    phone VARCHAR(20),
    email VARCHAR(150),
    address VARCHAR(500),
    tax_number VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    INDEX idx_sup_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Link users.supplier_id to suppliers after suppliers exists
ALTER TABLE users ADD CONSTRAINT fk_users_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL;

CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(80) UNIQUE,
    name VARCHAR(200) NOT NULL,
    short_desc VARCHAR(500),
    full_desc TEXT,
    category_id BIGINT NOT NULL,
    brand_id BIGINT NULL,
    unit_id BIGINT NOT NULL,
    selling_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    cost_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    current_stock INT NOT NULL DEFAULT 0,
    min_stock_level INT NOT NULL DEFAULT 10,
    max_stock_level INT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    image_url VARCHAR(500),
    version BIGINT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    created_by BIGINT NULL,
    updated_by BIGINT NULL,
    CONSTRAINT fk_prod_cat FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_prod_brand FOREIGN KEY (brand_id) REFERENCES brands(id) ON DELETE SET NULL,
    CONSTRAINT fk_prod_unit FOREIGN KEY (unit_id) REFERENCES units(id),
    CONSTRAINT chk_stock_nonneg CHECK (current_stock >= 0),
    CONSTRAINT chk_sell_nonneg CHECK (selling_price >= 0),
    CONSTRAINT chk_cost_nonneg CHECK (cost_price >= 0),
    INDEX idx_prod_cat_brand (category_id, brand_id),
    INDEX idx_prod_status (status),
    INDEX idx_prod_name (name),
    FULLTEXT INDEX ft_prod_search (name, short_desc, sku, barcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    alt_text VARCHAR(200),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_pimg_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_pimg_prod (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    movement_type VARCHAR(30) NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    reason VARCHAR(500),
    user_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_inv_prod FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_inv_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_inv_prod_date (product_id, created_at),
    INDEX idx_inv_type (movement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    po_number VARCHAR(40) NOT NULL UNIQUE,
    supplier_id BIGINT NOT NULL,
    order_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    expected_delivery_date DATE NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    notes TEXT,
    created_by BIGINT NULL,
    approved_by BIGINT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_po_sup FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_po_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_po_sup_date (supplier_id, order_date),
    INDEX idx_po_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS purchase_order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    ordered_qty INT NOT NULL,
    unit_cost DECIMAL(12,2) NOT NULL,
    total_cost DECIMAL(12,2) NOT NULL,
    received_qty INT NOT NULL DEFAULT 0,
    damaged_qty INT NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    CONSTRAINT fk_poi_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_poi_prod FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_poi_qty CHECK (ordered_qty > 0),
    INDEX idx_poi_po (purchase_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goods_receipts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(40) NOT NULL UNIQUE,
    purchase_order_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,
    received_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    received_by BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    notes TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_gr_po FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    CONSTRAINT fk_gr_sup FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT fk_gr_user FOREIGN KEY (received_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_gr_po (purchase_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS goods_receipt_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goods_receipt_id BIGINT NOT NULL,
    purchase_order_item_id BIGINT NOT NULL,
    received_qty INT NOT NULL,
    accepted_qty INT NOT NULL,
    damaged_qty INT NOT NULL DEFAULT 0,
    notes VARCHAR(500),
    CONSTRAINT fk_gri_gr FOREIGN KEY (goods_receipt_id) REFERENCES goods_receipts(id) ON DELETE CASCADE,
    CONSTRAINT fk_gri_poi FOREIGN KEY (purchase_order_item_id) REFERENCES purchase_order_items(id),
    INDEX idx_gri_gr (goods_receipt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receipt_number VARCHAR(40) NOT NULL UNIQUE,
    customer_id BIGINT NULL,
    customer_name VARCHAR(150),
    customer_phone VARCHAR(20),
    cashier_id BIGINT NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_type VARCHAR(20),
    total_amount DECIMAL(12,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    sale_status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    notes VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_sale_cust FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_sale_cashier FOREIGN KEY (cashier_id) REFERENCES users(id),
    INDEX idx_sale_date_cashier (created_at, cashier_id),
    INDEX idx_sale_receipt (receipt_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_type VARCHAR(20),
    line_total DECIMAL(12,2) NOT NULL,
    cost_price_at_sale DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_si_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_si_prod FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT chk_si_qty CHECK (quantity > 0),
    INDEX idx_si_sale (sale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    amount_paid DECIMAL(12,2) NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    transaction_ref VARCHAR(150),
    status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    paid_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    created_by BIGINT NULL,
    CONSTRAINT fk_pay_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT fk_pay_user FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_pay_sale (sale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS returns (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_number VARCHAR(40) NOT NULL UNIQUE,
    sale_id BIGINT NOT NULL,
    customer_id BIGINT NULL,
    processed_by BIGINT NOT NULL,
    total_refund DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    reason TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_ret_sale FOREIGN KEY (sale_id) REFERENCES sales(id),
    CONSTRAINT fk_ret_cust FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE SET NULL,
    CONSTRAINT fk_ret_user FOREIGN KEY (processed_by) REFERENCES users(id),
    INDEX idx_ret_sale (sale_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS return_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_id BIGINT NOT NULL,
    sale_item_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    returned_qty INT NOT NULL,
    refund_amount DECIMAL(12,2) NOT NULL,
    reason VARCHAR(500),
    item_condition VARCHAR(30),
    restocked BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ri_ret FOREIGN KEY (return_id) REFERENCES returns(id) ON DELETE CASCADE,
    CONSTRAINT fk_ri_sitem FOREIGN KEY (sale_item_id) REFERENCES sale_items(id),
    CONSTRAINT fk_ri_prod FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_ri_ret (return_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS customer_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id VARCHAR(40) NOT NULL UNIQUE,
    customer_name VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    admin_response TEXT,
    responded_by BIGINT NULL,
    responded_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_req_user FOREIGN KEY (responded_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_req_status_date (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    username VARCHAR(100),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(50),
    old_values TEXT,
    new_values TEXT,
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_entity (entity_type, entity_id),
    INDEX idx_audit_date (created_at),
    INDEX idx_audit_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
