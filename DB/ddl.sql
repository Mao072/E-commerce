-- =============================================
-- DDL: 建立資料表
-- E-commerce Shopping Center Database Schema
-- 相容 MySQL 8.0 / MariaDB 10.x
-- =============================================

-- 刪除並重建資料庫（確保一致的 Collation）
DROP DATABASE IF EXISTS ecommerce_db;
CREATE DATABASE ecommerce_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_general_ci;

USE ecommerce_db;

-- =============================================
-- 商品表 (Product)
-- =============================================
CREATE TABLE IF NOT EXISTS product (
    product_id VARCHAR(20) PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    quantity INT NOT NULL,
    INDEX idx_product_quantity (quantity),
    CONSTRAINT chk_price CHECK (price >= 0),
    CONSTRAINT chk_quantity CHECK (quantity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =============================================
-- 會員表 (Member)
-- =============================================
CREATE TABLE IF NOT EXISTS member (
    member_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    CONSTRAINT chk_role CHECK (role IN ('ADMIN', 'USER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =============================================
-- 訂單表 (Order)
-- =============================================
CREATE TABLE IF NOT EXISTS `order` (
    order_id VARCHAR(30) PRIMARY KEY,
    member_id VARCHAR(20) NOT NULL,
    total_price DECIMAL(12,2) NOT NULL,
    pay_status TINYINT NOT NULL DEFAULT 0,
    order_status TINYINT NOT NULL DEFAULT 0,
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    INDEX idx_order_member (member_id),
    INDEX idx_order_pay_status (pay_status),
    INDEX idx_order_status (order_status),
    CONSTRAINT chk_total_price CHECK (total_price >= 0),
    CONSTRAINT chk_pay_status CHECK (pay_status IN (0, 1)),
    CONSTRAINT chk_order_status CHECK (order_status IN (-1, 0, 1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- =============================================
-- 訂單明細表 (OrderDetail)
-- =============================================
CREATE TABLE IF NOT EXISTS order_detail (
    order_item_sn BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id VARCHAR(30) NOT NULL,
    product_id VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    stand_price DECIMAL(12,2) NOT NULL,
    item_price DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_order_detail_order (order_id),
    INDEX idx_order_detail_product (product_id),
    CONSTRAINT chk_detail_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
