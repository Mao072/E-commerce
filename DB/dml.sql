-- =============================================
-- DML: 初始資料
-- E-commerce Shopping Center
-- =============================================

USE ecommerce_db;

-- =============================================
-- 插入會員資料 (密碼為 BCrypt 加密後的 "password123")
-- 使用 BCrypt Strength 10
-- =============================================
INSERT INTO member (member_id, username, password, role) VALUES
('M001', 'admin', '$2a$10$ydpZk/UHpZs7qA7knJ3efOoX0OAlNv2mJjLZpr0OpBx0PFt5RFFbS', 'ADMIN'),
('M002', 'user1', '$2a$10$ydpZk/UHpZs7qA7knJ3efOoX0OAlNv2mJjLZpr0OpBx0PFt5RFFbS', 'USER'),
('458', 'member458', '$2a$10$ydpZk/UHpZs7qA7knJ3efOoX0OAlNv2mJjLZpr0OpBx0PFt5RFFbS', 'USER'),
('55688', 'member55688', '$2a$10$ydpZk/UHpZs7qA7knJ3efOoX0OAlNv2mJjLZpr0OpBx0PFt5RFFbS', 'USER'),
('1713', 'member1713', '$2a$10$ydpZk/UHpZs7qA7knJ3efOoX0OAlNv2mJjLZpr0OpBx0PFt5RFFbS', 'USER');

-- =============================================
-- 插入商品資料
-- =============================================
INSERT INTO product (product_id, product_name, price, quantity) VALUES
('P001', 'osii 舒壓按摩椅', 98000.00, 5),
('P002', '網友最愛起司蛋糕', 1200.00, 50),
('P003', '真愛密碼項鍊', 8500.00, 20);

-- =============================================
-- 插入訂單資料
-- =============================================
INSERT INTO `order` (order_id, member_id, total_price, pay_status, order_status) VALUES
('Ms20250801186230', '458', 98000.00, 1, 2),
('Ms20250805157824', '55688', 9700.00, 0, 0),
('Ms20250805258200', '1713', 2400.00, 1, 1);

-- =============================================
-- 插入訂單明細資料
-- =============================================
INSERT INTO order_detail (order_id, product_id, quantity, stand_price, item_price) VALUES
('Ms20250801186230', 'P001', 1, 98000.00, 98000.00),
('Ms20250805157824', 'P002', 1, 1200.00, 1200.00),
('Ms20250805157824', 'P003', 1, 8500.00, 8500.00),
('Ms20250805258200', 'P002', 2, 1200.00, 2400.00);
