# 電商購物中心系統

使用 Maven 做為專案建立的工具

## 資料庫連線: 在 ecommerce-backend\src\main\resources\application.yml 設定 

其中9~11行
```
    url: jdbc:mysql://localhost:3306/ecommerce_db
    username: root
    password: 您的密碼 
```

## 資料庫匯入
在ecommerce-backend\DB中，有三個匯入檔案
ddl.sql, stored_procedures.sql, dml.sql


## 啟動後端 (Port 8080)
```
cd ecommerce-backend
mvn spring-boot:run
```
若8080已被占用，一樣在 ecommerce-backend\src\main\resources\application.yml 中第一行設定 

```
server:
  port: 8080 //可改成8081
```

## 啟動前端 (Port 5173)

```
cd ecommerce-frontend
npm install
npm run dev
```


## 測試帳號

| 角色 | 帳號(皆為MemberID) | 密碼(皆為password123) |
|------|------|------|
| 管理員 | admin | password123 |
| 會員 | user1(458、55688、1713) | password123 |


## 專案結構

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

## 資安實作

| 攻擊類型 | 防護措施 |
|----------|----------|
| SQL Injection | 使用 Stored Procedures (參數化查詢) |
| XSS | Vue.js 自動轉義 + CSP Header |
| 未授權存取 | JWT Token + 角色權限控管 |

---

##  API 端點

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
