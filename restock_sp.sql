USE ecommerce_db;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_update_product_stock //
CREATE PROCEDURE sp_update_product_stock(
    IN p_product_id VARCHAR(20),
    IN p_add_quantity INT,
    OUT p_result INT,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_exists INT;
    
    SELECT COUNT(*) INTO v_exists FROM product WHERE product_id = p_product_id;
    
    IF v_exists = 0 THEN
        SET p_result = 0;
        SET p_message = 'Product not found';
    ELSE
        UPDATE product 
        SET quantity = quantity + p_add_quantity 
        WHERE product_id = p_product_id;
        
        SET p_result = 1;
        SET p_message = 'Stock updated successfully';
    END IF;
END //

DELIMITER ;
