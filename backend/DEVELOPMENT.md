# Campus Trade Backend Development Guide

## Overview

This backend is a Spring Boot 3 service for a campus second-hand trading platform. It currently supports:

- Email-code registration, login, and password reset
- JWT stateless authentication
- User profile query and update
- Goods create/update/delete/list/status update
- Category listing
- Buyer/seller conversation and messaging
- Image upload and file serving

## Tech Stack

- Java 17
- Spring Boot 3.3.5
- Spring Web
- MyBatis
- Spring Security
- Spring Validation
- Spring Mail
- Flyway
- JJWT 0.12.6
- H2 / MySQL
- Redis
- Maven

## Package Structure

```text
backend
|-- src/main/java/com/campustrade/platform
|   |-- auth
|   |   |-- controller
|   |   |-- assembler
|   |   |-- service
|   |   |-- mapper
|   |   |-- dataobject
|   |   |-- dto/request
|   |   |-- dto/response
|   |   `-- enums
|   |-- category
|   |   |-- controller
|   |   |-- assembler
|   |   |-- mapper
|   |   |-- dataobject
|   |   `-- dto/response
|   |-- goods
|   |   |-- controller
|   |   |-- assembler
|   |   |-- service
|   |   |-- mapper
|   |   |-- dataobject
|   |   |-- dto/request
|   |   |-- dto/response
|   |   `-- enums
|   |-- message
|   |   |-- controller
|   |   |-- assembler
|   |   |-- service
|   |   |-- mapper
|   |   |-- dataobject
|   |   |-- dto/request
|   |   `-- dto/response
|   |-- upload
|   |   |-- controller
|   |   |-- service
|   |   `-- dto/response
|   |-- user
|   |   |-- controller
|   |   |-- assembler
|   |   |-- service
|   |   |-- mapper
|   |   |-- dataobject
|   |   |-- dto/request
|   |   `-- dto/response
|   |-- common
|   |-- config
|   |-- security
|   `-- CampusTradeApplication.java
`-- src/main/resources/application.yml
```

## Layer Rules

- `controller`: HTTP endpoints, request validation entry, service calls, `ApiResponse` wrapping
- `assembler`: DO -> ResponseDTO mapping and response composition
- `service`: business orchestration and transaction boundary
- `mapper`: MyBatis mapper interfaces + SQL XML
- `dataobject`: persistence entities (`*DO`)
- `dto/request`: request objects (`*RequestDTO`)
- `dto/response`: response objects (`*ResponseDTO`)
- `enums`: domain enums
- `common/config/security`: shared cross-domain infrastructure

## Run and Test

From `backend` directory:

```bash
mvn spring-boot:run
```

```bash
mvn test
```

## Main Config

Defined in `src/main/resources/application.yml`:

- `server.port`
- `spring.datasource.*`
- `mybatis.*`
- `spring.flyway.*`
- `app.jwt-secret`
- `app.jwt-expiration-minutes`
- `app.verification-code.*`
- `app.auth.*`
- `app.mail.*`
- `app.upload.dir`

## API Paths 

- `/api/v1/auth/**`
- `/api/v1/users/**`
- `/api/v1/categories/**`
- `/api/v1/goods/**`
- `/api/v1/messages/**`
- `/api/v1/uploads/**`

# ========================
# 数据库配置（MySQL）
# ========================
export DB_URL="jdbc:mysql://localhost:3306/campus_trade?useSSL=false&serverTimezone=UTC&characterEncoding=utf8"
export DB_DRIVER="com.mysql.cj.jdbc.Driver"
export DB_USERNAME="root"
export DB_PASSWORD="135790"

# ========================
# Redis 配置
# ========================
export REDIS_HOST="localhost"
export REDIS_PORT="6379"
export REDIS_PASSWORD=""
export REDIS_DATABASE="0"
export REDIS_TIMEOUT="3s"

# ========================
# JWT 配置
# ========================
export JWT_SECRET="bXlfc3VwZXJfc2VjdXJlX2p3dF9rZXlfZm9yX2NhbXB1c190cmFkZV9hcHA"
export JWT_EXPIRATION_MINUTES="10080"

# ========================
# 邮件配置
# ========================
export MAIL_ENABLED="true"
export MAIL_HOST="smtp.qq.com"
export MAIL_PORT="465"
export MAIL_USERNAME="2475594430@qq.com"
export MAIL_PASSWORD="xanenspgxrmkecba"
export MAIL_PROTOCOL="smtp"
export MAIL_AUTH="true"
export MAIL_SSL_ENABLED="true"
export MAIL_STARTTLS_ENABLED="false"
export MAIL_DEBUG="true"

# ========================
# 业务配置
# ========================
export REDIS_REQUIRED="true"

export CATEGORY_CACHE_TTL_MINUTES="30"
export GOODS_LIST_CACHE_TTL_MINUTES="2"

export VERIFICATION_CODE_EXPIRE_MINUTES="5"
export VERIFICATION_RESEND_COOLDOWN_SECONDS="60"
export VERIFICATION_HOURLY_LIMIT="8"
export VERIFICATION_CODE_DEBUG_RETURN_ENABLED="false"

export VERIFICATION_CODE_KEY_PREFIX="auth:code:"
export VERIFICATION_LIMIT_KEY_PREFIX="auth:code:limit:"

export MAX_LOGIN_FAILURES="5"
export LOGIN_LOCK_MINUTES="15"

export UPLOAD_DIR="uploads"