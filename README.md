# 🛒 E-commerce Shopping Center

電商購物中心系統 - 使用 Spring Boot + Vue.js 實作

## 📋 技術規格

| 項目 | 技術 |
|------|------|
| 前端 | Vue.js 3 + Vite |
| 後端 | Spring Boot 3.x |
| 資料庫 | MySQL 8.0+ / MariaDB 10.4+ |
| 建置工具 | Maven |
| API 風格 | RESTful |
| 認證機制 | JWT (Spring Security) |

---

## 🚀 快速開始

### 1. 環境需求

- **Java**: 17+
- **Maven**: 3.8+
- **Node.js**: 18+
- **MySQL** 8.0+ 或 **MariaDB** 10.4+ (可使用 XAMPP)

### 2. 資料庫設定

**使用 phpMyAdmin (XAMPP)**:
1. 開啟 XAMPP Control Panel，啟動 MySQL
2. 點擊 MySQL 旁的 **Admin** 開啟 phpMyAdmin
3. 依序匯入以下 SQL 檔案（順序重要）：
   - `DB/ddl.sql` - 建立資料表
   - `DB/stored_procedures.sql` - 建立預存程序
   - `DB/dml.sql` - 插入初始資料

**使用 MySQL 命令列**:
```bash
mysql -u root -p < DB/ddl.sql
mysql -u root -p < DB/stored_procedures.sql
mysql -u root -p < DB/dml.sql
```

### 3. 設定資料庫連線

編輯 `ecommerce-backend/src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: root
    password: 您的密碼  # XAMPP 預設為空
```

### 4. 啟動後端 (Port 8080)

```bash
cd ecommerce-backend
mvn spring-boot:run
```

### 5. 啟動前端 (Port 5173)

```bash
cd ecommerce-frontend
npm install
npm run dev
```

### 6. 開啟瀏覽器

前往 http://localhost:5173

---

## 👤 測試帳號

| 角色 | 帳號 | 密碼 |
|------|------|------|
| 管理員 | admin | password123 |
| 會員 | user1 | password123 |

---

## 📁 專案結構

```
E-commerce/
├── DB/                         # 資料庫腳本
│   ├── ddl.sql                 # DDL (建立資料表)
│   ├── stored_procedures.sql   # Stored Procedures
│   └── dml.sql                 # DML (初始資料)
│
├── ecommerce-backend/          # Spring Boot 後端
│   ├── pom.xml
│   └── src/main/java/com/esunbank/ecommerce/
│       ├── controller/         # REST API 控制器
│       ├── service/            # 業務邏輯層
│       ├── repository/         # 資料存取層 (呼叫 SP)
│       ├── dto/                # 資料傳輸物件
│       ├── entity/             # 實體類別
│       ├── security/           # JWT 認證
│       └── config/             # Spring Security 設定
│
└── ecommerce-frontend/         # Vue.js 前端
    ├── package.json
    └── src/
        ├── views/              # 頁面元件
        ├── router/             # 路由設定
        └── services/           # API 服務
```

---

## 🔒 資安實作

| 攻擊類型 | 防護措施 |
|----------|----------|
| SQL Injection | 使用 Stored Procedures (參數化查詢) |
| XSS | Vue.js 自動轉義 + CSP Header |
| 未授權存取 | JWT Token + 角色權限控管 |

---

## 📡 API 端點

| Method | Endpoint | 權限 | 說明 |
|--------|----------|------|------|
| POST | /api/auth/register | 公開 | 會員註冊 |
| POST | /api/auth/login | 公開 | 會員登入 |
| GET | /api/products | 公開 | 查詢所有商品 |
| POST | /api/products | ADMIN | 新增商品 |
| POST | /api/orders | USER | 建立訂單 |
| GET | /api/orders/my | USER | 查詢我的訂單 |
| GET | /api/orders/all | ADMIN | 查詢所有訂單 |
| PATCH | /api/orders/{id}/status | ADMIN | 更新訂單狀態 |
