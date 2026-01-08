# 電商購物中心系統規格書

## 目錄

1. [專案概述](#1-專案概述)
2. [系統架構設計](#2-系統架構設計)
3. [資料庫設計](#3-資料庫設計)
4. [API 規格](#4-api-規格)
5. [前端設計](#5-前端設計)
6. [安全性設計](#6-安全性設計)
7. [專案結構](#7-專案結構)
8. [部署與執行](#8-部署與執行)

---

## 1. 專案概述

### 1.1 專案背景
本專案為一簡易電商購物中心平台，提供商品管理與訂單管理功能。

### 1.2 功能範圍

| 功能模組 | 描述 |
|---------|------|
| 商品管理 | 新增商品（商品編號、商品名稱、售價、庫存） |
| 訂單管理 | 顯示可購買商品、建立訂單、更新庫存、更新訂單狀態 (備貨中/運送中/已到達) |
| 會員管理 | 會員註冊、登入 (含管理員與一般會員權限區分) |

### 1.3 技術棧

| 層級 | 技術 |
|------|------|
| 前端 | Vue.js 3 + Vite |
| 後端 | Spring Boot 3.x |
| 資料庫 | MySQL 8.0 / PostgreSQL |
| 建置工具 | Maven / Gradle |
| API 風格 | RESTful API |

---

## 2. 系統架構設計

### 2.1 三層式架構

```
┌─────────────────────────────────────────────────────────────────┐
│                        Web Server (Nginx)                        │
│                     靜態資源 + 反向代理                           │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Application Server (Spring Boot)               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐              │
│  │  Controller │  │   Service   │  │ Repository  │              │
│  │  (展示層)   │──│  (業務層)   │──│  (資料層)   │              │
│  └─────────────┘  └─────────────┘  └─────────────┘              │
│                          │                                       │
│                  ┌───────┴───────┐                               │
│                  │   共用層      │                               │
│                  │ (Utils/DTO)   │                               │
│                  └───────────────┘                               │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Database Server (MySQL)                       │
│              Tables + Stored Procedures                          │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 後端分層架構

| 層級 | 職責 | 套件命名 |
|------|------|---------|
| **展示層 (Presentation)** | 接收 HTTP 請求、參數驗證、回應格式化 | `controller` |
| **業務層 (Business)** | 商業邏輯處理、交易管理 | `service` |
| **資料層 (Data Access)** | 資料庫存取、呼叫 Stored Procedure | `repository` |
| **共用層 (Common)** | DTO、工具類、常數、例外處理 | `common`, `dto`, `exception`, `config` |

---

## 3. 資料庫設計

### 3.1 Entity Relationship Diagram (ERD)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│     Product     │       │      Order      │       │   OrderDetail   │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ PK product_id   │       │ PK order_id     │       │ PK order_item_sn│
│    product_name │       │ FK member_id    │       │ FK order_id     │
│    price        │       │    total_price  │       │ FK product_id   │
│    quantity     │       │    pay_status   │       │    quantity     │
│    created_at   │       │    order_status │       │    stand_price  │
│    updated_at   │       │    created_at   │       │    item_price   │
│                 │       │    updated_at   │       │                 │
└─────────────────┘       └─────────────────┘       └─────────────────┘
                             │                        │
         ┌───────────────────┘                        │
         │                                            │
┌─────────────────┐                                   │
│     Member      │                                   │
├─────────────────┤                                   │
│ PK member_id    │                                   │
│    username     │                                   │
│    password     │                                   │
│    role         │                                   │
│    created_at   │                                   │
└─────────────────┘
         │
         │
         └────────────────────────────────────────────┘
                     1:N
```

### 3.2 資料表定義

#### 3.2.1 Product（商品表）

| 欄位名稱 | 資料型態 | 限制 | 說明 |
|---------|---------|------|------|
| product_id | VARCHAR(20) | PK, NOT NULL | 商品編號 |
| product_name | VARCHAR(100) | NOT NULL | 商品名稱 |
| price | DECIMAL(12,2) | NOT NULL, >= 0 | 售價 |
| quantity | INT | NOT NULL, >= 0 | 庫存數量 |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新時間 |

#### 3.2.2 Member（會員表）

| 欄位名稱 | 資料型態 | 限制 | 說明 |
|---------|---------|------|------|
| member_id | VARCHAR(20) | PK, NOT NULL | 會員編號 |
| username | VARCHAR(50) | NOT NULL, UNIQUE | 帳號 |
| password | VARCHAR(100) | NOT NULL | 密碼 (加密儲存) |
| role | VARCHAR(10) | NOT NULL, DEFAULT 'USER' | 角色 (ADMIN/USER) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 建立時間 |

#### 3.2.3 Order（訂單表）

| 欄位名稱 | 資料型態 | 限制 | 說明 |
|---------|---------|------|------|
| order_id | VARCHAR(30) | PK, NOT NULL | 訂單編號 |
| member_id | VARCHAR(20) | NOT NULL | 會員編號 |
| total_price | DECIMAL(12,2) | NOT NULL, >= 0 | 訂單總金額 |
| pay_status | TINYINT | NOT NULL, DEFAULT 0 | 付款狀態 (0:未付款, 1:已付款) |
| order_status | TINYINT | NOT NULL, DEFAULT 0 | 訂單狀態 (0:備貨中/1:運送中/2:已到達) |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | ON UPDATE CURRENT_TIMESTAMP | 更新時間 |

#### 3.2.3 OrderDetail（訂單明細表）

| 欄位名稱 | 資料型態 | 限制 | 說明 |
|---------|---------|------|------|
| order_item_sn | BIGINT | PK, AUTO_INCREMENT | 明細流水號 |
| order_id | VARCHAR(30) | FK, NOT NULL | 訂單編號 |
| product_id | VARCHAR(20) | FK, NOT NULL | 商品編號 |
| quantity | INT | NOT NULL, > 0 | 購買數量 |
| stand_price | DECIMAL(12,2) | NOT NULL | 單價 |
| item_price | DECIMAL(12,2) | NOT NULL | 單品項總價 |

### 3.3 DDL 腳本

```sql
-- =============================================
-- DDL: 建立資料表
-- 檔案位置: /DB/ddl.sql
-- =============================================

-- 建立資料庫
CREATE DATABASE IF NOT EXISTS ecommerce_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE ecommerce_db;

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    product_id VARCHAR(20) PRIMARY KEY COMMENT '商品編號',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名稱',
    price DECIMAL(12,2) NOT NULL CHECK (price >= 0) COMMENT '售價',
    quantity INT NOT NULL CHECK (quantity >= 0) COMMENT '庫存數量',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    INDEX idx_product_quantity (quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主檔';

-- 會員表
CREATE TABLE IF NOT EXISTS member (
    member_id VARCHAR(20) PRIMARY KEY COMMENT '會員編號',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '帳號',
    password VARCHAR(100) NOT NULL COMMENT '密碼(BCrypt)',
    role VARCHAR(10) NOT NULL DEFAULT 'USER' CHECK (role IN ('ADMIN', 'USER')) COMMENT '角色',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='會員主檔';

-- 訂單表
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
    INDEX idx_order_status (pay_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='訂單主檔';

-- 訂單明細表
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
```

### 3.4 DML 腳本 (初始資料)

```sql
-- =============================================
-- DML: 初始資料
-- 檔案位置: /DB/dml.sql
-- =============================================

USE ecommerce_db;

-- 插入商品資料
INSERT INTO product (product_id, product_name, price, quantity) VALUES
('P001', 'osii 舒壓按摩椅', 98000.00, 5),
('P002', '網友最愛起司蛋糕', 1200.00, 50),
('P003', '真愛密碼項鍊', 8500.00, 20);

-- 插入訂單資料
INSERT INTO `order` (order_id, member_id, total_price, pay_status) VALUES
('Ms20250801186230', '458', 98000.00, 1),
('Ms20250805157824', '55688', 9700.00, 0),
('Ms20250805258200', '1713', 2400.00, 1);

-- 插入訂單明細資料
INSERT INTO order_detail (order_id, product_id, quantity, stand_price, item_price) VALUES
('Ms20250801186230', 'P001', 1, 98000.00, 98000.00),
('Ms20250805157824', 'P002', 1, 1200.00, 1200.00),
('Ms20250805157824', 'P003', 1, 8500.00, 8500.00),
('Ms20250805258200', 'P002', 2, 1200.00, 2400.00);
```

### 3.5 Stored Procedures

```sql
-- =============================================
-- Stored Procedures
-- 檔案位置: /DB/stored_procedures.sql
-- =============================================

USE ecommerce_db;

DELIMITER //

-- =============================================
-- SP: 新增商品
-- =============================================
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
-- SP: 查詢庫存大於零的商品
-- =============================================
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
CREATE PROCEDURE sp_create_order(
    IN p_order_id VARCHAR(30),
    IN p_member_id VARCHAR(20),
    IN p_order_items JSON,
    OUT p_result INT,
    OUT p_message VARCHAR(255),
    OUT p_total_price DECIMAL(12,2)
)
BEGIN
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
            LEAVE;
        ELSEIF v_current_stock < v_quantity THEN
            SET p_result = 0;
            SET p_message = CONCAT('Insufficient stock for product: ', v_product_id);
            ROLLBACK;
            LEAVE;
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
    INSERT INTO `order` (order_id, member_id, total_price, pay_status)
    VALUES (p_order_id, p_member_id, v_total, 0);
    
    SET p_result = 1;
    SET p_message = 'Order created successfully';
    SET p_total_price = v_total;
    
    COMMIT;
END //

-- =============================================
-- SP: 查詢所有商品
-- =============================================
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
-- SP: 查詢訂單詳情
-- =============================================
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

DELIMITER ;
```

---

## 4. API 規格

### 4.1 API 總覽

| 方法 | 端點 | 說明 |
|------|------|------|
| GET | `/api/products` | 取得所有商品列表 |
| GET | `/api/products/available` | 取得庫存大於零的商品 |
| POST | `/api/products` | 新增商品 (需 ADMIN 權限) |
| POST | `/api/orders` | 建立訂單 (需 USER 權限) |
| GET | `/api/orders/{orderId}` | 查詢訂單詳情 (需 USER 權限) |
| PATCH | `/api/orders/{orderId}/status` | 更新訂單狀態 (需 ADMIN 權限) |
| POST | `/api/auth/register` | 會員註冊 |
| POST | `/api/auth/login` | 會員登入 (取得 JWT) |

### 4.2 API 詳細規格

#### 4.2.1 取得所有商品

```
GET /api/products
```

**Response (200 OK):**
```json
{
    "success": true,
    "data": [
        {
            "productId": "P001",
            "productName": "osii 舒壓按摩椅",
            "price": 98000.00,
            "quantity": 5
        }
    ],
    "message": "Success"
}
```

#### 4.2.2 取得可購買商品（庫存 > 0）

```
GET /api/products/available
```

**Response (200 OK):**
```json
{
    "success": true,
    "data": [
        {
            "productId": "P001",
            "productName": "osii 舒壓按摩椅",
            "price": 98000.00,
            "quantity": 5
        }
    ],
    "message": "Success"
}
```

#### 4.2.3 新增商品

```
POST /api/products
Content-Type: application/json
```

**Request Body:**
```json
{
    "productId": "P004",
    "productName": "無線藍芽耳機",
    "price": 3500.00,
    "quantity": 100
}
```

**驗證規則:**
| 欄位 | 規則 |
|------|------|
| productId | 必填, 1-20 字元, 不可重複 |
| productName | 必填, 1-100 字元 |
| price | 必填, >= 0 |
| quantity | 必填, >= 0 |

**Response (201 Created):**
```json
{
    "success": true,
    "data": {
        "productId": "P004",
        "productName": "無線藍芽耳機",
        "price": 3500.00,
        "quantity": 100
    },
    "message": "Product added successfully"
}
```

**Response (400 Bad Request):**
```json
{
    "success": false,
    "data": null,
    "message": "Product ID already exists"
}
```

#### 4.2.4 建立訂單

```
POST /api/orders
Content-Type: application/json
```

**Request Body:**
```json
{
    "memberId": "12345",
    "orderItems": [
        {
            "productId": "P001",
            "quantity": 1
        },
        {
            "productId": "P002",
            "quantity": 2
        }
    ]
}
```

**驗證規則:**
| 欄位 | 規則 |
|------|------|
| memberId | 必填 |
| orderItems | 必填, 至少一項 |
| orderItems[].productId | 必填, 必須存在 |
| orderItems[].quantity | 必填, > 0, <= 該商品庫存 |

**Response (201 Created):**
```json
{
    "success": true,
    "data": {
        "orderId": "Ms20260108235959001",
        "memberId": "12345",
        "totalPrice": 100400.00,
        "payStatus": 0,
        "orderItems": [
            {
                "productId": "P001",
                "productName": "osii 舒壓按摩椅",
                "quantity": 1,
                "standPrice": 98000.00,
                "itemPrice": 98000.00
            },
            {
                "productId": "P002",
                "productName": "網友最愛起司蛋糕",
                "quantity": 2,
                "standPrice": 1200.00,
                "itemPrice": 2400.00
            }
        ]
    },
    "message": "Order created successfully"
}
```

**Response (400 Bad Request):**
```json
{
    "success": false,
    "data": null,
    "message": "Insufficient stock for product: P001"
}
```

#### 4.2.5 更新訂單狀態

```
PATCH /api/orders/{orderId}/status
Content-Type: application/json
```

**Request Body:**
```json
{
    "status": 1
}
```

**狀態碼說明:**
*   0: 備貨中
*   1: 運送中
*   2: 已到達
*   3: 已取貨
*   -1: 已取消

**Response (200 OK):**
```json
{
    "success": true,
    "message": "Order status updated successfully"
}
```

#### 4.2.6 會員註冊

```
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
    "username": "testuser",
    "password": "password123",
    "role": "USER"
}
```

**Response (201 Created):**
```json
{
    "success": true,
    "message": "User registered successfully"
}
```

#### 4.2.7 會員登入

```
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
    "username": "testuser",
    "password": "password123"
}
```

**Response (200 OK):**
```json
{
    "success": true,
    "data": {
        "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTYzOTIw..."
    },
    "message": "Login successful"
}
```

---

## 5. 前端設計

### 5.1 頁面架構

```
src/
├── App.vue
├── main.js
├── router/
│   └── index.js
├── views/
│   ├── Login.vue                # [NEW] 登入頁面
│   ├── Register.vue             # [NEW] 註冊頁面
│   ├── ProductManagement.vue    # 商品管理頁面 (Admin)
│   └── OrderManagement.vue      # 訂單管理頁面 (User)
├── components/
│   ├── ProductForm.vue          # 新增商品表單
│   ├── ProductList.vue          # 商品列表
│   ├── OrderCart.vue            # 購物車
│   └── OrderSummary.vue         # 訂單摘要
├── services/
│   └── api.js                   # API 呼叫服務
└── assets/
    └── styles/
        └── main.css
```

### 5.2 頁面功能

#### 5.2.1 商品管理頁面

| 功能 | 說明 |
|------|------|
| 商品列表 | 顯示所有商品資訊（編號、名稱、售價、庫存） |
| 新增商品 | 表單輸入商品資訊，提交後新增至資料庫 |

#### 5.2.2 訂單管理頁面

| 功能 | 說明 |
|------|------|
| 可購買商品列表 | 顯示庫存 > 0 的商品，可勾選加入購物車 |
| 購物車 | 顯示已選商品、可調整數量、顯示小計 |
| 訂單摘要 | 顯示訂單總金額 |
| 建立訂單 | 提交訂單，成功後顯示訂單確認 |

### 5.3 XSS 防護

```javascript
// 使用 Vue.js 的自動 HTML 轉義機制
// 避免使用 v-html，若必須使用，需先進行 sanitize

import DOMPurify from 'dompurify';

const sanitizedHtml = DOMPurify.sanitize(userInput);
```

---

## 6. 安全性設計

### 6.1 SQL Injection 防護

| 策略 | 實作方式 |
|------|---------|
| 參數化查詢 | 使用 Stored Procedure 的參數傳遞 |
| 輸入驗證 | 後端使用 `@Valid` 註解進行參數驗證 |
| 型別限制 | 資料庫欄位設定適當的資料型態 |

**範例：Spring Boot 參數化呼叫**
```java
@Repository
public class ProductRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public void addProduct(ProductDTO product) {
        jdbcTemplate.update(
            "CALL sp_add_product(?, ?, ?, ?, @result, @message)",
            product.getProductId(),
            product.getProductName(),
            product.getPrice(),
            product.getQuantity()
        );
    }
}
```

### 6.2 XSS 防護

| 層級 | 策略 |
|------|------|
| 前端 | Vue.js 預設 HTML 轉義、使用 DOMPurify |
| 後端 | 輸入驗證、輸出編碼 |
| HTTP Header | 設定 `Content-Security-Policy`、`X-XSS-Protection` |
| 認證與授權 | 使用 Spring Security + JWT，密碼雜湊使用 BCrypt (Strength 10) |

**範例：Spring Boot 安全設定**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // RESTful API 通常關閉 CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .headers(headers -> headers
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'"))
            );
        return http.build();
    }
}
```

### 6.3 Transaction 處理

**建立訂單的 Transaction 流程:**

```
BEGIN TRANSACTION
    │
    ├── 1. 驗證各商品庫存 (SELECT FOR UPDATE)
    │       └── 庫存不足 → ROLLBACK
    │
    ├── 2. 更新商品庫存 (UPDATE)
    │
    ├── 3. 新增訂單明細 (INSERT order_detail)
    │
    ├── 4. 新增訂單主檔 (INSERT order)
    │
    └── 5. COMMIT
```

---

## 7. 專案結構

### 7.1 後端專案結構 (Spring Boot)

```
ecommerce-backend/
├── pom.xml (或 build.gradle)
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── esunbank/
│   │   │           └── ecommerce/
│   │   │               ├── EcommerceApplication.java
│   │   │               ├── controller/           # 展示層
│   │   │               │   ├── ProductController.java
│   │   │               │   └── OrderController.java
│   │   │               ├── service/              # 業務層
│   │   │               │   ├── ProductService.java
│   │   │               │   ├── ProductServiceImpl.java
│   │   │               │   ├── OrderService.java
│   │   │               │   └── OrderServiceImpl.java
│   │   │               ├── repository/           # 資料層
│   │   │               │   ├── ProductRepository.java
│   │   │               │   └── OrderRepository.java
│   │   │               ├── dto/                  # 共用層 - 資料傳輸物件
│   │   │               │   ├── ProductDTO.java
│   │   │               │   ├── OrderDTO.java
│   │   │               │   ├── OrderItemDTO.java
│   │   │               │   └── ApiResponse.java
│   │   │               ├── entity/               # 共用層 - 實體類
│   │   │               │   ├── Product.java
│   │   │               │   ├── Order.java
│   │   │               │   └── OrderDetail.java
│   │   │               ├── exception/            # 共用層 - 例外處理
│   │   │               │   ├── GlobalExceptionHandler.java
│   │   │               │   └── BusinessException.java
│   │   │               ├── config/               # 共用層 - 設定
│   │   │               │   ├── SecurityConfig.java
│   │   │               │   └── CorsConfig.java
│   │   │               └── util/                 # 共用層 - 工具類
│   │   │                   └── OrderIdGenerator.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── application-dev.yml
│   └── test/
│       └── java/
│           └── com/esunbank/ecommerce/
│               ├── controller/
│               ├── service/
│               └── repository/
└── DB/                                           # 資料庫腳本
    ├── ddl.sql
    ├── dml.sql
    └── stored_procedures.sql
```

### 7.2 前端專案結構 (Vue.js)

```
ecommerce-frontend/
├── package.json
├── vite.config.js
├── index.html
├── public/
│   └── favicon.ico
└── src/
    ├── App.vue
    ├── main.js
    ├── router/
    │   └── index.js
    ├── views/
    │   ├── ProductManagement.vue
    │   └── OrderManagement.vue
    ├── components/
    │   ├── common/
    │   │   ├── AppHeader.vue
    │   │   └── AppFooter.vue
    │   ├── product/
    │   │   ├── ProductForm.vue
    │   │   └── ProductList.vue
    │   └── order/
    │       ├── ProductSelector.vue
    │       ├── OrderCart.vue
    │       └── OrderSummary.vue
    ├── services/
    │   └── api.js
    ├── stores/                                   # Pinia 狀態管理
    │   └── orderStore.js
    └── assets/
        └── styles/
            └── main.css
```

---

## 8. 部署與執行

### 8.1 環境需求

| 項目 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.8+ |

### 8.2 資料庫設定

1. 建立資料庫並執行 DDL
2. 執行 Stored Procedures
3. (選用) 執行 DML 插入測試資料

```bash
mysql -u root -p < DB/ddl.sql
mysql -u root -p < DB/stored_procedures.sql
mysql -u root -p < DB/dml.sql
```

### 8.3 後端執行

```bash
cd ecommerce-backend
mvn spring-boot:run
```

後端服務預設運行於 `http://localhost:8080`

### 8.4 前端執行

```bash
cd ecommerce-frontend
npm install
npm run dev
```

前端服務預設運行於 `http://localhost:5173`

### 8.5 API 測試

可使用 Postman 或 curl 測試 API：

```bash
# 取得商品列表
curl http://localhost:8080/api/products

# 新增商品
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"productId":"P004","productName":"測試商品","price":1000,"quantity":10}'

# 建立訂單
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"memberId":"12345","orderItems":[{"productId":"P001","quantity":1}]}'
```

---

## 附錄

### A. 訂單編號生成規則

格式：`Ms` + `yyyyMMdd` + `HHmmss` + `3位流水號`

例如：`Ms20260108235959001`

### B. 錯誤碼對照表

| 錯誤碼 | 說明 |
|--------|------|
| 400 | 請求參數錯誤 |
| 404 | 資源不存在 |
| 409 | 資源衝突（如商品編號重複） |
| 500 | 伺服器內部錯誤 |

### C. 版本紀錄

| 版本 | 日期 | 說明 |
|------|------|------|
| 1.0 | 2026-01-08 | 初版規格書 |
