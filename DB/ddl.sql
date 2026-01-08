-- =============================================
-- DDL: 建立資料表
-- E-commerce Shopping Center Database Schema
-- =============================================

-- 建立資料庫
CREATE DATABASE IF NOT EXISTS ecommerce_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ecommerce_db;

-- =============================================
-- 商品表 (Product)
-- =============================================
CREATE TABLE IF NOT EXISTS product (
    product_id VARCHAR(20) PRIMARY KEY COMMENT '商品編號',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名稱',
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0) COMMENT '售價',
    quantity INT NOT NULL CHECK (quantity >= 0) COMMENT '庫存數量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_product_quantity (quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主檔';

-- =============================================
-- 會員表 (Member)
-- =============================================
CREATE TABLE IF NOT EXISTS member (
    member_id VARCHAR(20) PRIMARY KEY COMMENT '會員編號',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '帳號',
    password VARCHAR(100) NOT NULL COMMENT '密碼(BCrypt)',
    role VARCHAR(10) NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER')) COMMENT '角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='會員主檔';

-- =============================================
-- 訂單表 (Order)
-- =============================================
CREATE TABLE IF NOT EXISTS `order` (
    order_id VARCHAR(30) PRIMARY KEY COMMENT '訂單編號',
    member_id VARCHAR(20) NOT NULL COMMENT '會員編號',
    total_price DECIMAL(12,2) NOT NULL CHECK (total_price >= 0) COMMENT '訂單總金額',
    pay_status TINYINT NOT NULL DEFAULT 0 CHECK (pay_status IN (0, 1)) COMMENT '付款狀態(0:未付款/1:已付款)',
    order_status TINYINT NOT NULL DEFAULT 0 CHECK (order_status IN (0, 1, 2, 3, -1)) COMMENT '訂單狀態(0:備貨中/1:運送中/2:已到達/3:已取貨/-1:已取消)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    FOREIGN KEY (member_id) REFERENCES member(member_id),
    INDEX idx_order_member (member_id),
    INDEX idx_order_pay_status (pay_status),
    INDEX idx_order_status (order_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單主檔';

-- =============================================
-- 訂單明細表 (OrderDetail)
-- =============================================
CREATE TABLE IF NOT EXISTS order_detail (
    order_item_sn BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '明細流水號',
    order_id VARCHAR(30) NOT NULL COMMENT '訂單編號',
    product_id VARCHAR(20) NOT NULL COMMENT '商品編號',
    quantity INT NOT NULL CHECK (quantity > 0) COMMENT '購買數量',
    stand_price DECIMAL(12,2) NOT NULL COMMENT '單價',
    item_price DECIMAL(12,2) NOT NULL COMMENT '單品項總價',
    FOREIGN KEY (order_id) REFERENCES `order`(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES product(product_id),
    INDEX idx_order_detail_order (order_id),
    INDEX idx_order_detail_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單明細表';
