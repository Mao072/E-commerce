USE ecommerce_db;

DELIMITER //

DROP PROCEDURE IF EXISTS sp_pay_order //
CREATE PROCEDURE sp_pay_order(
    IN p_order_id VARCHAR(30),
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
        SET pay_status = 1
        WHERE order_id = p_order_id;
        
        SET p_result = 1;
        SET p_message = 'Payment successful';
    END IF;
END //

DELIMITER ;
