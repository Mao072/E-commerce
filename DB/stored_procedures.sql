-- =============================================
-- Stored Procedures
-- E-commerce Shopping Center
-- =============================================

USE ecommerce_db;

DELIMITER //

-- =============================================
-- SP: 新增商品
-- =============================================
DROP PROCEDURE IF EXISTS sp_add_product //
CREATE PROCEDURE sp_add_product(
    IN p_product_id VARCHAR(20),
    IN p_product_name VARCHAR(100),
    IN p_price DECIMAL(12,2),
    IN p_quantity INT,
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_result = -1;
        SET p_message = 'Database error occurred';
        ROLLBACK;
    END;
    
    -- 檢查商品編號是否已存在
    IF EXISTS (SELECT 1 FROM product WHERE product_id = p_product_id) THEN
        SET p_result = 0;
        SET p_message = 'Product ID already exists';
    ELSE
        INSERT INTO product (product_id, product_name, price, quantity)
        VALUES (p_product_id, p_product_name, p_price, p_quantity);
        
        SET p_result = 1;
        SET p_message = 'Product added successfully';
    END IF;
END //

-- =============================================
-- SP: 查詢所有商品
-- =============================================
DROP PROCEDURE IF EXISTS sp_get_all_products //
CREATE PROCEDURE sp_get_all_products()
BEGIN
    SELECT 
        product_id,
        product_name,
        price,
        quantity,
        created_at,
        updated_at
    FROM product
    ORDER BY created_at DESC;
END //

-- =============================================
-- SP: 查詢庫存大於零的商品
-- =============================================
DROP PROCEDURE IF EXISTS sp_get_available_products //
CREATE PROCEDURE sp_get_available_products()
BEGIN
    SELECT 
        product_id,
        product_name,
        price,
        quantity
    FROM product
    WHERE quantity > 0
    ORDER BY product_id;
END //

-- =============================================
-- SP: 建立訂單 (含 Transaction)
-- =============================================
DROP PROCEDURE IF EXISTS sp_create_order //
CREATE PROCEDURE sp_create_order(
    IN p_order_id VARCHAR(30),
    IN p_member_id VARCHAR(20),
    IN p_order_items JSON,
    OUT p_result INT,
    OUT p_message VARCHAR(255),
    OUT p_total_price DECIMAL(12,2)
)
proc_label: BEGIN
    DECLARE v_index INT DEFAULT 0;
    DECLARE v_count INT;
    DECLARE v_product_id VARCHAR(20);
    DECLARE v_quantity INT;
    DECLARE v_current_stock INT;
    DECLARE v_price DECIMAL(12,2);
    DECLARE v_item_price DECIMAL(12,2);
    DECLARE v_total DECIMAL(12,2) DEFAULT 0;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 p_message = MESSAGE_TEXT;
        SET p_result = -1;
        ROLLBACK;
    END;
    
    START TRANSACTION;
    
    -- 取得訂單項目數量
    SET v_count = JSON_LENGTH(p_order_items);
    
    -- 遍歷每個訂單項目
    WHILE v_index < v_count DO
        SET v_product_id = JSON_UNQUOTE(JSON_EXTRACT(p_order_items, CONCAT('$[', v_index, '].productId')));
        SET v_quantity = JSON_EXTRACT(p_order_items, CONCAT('$[', v_index, '].quantity'));
        
        -- 檢查並鎖定商品資料 (使用 FOR UPDATE 避免競爭條件)
        SELECT quantity, price INTO v_current_stock, v_price
        FROM product
        WHERE product_id = v_product_id
        FOR UPDATE;
        
        -- 檢查庫存是否足夠
        IF v_current_stock IS NULL THEN
            SET p_result = 0;
            SET p_message = CONCAT('Product not found: ', v_product_id);
            ROLLBACK;
            LEAVE proc_label;
        ELSEIF v_current_stock < v_quantity THEN
            SET p_result = 0;
            SET p_message = CONCAT('Insufficient stock for product: ', v_product_id);
            ROLLBACK;
            LEAVE proc_label;
        END IF;
        
        -- 計算單品項總價
        SET v_item_price = v_price * v_quantity;
        SET v_total = v_total + v_item_price;
        
        -- 更新庫存
        UPDATE product
        SET quantity = quantity - v_quantity
        WHERE product_id = v_product_id;
        
        -- 插入訂單明細
        INSERT INTO order_detail (order_id, product_id, quantity, stand_price, item_price)
        VALUES (p_order_id, v_product_id, v_quantity, v_price, v_item_price);
        
        SET v_index = v_index + 1;
    END WHILE;
    
    -- 插入訂單主檔
    INSERT INTO `order` (order_id, member_id, total_price, pay_status, order_status)
    VALUES (p_order_id, p_member_id, v_total, 0, 0);
    
    SET p_result = 1;
    SET p_message = 'Order created successfully';
    SET p_total_price = v_total;
    
    COMMIT;
END //

-- =============================================
-- SP: 查詢訂單詳情
-- =============================================
DROP PROCEDURE IF EXISTS sp_get_order_details //
CREATE PROCEDURE sp_get_order_details(
    IN p_order_id VARCHAR(30)
)
BEGIN
    SELECT 
        o.order_id,
        o.member_id,
        o.total_price,
        o.pay_status,
        o.order_status,
        o.created_at,
        od.order_item_sn,
        od.product_id,
        p.product_name,
        od.quantity,
        od.stand_price,
        od.item_price
    FROM `order` o
    JOIN order_detail od ON o.order_id = od.order_id
    JOIN product p ON od.product_id = p.product_id
    WHERE o.order_id = p_order_id;
END //

-- =============================================
-- SP: 更新訂單狀態
-- =============================================
DROP PROCEDURE IF EXISTS sp_update_order_status //
CREATE PROCEDURE sp_update_order_status(
    IN p_order_id VARCHAR(30),
    IN p_order_status TINYINT,
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_exists INT;
    
    SELECT COUNT(*) INTO v_exists FROM `order` WHERE order_id = p_order_id;
    
    IF v_exists = 0 THEN
        SET p_result = 0;
        SET p_message = 'Order not found';
    ELSE
        UPDATE `order`
        SET order_status = p_order_status
        WHERE order_id = p_order_id;
        
        SET p_result = 1;
        SET p_message = 'Order status updated successfully';
    END IF;
END //

-- =============================================
-- SP: 會員註冊
-- =============================================
DROP PROCEDURE IF EXISTS sp_register_member //
CREATE PROCEDURE sp_register_member(
    IN p_member_id VARCHAR(20),
    IN p_username VARCHAR(50),
    IN p_password VARCHAR(100),
    IN p_role VARCHAR(10),
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        SET p_result = -1;
        SET p_message = 'Database error occurred';
    END;
    
    IF EXISTS (SELECT 1 FROM member WHERE username = p_username) THEN
        SET p_result = 0;
        SET p_message = 'Username already exists';
    ELSE
        INSERT INTO member (member_id, username, password, role)
        VALUES (p_member_id, p_username, p_password, IFNULL(p_role, 'USER'));
        
        SET p_result = 1;
        SET p_message = 'User registered successfully';
    END IF;
END //

-- =============================================
-- SP: 查詢會員 (by username)
-- =============================================
DROP PROCEDURE IF EXISTS sp_get_member_by_username //
CREATE PROCEDURE sp_get_member_by_username(
    IN p_username VARCHAR(50)
)
BEGIN
    SELECT 
        member_id,
        username,
        password,
        role,
        created_at
    FROM member
    WHERE username = p_username;
END //

-- =============================================
-- SP: 查詢會員訂單列表
-- =============================================
DROP PROCEDURE IF EXISTS sp_get_member_orders //
CREATE PROCEDURE sp_get_member_orders(
    IN p_member_id VARCHAR(20)
)
BEGIN
    SELECT 
        order_id,
        member_id,
        total_price,
        pay_status,
        order_status,
        created_at,
        updated_at
    FROM `order`
    WHERE member_id = p_member_id
    ORDER BY created_at DESC;
END //

-- =============================================
-- SP: 查詢所有訂單 (Admin)
-- =============================================
DROP PROCEDURE IF EXISTS sp_get_all_orders //
CREATE PROCEDURE sp_get_all_orders()
BEGIN
    SELECT 
        o.order_id,
        o.member_id,
        m.username,
        o.total_price,
        o.pay_status,
        o.order_status,
        o.created_at,
        o.updated_at
    FROM `order` o
    JOIN member m ON o.member_id = m.member_id
    ORDER BY o.created_at DESC;
END //

DELIMITER ;
