# 校园交易 后端 API 文档

## 1. 文档范围

本文档基于工程中 `backend/src/main/java` 下的后端代码，反映了编写本文时项目中已实现的 API 行为。

- 基础 URL: `http://localhost:8080/api/v1`
- Content-Type: `application/json`
- 身份认证: 使用请求头 `Authorization: Bearer <token>`
- 分页下标: `page` 从 `0` 开始

## 2. 统一响应格式

所有接口均使用以下响应包装结构：

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

### 成功响应示例

```json
{
  "success": true,
  "message": "OK",
  "data": {}
}
```

### 失败响应示例

```json
{
  "success": false,
  "message": "Request validation failed",
  "data": {
    "fieldName": "error message"
  }
}
```

## 3. 认证规则

### 公开（无需登录）接口

- `POST /auth/email-code`
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/reset-password`
- `GET /auth/me`
- `GET /categories`
- `GET /goods`
- `GET /goods/{id}`

### 需要登录（JWT）接口

以下接口需在请求头中带上有效的 JWT：

- `GET /users/me`
- `PUT /users/me`
- `POST /goods`
- `PUT /goods/{id}`
- `DELETE /goods/{id}`
- `PATCH /goods/{id}/status`
- `GET /goods/mine`
- `POST /messages/conversations/start`
- `GET /messages/conversations`
- `GET /messages/conversations/{conversationId}/items`
- `POST /messages/items`
- `POST /uploads/image`

### JWT 使用示例

```http
Authorization: Bearer eyJhbGciOi...
```

## 4. 常用数据结构

### `UserProfileResponseDTO`

```json
{
  "id": 1,
  "email": "demo@qq.com",
  "nickname": "CampusUser",
  "avatarUrl": "https://example.com/avatar.png"
}
```

### `CategoryResponseDTO`

```json
{
  "id": 1,
  "name": "Books",
  "sortOrder": 1
}
```

### `PageResponse<T>`

```json
{
  "items": [],
  "total": 100,
  "page": 0,
  "size": 10
}
```

## 5. 认证模块（Auth）

### 5.1 发送邮箱验证码

- 方法：`POST`
- 路径：`/auth/email-code`
- 是否需要认证：否

请求体示例：

```json
{
  "email": "demo@qq.com",
  "purpose": "REGISTER"
}
```

字段说明：

- `email`：必填，仅允许 `qq.com` 或 `foxmail.com` 邮箱
- `purpose`：可选，枚举：`REGISTER`、`RESET_PASSWORD`
- 若不传 `purpose`，后端默认 `REGISTER`

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "delivered": true,
    "debugCode": null
  }
}
```

业务说明：

- 注册场景发送验证码时若邮箱已存在则发送失败
- 找回密码场景若邮箱不存在则发送失败
- 验证码有效期 5 分钟
- 重发冷却时间 60 秒
- 每小时限制 8 次请求

### 5.2 注册

- 方法：`POST`
- 路径：`/auth/register`
- 是否需要认证：否

请求体示例：

```json
{
  "email": "demo@qq.com",
  "code": "123456",
  "password": "123456",
  "nickname": "CampusUser"
}
```

字段说明：

- `email`：必填，需为 `qq.com` 或 `foxmail.com`
- `code`：必填，6 位数字
- `password`：必填，长度 6-64
- `nickname`：必填，最大长度 64

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "token": "jwt-token",
    "user": {
      "id": 1,
      "email": "demo@qq.com",
      "nickname": "CampusUser",
      "avatarUrl": null
    }
  }
}
```

### 5.3 登录

- 方法：`POST`
- 路径：`/auth/login`
- 是否需要认证：否

请求体示例：

```json
{
  "email": "demo@qq.com",
  "password": "123456"
}
```

字段说明：

- `email`：必填，`qq.com` 或 `foxmail.com`
- `password`：必填，长度 6-64

响应示例：与注册相同。

业务说明：

- 错误密码或未知邮箱返回 `401`
- 连续登录失败超过阈值会短时锁定账户（当前配置：5 次失败后锁定 15 分钟）

### 5.4 重置密码

- 方法：`POST`
- 路径：`/auth/reset-password`
- 是否需要认证：否

请求体示例：

```json
{
  "email": "demo@qq.com",
  "code": "123456",
  "newPassword": "new-password-123"
}
```

字段说明：

- `email`：必填
- `code`：必填，6 位数字
- `newPassword`：必填，长度 6-64

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

### 5.5 通过认证模块获取当前用户信息

- 方法：`GET`
- 路径：`/auth/me`
- 是否需要认证：实际使用中需认证

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "email": "demo@qq.com",
    "nickname": "CampusUser",
    "avatarUrl": null
  }
}
```

说明：

- 虽然安全配置可能开放了 `/auth/**` 路径，但该接口从安全上下文读取当前认证用户，实际应视为需登录的接口。

## 6. 用户模块（User）

### 6.1 获取我的资料

- 方法：`GET`
- 路径：`/users/me`
- 是否需要认证：是

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "email": "demo@qq.com",
    "nickname": "CampusUser",
    "avatarUrl": "https://example.com/avatar.png"
  }
}
```

### 6.2 更新我的资料

- 方法：`PUT`
- 路径：`/users/me`
- 是否需要认证：是

请求体示例：

```json
{
  "nickname": "NewName",
  "avatarUrl": "https://example.com/avatar.png"
}
```

字段说明：

- `nickname`：必填，最大长度 64
- `avatarUrl`：可选，最大长度 500

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "id": 1,
    "email": "demo@qq.com",
    "nickname": "NewName",
    "avatarUrl": "https://example.com/avatar.png"
  }
}
```

## 7. 分类模块（Category）

### 7.1 列表分类

- 方法：`GET`
- 路径：`/categories`
- 是否需要认证：否

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "name": "Books",
      "sortOrder": 1
    }
  ]
}
```

说明：

- 该接口仅返回已启用的分类。

## 8. 商品模块（Goods）

### 8.1 商品对象示例

```json
{
  "id": 1,
  "title": "MacBook Air",
  "description": "95% new",
  "price": 3999.00,
  "conditionLevel": "95_NEW",
  "campusLocation": "Main Campus",
  "status": "ON_SALE",
  "category": {
    "id": 1,
    "name": "Digital",
    "sortOrder": 1
  },
  "seller": {
    "id": 2,
    "email": "seller@qq.com",
    "nickname": "Seller",
    "avatarUrl": null
  },
  "imageUrls": [
    "http://example.com/a.jpg"
  ],
  "createdAt": "2026-03-27T10:00:00",
  "updatedAt": "2026-03-27T10:00:00"
}
```

### 8.2 列表商品

- 方法：`GET`
- 路径：`/goods`
- 是否需要认证：否

查询参数：

- `keyword`：可选
- `categoryId`：可选
- `status`：可选，枚举：`ON_SALE`、`OFF_SHELF`
- `page`：可选，默认 `0`，最小 `0`
- `size`：可选，默认 `10`，最小 `1`，最大 `50`

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [],
    "total": 0,
    "page": 0,
    "size": 10
  }
}
```

### 8.3 获取商品详情

- 方法：`GET`
- 路径：`/goods/{id}`
- 是否需要认证：否

路径参数：

- `id`：商品 ID

响应说明：

- `data` 为 `GoodsResponseDTO`。

### 8.4 创建商品

- 方法：`POST`
- 路径：`/goods`
- 是否需要认证：是

请求体示例：

```json
{
  "title": "MacBook Air",
  "description": "95% new, no repair history",
  "price": 3999.00,
  "conditionLevel": "95_NEW",
  "campusLocation": "Main Campus",
  "categoryId": 1,
  "imageUrls": [
    "http://example.com/1.jpg",
    "http://example.com/2.jpg"
  ]
}
```

字段说明：

- `title`：必填，最大长度 120
- `description`：必填，最大长度 5000
- `price`：必填，最小值 >= 0.01
- `conditionLevel`：必填，最大长度 50
- `campusLocation`：必填，最大长度 120
- `categoryId`：可选，若提供则必须存在
- `imageUrls`：必填，至少 1 张，最多 9 张
- 每个 `imageUrls[]`：必填，非空，最大长度 500

响应说明：

- `data` 为 `GoodsResponseDTO`。

业务说明：

- 新建商品默认状态为 `ON_SALE`。

### 8.5 更新商品

- 方法：`PUT`
- 路径：`/goods/{id}`
- 是否需要认证：是

路径参数：

- `id`：商品 ID

请求体：同创建商品

响应说明：

- `data` 为 `GoodsResponseDTO`。

业务说明：

- 仅卖家本人可更新商品
- 提交的 `imageUrls` 会完全替换原有图片

### 8.6 删除商品

- 方法：`DELETE`
- 路径：`/goods/{id}`
- 是否需要认证：是

路径参数：

- `id`：商品 ID

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

业务说明：

- 仅卖家本人可删除商品

### 8.7 更新商品状态

- 方法：`PATCH`
- 路径：`/goods/{id}/status`
- 是否需要认证：是

请求体示例：

```json
{
  "status": "OFF_SHELF"
}
```

字段说明：

- `status`：必填，枚举：`ON_SALE`、`OFF_SHELF`

响应说明：

- `data` 为 `GoodsResponseDTO`。

业务说明：

- 仅卖家本人可更新商品状态

### 8.8 列出我的商品

- 方法：`GET`
- 路径：`/goods/mine`
- 是否需要认证：是

查询参数：

- `page`：可选，默认 `0`，最小 `0`
- `size`：可选，默认 `10`，最小 `1`，最大 `50`

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [
      {
        "id": 1,
        "title": "MacBook Air"
      }
    ],
    "total": 1,
    "page": 0,
    "size": 10
  }
}
```

说明：

- 每个 `items[]` 条目为完整的 `GoodsResponseDTO`。

## 9. 消息模块（Message）

### 9.1 会话对象示例

```json
{
  "id": 10,
  "goodsId": 1,
  "goodsTitle": "MacBook Air",
  "goodsCoverImage": "http://example.com/cover.jpg",
  "buyer": {
    "id": 3,
    "email": "buyer@qq.com",
    "nickname": "Buyer",
    "avatarUrl": null
  },
  "seller": {
    "id": 2,
    "email": "seller@qq.com",
    "nickname": "Seller",
    "avatarUrl": null
  },
  "lastMessageAt": "2026-03-27T12:00:00"
}
```

### 9.2 消息对象示例

```json
{
  "id": 100,
  "conversationId": 10,
  "sender": {
    "id": 3,
    "email": "buyer@qq.com",
    "nickname": "Buyer",
    "avatarUrl": null
  },
  "content": "Is it still available?",
  "createdAt": "2026-03-27T12:01:00"
}
```

### 9.3 发起会话

 - 方法：`POST`
 - 路径：`/messages/conversations/start`
 - 是否需要认证：是

请求体示例：

```json
{
  "goodsId": 1
}
```

字段说明：

- `goodsId`：必填

响应说明：

- `data` 为 `ConversationResponseDTO`。

业务说明：

- 若当前用户已就该商品存在会话，后端会返回已存在的会话
- 卖家不能对自己发布的商品发起会话

### 9.4 会话列表

- 方法：`GET`
- 路径：`/messages/conversations`
- 是否需要认证：是

查询参数：

- `page`：可选，默认 `0`，最小 `0`
- `size`：可选，默认 `20`，最小 `1`，最大 `50`

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [],
    "total": 0,
    "page": 0,
    "size": 20
  }
}
```

说明：

- 每个 `items[]` 条目为 `ConversationResponseDTO`。

### 9.5 列出会话中的消息

- 方法：`GET`
- 路径：`/messages/conversations/{conversationId}/items`
- 是否需要认证：是

路径参数：

- `conversationId`：会话 ID

查询参数：

- `page`：可选，默认 `0`，最小 `0`
- `size`：可选，默认 `50`，最小 `1`，最大 `200`

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "items": [],
    "total": 0,
    "page": 0,
    "size": 50
  }
}
```

说明：

- 每个 `items[]` 条目为 `MessageResponseDTO`
- 只有会话中的买家或卖家可以访问该会话消息

### 9.6 发送消息

- 方法：`POST`
- 路径：`/messages/items`
- 是否需要认证：是

请求体示例：

```json
{
  "conversationId": 10,
  "content": "Is it still available?"
}
```

字段说明：

- `conversationId`：必填
- `content`：必填，最大长度 1000

响应说明：

- `data` 为 `MessageResponseDTO`。

业务说明：

- 只有会话中的买家或卖家可以发送消息
- 发送消息会更新会话的 `lastMessageAt` 字段

## 10. 上传模块（Upload）

### 10.1 上传图片

- 方法：`POST`
- 路径：`/uploads/image`
- 是否需要认证：是
- Content-Type：`multipart/form-data`

表单字段：

- `file`：必填，图片文件

允许的文件扩展名：

- `.jpg`
- `.jpeg`
- `.png`
- `.webp`
- `.gif`

响应示例：

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "url": "http://110.40.131.82:9000/campus-trade/images/2026/03/abc123.jpg",
    "filename": "images/2026/03/abc123.jpg"
  }
}
```

业务说明：

- 后端会同时校验文件扩展名和 `contentType`
- 返回的 `filename` 为存储在 MinIO 中的对象 key

示例 cURL：

```bash
curl -X POST "http://localhost:8080/api/v1/uploads/image" \
  -H "Authorization: Bearer <token>" \
  -F "file=@C:/path/demo.jpg"
```

## 11. 常见状态码

- `200 OK`：成功
- `400 Bad Request`：参数校验失败、请求体格式错误、验证码错误等
- `401 Unauthorized`：未登录或登录凭证无效
- `403 Forbidden`：已登录但无权操作该资源
- `404 Not Found`：用户、商品、分类或会话未找到
- `409 Conflict`：注册邮箱已存在
- `423 Locked`：连续登录失败导致账户被临时锁定
- `429 Too Many Requests`：验证码请求过于频繁
- `500 Internal Server Error`：未知的服务器内部错误
- `503 Service Unavailable`：验证码服务或邮件服务暂不可用

## 12. 前端接入说明

- 前端 axios 的 base URL 为 `/api/v1`
- 前端期望后端返回统一的包装格式，最终使用 `response.data.data` 获取业务数据
- 当 `success` 为 `false` 时，前端会将整个返回体作为错误处理
- 推荐登录流程：
  1. 调用 `/auth/login`
  2. 保存返回的 `data.token`
  3. 后续请求在请求头中带上 `Authorization: Bearer <token>`

## 13. 联调建议顺序

进行端到端联调时，建议按以下顺序验证接口：

1. `/auth/email-code`
2. `/auth/register` 或 `/auth/login`
3. `/categories`
4. `/uploads/image`
5. `/goods`
6. `/goods/mine`
7. `/messages/conversations/start`
8. `/messages/items`

