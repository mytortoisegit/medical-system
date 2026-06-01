# 中医药品管理系统

> 基于 Spring Boot 的中医药品全生命周期管理平台，涵盖药品信息管理、智能库存预警、处方开具与审核等功能。

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术选型](#2-技术选型)
- [3. 系统架构](#3-系统架构)
- [4. 模块设计](#4-模块设计)
- [5. 数据库设计](#5-数据库设计)
- [6. 接口规范](#6-接口规范)
- [7. 安全设计](#7-安全设计)
- [8. 迭代开发指南](#8-迭代开发指南)
- [9. 环境搭建](#9-环境搭建)
- [10. 打包部署](#10-打包部署)
- [11. 运维配置](#11-运维配置)
- [12. 监控与日志](#12-监控与日志)

---

## 1. 项目概述

### 业务背景

传统中药房管理存在以下痛点：

- 药材种类繁多（数千种），信息检索效率低
- 性味归经、功效禁忌等中医特色属性难以结构化存储
- 库存管理粗放，缺乏智能预警机制
- 处方流转依赖纸质单据，审核追溯困难

本系统为中医诊所、中药房提供数字化的药品管理解决方案。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| **药品管理** | 中药药品的增删改查，支持按性味归经、功效、产地等多维度检索 |
| **分类管理** | 树形分类体系（解表药、清热药、补虚药等15大类） |
| **库存管理** | 实时库存查询、出入仓操作、库存预警自动提醒 |
| **处方管理** | 处方开具、审核流程、明细追溯、用量计算 |
| **用户权限** | 基于 RBAC 的角色权限控制，JWT 无状态认证 |
| **数据导入导出** | 基于 EasyExcel 的批量导入导出 |

---

## 2. 技术选型

| 层级 | 技术 | 版本 | 选型理由 |
|------|------|------|---------|
| 基础框架 | Spring Boot | 2.7.18 | 生态成熟，企业级首选 |
| ORM | MyBatis-Plus | 3.5.3.1 | 零侵入 CRUD + 复杂 SQL 兜底 |
| 数据库 | MySQL 8.0 | 8.0.33 | 稳定可靠，社区支持广泛 |
| 连接池 | Druid | 1.2.20 | 自带 SQL 监控 + 防火墙 |
| 缓存 | Redis + Lettuce | — | 高性能缓存，Token 存储 |
| 安全 | Spring Security + JWT | jjwt 0.9.1 | 无状态认证，前后端分离最佳实践 |
| 接口文档 | Knife4j | 4.1.0 | OpenAPI 3 规范，UI 友好 |
| 工具库 | Hutool | 5.8.23 | Java 工具集，减少重复代码 |
| JSON | Fastjson2 | 2.0.43 | 高性能序列化 |
| 表格处理 | EasyExcel | 3.3.2 | 百万级数据导入导出不 OOM |
| 构建工具 | Maven | — | 多模块项目标准选择 |

---

## 3. 系统架构

### 3.1 整体架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      前端 (Vue/React)                        │
│                    HTTP/REST + JWT Bearer                    │
└─────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┼───────────────────────────────┐
│                   Spring Boot 后端服务                       │
│                                                             │
│  ┌──────────┐    ┌──────────┐    ┌──────────────────┐      │
│  │ Controller│───▶│ Service  │───▶│ Mapper (MyBatis) │      │
│  │  (API层)  │    │ (业务层)  │    │    (数据访问层)   │      │
│  └──────────┘    └──────────┘    └────────┬─────────┘      │
│                                           │                │
│                   ┌──────────┐            │                │
│                   │  Redis   │◀── 缓存 ──┤                │
│                   └──────────┘            │                │
│                                           ▼                │
│                                    ┌──────────┐            │
│                                    │  MySQL   │            │
│                                    └──────────┘            │
└─────────────────────────────────────────────────────────────┘
```

### 3.2 请求处理流程

```
客户端请求
    │
    ▼
[Tomcat 线程池]
    │
    ▼
[JWT 认证过滤器] ── 解析 Token → 验证有效性 → 注入 SecurityContext
    │
    ▼
[Controller] ── 参数校验 (@Valid) → 调用 Service
    │
    ▼
[Service] ── 业务逻辑 → 事务管理 (@Transactional)
    │
    ├──▶ [Redis] ── 优先读取缓存，缓存未命中则查 DB 并回写
    │
    └──▶ [Mapper] ── MyBatis-Plus CRUD / 自定义 XML SQL
              │
              ▼
          [MySQL]
    │
    ▼
[统一响应 R<T>] ── 封装 code + message + data + timestamp
    │
    ▼
[全局异常处理] ── 捕获未处理异常，统一返回错误格式
```

### 3.3 分层架构

```
┌──────────────────────────────────────────────────┐
│  medical-admin       启动模块 + Controller        │  ← 入口层
├──────────────────────────────────────────────────┤
│  medical-framework   安全/配置/切面/全局异常        │  ← 横切层
├──────────────────────────────────────────────────┤
│  medical-service     业务逻辑 + 事务               │  ← 业务层
├──────────────────────────────────────────────────┤
│  medical-mapper      MyBatis Mapper + XML         │  ← 持久层
├──────────────────────────────────────────────────┤
│  medical-model       Entity/DTO/VO                │  ← 模型层
├──────────────────────────────────────────────────┤
│  medical-common      基础类/工具/异常/统一响应       │  ← 基础层
└──────────────────────────────────────────────────┘
```

**依赖方向**：上层依赖下层，下层绝不反向依赖上层。

---

## 4. 模块设计

### 4.1 medical-common（公共模块）

| 包路径 | 说明 |
|--------|------|
| `base/BaseEntity.java` | 实体基类，提供 `id`(雪花算法)、`createTime`、`updateTime`、逻辑删除等通用字段 |
| `base/BaseController.java` | Controller 基类，提供分页参数校验 |
| `constant/Constants.java` | Token、验证码、角色等系统级常量 |
| `constant/RedisConstants.java` | Redis 缓存 Key 规范定义 |
| `enums/ResultCode.java` | 30+ 业务状态码枚举（200 成功、4xx 客户端错误、5xx 服务端错误、1xxx-5xxx 业务错误） |
| `exception/BusinessException.java` | 业务异常类，携带 code + message |
| `result/R.java` | 统一响应体 `{code, message, data, timestamp}` |
| `result/PageResult.java` | 分页结果封装 `{total, pageNum, pageSize, pages, records}` |
| `utils/JwtUtil.java` | Token 生成/解析/验证/刷新 |
| `utils/RedisUtil.java` | Redis 操作工具（String/Hash/List/Set/ZSet + 分布式锁） |

### 4.2 medical-model（数据模型模块）

**实体类设计要点：**

- 所有实体继承 `BaseEntity`，自动获得主键和审计字段
- 药品实体包含中医特有字段：性味、归经、功效、毒性等
- 处方实体支持审核工作流：待审核 → 已审核/已驳回

```
Entity 命名规范：
  sys_xxx  →  系统表
  med_xxx  →  业务表

DTO 命名规范：
  XxxQueryDTO  →  查询条件
  XxxSaveDTO   →  新增/修改
  XxxVO        →  展示用（脱敏）
```

### 4.3 medical-mapper（数据访问层）

95% 场景使用 MyBatis-Plus 内置 CRUD，5% 复杂 SQL 写在 `resources/mapper/*.xml`。

**XML 存放位置**：`medical-mapper/src/main/resources/mapper/`

**命名规范**：
- XML 文件名 = Mapper 接口名（如 `MedicineMapper.java` → `MedicineMapper.xml`）
- `namespace` = Mapper 接口全限定名
- `id` = 接口方法名
- 多参数使用 `@Param` 注解

### 4.4 medical-service（业务逻辑层）

**设计原则：**
- 所有写操作加 `@Transactional(rollbackFor = Exception.class)`
- 业务异常统一抛出 `BusinessException`
- 关键操作记录日志（`log.info/warn/error`）
- 用户登录：BCrypt 密码验证 + 失败次数累积 + 自动锁定机制

### 4.5 medical-framework（核心框架模块）

| 组件 | 作用 |
|------|------|
| `SecurityConfig` | Spring Security 无状态配置，放行登录/文档接口 |
| `JwtAuthenticationFilter` | 每次请求拦截，解析 Token + 自动续期 |
| `JwtAuthenticationEntryPoint` | 未认证时返回统一 JSON |
| `RedisConfig` | Jackson 序列化，支持 Java 8 时间 |
| `MybatisPlusConfig` | 分页 + 乐观锁 + 防全表操作 + 自动填充 |
| `WebMvcConfig` | CORS 跨域 + 静态资源 |
| `Knife4jConfig` | API 文档 JWT 认证配置 |
| `LogAspect` | AOP 请求日志（方法、参数、耗时） |
| `GlobalExceptionHandler` | 7 种异常统一处理 |

### 4.6 medical-admin（启动模块）

- 启动类：`MedicalAdminApplication.java`
- Controller 按业务分组，使用 `@Tag` 注解配合 Knife4j
- 权限控制：`@PreAuthorize("hasRole('admin')")`

---

## 5. 数据库设计

### 5.1 ER 关系图

```
┌─────────────┐       ┌──────────────────┐
│ med_category │       │    sys_user      │
│  分类表       │       │    用户表         │
└──────┬───────┘       └────────┬─────────┘
       │ 1:N                    │ 1:N
       ▼                        ▼
┌─────────────┐       ┌──────────────────┐
│ med_medicine│       │med_prescription  │
│  药品表      │◀──┐   │   处方表          │
└─────────────┘   │   └────────┬─────────┘
                  │            │ 1:N
                  └────────────┤
                               ▼
                  ┌────────────────────────┐
                  │ med_prescription_detail│
                  │   处方明细表             │
                  └────────────────────────┘
```

### 5.2 核心表说明

| 表名 | 说明 | 关键字段 |
|------|------|---------|
| `sys_user` | 系统用户 | `username`(唯一)、`password`(BCrypt)、`login_fail_count`、`lock_time` |
| `med_category` | 药品分类 | `parent_id`(树形)、`level`(层级)、`sort_order` |
| `med_medicine` | 中药药品 | `property`(性味)、`meridian`(归经)、`efficacy`(功效)、`toxicity`(毒性)、`stock_quantity`、`stock_alert_threshold` |
| `med_prescription` | 处方 | `prescription_no`(唯一)、`audit_status`(审核状态)、`total_amount` |
| `med_prescription_detail` | 处方明细 | `prescription_id`、`medicine_id`、`quantity`、`amount` |

### 5.3 通用字段规范

所有表统一包含以下审计字段：

```sql
id          BIGINT     -- 主键（雪花算法 ID）
create_time DATETIME   -- 创建时间（自动填充）
update_time DATETIME   -- 更新时间（自动更新）
create_by   VARCHAR    -- 创建人
update_by   VARCHAR    -- 更新人
del_flag    TINYINT    -- 逻辑删除（0-正常 1-删除）
remark      VARCHAR    -- 备注
```

### 5.4 初始化

```bash
# 1. 创建数据库并导入表结构 + 示例数据
mysql -u root -p < sql/init.sql
```

初始化后自动创建：
- 管理员账号：`admin` / `admin123`
- 15 个中药分类（解表药 ~ 收涩药）

---

## 6. 接口规范

### 6.1 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1717234567890
}
```

### 6.2 分页响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 156,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 16,
    "records": []
  }
}
```

### 6.3 错误响应格式

```json
{
  "code": 2002,
  "message": "药品库存不足",
  "data": null,
  "timestamp": 1717234567890
}
```

### 6.4 状态码速查

| 状态码 | 含义 |
|--------|------|
| 200 | 操作成功 |
| 400 | 参数校验失败 |
| 401 | 未授权，需登录（Token 无效或过期） |
| 403 | 无权限 |
| 500 | 服务器内部错误 |
| 1001-1006 | 用户相关业务错误 |
| 2001-2003 | 药品相关业务错误 |
| 3001 | 处方相关业务错误 |
| 4001-4003 | 文件操作相关错误 |
| 5001-5005 | 通用数据操作错误 |

### 6.5 API 文档

启动后访问：`http://localhost:8080/doc.html`

支持在线调试，文档页面已集成 JWT 认证（点击 Authorize 按钮填入 Token）。

---

## 7. 安全设计

### 7.1 认证流程

```
┌─────────┐     ┌──────────────┐     ┌─────────┐
│  客户端   │     │  后端服务      │     │  Redis  │
└────┬────┘     └──────┬───────┘     └────┬────┘
     │                 │                  │
     │  POST /login    │                  │
     │────────────────▶│                  │
     │                 │  BCrypt 验证密码  │
     │                 │──────────────▶  │
     │                 │  生成 JWT        │
     │                 │  存 Token 到 Redis│
     │                 │─────────────────▶│
     │  {token, user}  │                  │
     │◀────────────────│                  │
     │                 │                  │
     │  GET /medicine  │                  │
     │  Authorization  │                  │
     │────────────────▶│                  │
     │                 │  解析 JWT         │
     │                 │  对比 Redis Token │
     │                 │─────────────────▶│
     │  业务数据        │                  │
     │◀────────────────│                  │
```

**双保险机制**：JWT 自身保证 Token 未被篡改，Redis 存储保证 Token 可主动失效（如修改密码后强制下线）。

### 7.2 Token 自动续期

- Token 剩余有效期 < 1 小时 → 自动刷新，新 Token 写入响应头
- 前端应监听响应头中的 `Authorization` 字段，静默更新本地 Token

### 7.3 密码安全

- 存储：BCrypt 加密（`$2a$10$...`，10 轮哈希）
- 登录失败策略：连续 5 次错误 → 锁定 30 分钟
- 密码修改：强制清除 Redis Token，需重新登录

### 7.4 接口权限

| 角色 | 药品查询 | 药品增删改 | 库存操作 | 处方审核 |
|------|---------|-----------|---------|---------|
| admin | ✅ | ✅ | ✅ | ✅ |
| pharmacist | ✅ | ✅ | ✅ | ✅ |
| doctor | ✅ | ❌ | ❌ | ❌ |

### 7.5 SQL 注入防护

- MyBatis-Plus `LambdaQueryWrapper` 天然防注入（参数化查询）
- Druid WallFilter 提供第二层 SQL 防火墙
- 自定义 XML SQL 必须使用 `#{}`（参数化），禁止 `${}` 拼接

---

## 8. 迭代开发指南

### 8.1 新增一个功能模块的标准步骤

以新增「供应商管理」为例：

```
第1步：定义 Entity
  → medical-model/src/main/java/com/medical/model/entity/Supplier.java
  继承 BaseEntity，加 @TableName("med_supplier")

第2步：创建 DTO/VO
  → medical-model/src/main/java/com/medical/model/dto/SupplierQueryDTO.java
  → medical-model/src/main/java/com/medical/model/vo/SupplierVO.java

第3步：创建 Mapper
  → medical-mapper/src/main/java/com/medical/mapper/SupplierMapper.java
  extends BaseMapper<Supplier>

第4步：创建 Service
  → medical-service/src/main/java/com/medical/service/SupplierService.java
       extends IService<Supplier>
  → medical-service/src/main/java/com/medical/service/impl/SupplierServiceImpl.java
       extends ServiceImpl<SupplierMapper, Supplier>

第5步：创建 Controller
  → medical-admin/src/main/java/com/medical/admin/controller/SupplierController.java
  加 @Tag(name = "供应商管理")

第6步：编写 SQL（如需要）
  → medical-mapper/src/main/resources/mapper/SupplierMapper.xml
  namespace = "com.medical.mapper.SupplierMapper"

第7步：数据库 DDL
  → sql/init.sql 中追加 CREATE TABLE
```

### 8.2 代码规范

**命名规范**：

| 类型 | 规范 | 示例 |
|------|------|------|
| 实体类 | 驼峰，与表名对应 | `MedicineCategory.java` → `med_category` |
| DTO | 功能 + DTO 后缀 | `LoginDTO`、`MedicineQueryDTO` |
| VO | 实体名 + VO 后缀 | `UserVO`、`MedicineVO` |
| Service | 实体名 + Service | `MedicineService`、`MedicineServiceImpl` |
| Mapper | 实体名 + Mapper | `MedicineMapper` |
| Controller | 模块名 + Controller | `MedicineController` |

**包结构规范**：

```
com.medical
  ├── common      → 不与业务耦合的基础设施
  ├── model       → 纯数据，无业务逻辑
  ├── mapper      → 仅数据库访问
  ├── service     → 业务逻辑 + 事务
  ├── framework   → 横切关注点
  └── admin       → 入口 + Controller
```

### 8.3 API 设计规范

```
GET    /api/{资源}/page     → 分页查询
GET    /api/{资源}/{id}     → 详情查询
POST   /api/{资源}          → 新增
PUT    /api/{资源}          → 修改
DELETE /api/{资源}/{id}     → 删除
PUT    /api/{资源}/{id}/{操作} → 特定操作（如 /stock、/audit）
```

### 8.4 缓存使用规范

| 场景 | Key 格式 | 过期时间 |
|------|---------|---------|
| 字典数据 | `dict:{类型}` | 1 小时 |
| 药品详情 | `medicine:{id}` | 2 小时 |
| 药品列表 | `medicine:page:{hash}` | 30 分钟 |
| 用户权限 | `user:permission:{userId}` | Token 过期时间 |

**缓存更新策略**：写操作时先更新数据库，再删除对应缓存（Cache Aside 模式）。

### 8.5 事务管理规范

```java
// ✅ 正确：在 Service 方法上加事务
@Override
@Transactional(rollbackFor = Exception.class)
public boolean addMedicine(Medicine medicine) {
    // 多个数据库操作在同一个事务中
}

// ❌ 错误：在 Controller 加事务
// ❌ 错误：忘记 rollbackFor，默认只回滚 RuntimeException
```

---

## 9. 环境搭建

### 9.1 前置依赖

| 软件 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 1.8+ | Java 运行环境 |
| Maven | 3.6+ | 项目构建 |
| MySQL | 8.0+ （或 5.7） | 数据存储 |
| Redis | 6.0+ | 缓存 + Token 存储 |
| IDEA / Eclipse | — | 开发工具 |

### 9.2 本地开发环境

```bash
# 1. 克隆项目（或解压）
cd medicalsystem

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 修改数据库连接（如需要）
# 编辑 medical-admin/src/main/resources/application-dev.yml
# 修改 spring.datasource.druid.username 和 password

# 4. 确保 Redis 已启动（默认 localhost:6379，无密码）

# 5. 编译运行
mvn clean compile
cd medical-admin
mvn spring-boot:run

# 6. 验证
# 浏览器访问：http://localhost:8080/doc.html
# Druid 监控：http://localhost:8080/druid （admin/admin123）
```

### 9.3 常见问题

**Q: 启动报错 `Communications link failure`**
A: MySQL 未启动或连接信息错误，检查 `application-dev.yml` 中的 `datasource.url`。

**Q: Redis 连接报错**
A: 确保 Redis 已启动，默认连接 `localhost:6379`，无密码。如有密码，在 `application-dev.yml` 中配置 `spring.redis.password`。

**Q: Lombok 注解无效**
A: IDEA 需要安装 Lombok 插件：File → Settings → Plugins → 搜索 Lombok → Install。同时确保 `Settings → Build → Compiler → Annotation Processors → Enable annotation processing` 已勾选。

---

## 10. 打包部署

### 10.1 本地打包

```bash
# 在项目根目录执行
cd medicalsystem
mvn clean package -DskipTests

# 可运行 jar 位置
# medical-admin/target/medical-admin-1.0.0-SNAPSHOT.jar
```

### 10.2 启动命令

```bash
# 开发环境
java -jar medical-admin-1.0.0-SNAPSHOT.jar

# 生产环境
java -jar medical-admin-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod

# 指定端口
java -jar medical-admin-1.0.0-SNAPSHOT.jar --server.port=9090

# 指定 JVM 参数
java -Xms512m -Xmx1024m -jar medical-admin-1.0.0-SNAPSHOT.jar
```

### 10.3 Docker 部署

```dockerfile
FROM openjdk:8-jre-alpine
WORKDIR /app
COPY medical-admin/target/medical-admin-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

```bash
# 构建镜像
docker build -t medical-system:1.0.0 .

# 运行容器
docker run -d \
  --name medical-system \
  -p 8080:8080 \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=your_password \
  -e REDIS_HOST=redis-host \
  -e REDIS_PASSWORD=your_redis_password \
  medical-system:1.0.0
```

---

## 11. 运维配置

### 11.1 多环境切换

项目通过 Spring Profile 管理多环境配置：

| 配置文件 | 环境 | 特点 |
|---------|------|------|
| `application.yml` | 公共配置 | 所有环境共享 |
| `application-dev.yml` | 开发环境 | 控制台日志、详细 SQL 打印 |
| `application-prod.yml` | 生产环境 | 文件日志、敏感信息环境变量注入 |

敏感信息（数据库密码、Redis 密码等）使用环境变量注入：

```yaml
# application-prod.yml
spring:
  datasource:
    druid:
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
```

启动时传参：

```bash
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
java -jar app.jar --spring.profiles.active=prod
```

### 11.2 配置项说明

```yaml
# ========== 服务器 ==========
server.port: 8080                        # 服务端口

# ========== 数据库 ==========
spring.datasource.druid.initial-size: 20 # 初始连接数
spring.datasource.druid.max-active: 200  # 最大连接数

# ========== Redis ==========
spring.redis.lettuce.pool.max-active: 200  # 最大连接
spring.redis.lettuce.pool.max-idle: 50     # 最大空闲

# ========== JWT ==========
jwt.secret: your-secret-key              # 签名密钥（生产务必修改）
jwt.expiration: 24                       # Token 过期小时数

# ========== 文件上传 ==========
spring.servlet.multipart.max-file-size: 50MB
```

### 11.3 JVM 调优建议

```bash
java \
  -server \
  -Xms2g -Xmx2g \                    # 堆内存 2G
  -XX:+UseG1GC \                     # G1 垃圾回收器
  -XX:MaxGCPauseMillis=200 \         # GC 最大停顿 200ms
  -XX:+HeapDumpOnOutOfMemoryError \  # OOM 时生成堆转储
  -XX:HeapDumpPath=/app/logs/ \      # 堆转储路径
  -jar medical-admin.jar
```

---

## 12. 监控与日志

### 12.1 日志体系

```
日志框架：SLF4J + Logback
配置文件：medical-admin/src/main/resources/logback-spring.xml
```

**日志输出策略**：

| 环境 | 输出 | 位置 |
|------|------|------|
| dev | 控制台（彩色） + 文件 | `{项目根目录}/logs/` |
| prod | 仅文件（不输出控制台） | `{项目根目录}/logs/` |

**日志文件**：

```
logs/
├── medical-system-info.log                ← 当前 INFO 日志
├── medical-system-info.2026-06-01.log     ← 历史（保留 30 天）
├── medical-system-error.log               ← 当前 ERROR 日志
└── medical-system-error.2026-06-01.log    ← 历史（保留 90 天）
```

**日志级别**：

```yaml
# application-dev.yml
logging:
  level:
    com.medical: debug      # 业务包 DEBUG 级别
    org.springframework: warn  # Spring 框架 WARN 级别
```

### 12.2 Druid 监控

**访问地址**：`http://{host}:{port}/druid`

**默认账号**：`admin` / `admin123`（可在 `application-dev.yml` 中修改）

**监控能力**：
- SQL 执行统计（次数、耗时、并发）
- 活跃连接数 / 连接池状态
- URI 请求监控
- Session 监控

### 12.3 AOP 请求日志

`LogAspect` 自动记录每次 API 请求：

```
API请求 -> {"method":"GET","url":"/api/medicine/page","class":"...MedicineController","methodName":"page","params":{...}}
API响应 -> GET /api/medicine/page | 耗时: 45ms
API异常 -> POST /api/medicine | 耗时: 120ms | 异常: 药品名称已存在
```

### 12.4 健康检查接口

可以扩展 Spring Boot Actuator 来添加健康检查：

```xml
<!-- 添加依赖到 medical-admin/pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when-authorized
```

---

## 附录

### A. 项目结构完整树

```
medicalsystem/
├── pom.xml                                    # 父 POM
├── README.md                                  # 本文档
├── sql/
│   └── init.sql                               # 数据库初始化
├── medical-common/
│   ├── pom.xml
│   └── src/main/java/com/medical/common/
│       ├── base/BaseEntity.java
│       ├── base/BaseController.java
│       ├── constant/Constants.java
│       ├── constant/RedisConstants.java
│       ├── enums/ResultCode.java
│       ├── exception/BusinessException.java
│       ├── result/R.java
│       ├── result/PageResult.java
│       └── utils/{JwtUtil,RedisUtil}.java
├── medical-model/
│   ├── pom.xml
│   └── src/main/java/com/medical/model/
│       ├── entity/{User,Medicine,MedicineCategory,Prescription,PrescriptionDetail}.java
│       ├── dto/{LoginDTO,PageQueryDTO,MedicineQueryDTO}.java
│       └── vo/UserVO.java
├── medical-mapper/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/medical/mapper/{User,Medicine,MedicineCategory,Prescription,PrescriptionDetail}Mapper.java
│       └── resources/mapper/MedicineMapper.xml
├── medical-service/
│   ├── pom.xml
│   └── src/main/java/com/medical/service/
│       ├── UserService.java
│       ├── MedicineService.java
│       └── impl/{UserServiceImpl,MedicineServiceImpl}.java
├── medical-framework/
│   ├── pom.xml
│   └── src/main/java/com/medical/framework/
│       ├── config/{SecurityConfig,RedisConfig,MybatisPlusConfig,WebMvcConfig,Knife4jConfig}.java
│       ├── security/{JwtAuthenticationFilter,JwtAuthenticationEntryPoint}.java
│       ├── aspect/LogAspect.java
│       └── handler/GlobalExceptionHandler.java
└── medical-admin/
    ├── pom.xml
    └── src/main/
        ├── java/com/medical/admin/
        │   ├── MedicalAdminApplication.java
        │   └── controller/{AuthController,MedicineController}.java
        └── resources/
            ├── application.yml
            ├── application-dev.yml
            ├── application-prod.yml
            └── logback-spring.xml
```

### B. 版本历史

| 日期 | 版本 | 说明 |
|------|------|------|
| 2026-06-01 | v1.0.0 | 初始版本：多模块架构、JWT 认证、药品管理、处方管理、库存预警 |

---

> 📧 技术支持：如有问题，请查阅 API 文档 (`/doc.html`) 或查看运行日志 (`logs/` 目录)。
